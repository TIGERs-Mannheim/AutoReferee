/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.apache.fury.memory.MemoryBuffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongSupplier;


@Log4j2
public class PersistenceTable<T extends PersistenceTable.IEntry<T>>
{
	static
	{
		/*
		 This is the LoggerFactory of Fury. Fury brings its own logging framework which directly prints on stdout.
		 Fury INFOs every compilation of a (de-)serializer for every single custom class and
		 WARNs for every Fury instance about the disabled class registration requirement since that allows
		 arbitrary object deserialization (a code injection security risk).
		 Configure log4j2.xml for filtering.
		 */
		LoggerFactory.useSlf4jLogging(true);
	}

	@Getter
	private final EPersistenceKeyType keyType;

	private final Fury fury;

	private final PersistenceIndex index;

	private final FileOutputStream appendStream;
	private final FileChannel file;

	@Getter
	private final Class<T> type;


	public PersistenceTable(Class<T> type, Path dbPath, EPersistenceKeyType keyType)
			throws IOException
	{
		this.type = type;
		this.keyType = keyType;

		fury = Fury.builder()
				// Reduce buffer size requirements by serializing in interpreter mode until the JIT code is generated
				.withAsyncCompilation(true)
				// No cross-language compatibility required
				.withLanguage(Language.JAVA)
				// Allows for serialization of arbitrary classes
				.requireClassRegistration(false)
				.build();

		register(new HashSet<>(), type);

		Path dbFile = dbPath.resolve(type.getSimpleName() + ".db");
		this.appendStream = new FileOutputStream(dbFile.toFile(), true);
		this.file = FileChannel.open(dbFile, StandardOpenOption.READ);

		this.index = new PersistenceIndex(dbPath.resolve(type.getSimpleName() + ".index"), file);
	}


	/**
	 * Recursively register most classes to reduce final database size and improve performance.
	 */
	private void register(Set<Class<?>> registered, Class<?> clazz)
	{
		if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.getPackageName()
				.startsWith("java.lang") || !registered.add(clazz))
			return;

		fury.register(clazz);
		for (Field field : clazz.getDeclaredFields())
		{
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))
				continue;

			register(registered, field.getType());
			if (field.getGenericType() instanceof ParameterizedType t)
			{
				for (Type argument : t.getActualTypeArguments())
				{
					if (argument instanceof Class<?> argClass)
						register(registered, argClass);
				}
			}
		}
	}


	public void write(final Collection<T> elements)
	{
		elements.forEach(this::write);
	}


	public void write(T element)
	{
		try
		{
			long id = element.getKey();
			long startIndex = appendStream.getChannel().position();

			fury.serialize(appendStream, element);
			index.append(id, startIndex);
		} catch (RuntimeException | IOException e)
		{
			log.error("Could not write to db", e);
		}
	}


	public int size()
	{
		return index.get().size();
	}


	public void forEach(Consumer<T> consumer)
	{
		for (long key : index.get().navigableKeySet())
		{
			consumer.accept(get(key));
		}
	}


	public List<T> load()
	{
		List<T> events = new ArrayList<>(size());
		forEach(events::add);
		return events;
	}


	@SuppressWarnings("unchecked")
	public synchronized T get(long key)
	{
		if (!index.get().containsKey(key))
		{
			return null;
		}

		try
		{
			T element = null;
			for (PersistenceIndex.Range range : index.get().get(key))
			{
				file.position(range.address());
				ByteBuffer buf = ByteBuffer.allocate(range.size());
				file.read(buf);
				buf.position(0);

				T entry = (T) fury.deserialize(MemoryBuffer.fromByteBuffer(buf));
				if (element != null)
				{
					element.merge(entry);
				} else
				{
					element = entry;
				}
			}
			return element;
		} catch (RuntimeException | IOException e)
		{
			log.error("Could not read from db", e);
			return null;
		}
	}


	public Long getFirstKey()
	{
		return noSuchElement(index.get()::firstKey);
	}


	public Long getLastKey()
	{
		return noSuchElement(index.get()::lastKey);
	}


	public Long getPreviousKey(long key)
	{
		return index.get().lowerKey(key);
	}


	public Long getNextKey(long key)
	{
		return index.get().higherKey(key);
	}


	public Long getNearestKey(long key)
	{
		Long neighbour = index.get().floorKey(key);
		Long ceil = index.get().ceilingKey(key);
		if (ceil != null && (neighbour == null || Math.abs(ceil - key) < Math.abs(neighbour - key)))
			return ceil;

		return neighbour;
	}


	public void close()
	{
		try
		{
			appendStream.close();
			file.close();
			index.close();
		} catch (IOException e)
		{
			log.error("Could not close db", e);
		}
	}


	private Long noSuchElement(LongSupplier supplier)
	{
		try
		{
			return supplier.getAsLong();
		} catch (NoSuchElementException e)
		{
			return null;
		}
	}


	public boolean isSumatraTimestampBased()
	{
		return keyType == EPersistenceKeyType.SUMATRA_TIMESTAMP;
	}


	public interface IEntry<S>
	{
		long getKey();

		default void merge(S other)
		{
			log.warn("Entry merge attempted for class {}", getClass().getName());
		}
	}
}
