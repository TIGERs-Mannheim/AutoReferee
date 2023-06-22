/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;


/**
 * Test class for ellipse
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class EllipseTest
{

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final double ASSERT_EQUALS_DELTA = 0.001;


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------


	private List<Ellipse> getFlatEllipses()
	{
		var ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 100, 70);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(100, 100), 100, 70);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(-100, -100), 100, 70);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(100, -100), 100, 70);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(-100, 100), 100, 70);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 10, 9);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 1000, 13);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 4000, 2000);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 4000, 3999);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 1000, 1000);
		ells.add(tmpEll);
		return ells;
	}


	private List<Ellipse> getThinEllipses()
	{
		var ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 70, 100);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(100, 100), 70, 100);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(-100, -100), 70, 100);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(100, -100), 70, 100);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(-100, 100), 70, 100);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 9, 10);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 13, 1000);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 2000, 4000);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 3999, 4000);
		ells.add(tmpEll);
		tmpEll = Ellipse.createEllipse(Vector2.fromXY(0, 0), 1000, 1000);
		ells.add(tmpEll);
		return ells;
	}


	private List<Ellipse> getTurnedEllipses()
	{
		// both flat and thin
		var ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 70, 100, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 70, 100, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 70, 100, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, 100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, -100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, -100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, -100), 70, 100, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, -100), 70, 100, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, -100), 70, 100, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, 100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 9, 10, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 13, 1000, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 2000, 4000, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 3999, 4000, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 1000, 1000, Math.PI);
		ells.add(tmpEll);

		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, 100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, -100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, -100), 100, 70, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, -100), 100, 70, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, -100), 100, 70, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(100, -100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(-100, 100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 10, 9, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 1000, 13, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 4000, 2000, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 4000, 3999, Math.PI);
		ells.add(tmpEll);
		tmpEll = Ellipse.createTurned(Vector2.fromXY(0, 0), 1000, 1000, Math.PI);
		ells.add(tmpEll);
		return ells;
	}


	private List<Ellipse> getAllEllipses()
	{
		var ells = new ArrayList<Ellipse>(50);
		ells.addAll(getFlatEllipses());
		ells.addAll(getThinEllipses());
		ells.addAll(getTurnedEllipses());
		return ells;
	}


	/**
	 * Test {@link Ellipse#getFocusFromCenter()}
	 */
	@Test
	public void testGetFocusFromCenter()
	{
		for (Ellipse ell : getAllEllipses())
		{
			final String errStr = "Failed for " + ell.toString();

			// vector from center to focus without turn angle of ell
			IVector2 focusFromCenter = ell.getFocusFromCenter().turnNew(-ell.getTurnAngle());
			double distCF = VectorMath.distancePP(ell.center().addNew(focusFromCenter), ell.center());

			// final double x = (ell.getRadiusX() < ell.getRadiusY() ? ell.getRadiusX() : 0);
			// final double y = (ell.getRadiusX() < ell.getRadiusY() ? 0 : ell.getRadiusY());

			// focus should be on one of the axis (remember, we turned focusFromCenter)
			Assert.assertEquals(errStr, 0, Math.min(focusFromCenter.x(), focusFromCenter.y()), ASSERT_EQUALS_DELTA);
			// is distance center <-> focus smaller than greater radius?
			Assert.assertTrue(errStr + " " + distCF, distCF < Math.max(ell.getRadiusX(), ell.getRadiusY()));
		}
	}


	@Test
	public void testConstructor()
	{
		IVector2 center = Vector2.fromXY(42, 42);
		double radiusX = 2 * 42;
		double radiusY = 3 * 42;
		Ellipse ell = Ellipse.createEllipse(center, radiusX, radiusY);
		Assert.assertEquals(radiusX, ell.getRadiusX(), ASSERT_EQUALS_DELTA);
		Assert.assertEquals(radiusY, ell.getRadiusY(), ASSERT_EQUALS_DELTA);
		Assert.assertTrue(center.isCloseTo(ell.center(), ASSERT_EQUALS_DELTA));
	}


	/**
	 * Test intersecting methods {@link Ellipse#intersectPerimeterPath(IPath)}
	 * {@link Ellipse#isIntersectingWithPath(IPath)}
	 */
	@Test
	public void testIntersectingWithLines()
	{
		for (Ellipse ell : getAllEllipses())
		{
			double smallerRadius = Math.min(ell.getRadiusX(), ell.getRadiusY());
			double biggerRadius = Math.max(ell.getRadiusX(), ell.getRadiusY());
			var lines = new HashMap<ILineBase, Integer>();
			ILineBase tmpLine;

			// Unbounded Lines
			tmpLine = Lines.lineFromDirection(ell.center(), Vector2.fromXY(1, 0));
			lines.put(tmpLine, 2);
			tmpLine = Lines.lineFromDirection(ell.center(), Vector2.fromXY(0, 1));
			lines.put(tmpLine, 2);
			tmpLine = Lines.lineFromDirection(ell.center().addNew(Vector2.fromXY(0, smallerRadius - 1)),
					Vector2.fromXY(1, 0));
			lines.put(tmpLine, 2);

			// Half lines
			tmpLine = Lines.halfLineFromDirection(ell.center(), Vector2.fromXY(1, 0));
			lines.put(tmpLine, 1);
			tmpLine = Lines.halfLineFromDirection(ell.center(), Vector2.fromXY(0, 1));
			lines.put(tmpLine, 1);

			// Line segments
			tmpLine = Lines.segmentFromOffset(ell.center(), Vector2.fromXY(smallerRadius - 1, 0));
			lines.put(tmpLine, 0);
			tmpLine = Lines.segmentFromOffset(ell.center(), Vector2.fromXY(0, smallerRadius - 1));
			lines.put(tmpLine, 0);
			tmpLine = Lines.segmentFromOffset(ell.center(), Vector2.fromXY(biggerRadius + 1, 0));
			lines.put(tmpLine, 1);
			tmpLine = Lines.segmentFromOffset(ell.center(), Vector2.fromXY(0, biggerRadius + 1));
			lines.put(tmpLine, 1);


			for (var entry : lines.entrySet())
			{
				var line = entry.getKey();
				int expIntersectionPoints = entry.getValue();

				final String errStr = "Failed with " + ell + " and " + line.toString();

				List<IVector2> points = ell.intersectPerimeterPath(line);
				Assert.assertEquals(errStr, expIntersectionPoints, points.size());

				for (IVector2 p : points)
				{
					double dist = VectorMath.distancePP(ell.center(), p);
					Assert.assertTrue(errStr, dist < ell.getDiameterMax());
				}
				if (expIntersectionPoints > 0)
				{
					Assert.assertTrue(errStr, ell.isIntersectingWithPath(line));
				}
			}
		}
	}


	@Test
	public void testNearestPointOutside()
	{
		for (Ellipse ell : getAllEllipses())
		{
			IVector2 p1 = ell.center().addNew(Vector2.fromXY(0, ell.getRadiusY() / 2.0).turn(ell.getTurnAngle()));
			Assert.assertTrue("Failed with " + ell + " " + p1, VectorMath.distancePP(
					ell.center().addNew(Vector2.fromXY(0, ell.getRadiusY()).turn(ell.getTurnAngle())),
					ell.nearestPointOutside(p1)) < ASSERT_EQUALS_DELTA);
		}
	}


	@Test
	public void testIsPointInShape()
	{
		for (Ellipse ell : getAllEllipses())
		{
			final String errStr = "Failed with " + ell.toString();

			double smallerRadius = Math.min(ell.getRadiusX(), ell.getRadiusY());

			Assert.assertTrue(errStr, ell.isPointInShape(ell.center()));
			Assert.assertTrue(errStr, ell.isPointInShape(ell.center().addNew(Vector2.fromXY(smallerRadius, 0))));
			Assert.assertTrue(errStr, ell.isPointInShape(ell.center().addNew(Vector2.fromXY(0, smallerRadius))));
		}

	}


	/**
	 * Tests {@link Ellipse#stepAlongPath(double)}
	 */
	@Test
	public void testStepAlongPath()
	{
		for (Ellipse ell : getAllEllipses())
		{
			var cfs = new HashMap<Double, Double>(2);
			cfs.put(ell.getPerimeterLength(), 0.0);
			cfs.put(-ell.getPerimeterLength(), 0.0);
			cfs.put(ell.getPerimeterLength() / 2, (ell.getRadiusX() * 2));
			cfs.put(-ell.getPerimeterLength() / 2, (ell.getRadiusX() * 2));
			for (Map.Entry<Double, Double> entry : cfs.entrySet())
			{
				double circumference = entry.getKey();
				var start = ell.getPathStart();
				var end = ell.stepAlongPath(circumference);
				double dist = VectorMath.distancePP(start, end);
				// we are not that accurate
				final double tol = 2 + entry.getValue();
				Assert.assertTrue("Failed with " + dist + " " + start + " " + end + " " + ell, dist < tol);
			}
		}
	}


	@Test
	public void testGetPerimeterPath()
	{
		var ell = Ellipse.createEllipse(Vector2.fromXY(0, 0), 100, 100);
		assertThat(ell.getPerimeterPath()).containsExactly(ell);
	}


	@Test
	public void testPerimeterPathOrder()
	{
		var perimeter = Ellipse.createEllipse(Vector2.fromXY(0, 0), 100, 100).getPerimeterPath();
		IBoundedPath lastPath = null;
		for (var p : perimeter)
		{
			if (lastPath != null)
			{
				assertThat(p.getPathStart()).isEqualTo(p.getPathStart());
			}
			lastPath = p;
		}
	}


	/**
	 * Tests the correctness of the circumference ("Umfang")
	 */
	@Test
	public void testGetPerimeterLength()
	{
		Ellipse ell;
		ell = Ellipse.createEllipse(Vector2.fromXY(0, 0), 100, 100);
		assertThat(2 * Math.PI * ell.getRadiusX()).isCloseTo(ell.getPerimeterLength(), within(ASSERT_EQUALS_DELTA));
		assertThat(ell.getLength()).isEqualTo(ell.getPerimeterLength());
		ell = Ellipse.createEllipse(Vector2.fromXY(0, 0), 4000, 4000);
		assertThat(2 * Math.PI * ell.getRadiusX()).isCloseTo(ell.getPerimeterLength(), within(ASSERT_EQUALS_DELTA));
		assertThat(ell.getLength()).isEqualTo(ell.getPerimeterLength());
		ell = Ellipse.createEllipse(Vector2.fromXY(42, -42), 2000, 2000);
		assertThat(2 * Math.PI * ell.getRadiusX()).isCloseTo(ell.getPerimeterLength(), within(ASSERT_EQUALS_DELTA));
		assertThat(ell.getLength()).isEqualTo(ell.getPerimeterLength());

		Ellipse ell1 = Ellipse.createEllipse(Vector2.fromXY(42, -42), 1800, 2200);
		Ellipse ell2 = Ellipse.createEllipse(Vector2.fromXY(42, -42), 2200, 1800);
		assertThat(ell1.getPerimeterLength()).isCloseTo(ell2.getPerimeterLength(), within(ASSERT_EQUALS_DELTA));

		Ellipse ell3 = Ellipse.createTurned(Vector2.fromXY(42, -42), 1800, 2200, 0);
		Ellipse ell4 = Ellipse.createTurned(Vector2.fromXY(42, -42), 2200, 1800, Math.PI / 2.0);
		assertThat(ell3.getPerimeterLength()).isCloseTo(ell4.getPerimeterLength(), within(ASSERT_EQUALS_DELTA));
	}


	@Test
	public void testWithMargin()
	{
		var ell = Ellipse.createEllipse(Vector2.fromXY(0, 0), 100, 150);
		var withMargin = ell.withMargin(-10);
		assertThat(withMargin.center()).isEqualTo(ell.center());
		assertThat(withMargin.getRadiusX()).isCloseTo(90.0, within(1e-6));
		assertThat(withMargin.getRadiusY()).isCloseTo(140.0, within(1e-6));
		withMargin = ell.withMargin(10);
		assertThat(withMargin.center()).isEqualTo(ell.center());
		assertThat(withMargin.getRadiusX()).isCloseTo(110.0, within(1e-6));
		assertThat(withMargin.getRadiusY()).isCloseTo(160.0, within(1e-6));
	}


	@Test
	public void testPointsAroundPath()
	{
		var ellipse = Ellipse.createEllipse(Vector2.fromX(2), 2, 1);
		assertThat(ellipse.nearestPointInside(ellipse.center())).isEqualTo(ellipse.center());
		assertThat(ellipse.nearestPointOnPerimeterPath(ellipse.center())).isEqualTo(Vector2.fromXY(2, 1));
		assertThat(ellipse.nearestPointOutside(ellipse.center())).isEqualTo(Vector2.fromXY(2, 1));

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(3.999, 0), Vector2.fromXY(4.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(0.001, 0), Vector2.fromXY(-0.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(2, 0.999), Vector2.fromXY(2, 1.001)),
				Lines.segmentFromPoints(Vector2.fromXY(2, -0.999), Vector2.fromXY(2, -1.001))
		);

		for (var segment : segments)
		{
			assertThat(ellipse.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(ellipse.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(ellipse.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(ellipse.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(ellipse.closestPointOnPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.closestPointOnPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(ellipse.closestPointOnPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(ellipse.distanceTo(segment.getPathStart())).isCloseTo(0.001, within(1e-10));
			assertThat(ellipse.distanceTo(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(ellipse.distanceTo(segment.getPathEnd())).isCloseTo(0.001, within(1e-10));

			assertThat(ellipse.distanceToSqr(segment.getPathStart())).isCloseTo(0.000001, within(1e-10));
			assertThat(ellipse.distanceToSqr(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(ellipse.distanceToSqr(segment.getPathEnd())).isCloseTo(0.000001, within(1e-10));

			assertThat(ellipse.isPointOnPath(segment.getPathStart())).isFalse();
			assertThat(ellipse.isPointOnPath(segment.getPathCenter())).isTrue();
			assertThat(ellipse.isPointOnPath(segment.getPathEnd())).isFalse();
		}
	}


	@Test
	public void testIntersectPerimeterPathCircle()
	{
		var roundEllipse = Ellipse.createEllipse(Vector2f.ZERO_VECTOR, 1, 1);
		var circle = Circle.createCircle(Vector2f.fromXY(1, 1), 1);

		assertThat(roundEllipse.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromX(1),
				Vector2.fromY(1)
		);

		var ellipse = Ellipse.createEllipse(Vector2f.ZERO_VECTOR, 1, 2);
		assertThatThrownBy(() -> roundEllipse.intersect(ellipse)).isOfAnyClassIn(NotImplementedException.class);
	}


	@Test
	public void testIntersectPerimeterPathArc()
	{
		var roundEllipse = Ellipse.createEllipse(Vector2f.ZERO_VECTOR, 1, 1);
		var arc = Arc.createArc(Vector2f.fromXY(1, 1), 1, -3 * AngleMath.PI_QUART, AngleMath.PI_QUART);
		assertThat(roundEllipse.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromX(1)
		);

		var ellipse = Ellipse.createEllipse(Vector2f.ZERO_VECTOR, 1, 2);
		assertThatThrownBy(() -> roundEllipse.intersect(ellipse)).isOfAnyClassIn(NotImplementedException.class);
	}


	@Test
	public void testIsValid()
	{
		var center = Vector2f.ZERO_VECTOR;
		var proper = Ellipse.createEllipse(center, 1, 1);
		var invalid1 = Ellipse.createEllipse(center, 1e-6, 1);
		var invalid2 = Ellipse.createEllipse(center, 1, 1e-6);

		assertThat(proper.isValid()).isTrue();
		assertThat(invalid1.isValid()).isFalse();
		assertThat(invalid2.isValid()).isFalse();

		proper = Ellipse.createTurned(center, 1, 1, AngleMath.PI_QUART);
		invalid1 = Ellipse.createTurned(center, 1e-6, 1, AngleMath.PI_HALF);
		invalid2 = Ellipse.createTurned(center, 1, 1e-6, AngleMath.PI);

		assertThat(proper.isValid()).isTrue();
		assertThat(invalid1.isValid()).isFalse();
		assertThat(invalid2.isValid()).isFalse();
	}


	@Test
	public void testGetPathPoints()
	{
		var center = Vector2f.ZERO_VECTOR;
		var radiusX = 10;
		var ellipse = Ellipse.createEllipse(center, radiusX, 100);
		assertThat(ellipse.getPathStart()).isEqualTo(Vector2.fromX(radiusX));
		assertThat(ellipse.getPathEnd()).isEqualTo(ellipse.getPathStart());
		assertThat(ellipse.getPathCenter()).isEqualTo(Vector2.fromX(-radiusX));

		radiusX = 1;
		ellipse = Ellipse.createEllipse(center, radiusX, 100);
		assertThat(ellipse.getPathStart()).isEqualTo(Vector2.fromX(radiusX));
		assertThat(ellipse.getPathEnd()).isEqualTo(ellipse.getPathStart());
		assertThat(ellipse.getPathCenter()).isEqualTo(Vector2.fromX(-radiusX));

		ellipse = Ellipse.createTurned(center, radiusX, 100, AngleMath.PI_HALF);
		assertThat(ellipse.getPathStart()).isEqualTo(Vector2.fromY(radiusX));
		assertThat(ellipse.getPathEnd()).isEqualTo(ellipse.getPathStart());
		assertThat(ellipse.getPathCenter()).isEqualTo(Vector2.fromY(-radiusX));
	}
}
