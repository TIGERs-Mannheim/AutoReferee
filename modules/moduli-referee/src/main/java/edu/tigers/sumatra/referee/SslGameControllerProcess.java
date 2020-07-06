/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A wrapper around the ssl-game-controller executable
 */
public class SslGameControllerProcess implements Runnable
{
	private static final Logger log = LogManager.getLogger(SslGameControllerProcess.class.getName());
	private static final String BINARY_NAME = "ssl-game-controller";
	private static final Path TEMP_DIR = Paths.get("temp");
	private static final File BINARY_FILE = TEMP_DIR.resolve(BINARY_NAME).toFile();

	private int gcUiPort = 50543;
	private String publishAddress = "";
	private String timeAcquisitionMode = "";
	private Process process = null;
	private SslGameControllerClient client = null;
	private final CountDownLatch clientLatch = new CountDownLatch(1);


	@Override
	public void run()
	{
		Thread.currentThread().setName(BINARY_NAME);

		if (!setupBinary())
		{
			return;
		}

		try
		{
			ProcessBuilder builder = new ProcessBuilder(BINARY_FILE.getAbsolutePath(),
					"-address", ":" + gcUiPort,
					"-timeAcquisitionMode", timeAcquisitionMode,
					"-publishAddress", publishAddress);
			builder.redirectErrorStream(true);
			builder.directory(Paths.get("").toAbsolutePath().toFile());
			process = builder.start();
			log.debug("game-controller process started");

			Scanner s = new Scanner(process.getInputStream());
			inputLoop(s);
			s.close();
		} catch (IOException e)
		{
			if (!"Stream closed".equals(e.getMessage()))
			{
				log.warn("Could not execute ssl-game-controller", e);
			}
		}
		if (process != null && !process.isAlive() && process.exitValue() != 0)
		{
			log.warn("game-controller has returned a non-zero exit code: " + process.exitValue());
		}
		log.debug("game-controller process thread finished");
	}


	private boolean setupBinary()
	{
		if (BINARY_FILE.exists())
		{
			return true;
		}
		File tmpDir = TEMP_DIR.toFile();
		if (tmpDir.mkdirs())
		{
			log.debug("Temp dir created: {}", tmpDir);
			tmpDir.deleteOnExit();
		}
		if (!writeResourceToFile(BINARY_NAME, BINARY_FILE))
		{
			return false;
		}
		BINARY_FILE.deleteOnExit();
		if (!BINARY_FILE.canExecute() && !BINARY_FILE.setExecutable(true))
		{
			log.warn("Binary is not executable and could not be made executable.");
			return false;
		}
		return true;
	}


	private static boolean writeResourceToFile(String resourcePath, File targetFile)
	{
		try
		{
			InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
			if (in == null)
			{
				log.warn("Could not find {} in classpath", resourcePath);
				return false;
			}

			try (FileOutputStream out = new FileOutputStream(targetFile))
			{
				IOUtils.copy(in, out);
			}
			return true;
		} catch (IOException e)
		{
			log.warn("Could not copy binary to temporary file", e);
		}
		return false;
	}


	private void inputLoop(final Scanner s)
	{
		while (s.hasNextLine())
		{
			String line = s.nextLine();
			if (line != null)
			{
				processLogLine(line);
			}
		}
	}


	private void processLogLine(final String line)
	{
		// Remove log date: 2018/10/29 10:00:10
		String truncatedLine = line.replaceFirst("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+ ", "");
		log.debug("GC: " + truncatedLine);

		if (truncatedLine.contains("UI is available at"))
		{
			createClient(truncatedLine);
		}
	}


	private void createClient(final String truncatedLine)
	{
		final Pattern pattern = Pattern.compile(":([0-9]+)");
		final Matcher matcher = pattern.matcher(truncatedLine);
		if (matcher.find())
		{
			String port = matcher.group(1);
			final URI uri = URI.create("http://localhost:" + port + "/api/control");
			log.debug("Connecting to " + uri);
			client = new SslGameControllerClient(uri);
			try
			{
				// wait a moment to allow the controller to actually listen for connections
				Thread.sleep(100);
				// connection should be established within one second
				boolean connected = client.connectBlocking(1, TimeUnit.SECONDS);
				if (!connected)
				{
					log.warn("Timed out waiting to connect to the game-controller.");
					client = null;
				}
				clientLatch.countDown();
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		} else
		{
			log.error("Could not extract port from log line, where a port was expected.");
		}
	}


	public void stop()
	{
		if (process == null)
		{
			return;
		}
		if (client != null)
		{
			client.close();
			client = null;
		}

		process.destroy();
		try
		{
			if (!process.waitFor(1, TimeUnit.SECONDS))
			{
				log.warn("Process could not be stopped and must be killed");
				process.destroyForcibly();
			}
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for the process to exit");
			Thread.currentThread().interrupt();
		}
		process = null;
	}


	public Optional<SslGameControllerClient> getClient()
	{
		return Optional.ofNullable(client);
	}


	/**
	 * @return a connected client, waiting if necessary until it is available and connected
	 */
	public Optional<SslGameControllerClient> getClientBlocking()
	{
		if (client != null)
		{
			return getClient();
		}
		try
		{
			final boolean latchTriggered = clientLatch.await(10, TimeUnit.SECONDS);
			if (!latchTriggered)
			{
				log.warn("Timed out waiting to get the client");
			}
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		return getClient();
	}


	public int getGcUiPort()
	{
		return gcUiPort;
	}


	public void setGcUiPort(final int gcUiPort)
	{
		this.gcUiPort = gcUiPort;
	}


	public void setPublishAddress(final String publishAddress)
	{
		this.publishAddress = publishAddress;
	}


	public void setTimeAcquisitionMode(final String timeAcquisitionMode)
	{
		this.timeAcquisitionMode = timeAcquisitionMode;
	}
}
