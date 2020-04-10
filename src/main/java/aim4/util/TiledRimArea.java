package aim4.util;

import aim4.config.Debug;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A tiled area - a subdivision of an area into a grid of small shapes.
 */
public class TiledRimArea {
	/////////////////////////////////
	// NESTED CLASSES
	/////////////////////////////////

	/**
	 * A tile.
	 */
	public static class Tile {
		/** The area controlled by this tile. */
		private final Area area;
		/** The start angle of the tile. */
		private final double startAngle;
		/** The angle of the tile. */
		private final double angle;
		/** the id of this tile */
		private final int id;

		/**
		 * Create a tile.
		 *
		 * @param area  the area of the tile
		 * @param angle the angle of the tile
		 * @param id    the ID of the tile
		 */
		public Tile(Area area, double startAngle, double angle, int id) {
			this.area = area;
			this.startAngle = startAngle;
			this.angle = angle;
			this.id = id;
		}

		/** Get the area controlled by this ReservationTile. */
		public Area getArea() {
			return area;
		}

		/** Get the start angle of the tile. */
		public double getStartAngle() {
			return startAngle;
		}

		/** Get the angle of the tile. */
		public double getAngle() {
			return angle;
		}

		/** Get the id of this tile */
		public int getId() {
			return id;
		}
	}

	/////////////////////////////////
	// PRIVATE FIELDS
	/////////////////////////////////

	/** The minimal circle of the intersection. */
	private final Ellipse2D minimalCircle;
	/** The central circle of the intersection. */
	private final Ellipse2D centralCircle;
	/** The maximal circle of the intersection. */
	private final Ellipse2D maximalCircle;
	/** The factor by which the circle is divided. */
	private final double granularity;
	/** The number of lanes per road. */
	private final int laneNum;
	/** A mapping from id to tiles */
	private final ArrayList<Tile> idToTiles;
	/** The number of tiles */
	private int numberOfTiles;
	/** The number of inner tiles */
	private int numberOfInnerTiles;

	/////////////////////////////////
	// CLASS CONSTRUCTORS
	/////////////////////////////////

	/**
	 * Create a tiled area
	 *
	 * @param minimalCircle the minimal circle of the intersection
	 * @param centralCircle the central circle of the intersection
	 * @param maximalCircle the maximal circle of the intersection
	 * @param granularity   the factor by which the tiles are divided
	 * @param laneNum 		the number of lanes per road
	 */
	public TiledRimArea(Ellipse2D minimalCircle, Ellipse2D centralCircle, Ellipse2D maximalCircle, double granularity,
			int laneNum) {
		this.minimalCircle = minimalCircle;
		this.centralCircle = centralCircle;
		this.maximalCircle = maximalCircle;
		this.granularity = granularity;
		this.laneNum = laneNum;
		numberOfInnerTiles = (int) granularity * 2 * laneNum;
		numberOfTiles = numberOfInnerTiles;
		idToTiles = new ArrayList<Tile>(numberOfTiles);
		createTiles();
	}

	/**
	 * Create the tiles
	 */
	private void createTiles() {
		// Angle in degrees by which the circle will be divided
		double angle = Math.toDegrees(GeomMath.PI) / granularity;

		// The origin of the minimal and maximal circle
		Point2D origin = new Point2D.Double(minimalCircle.getCenterX(), minimalCircle.getCenterY());
		// The radius of the minimal circle
		double minimalRadius = minimalCircle.getWidth() / 2;
		// The radius of the central circle
		double centralRadius = centralCircle.getWidth() / 2;
		
		// The radius of the maximal circle
		double maximalRadius = 0;
		if (maximalCircle != null) {
			maximalRadius = maximalCircle.getWidth() / 2;
		}
		

		// Construct the tiles starting from zero angle
		for (int id = 0; id < numberOfInnerTiles / laneNum; id++) {
			Arc2D minimalArc = new Arc2D.Double();
			Arc2D centralArc = new Arc2D.Double();
			Arc2D maximalArc = new Arc2D.Double();

			// Find starting angle from zero
			double startAngle = id * angle;

			// Find minimal and maximal arc
			minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, angle, 0);
			centralArc.setArcByCenter(origin.getX(), origin.getY(), centralRadius, startAngle + angle, -angle, 0);

			// Create tile shape
			GeneralPath tileShape = new GeneralPath();
			tileShape.moveTo(centralArc.getEndPoint().getX(), centralArc.getEndPoint().getY());
			tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
			tileShape.append(minimalArc, false);
			tileShape.lineTo(centralArc.getStartPoint().getX(), centralArc.getStartPoint().getY());
			tileShape.append(centralArc, true);

			idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id * 2));
			
			if (laneNum == 2) {
				maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle, angle, 0);
				GeneralPath tileShape2 = new GeneralPath();
				tileShape2.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
				tileShape2.lineTo(centralArc.getStartPoint().getX(), centralArc.getStartPoint().getY());
				tileShape2.append(centralArc, false);
				tileShape2.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
				tileShape2.append(maximalArc, true);

				idToTiles.add(new Tile(new Area(tileShape2), startAngle, angle, (id * 2) + 1));
			}
		}

		// Construct approach & merging entry tiles
		for (Road road : Debug.currentRimMap.getRoads()) {
			createEntryTiles(27.0, road);
			createExitTiles(27.0, road);
		}
		
//		Tile tile25 = idToTiles.get(25);
//		Tile tile26 = idToTiles.get(26);
//		Tile tile48 = idToTiles.get(48);
		
		
//		for (int j = idToTiles.size() - 1; j > -1; j--) {
//			idToTiles.remove(j);
//		}
//		Area newArea = tile25.getArea();
//		Area area47 = tile47.getArea();
//		Area newArea2 = newArea;
//		newArea.intersect(area47);
////		area47.intersect(newArea);
//		newArea = newArea2)
//		tile25.getArea().intersect(tile48.getArea());
//		Area newArea = tile25.getArea();
//		idToTiles.add(newArea, 25.0, 25.0, 100);
		
//		idToTiles.add(tile47);
		
//		idToTiles.add(new Tile(newArea, tile48.startAngle, tile48.angle, idToTiles.get(63).id + 1));
//		idToTiles.remove(31);
//		idToTiles.remove(25);
//		idToTiles.remove(23);
//		idToTiles.remove(17);
//		idToTiles.remove(15);
//		idToTiles.remove(9);
//		idToTiles.remove(7);
//		idToTiles.remove(1);
		
		
		
		
		
		
		
//		idToTiles.remove(47);
//		idToTiles.remove(47);
//		idToTiles.remove(36);
//		idToTiles.remove(4);
//		createEntryTiles(27.0, Debug.currentRimMap.getRoads().get(2));
//		idToTiles.remove(1);
//		createExitTiles(27.0, road);

	}

	private void createEntryTiles(double angle, Road road) {

		for (int i = 0; i < laneNum; i++) {
			// The approach entry lane to be divided
			ArcSegmentLane approachEntryLane = (ArcSegmentLane) road.getEntryApproachLane(i);

			// The origin of the minimal and maximal circle
			Point2D origin = new Point2D.Double(approachEntryLane.getArc().getCenterX(),
					approachEntryLane.getArc().getCenterY());
			// The radius of the minimal circle
			double minimalRadius = approachEntryLane.rightBorder().getWidth() / 2;
			// The radius of the maximal circle
			double maximalRadius = approachEntryLane.leftBorder().getWidth() / 2;

			int offset = numberOfTiles;

			// Create the entry tiles
			for (int id = offset; id < offset + 2; id++) {
				// Create first tile
				Arc2D minimalArc = new Arc2D.Double();
				Arc2D maximalArc = new Arc2D.Double();

				double startAngle = approachEntryLane.getArc().getAngleStart() - (id - offset) * angle;

				// Find minimal and maximal arc
				minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, -angle, 0);
				maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle - angle, +angle, 0);

				// Create tile shape
				GeneralPath tileShape = new GeneralPath();
				tileShape.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
				tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
				tileShape.append(minimalArc, false);
				tileShape.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
				tileShape.append(maximalArc, true);

				idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id));
				numberOfTiles++;
			}
		}
	}

	private void createExitTiles(double angle, Road road) {

		for (int i = 0; i < laneNum; i++) {
			// The approach exit lane to be divided
			ArcSegmentLane approachExitLane = (ArcSegmentLane) road.getExitApproachLane(i);

			// The origin of the minimal and maximal circle
			Point2D origin = new Point2D.Double(approachExitLane.getArc().getCenterX(),
					approachExitLane.getArc().getCenterY());
			// The radius of the minimal circle
			double minimalRadius = approachExitLane.rightBorder().getWidth() / 2;
			// The radius of the maximal circle
			double maximalRadius = approachExitLane.leftBorder().getWidth() / 2;

			int offset = numberOfTiles;

			// Create the exit tiles
			for (int id = offset; id < offset + 2; id++) {
				// Create first tile
				Arc2D minimalArc = new Arc2D.Double();
				Arc2D maximalArc = new Arc2D.Double();

				double startAngle = approachExitLane.getArc().getAngleStart()
						+ approachExitLane.getArc().getAngleExtent() + (id - offset) * angle;

				// Find minimal and maximal arc
				minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, angle, 0);
				maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle + angle, -angle, 0);

				// Create tile shape
				GeneralPath tileShape = new GeneralPath();
				tileShape.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
				tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
				tileShape.append(minimalArc, false);
				tileShape.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
				tileShape.append(maximalArc, true);

				idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id));
				numberOfTiles++;
			}
		}
	}

	//////////////////////////////////////////
	// PUBLIC METHODS (getters and setters)
	//////////////////////////////////////////

	/**
	 * Get the minimal circle.
	 *
	 * @return the minimal circle
	 */
	public Ellipse2D getMinimalCircle() {
		return minimalCircle;
	}

	/**
	 * Get the maximal circle.
	 *
	 * @return the maximal circle
	 */
	public Ellipse2D getMaximalCircle() {
		return maximalCircle;
	}

	/**
	 * Get a tile according to its id.
	 *
	 * @return the tile
	 */
	public ArrayList<Tile> getAllTilesById() {
		return idToTiles;
	}

	/**
	 * Get the granularity of this area.
	 *
	 * @return the granularity
	 */
	public double getGranularity() {
		return granularity;
	}

	/**
	 * Get the number of tiles.
	 *
	 * @return the number of tiles
	 */
	public int getNumberOfTiles() {
		return numberOfTiles;
	}

	/////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////

	/**
	 * Get a tile according to its id.
	 *
	 * @param id the id of the tile
	 * @return the tile
	 */
	public Tile getTileById(int id) {
		return idToTiles.get(id);
	}

	/**
	 * Get the list of tiles that are occupied by the given Shape.
	 *
	 * @param points the four corners of the vehicle for which to find occupied tiles
	 * @return the List of tiles that are occupied by the given Shape
	 */
	public List<Tile> findOccupiedTiles(Point2D[] points) {
		List<Tile> occupiedTiles = new ArrayList<Tile>();
		// Check four corners for position within tile for accurate intersection 
		idToTiles.forEach(tile -> {
			for (int i = 0; i < points.length; i++) {
				if (tile.getArea().contains(points[i])) {
					occupiedTiles.add(tile);
				}
			}
		});
		return occupiedTiles;
	}
}
