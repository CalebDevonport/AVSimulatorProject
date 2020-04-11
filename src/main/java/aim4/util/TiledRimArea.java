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
//		if (laneNum == 2) {
//			formatTiles();	
//		}
	}

	private void formatTiles() {
		int westEntryInnerNum = 1;
		int northExitInnerNum = (int) this.granularity - 1;
		int southEntryInnerNum = (int) this.granularity + 1;
		int westExitInnerNum = (int) (2 * this.granularity) - 1;
		int eastEntryInnerNum = (int) (2 * this.granularity) + 1;
		int southExitInnerNum = (int) (3 * this.granularity) - 1;
		int northEntryInnerNum = (int) (3 * this.granularity) + 1;
		int eastExitInnerNum = (int) (4 * this.granularity) - 1;
		int westEntryExtraInnerNum = 3;
		int northExitExtraInnerNum = (int) this.granularity - 3;
		int southEntryExtraInnerNum = (int) this.granularity + 3;
		int westExitExtraInnerNum = (int) (2 * this.granularity) - 3;
		int eastEntryExtraInnerNum = (int) (2 * this.granularity) + 3;
		int southExitExtraInnerNum = (int) (3 * this.granularity) - 3;
		int northEntryExtraInnerNum = (int) (3 * this.granularity) + 3;
		int eastExitExtraInnerNum = (int) (4 * this.granularity) - 3;
		
		
		Tile westEntryTile = idToTiles.get(westEntryInnerNum);
		Area westEntryArea = westEntryTile.getArea();
		int westEntryNum = this.numberOfInnerTiles + 8; 
		westEntryArea.subtract(idToTiles.get(westEntryNum).getArea());
		westEntryArea.subtract(idToTiles.get(westEntryNum + 1).getArea());
		westEntryArea.subtract(idToTiles.get(westEntryNum + 2).getArea());
		westEntryArea.subtract(idToTiles.get(westEntryNum + 3).getArea());
		idToTiles.set(westEntryInnerNum, new Tile(westEntryArea, westEntryTile.getStartAngle(), westEntryTile.startAngle, westEntryTile.id));
		
		Tile northExitTile = idToTiles.get(northExitInnerNum);
		Area northExitArea = northExitTile.getArea();
		int northExitNum = this.numberOfInnerTiles + 20; 
		northExitArea.subtract(idToTiles.get(northExitNum).getArea());
		northExitArea.subtract(idToTiles.get(northExitNum + 1).getArea());
		northExitArea.subtract(idToTiles.get(northExitNum + 2).getArea());
		northExitArea.subtract(idToTiles.get(northExitNum + 3).getArea());
		idToTiles.set(northExitInnerNum, new Tile(northExitArea, northExitTile.getStartAngle(), northExitTile.startAngle, northExitTile.id));
		
		Tile southEntryTile = idToTiles.get(southEntryInnerNum);
		Area southEntryArea = southEntryTile.getArea();
		int southEntryNum = this.numberOfInnerTiles + 24; 
		southEntryArea.subtract(idToTiles.get(southEntryNum).getArea());
		southEntryArea.subtract(idToTiles.get(southEntryNum + 1).getArea());
		southEntryArea.subtract(idToTiles.get(southEntryNum + 2).getArea());
		southEntryArea.subtract(idToTiles.get(southEntryNum + 3).getArea());
		idToTiles.set(southEntryInnerNum, new Tile(southEntryArea, southEntryTile.getStartAngle(), southEntryTile.startAngle, southEntryTile.id));
		
		Tile westExitTile = idToTiles.get(westExitInnerNum);
		Area westExitArea = westExitTile.getArea();
		int westExitNum = this.numberOfInnerTiles + 12; 
		westExitArea.subtract(idToTiles.get(westExitNum).getArea());
		westExitArea.subtract(idToTiles.get(westExitNum + 1).getArea());
		westExitArea.subtract(idToTiles.get(westExitNum + 2).getArea());
		westExitArea.subtract(idToTiles.get(westExitNum + 3).getArea());
		idToTiles.set(westExitInnerNum, new Tile(westExitArea, westExitTile.getStartAngle(), westExitTile.startAngle, westExitTile.id));
		
		Tile eastEntryTile = idToTiles.get(eastEntryInnerNum);
		Area eastEntryArea = eastEntryTile.getArea();
		int eastEntryNum = this.numberOfInnerTiles; 
		eastEntryArea.subtract(idToTiles.get(eastEntryNum).getArea());
		eastEntryArea.subtract(idToTiles.get(eastEntryNum + 1).getArea());
		eastEntryArea.subtract(idToTiles.get(eastEntryNum + 2).getArea());
		eastEntryArea.subtract(idToTiles.get(eastEntryNum + 3).getArea());
		idToTiles.set(eastEntryInnerNum, new Tile(eastEntryArea, eastEntryTile.getStartAngle(), eastEntryTile.startAngle, eastEntryTile.id));
		
		Tile southExitTile = idToTiles.get(southExitInnerNum);
		Area southExitArea = southExitTile.getArea();
		int southExitNum = this.numberOfInnerTiles + 28; 
		southExitArea.subtract(idToTiles.get(southExitNum).getArea());
		southExitArea.subtract(idToTiles.get(southExitNum + 1).getArea());
		southExitArea.subtract(idToTiles.get(southExitNum + 2).getArea());
		southExitArea.subtract(idToTiles.get(southExitNum + 3).getArea());
		idToTiles.set(southExitInnerNum, new Tile(southExitArea, southExitTile.getStartAngle(), southExitTile.startAngle, southExitTile.id));
		
		Tile northEntryTile = idToTiles.get(northEntryInnerNum);
		Area northEntryArea = northEntryTile.getArea();
		int northEntryNum = this.numberOfInnerTiles + 16; 
		northEntryArea.subtract(idToTiles.get(northEntryNum).getArea());
		northEntryArea.subtract(idToTiles.get(northEntryNum + 1).getArea());
		northEntryArea.subtract(idToTiles.get(northEntryNum + 2).getArea());
		northEntryArea.subtract(idToTiles.get(northEntryNum + 3).getArea());
		idToTiles.set(northEntryInnerNum, new Tile(northEntryArea, northEntryTile.getStartAngle(), northEntryTile.startAngle, northEntryTile.id));
		
		Tile eastExitTile = idToTiles.get(eastExitInnerNum);
		Area eastExitArea = eastExitTile.getArea();
		int eastExitNum = this.numberOfInnerTiles + 4; 
		eastExitArea.subtract(idToTiles.get(eastExitNum).getArea());
		eastExitArea.subtract(idToTiles.get(eastExitNum + 1).getArea());
		eastExitArea.subtract(idToTiles.get(eastExitNum + 2).getArea());
		eastExitArea.subtract(idToTiles.get(eastExitNum + 3).getArea());
		idToTiles.set(eastExitInnerNum, new Tile(eastExitArea, eastExitTile.getStartAngle(), eastExitTile.startAngle, eastExitTile.id));
		
		if (this.granularity > 6) {
			Tile westEntryExtraTile = idToTiles.get(westEntryExtraInnerNum);
			Area westEntryExtraArea = westEntryExtraTile.getArea();
			int westEntryExtraNum = this.numberOfInnerTiles + 8; 
			westEntryExtraArea.subtract(idToTiles.get(westEntryExtraNum).getArea());
			westEntryExtraArea.subtract(idToTiles.get(westEntryExtraNum + 1).getArea());
			westEntryExtraArea.subtract(idToTiles.get(westEntryExtraNum + 2).getArea());
			westEntryExtraArea.subtract(idToTiles.get(westEntryExtraNum + 3).getArea());
			idToTiles.set(westEntryExtraInnerNum, new Tile(westEntryExtraArea, westEntryExtraTile.getStartAngle(), westEntryExtraTile.startAngle, westEntryExtraTile.id));
			
			Tile northExitExtraTile = idToTiles.get(northExitExtraInnerNum);
			Area northExitExtraArea = northExitExtraTile.getArea();
			int northExitExtraNum = this.numberOfInnerTiles + 20; 
			northExitExtraArea.subtract(idToTiles.get(northExitExtraNum).getArea());
			northExitExtraArea.subtract(idToTiles.get(northExitExtraNum + 1).getArea());
			northExitExtraArea.subtract(idToTiles.get(northExitExtraNum + 2).getArea());
			northExitExtraArea.subtract(idToTiles.get(northExitExtraNum + 3).getArea());
			idToTiles.set(northExitExtraInnerNum, new Tile(northExitExtraArea, northExitExtraTile.getStartAngle(), northExitExtraTile.startAngle, northExitExtraTile.id));
			
			Tile southEntryExtraTile = idToTiles.get(southEntryExtraInnerNum);
			Area southEntryExtraArea = southEntryExtraTile.getArea();
			int southEntryExtraNum = this.numberOfInnerTiles + 24; 
			southEntryExtraArea.subtract(idToTiles.get(southEntryExtraNum).getArea());
			southEntryExtraArea.subtract(idToTiles.get(southEntryExtraNum + 1).getArea());
			southEntryExtraArea.subtract(idToTiles.get(southEntryExtraNum + 2).getArea());
			southEntryExtraArea.subtract(idToTiles.get(southEntryExtraNum + 3).getArea());
			idToTiles.set(southEntryExtraInnerNum, new Tile(southEntryExtraArea, southEntryExtraTile.getStartAngle(), southEntryExtraTile.startAngle, southEntryExtraTile.id));
			
			Tile westExitExtraTile = idToTiles.get(westExitExtraInnerNum);
			Area westExitExtraArea = westExitExtraTile.getArea();
			int westExitExtraNum = this.numberOfInnerTiles + 12; 
			westExitExtraArea.subtract(idToTiles.get(westExitExtraNum).getArea());
			westExitExtraArea.subtract(idToTiles.get(westExitExtraNum + 1).getArea());
			westExitExtraArea.subtract(idToTiles.get(westExitExtraNum + 2).getArea());
			westExitExtraArea.subtract(idToTiles.get(westExitExtraNum + 3).getArea());
			idToTiles.add(new Tile(westExitExtraArea, westExitExtraTile.getStartAngle(), westExitExtraTile.startAngle, westExitExtraTile.id));
			
			Tile eastEntryExtraTile = idToTiles.get(eastEntryExtraInnerNum);
			Area eastEntryExtraArea = eastEntryExtraTile.getArea();
			int eastEntryExtraNum = this.numberOfInnerTiles; 
			eastEntryExtraArea.subtract(idToTiles.get(eastEntryExtraNum).getArea());
			eastEntryExtraArea.subtract(idToTiles.get(eastEntryExtraNum + 1).getArea());
			eastEntryExtraArea.subtract(idToTiles.get(eastEntryExtraNum + 2).getArea());
			eastEntryExtraArea.subtract(idToTiles.get(eastEntryExtraNum + 3).getArea());
			idToTiles.set(eastEntryExtraInnerNum, new Tile(eastEntryExtraArea, eastEntryExtraTile.getStartAngle(), eastEntryExtraTile.startAngle, eastEntryExtraTile.id));
			
			Tile southExitExtraTile = idToTiles.get(southExitExtraInnerNum);
			Area southExitExtraArea = southExitExtraTile.getArea();
			int southExitExtraNum = this.numberOfInnerTiles + 28; 
			southExitExtraArea.subtract(idToTiles.get(southExitExtraNum).getArea());
			southExitExtraArea.subtract(idToTiles.get(southExitExtraNum + 1).getArea());
			southExitExtraArea.subtract(idToTiles.get(southExitExtraNum + 2).getArea());
			southExitExtraArea.subtract(idToTiles.get(southExitExtraNum + 3).getArea());
			idToTiles.set(southExitExtraInnerNum, new Tile(southExitExtraArea, southExitExtraTile.getStartAngle(), southExitExtraTile.startAngle, southExitExtraTile.id));
			
			Tile northEntryExtraTile = idToTiles.get(northEntryExtraInnerNum);
			Area northEntryExtraArea = northEntryExtraTile.getArea();
			int northEntryExtraNum = this.numberOfInnerTiles + 16; 
			northEntryExtraArea.subtract(idToTiles.get(northEntryExtraNum).getArea());
			northEntryExtraArea.subtract(idToTiles.get(northEntryExtraNum + 1).getArea());
			northEntryExtraArea.subtract(idToTiles.get(northEntryExtraNum + 2).getArea());
			northEntryExtraArea.subtract(idToTiles.get(northEntryExtraNum + 3).getArea());
			idToTiles.set(northEntryExtraInnerNum, new Tile(northEntryExtraArea, northEntryExtraTile.getStartAngle(), northEntryExtraTile.startAngle, northEntryExtraTile.id));
			
			Tile eastExitExtraTile = idToTiles.get(eastExitExtraInnerNum);
			Area eastExitExtraArea = eastExitExtraTile.getArea();
			int eastExitExtraNum = this.numberOfInnerTiles + 4; 
			eastExitExtraArea.subtract(idToTiles.get(eastExitExtraNum).getArea());
			eastExitExtraArea.subtract(idToTiles.get(eastExitExtraNum + 1).getArea());
			eastExitExtraArea.subtract(idToTiles.get(eastExitExtraNum + 2).getArea());
			eastExitExtraArea.subtract(idToTiles.get(eastExitExtraNum + 3).getArea());
			idToTiles.set(eastExitExtraInnerNum, new Tile(eastExitExtraArea, eastExitExtraTile.getStartAngle(), eastExitExtraTile.startAngle, eastExitExtraTile.id));
		}
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
