/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.recorder;

import edu.tigers.sumatra.visualizer.field.FieldPanel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


@Log4j2
@RequiredArgsConstructor
public class MediaRecorderPresenter implements IMediaRecorderListener
{
	private static final Path BASE_SCREENCAST_PATH = Path.of("data/screencast/");
	private final FieldPanel fieldPanel;


	@Override
	public void setMediaParameters(final int w, final int h, EMediaOption mediaOption)
	{
		fieldPanel.setMediaParameters(w, h, mediaOption);
	}


	@Override
	public void takeScreenshot()
	{
		Path path = newFilePath("screenshot", ".png");
		fieldPanel.saveScreenshot(path);
	}


	@Override
	public boolean startRecordingVideo()
	{
		Path path = newFilePath("video", ".mp4");
		if (path != null && fieldPanel.startRecordingVideo(path))
		{
			log.info("Started recording video to: {}", path);
			return true;
		}
		log.warn("Could not start recording to: {}", path);
		return false;
	}


	@Override
	public void stopRecordingVideo()
	{
		fieldPanel.stopRecordingVideo();
	}


	private Path newFilePath(String prefix, String ending)
	{
		try
		{
			Files.createDirectories(BASE_SCREENCAST_PATH);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			sdf.setTimeZone(TimeZone.getDefault());
			String filename = prefix + "_" + sdf.format(new Date()) + ending;
			return BASE_SCREENCAST_PATH.resolve(filename).toAbsolutePath();
		} catch (IOException e)
		{
			log.warn("Could not create screencast directory", e);
		}
		return null;
	}
}
