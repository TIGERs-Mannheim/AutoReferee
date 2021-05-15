/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Berkeley storage for cam frames
 */
@Log4j2
public class ShapeMapBerkeleyRecorder implements IBerkeleyRecorder
{
	private static final long BUFFER_TIME = 1_000_000_000L;
	private final WfwObserver wfwObserver = new WfwObserver();
	private final BerkeleyDb db;
	private List<ShapeMapWithSource> globalBuffer = new ArrayList<>();
	private long latestWrittenTimestamp = 0;
	private long latestReceivedTimestamp = 0;
	private boolean running = false;


	/**
	 * Create berkeley storage for cam frames
	 */
	public ShapeMapBerkeleyRecorder(BerkeleyDb db)
	{
		this.db = db;
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(wfwObserver);
		running = true;
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(wfwObserver);
		running = false;
	}


	@Override
	public void flush()
	{
		Map<Long, BerkeleyShapeMapFrame> toSave = new HashMap<>();

		List<ShapeMapWithSource> newBuffer = new ArrayList<>(globalBuffer.size());
		List<ShapeMapWithSource> localBuffer = globalBuffer;
		globalBuffer = newBuffer;

		Collections.sort(localBuffer);

		for (ShapeMapWithSource s : localBuffer)
		{
			// drop frame, if it is too old
			if (s.timestamp > latestWrittenTimestamp)
			{
				// frame is not too old, check if it is still buffering
				if (isBuffering(s.timestamp))
				{
					// this frame is still within the buffering time and so will all following
					// we have to re-add it to the buffer for next time
					int currentIndex = localBuffer.indexOf(s);
					globalBuffer.addAll(localBuffer.subList(currentIndex, localBuffer.size()));
					break;
				}
				BerkeleyShapeMapFrame f = toSave.computeIfAbsent(s.timestamp, BerkeleyShapeMapFrame::new);
				f.putShapeMap(s.source, s.shapeMap);
			} else
			{
				log.warn("Dropping too old shape map ({})", (s.timestamp - latestReceivedTimestamp));
			}
		}

		latestWrittenTimestamp = toSave.keySet().stream().mapToLong(i -> i).max().orElse(latestWrittenTimestamp);

		db.write(BerkeleyShapeMapFrame.class, toSave.values());
	}


	private boolean isBuffering(long timestamp)
	{
		return running && timestamp >= latestReceivedTimestamp - BUFFER_TIME;
	}


	@Value
	private static class ShapeMapWithSource implements Comparable<ShapeMapWithSource>
	{
		long timestamp;
		ShapeMap shapeMap;
		ShapeMapSource source;


		@Override
		public int compareTo(ShapeMapWithSource o)
		{
			return Long.compare(timestamp, o.timestamp);
		}
	}

	private class WfwObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final ShapeMapSource source)
		{
			ShapeMap shapeMapCopy = new ShapeMap();
			shapeMapCopy.addAll(shapeMap);
			shapeMapCopy.removeNonPersistent();
			globalBuffer.add(new ShapeMapWithSource(timestamp, shapeMapCopy, source));
			latestReceivedTimestamp = Math.max(timestamp, latestReceivedTimestamp);
		}
	}
}
