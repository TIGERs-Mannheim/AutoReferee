/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.ACamObject;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.ETimable;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 */
public abstract class AWorldPredictor extends AModule implements ICamFrameObserver, IConfigObserver
{
	@SuppressWarnings("unused")
	private static final Logger				log					= Logger.getLogger(AWorldPredictor.class.getName());
																				
	/** */
	public static final String					MODULE_TYPE			= "AWorldPredictor";
	/** */
	public static final String					MODULE_ID			= "worldpredictor";
																				
	private SumatraTimer							timer					= null;
	private List<IWorldFrameObserver>		observers			= new CopyOnWriteArrayList<>();
	private boolean								geometryReceived	= false;
																				
	private WorldInfoProcessor					infoProcessor		= new WorldInfoProcessor();
	private final List<IWfPostProcessor>	postProcessors		= new ArrayList<>();
																				
	private CamBall								lastSeenBall		= new CamBall();
																				
	@Configurable(comment = "P1 of rectangle defining a range where objects are ignored")
	private static IVector2						exclusionRectP1	= Vector2.ZERO_VECTOR;
	@Configurable(comment = "P2 of rectangle defining a range where objects are ignored")
	private static IVector2						exclusionRectP2	= Vector2.ZERO_VECTOR;
	@Configurable
	private static boolean						ownThread			= false;
																				
																				
	private ExecutorService						execService;
	private final Object							execServiceSync	= new Object();
	private final Object							lastSeenBallSync	= new Object();
																				
																				
	static
	{
		ConfigRegistration.registerClass("wp", AWorldPredictor.class);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param config
	 */
	public AWorldPredictor(final SubnodeConfiguration config)
	{
		ConfigRegistration.registerConfigurableCallback("wp", this);
	}
	
	
	private void startTime(final long frameId)
	{
		if (timer != null)
		{
			timer.start(ETimable.WP, frameId);
		}
	}
	
	
	private void stopTime(final long frameId)
	{
		if (timer != null)
		{
			timer.stop(ETimable.WP, frameId);
		}
	}
	
	
	private boolean isWithinExclusionRectangle(final IVector2 p)
	{
		Rectangle rect = new Rectangle(exclusionRectP1, exclusionRectP2);
		return rect.isPointInShape(p, -0.00001f);
	}
	
	
	private <T extends ACamObject> List<T> filterCamObjects(final List<T> incoming)
	{
		List<T> newObjects = new ArrayList<>();
		for (T newBall : incoming)
		{
			if (isWithinExclusionRectangle(newBall.getPos().getXYVector()))
			{
				continue;
			}
			newObjects.add(newBall);
		}
		return newObjects;
	}
	
	
	private void processCameraDetectionFrameInternal(final CamDetectionFrame frame)
	{
		startTime(frame.getFrameNumber());
		
		List<CamRobot> newBotY = filterCamObjects(frame.getRobotsYellow());
		List<CamRobot> newBotB = filterCamObjects(frame.getRobotsBlue());
		List<CamBall> newBalls = filterCamObjects(frame.getBalls());
		
		CamBall ball = findCurrentBall(newBalls);
		
		ExtendedCamDetectionFrame eFrame = new ExtendedCamDetectionFrame(frame, newBalls, newBotY, newBotB, ball);
		
		for (IWorldFrameObserver obs : observers)
		{
			obs.onNewCamDetectionFrame(eFrame);
		}
		processCameraDetectionFrame(eFrame);
		stopTime(frame.getFrameNumber());
	}
	
	
	private CamBall findCurrentBall(final List<CamBall> balls)
	{
		if (balls.isEmpty())
		{
			return lastSeenBall;
		}
		
		synchronized (lastSeenBallSync)
		{
			CamBall ballToUse = balls.get(0);
			double shortestDifference = difference(ballToUse, lastSeenBall);
			for (CamBall ball : balls)
			{
				double diff = difference(ball, lastSeenBall);
				if (diff < shortestDifference)
				{
					ballToUse = ball;
					shortestDifference = diff;
				}
			}
			
			boolean containsBallFromCurrentCam = balls.stream()
					.anyMatch(b -> b.getCameraId() == lastSeenBall.getCameraId());
			double dt = Math.abs(lastSeenBall.getTimestamp() - ballToUse.getTimestamp()) / 1e9;
			if (!containsBallFromCurrentCam && (dt < 0.05))
			{
				// wait some time before switching cameras
				// note: with a delay of 50ms, the ball can travel up to 40cm with 8m/s
				// but: if the ball is fast, it will slow down quickly and it will not do unexpected direction changes,
				// so we accept this to get more stable ball positions.
				return lastSeenBall;
			}
			double dist = difference(ballToUse, lastSeenBall) / 1000.0;
			double vel = dist * dt;
			if (vel > 15)
			{
				// high velocity, probably noise
				return lastSeenBall;
			}
			
			// 1mm -> 0.1ms
			// 10mm -> 1ms -> 10m/s
			// if the ball is moving with less than 10m/s, the new ball will always be used. (following if will be false)
			// if (difference(lastSeenBall, ballToUse) > (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()
			// - lastSeenBall.getTimestamp()) * 10))
			// {
			// return lastSeenBall;
			// }
			lastSeenBall = new CamBall(ballToUse);
			return ballToUse;
		}
	}
	
	
	private double difference(final CamBall ball1, final CamBall ball2)
	{
		if (ball1.getTimestamp() > (ball2.getTimestamp() + 1e8))
		{
			return 0;
		}
		return ball1.getPos().subtractNew(ball2.getPos()).getLength2()
				// add a penalty to balls that are not on same camera
				+ (ball1.getCameraId() == ball2.getCameraId() ? 0 : 200);
	}
	
	
	protected abstract void processCameraDetectionFrame(final ExtendedCamDetectionFrame frame);
	
	
	protected void processCameraGeometry(final CamGeometry geometry)
	{
	}
	
	
	protected void start()
	{
	}
	
	
	protected void stop()
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param swf
	 */
	public final void pushFrame(final SimpleWorldFrame swf)
	{
		try
		{
			SimpleWorldFrame ppSwf = swf;
			for (IWfPostProcessor pp : postProcessors)
			{
				ppSwf = pp.process(ppSwf);
			}
			WorldFrameWrapper wrapped = infoProcessor.processSimpleWorldFrame(ppSwf);
			for (IWorldFrameObserver c : observers)
			{
				c.onNewWorldFrame(wrapped);
			}
		} catch (Throwable err)
		{
			log.error("Error processing worldframe", err);
		}
	}
	
	
	protected void processMotionContext(final MotionContext context)
	{
		for (IWfPostProcessor pp : postProcessors)
		{
			pp.processMotionContext(context);
		}
	}
	
	
	/**
	 * Set a new last ball pos to force the BallProcessor to use another visible ball
	 * 
	 * @param pos
	 */
	public void setLatestBallPosHint(final IVector2 pos)
	{
		synchronized (lastSeenBallSync)
		{
			long timestamp = lastSeenBall.getTimestamp() + (long) (5e8);
			lastSeenBall = new CamBall(1, 0, pos.x(), pos.y(), 0, 0, 0, timestamp, timestamp, lastSeenBall.getCameraId(),
					lastSeenBall.getFrameId());
		}
	}
	
	
	/**
	 * @param consumer
	 */
	public final void addWorldFrameConsumer(final IWorldFrameObserver consumer)
	{
		observers.add(consumer);
	}
	
	
	/**
	 * @param consumer
	 */
	public final void removeWorldFrameConsumer(final IWorldFrameObserver consumer)
	{
		observers.remove(consumer);
	}
	
	
	/**
	 * @param pp
	 */
	public final void addPostProcessor(final IWfPostProcessor pp)
	{
		postProcessors.add(pp);
	}
	
	
	/**
	 * @param pp
	 * @return
	 */
	public final boolean removePostProcessor(final IWfPostProcessor pp)
	{
		return postProcessors.remove(pp);
	}
	
	
	@Override
	public final void onNewCameraFrame(final CamDetectionFrame frame)
	{
		synchronized (execServiceSync)
		{
			if (ownThread && (execService != null))
			{
				execService.execute(() -> processCameraDetectionFrameInternal(frame));
			} else
			{
				processCameraDetectionFrameInternal(frame);
			}
		}
	}
	
	
	@Override
	public final void onNewCameraGeometry(final CamGeometry geometry)
	{
		if (Geometry.isReceiveGeometry())
		{
			Geometry.setCamDetection(geometry);
			if (!geometryReceived)
			{
				geometryReceived = true;
				log.info("Received geometry from vision");
			}
		}
		
		processCameraGeometry(geometry);
	}
	
	
	@Override
	public void onClearCamFrame()
	{
		for (IWorldFrameObserver obs : observers)
		{
			obs.onClearWorldFrame();
			obs.onClearCamDetectionFrame();
		}
		synchronized (lastSeenBallSync)
		{
			lastSeenBall = new CamBall();
		}
		infoProcessor.stop();
		infoProcessor.start();
	}
	
	
	@Override
	public final void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public final void deinitModule()
	{
	}
	
	
	@Override
	public final void startModule() throws StartModuleException
	{
		start();
		
		geometryReceived = false;
		Geometry.refresh();
		
		if (!observers.isEmpty())
		{
			log.warn("There were observers left: " + observers);
			observers.clear();
		}
		infoProcessor.start();
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.warn("No timer found");
		}
	}
	
	
	@Override
	public final void stopModule()
	{
		stop();
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		infoProcessor.stop();
		onClearCamFrame();
		if (execService != null)
		{
			synchronized (execServiceSync)
			{
				execService.shutdown();
				execService = null;
			}
		}
	}
	
	
	/**
	 * @param configClient
	 */
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		synchronized (execServiceSync)
		{
			if (ownThread && (execService == null))
			{
				execService = Executors.newSingleThreadExecutor(new NamedThreadFactory("WP_worker"));
			}
		}
	}
}
