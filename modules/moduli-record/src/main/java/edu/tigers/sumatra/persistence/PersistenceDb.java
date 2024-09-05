/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.model.SumatraModel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Fury database
 */
@Log4j2
public class PersistenceDb
{
	private final Path dbPath;

	private final Map<Class<?>, PersistenceTable<?>> tables = new HashMap<>();

	@Setter
	private boolean compressOnClose = false;

	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	public PersistenceDb(final Path dbPath)
	{
		if (dbPath.toString().endsWith(".zip"))
		{
			String folderName = determineFolderName(dbPath.toFile());
			this.dbPath = Paths.get(dbPath.toString().substring(0, dbPath.toString().length() - 4));
			if (!this.dbPath.toFile().exists())
			{
				unpackDatabase(dbPath.toFile(), folderName);
			} else
			{
				log.info("Database is already extracted, using: {}", this.dbPath);
			}
		} else
		{
			this.dbPath = dbPath;
		}

		File folder = this.dbPath.toFile();
		if(!folder.exists() && !folder.mkdirs())
			log.warn("Could not create folder for database {}", this.dbPath);
	}


	private String determineFolderName(final File file)
	{
		try (ZipFile zipFile = new ZipFile(file))
		{
			String topLevel = null;
			for (FileHeader fh : zipFile.getFileHeaders())
			{
				String root = fh.getFileName().split("/")[0];
				if (root.equals(topLevel) || topLevel == null)
				{
					topLevel = root;
				} else
				{
					log.error("Recording contains more than one folder, this is invalid");
					return file.getName();
				}
			}
			return topLevel;
		} catch (IOException e)
		{
			log.error("Error while processing zip recording: ", e);
		}
		return file.getName();
	}


	/**
	 * @param matchType
	 * @param stage
	 * @param teamYellow name of yellow team
	 * @param teamBlue   name of blue team
	 * @return a new empty unopened database at the default location
	 */
	public static PersistenceDb withDefaultLocation(String matchType, String stage, String teamYellow, String teamBlue)
	{
		return new PersistenceDb(Paths.get(getDefaultBasePath(), getDefaultName(matchType, stage, teamYellow, teamBlue)));
	}


	/**
	 * @param customLocation a custom absolute path to a database
	 * @return a new database handle with a custom name at the default base path
	 */
	public static PersistenceDb withCustomLocation(final Path customLocation)
	{
		return new PersistenceDb(customLocation);
	}


	/**
	 * @param matchType  type of match
	 * @param stage      stage of game
	 * @param teamYellow name of yellow team
	 * @param teamBlue   name of blue team
	 * @return the default name for a new database
	 */
	public static String getDefaultName(String matchType, String stage, String teamYellow, String teamBlue)
	{
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		dt.setTimeZone(TimeZone.getDefault());
		return dt.format(new Date()) + String.format("-%s-%s-%s-vs-%s", matchType, stage, teamYellow, teamBlue);
	}


	/**
	 * @return
	 */
	public static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty("edu.tigers.sumatra.persistence.basePath", "data/record");
	}


	public <T extends PersistenceTable.IEntry<T>> void add(Class<T> clazz, EPersistenceKeyType keyType)
	{
		try
		{
			tables.put(clazz, new PersistenceTable<>(clazz, dbPath, keyType));
		} catch (IOException e)
		{
			log.error("Could not add datatype to db", e);
		}
	}


	@SuppressWarnings("unchecked")
	public <T extends PersistenceTable.IEntry<T>> PersistenceTable<T> getTable(Class<T> clazz)
	{
		return (PersistenceTable<T>) tables.get(clazz);
	}


	public Collection<Class<?>> getTableTypes()
	{
		return tables.keySet();
	}


	public void forEachTable(Consumer<PersistenceTable<?>> consumer)
	{
		tables.values().forEach(consumer);
	}


	private void unpackDatabase(final File file, final String folderName)
	{
		log.info("Unpacking database: {}", file);
		try (ZipFile zipFile = new ZipFile(file))
		{
			zipFile.extractAll(dbPath.toFile().getParent());

			File extracted = Paths.get(dbPath.toFile().getParent(), folderName).toFile();

			Files.move(extracted.toPath(), Paths.get(dbPath.getParent().toString(), file.getName().replace(".zip", "")));

			log.info("Unpacking finished.");
		} catch (ZipException e)
		{
			log.error("Unpacking failed.", e);
		} catch (IOException e)
		{
			log.error("Could not move extracted replay: ", e);
		}
	}


	/**
	 * Close database
	 */
	public void close()
	{
		tables.values().forEach(PersistenceTable::close);
		tables.clear();

		if (compressOnClose)
		{
			try
			{
				compress();
			} catch (IOException e)
			{
				log.error("Could not compress the replay during closing", e);
			}
		}
	}


	/**
	 * Delete the database from filesystem
	 *
	 * @throws IOException
	 */
	public void delete() throws IOException
	{
		if (!tables.isEmpty())
		{
			throw new IllegalStateException("Database must be closed before deletion.");
		}
		FileUtils.deleteDirectory(dbPath.toFile());
	}


	/**
	 * Compress the database
	 *
	 * @throws IOException
	 */
	public void compress() throws IOException
	{
		log.info("Compressing database {}", this.dbPath);
		//https://stackoverflow.com/a/32052016 CC BY-SA 3.0 by Nikita Koksharov
		try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(dbPath.resolveSibling(dbPath.getFileName().toString() + ".zip")))) {
			try(Stream<Path> stream = Files.walk(dbPath))
			{
				stream
						.filter(path -> !Files.isDirectory(path))
						.forEach(path -> {
							ZipEntry zipEntry = new ZipEntry(dbPath.relativize(path).toString());
							try {
								zs.putNextEntry(zipEntry);
								Files.copy(path, zs);
								zs.closeEntry();
							} catch (IOException e) {
								log.error("Could not compress file {}", path, e);
							}
						});
			} catch (RuntimeException e)
			{
				throw new IOException(e);
			}
		}
		log.info("Compressed database {}", this.dbPath);
	}


	public Long getFirstKey()
	{
		return tables.values().stream()
				.filter(PersistenceTable::isSumatraTimestampBased)
				.map(PersistenceTable::getFirstKey)
				.filter(Objects::nonNull)
				.reduce(Math::min)
				.orElse(null);
	}


	public Long getLastKey()
	{
		return tables.values().stream()
				.filter(PersistenceTable::isSumatraTimestampBased)
				.map(PersistenceTable::getLastKey)
				.filter(Objects::nonNull)
				.reduce(Math::max)
				.orElse(null);
	}


	public Long getNextKey(long tCur)
	{
		return tables.values().stream()
				.filter(PersistenceTable::isSumatraTimestampBased)
				.map(s -> s.getNextKey(tCur))
				.filter(Objects::nonNull)
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}


	public Long getPreviousKey(long tCur)
	{
		return tables.values().stream()
				.filter(PersistenceTable::isSumatraTimestampBased)
				.map(s -> s.getPreviousKey(tCur))
				.filter(Objects::nonNull)
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}


	public Long getKey(long tCur)
	{
		return tables.values().stream()
				.filter(PersistenceTable::isSumatraTimestampBased)
				.map(s -> s.getNearestKey(tCur))
				.filter(Objects::nonNull)
				.reduce((k1, k2) -> getNearestKey(tCur, k1, k2))
				.orElse(null);
	}


	private Long getNearestKey(final long key, final long k1, final long k2)
	{
		long diff1 = Math.abs(key - k1);
		long diff2 = Math.abs(key - k2);

		if (diff1 < diff2)
		{
			return k1;
		}
		return k2;
	}


	/**
	 * @return the db path
	 */
	public String getDbPath()
	{
		return dbPath.toString();
	}
}
