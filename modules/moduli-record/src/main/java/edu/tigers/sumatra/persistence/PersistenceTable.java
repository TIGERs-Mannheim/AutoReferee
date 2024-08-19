/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.io.FuryReadableChannel;
import org.apache.fury.logging.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
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

	private final RandomAccessFile db;
	private final RandomAccessFile indexFile;

	private final TreeMap<Long, List<Long>> index = new TreeMap<>();


	public PersistenceTable(Class<T> clazz, Path dbPath, EPersistenceKeyType keyType)
			throws IOException
	{
		this.keyType = keyType;

		fury = Fury.builder()
				.withAsyncCompilation(
						true) // Reduce buffer size requirements by serializing in interpreter mode until the JIT code is generated
				.withLanguage(Language.JAVA) // No cross-language compatibility required
				.requireClassRegistration(false) // Allows for serialization of arbitrary classes
				.withCompatibleMode(CompatibleMode.COMPATIBLE) // Try to preserve compatibility across class changes
				.build();

		register(new HashSet<>(), clazz);

		this.db = new RandomAccessFile(dbPath.resolve(clazz.getSimpleName() + ".db").toFile(), "rw");
		this.db.seek(this.db.length());
		this.indexFile = new RandomAccessFile(dbPath.resolve(clazz.getSimpleName() + ".index").toFile(), "rw");

		try
		{
			while (indexFile.length() - indexFile.getFilePointer() > 0)
				index.computeIfAbsent(indexFile.readLong(), key -> new ArrayList<>()).add(indexFile.readLong());
		} catch (IOException e)
		{
			log.error("Could not read index", e);
		}
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
			if (field.getGenericType() instanceof ParameterizedType type)
			{
				for (Type argument : type.getActualTypeArguments())
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
			long startIndex = db.getChannel().position();

			fury.serialize(new FileOutputStream(db.getFD()), element);
			indexFile.writeLong(id);
			indexFile.writeLong(startIndex);
			index.computeIfAbsent(id, key -> new ArrayList<>()).add(startIndex);
		} catch (RuntimeException | IOException e)
		{
			log.error("Could not write to db", e);
		}
	}


	public int size()
	{
		return index.size();
	}


	public void forEach(Consumer<T> consumer)
	{
		for (long key : index.navigableKeySet())
			consumer.accept(get(key));
	}


	public List<T> load()
	{
		List<T> events = new ArrayList<>(size());
		forEach(events::add);
		return events;
	}


	@SuppressWarnings("unchecked")
	public T get(long key)
	{
		try
		{
			T element = null;
			for (Long startIndex : index.get(key))
			{
				db.seek(startIndex);
				T entry = (T) fury.deserialize(new FuryReadableChannel(db.getChannel()));
				if (element != null)
					element.merge(entry);
				else
					element = entry;
			}
			return element;
		} catch (RuntimeException | IOException e)
		{
			return null;
		}
	}


	public Long getFirstKey()
	{
		return noSuchElement(index::firstKey);
	}


	public Long getLastKey()
	{
		return noSuchElement(index::lastKey);
	}


	public Long getPreviousKey(long key)
	{
		return index.lowerKey(key);
	}


	public Long getNextKey(long key)
	{
		return index.higherKey(key);
	}


	public Long getNearestKey(long key)
	{
		Long neighbour = index.floorKey(key);
		Long ceil = index.ceilingKey(key);
		if (ceil != null && (neighbour == null || Math.abs(ceil - key) < Math.abs(neighbour - key)))
			return ceil;

		return neighbour;
	}


	public void close()
	{
		try
		{
			db.close();
			indexFile.close();
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
		}
	}
}
