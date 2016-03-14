/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;


/**
 * Configuration object for geometry parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Geometry
{
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				fieldLength								= 8090;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				fieldWidth								= 6050;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				boundaryWidth							= 350;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				boundaryLength							= 350;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				judgesBorderWidth						= 425;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				judgesBorderLength					= 425;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH",
			"ROBOCUP" }, comment = "Distance (goal line - penalty mark)")
	private static double				distanceToPenaltyMark				= 1000;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH",
			"ROBOCUP" }, comment = "radius of the two, small quarter circles at the sides of the penalty area.")
	private static double				distanceToPenaltyArea				= 1000;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH",
			"ROBOCUP" }, comment = "the length of the short line of the penalty area, that is parallel to the goal line")
	private static double				lengthOfPenaltyAreaFrontLine		= 500;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				goalSize									= 1000;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static double				centerCircleRadius					= 1000;
	@Configurable(spezis = { "GRSIM", "SUMATRA", "LAB", "TISCH", "ROBOCUP" })
	private static String				ballModelIdentifier					= "default";
																							
																							
	@Configurable
	private static double				ballRadius								= 21.5;
	@Configurable
	private static double				botRadius								= 90;
	@Configurable
	private static double				stopSpeed								= 1.5;
	@Configurable
	private static double				botToBallDistanceStop				= 500;
	@Configurable
	private static double				goalDepth								= 180;
	@Configurable(comment = "Bots must be behind this line on penalty shot")
	private static double				distancePenaltyMarkToPenaltyLine	= 400;
																							
	@Configurable
	private static double				penaltyAreaMargin						= 100;
	@Configurable
	private static double				center2DribblerDistDefault			= 75;
																							
	@Configurable
	private static Double[]				cameraHeights							= new Double[] { 3500.0, 3500.0, 3500.0, 3500.0 };
	@Configurable
	private static Double[]				cameraFocalLength						= new Double[] { 0.0, 0.0, 0.0, 0.0 };
	@Configurable
	private static Double[]				cameraPrincipalPointX				= new Double[] { 0.0, 0.0, 0.0, 0.0 };
	@Configurable
	private static Double[]				cameraPrincipalPointY				= new Double[] { 0.0, 0.0, 0.0, 0.0 };
																							
																							
	@Configurable(comment = "If true, Geometry will be refreshed with data from vision, if available.")
	private static boolean				receiveGeometry						= true;
																							
																							
	/** Represents the field as a rectangle */
	private final Rectangle				field;
	/** Represents the field WITH margin as a rectangle */
	private final Rectangle				fieldWBorders;
	/** Represents the field with margin and referee area */
	private final Rectangle				fieldWReferee;
	/** Our Goal */
	private final Goal					goalOur;
	/** Their Goal */
	private final Goal					goalTheir;
	/** Tigers goal line */
	private final Line					goalLineOur;
	/** Opponent goal line */
	private final Line					goalLineTheir;
	/** Our Penalty Area ("Strafraum") */
	private final PenaltyArea			penaltyAreaOur;
	/** Their Penalty Area ("Strafraum") */
	private final PenaltyArea			penaltyAreaTheir;
	/** The no-go area for all bots during a penalty kick */
	private final Rectangle				penaltyKickAreaOur;
	/** The no-go area for all bots during a penalty kick */
	private final Rectangle				penaltyKickAreaTheir;
	/** Our penalty mark */
	private final Vector2f				penaltyMarkOur;
	/** Their penalty mark */
	private final Vector2f				penaltyMarkTheir;
	/** penalty line on our side (bots must be behind this line when a penalty kick is executed) */
	private final Vector2f				penaltyLineOur;
	/** penalty line on their side (bots must be behind this line when a penalty kick is executed) */
	private final Vector2f				penaltyLineTheir;
	/** The center of the field */
	private final IVector2				center									= Vector2.ZERO_VECTOR;
	/** The center circle ("Mittelkreis") */
	private final Circle					centerCircle;
												
	private final Rectangle				ourHalf;
	
	private final Rectangle				theirHalf;
	
	private final LearnedBallModel	ballModel;
												
												
	private static class ConfigCallback implements IConfigObserver
	{
		@Override
		public void afterApply(final IConfigClient configClient)
		{
			instance = new Geometry();
		}
	}
	
	
	static
	{
		ConfigRegistration.registerClass("geom", Geometry.class);
		ConfigRegistration.registerConfigurableCallback("geom", new ConfigCallback());
	}
	
	private static Geometry instance = new Geometry();
	
	
	/**
	 */
	private Geometry()
	{
		field = calcField(center, fieldLength, fieldWidth);
		fieldWBorders = calcField(center, fieldLength + (boundaryLength * 2), fieldWidth + (boundaryWidth * 2));
		fieldWReferee = calcField(center, fieldLength + (boundaryLength * 2) + (judgesBorderLength * 2), fieldWidth
				+ (boundaryWidth * 2) + (judgesBorderWidth * 2));
		goalOur = calcOurGoal(goalSize, fieldLength);
		goalTheir = calcTheirGoal(goalSize, fieldLength);
		goalLineOur = calcGoalLine(goalOur.getGoalCenter(), AVector2.Y_AXIS);
		goalLineTheir = calcGoalLine(goalTheir.getGoalCenter(), AVector2.Y_AXIS);
		penaltyAreaOur = new PenaltyArea(ETeam.TIGERS);
		penaltyAreaTheir = new PenaltyArea(ETeam.OPPONENTS);
		
		penaltyMarkOur = calcOurPenalityMark(fieldLength, distanceToPenaltyMark);
		penaltyMarkTheir = calcTheirPenalityMark(fieldLength, distanceToPenaltyMark);
		centerCircle = calcCenterCircle(center, centerCircleRadius);
		penaltyLineOur = calcOurPenalityLine(fieldLength, distanceToPenaltyMark, distancePenaltyMarkToPenaltyLine);
		penaltyLineTheir = calcTheirPenalityLine(fieldLength, distanceToPenaltyMark, distancePenaltyMarkToPenaltyLine);
		penaltyKickAreaOur = calcOurPenaltyKickArea(center, fieldLength, fieldWidth, distanceToPenaltyMark,
				distancePenaltyMarkToPenaltyLine);
		penaltyKickAreaTheir = calcTheirPenaltyKickArea(center, fieldLength, fieldWidth, distanceToPenaltyMark,
				distancePenaltyMarkToPenaltyLine);
		
		ourHalf = new Rectangle(field.topLeft(), field.xExtend() / 2, field.yExtend());
		theirHalf = new Rectangle(field.topLeft().addNew(new Vector2(field.xExtend() / 2, 0)), field.xExtend() / 2,
				field.yExtend());
		
		ballModel = new LearnedBallModel(ballModelIdentifier);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param geometry
	 */
	public static void setCamDetection(final CamGeometry geometry)
	{
		boundaryLength = geometry.getField().getBoundaryWidth();
		boundaryWidth = geometry.getField().getBoundaryWidth();
		fieldLength = geometry.getField().getFieldLength();
		fieldWidth = geometry.getField().getFieldWidth();
		goalSize = geometry.getField().getGoalWidth();
		goalDepth = geometry.getField().getGoalDepth();
		
		// TODO extract penalty area
		
		for (CamCalibration calib : geometry.getCalibrations().values())
		{
			Geometry.getCameraFocalLength()[calib.getCameraId()] = calib.getFocalLength();
			Geometry.getCameraPrincipalPointX()[calib.getCameraId()] = calib.getPrincipalPointX();
			Geometry.getCameraPrincipalPointY()[calib.getCameraId()] = calib.getPrincipalPointY();
		}
		
		instance = new Geometry();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static void refresh()
	{
		String env = SumatraModel.getInstance().getGlobalConfiguration().getString("environment");
		ConfigRegistration.applySpezi("geom", env);
		instance = new Geometry();
	}
	
	
	private Rectangle calcField(final IVector2 center, final double fieldLength, final double fieldWidth)
	{
		return new Rectangle(center.addNew(new Vector2f(-fieldLength / 2, fieldWidth / 2.0)), fieldLength, fieldWidth);
	}
	
	
	private Goal calcOurGoal(final double goalSize, final double fieldLength)
	{
		return new Goal(goalSize, new Vector2f(-fieldLength / 2, 0), -getGoalDepth());
	}
	
	
	private Goal calcTheirGoal(final double goalSize, final double fieldLength)
	{
		return new Goal(goalSize, new Vector2f(fieldLength / 2, 0), getGoalDepth());
	}
	
	
	private Line calcGoalLine(final IVector2 goalCenter, final IVector2 dir)
	{
		return new Line(goalCenter, dir);
	}
	
	
	private Circle calcCenterCircle(final IVector2 center, final double radius)
	{
		return new Circle(center, radius);
	}
	
	
	private Vector2f calcOurPenalityMark(final double fieldLength, final double distanceToPenaltyMark)
	{
		return new Vector2f((-fieldLength / 2.0) + distanceToPenaltyMark, 0);
	}
	
	
	private Vector2f calcTheirPenalityMark(final double fieldLength, final double distanceToPenaltyMark)
	{
		return new Vector2f((fieldLength / 2.0) - distanceToPenaltyMark, 0);
	}
	
	
	private Vector2f calcTheirPenalityLine(final double fieldLength, final double distanceToPenaltyMark,
			final double distanceToPenaltyLine)
	{
		return new Vector2f((fieldLength / 2.0) - distanceToPenaltyMark - distanceToPenaltyLine, 0);
	}
	
	
	private Vector2f calcOurPenalityLine(final double fieldLength, final double distanceToPenaltyMark,
			final double distanceTopenaltyLine)
	{
		return new Vector2f((-fieldLength / 2.0) + distanceToPenaltyMark + distanceTopenaltyLine, 0);
	}
	
	
	private Rectangle calcOurPenaltyKickArea(final IVector2 center, final double fieldLength, final double fieldWidth,
			final double distanceToPenaltyMark, final double distancePenaltyMarkToPenaltyLine)
	{
		return new Rectangle(center.addNew(new Vector2f(-fieldLength / 2, fieldWidth / 2)), distanceToPenaltyMark
				+ distancePenaltyMarkToPenaltyLine, fieldWidth);
	}
	
	
	private Rectangle calcTheirPenaltyKickArea(final IVector2 center, final double fieldLength, final double fieldWidth,
			final double distanceToPenaltyMark, final double distancePenaltyMarkToPenaltyLine)
	{
		return new Rectangle(center.addNew(new Vector2f(
				(fieldLength / 2) - (distanceToPenaltyMark + distancePenaltyMarkToPenaltyLine), fieldWidth / 2)),
				distanceToPenaltyMark + distancePenaltyMarkToPenaltyLine, fieldWidth);
	}
	
	
	/**
	 * Returns our goal.
	 * 
	 * @return goal object
	 */
	public static Goal getGoalOur()
	{
		return instance.goalOur;
	}
	
	
	/**
	 * Returns their goal.
	 * 
	 * @return goal object
	 */
	public static Goal getGoalTheir()
	{
		return instance.goalTheir;
	}
	
	
	/**
	 * @return the ballRadius [mm]
	 */
	public static double getBallRadius()
	{
		return ballRadius;
	}
	
	
	/**
	 * @return the stopSpeed
	 */
	public static double getStopSpeed()
	{
		return stopSpeed;
	}
	
	
	/**
	 * @return the botRadius [mm]
	 */
	public static double getBotRadius()
	{
		return botRadius;
	}
	
	
	/**
	 * Returns the field length [mm].
	 * 
	 * @return returns field length (x-axis).
	 */
	public static double getFieldLength()
	{
		return fieldLength;
	}
	
	
	/**
	 * Returns the field width [mm].
	 * 
	 * @return returns field length (y-axis).
	 */
	public static double getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	/**
	 * @return the field
	 */
	public static Rectangle getField()
	{
		return instance.field;
	}
	
	
	/**
	 * Field with border margin, but without referee area
	 * 
	 * @return the fieldWBorders
	 */
	public static Rectangle getFieldWBorders()
	{
		return instance.fieldWBorders;
	}
	
	
	/**
	 * Field including referee area
	 * 
	 * @return the fieldWBorders
	 */
	public static Rectangle getFieldWReferee()
	{
		return instance.fieldWReferee;
	}
	
	
	/**
	 * @return
	 */
	public static Line getGoalLineOur()
	{
		return instance.goalLineOur;
	}
	
	
	/**
	 * @return
	 */
	public static Line getGoalLineTheir()
	{
		return instance.goalLineTheir;
	}
	
	
	/**
	 * @return distance from goal line to penalty mark
	 */
	public static double getDistanceToPenaltyMark()
	{
		return distanceToPenaltyMark;
	}
	
	
	/**
	 * @return Vector pointing to our penalty mark
	 */
	public static Vector2f getPenaltyMarkOur()
	{
		return instance.penaltyMarkOur;
	}
	
	
	/**
	 * @return Vector pointing to their penalty mark
	 */
	public static Vector2f getPenaltyMarkTheir()
	{
		return instance.penaltyMarkTheir;
	}
	
	
	/**
	 * @return
	 */
	public static IVector2 getCenter()
	{
		return instance.center;
	}
	
	
	/**
	 * @return
	 */
	public static double getGoalSize()
	{
		return goalSize;
	}
	
	
	/**
	 * @return the goalDepth
	 */
	public static double getGoalDepth()
	{
		return goalDepth;
	}
	
	
	/**
	 * @return
	 */
	public static double getCenterCircleRadius()
	{
		return centerCircleRadius;
	}
	
	
	/**
	 * @return
	 */
	public static Circle getCenterCircle()
	{
		return instance.centerCircle;
	}
	
	
	/**
	 * @return distance from goal line to penalty area
	 */
	public static double getDistanceToPenaltyArea()
	{
		return distanceToPenaltyArea;
	}
	
	
	/**
	 * @return
	 */
	public static double getLengthOfPenaltyAreaFrontLine()
	{
		return lengthOfPenaltyAreaFrontLine;
	}
	
	
	/**
	 * @return
	 */
	public static PenaltyArea getPenaltyAreaOur()
	{
		return instance.penaltyAreaOur;
	}
	
	
	/**
	 * @return
	 */
	public static PenaltyArea getPenaltyAreaTheir()
	{
		return instance.penaltyAreaTheir;
	}
	
	
	/**
	 * @return the ourHalf
	 */
	public static Rectangle getHalfOur()
	{
		return instance.ourHalf;
	}
	
	
	/**
	 * @return their half of the field
	 */
	public static Rectangle getHalfTheir()
	{
		return instance.theirHalf;
	}
	
	
	/**
	 * penalty line on our side (bots must be behind this line when a penalty kick is executed)
	 * 
	 * @return vector pointing to the center of the line
	 */
	public static Vector2f getPenaltyLineOur()
	{
		return instance.penaltyLineOur;
	}
	
	
	/**
	 * penalty line their side(bots must be behind this line when a penalty kick is executed)
	 * 
	 * @return vector pointing to the center of the line
	 */
	public static Vector2f getPenaltyLineTheir()
	{
		return instance.penaltyLineTheir;
	}
	
	
	/**
	 * @return The width of the border around the field
	 */
	public static double getBoundaryWidth()
	{
		return boundaryWidth;
	}
	
	
	/**
	 * @return distance from penalty mark to penalty line
	 */
	public static double getDistancePenaltyMarkToPenaltyLine()
	{
		return distancePenaltyMarkToPenaltyLine;
	}
	
	
	/**
	 * distance between ball and bot required during stop signal (without ball and bot radius!)
	 * 
	 * @return distance
	 */
	public static double getBotToBallDistanceStop()
	{
		return botToBallDistanceStop;
	}
	
	
	/**
	 * @return the boundaryLength
	 */
	public static final double getBoundaryLength()
	{
		return boundaryLength;
	}
	
	
	/**
	 * @return the judgesBorderWidth
	 */
	public static double getJudgesBorderWidth()
	{
		return judgesBorderWidth;
	}
	
	
	/**
	 * @return the judgesBorderLength
	 */
	public static double getJudgesBorderLength()
	{
		return judgesBorderLength;
	}
	
	
	/**
	 * The default penalty Area margin. Use this is your base margin.
	 * You may want to set a relative margin to this one.
	 * 
	 * @return
	 */
	public static double getPenaltyAreaMargin()
	{
		return penaltyAreaMargin;
	}
	
	
	/**
	 * Default distance from robot center to dribbler.
	 * Consider taking this value from ABot, as it could have a better value!
	 * 
	 * @return the center2DribblerDistDefault
	 */
	public static final double getCenter2DribblerDistDefault()
	{
		return center2DribblerDistDefault;
	}
	
	
	/**
	 * @return the cameraHeights
	 */
	public static Double[] getCameraHeights()
	{
		return cameraHeights;
	}
	
	
	/**
	 * @return the cameraHeights
	 */
	public static Double[] getCameraFocalLength()
	{
		return cameraFocalLength;
	}
	
	
	/**
	 * @return the cameraPrincipalPointX
	 */
	public static Double[] getCameraPrincipalPointX()
	{
		return cameraPrincipalPointX;
	}
	
	
	/**
	 * @return the cameraPrincipalPointY
	 */
	public static Double[] getCameraPrincipalPointY()
	{
		return cameraPrincipalPointY;
	}
	
	
	/**
	 * @return the receiveGeometry
	 */
	public static boolean isReceiveGeometry()
	{
		return receiveGeometry;
	}
	
	
	/**
	 * @return the ballModelIdentifier
	 */
	public static String getBallModelIdentifier()
	{
		return ballModelIdentifier;
	}
	
	
	/**
	 * @return the ballModel
	 */
	public static LearnedBallModel getBallModel()
	{
		return instance.ballModel;
	}
	
	
	/**
	 * @return the penaltyKickAreaOur
	 */
	public static Rectangle getPenaltyKickAreaOur()
	{
		return instance.penaltyKickAreaOur;
	}
	
	
	/**
	 * @return the penaltyKickAreaTheir
	 */
	public static Rectangle getPenaltyKickAreaTheir()
	{
		return instance.penaltyKickAreaTheir;
	}
}
