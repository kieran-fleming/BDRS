package au.com.gaiaresources.bdrs.model.grid.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.grid.Grid;
import au.com.gaiaresources.bdrs.model.grid.GridEntry;
import au.com.gaiaresources.bdrs.model.grid.GridService;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.kml.KMLWriter;

public class GridImageGenerator {
	private Logger logger = Logger.getLogger(getClass());

	private FileService fileService;
	private GridService gridService;
	//private GeometryTransformer geometryTransformer;

	private org.geotools.styling.StyleFactory styleFactory;
	private org.opengis.filter.FilterFactory filterFactory;

	protected GridImageGenerator() {
		fileService = AppContext.getBean(FileService.class);
		gridService = AppContext.getBean(GridService.class);
		//geometryTransformer = AppContext.getBean(GeometryTransformer.class);

		styleFactory = CommonFactoryFinder.getStyleFactory(GeoTools
				.getDefaultHints());
		filterFactory = CommonFactoryFinder.getFilterFactory(GeoTools
				.getDefaultHints());
	}

	void generateKML(Grid grid, IndicatorSpecies s) throws Exception {
		try {
			logger.info("Generating grid image for: " + grid);

			// regenerate grid for all species.
			File targetFile = fileService.createTargetFile(Grid.class, grid
					.getId(), "gridkml.kml");
			logger.info("Target file is: " + targetFile.getAbsolutePath());

			KMLWriter writer = new KMLWriter();
			writer.createFolder("Squares");

			List<? extends GridEntry> entries = gridService.getGridEntries(
					grid, null);
			int[] values = new int[entries.size()];
			for (int i = 0; i < entries.size(); i++) {
				values[i] = entries.get(i).getNumberOfRecords();
			}

			ColourClassifier classifier = new ColourClassifier(values, 10,
					new Color(255, 0, 0), 50, 230);
			for (Color c : classifier.colours) {
				writer.createStylePoly(convertColourToID(c), c);
			}

			for (GridEntry entry : entries) {
				writer.createPlacemark("Squares", "L" + entry.getId(), entry
						.getBoundary(), convertColourToID(classifier
						.getColour(entry.getNumberOfRecords())));
			}

			FileOutputStream fo = new FileOutputStream(targetFile);
			try {
				writer.write(false, fo);
			} finally {
				fo.close();
			}
			logger.info("Finished writing : " + targetFile.getAbsolutePath());

			// Now need to generate species specific one.
			targetFile = fileService.createTargetFile(Grid.class, grid.getId(),
					s.getCommonName() + "-gridkml.kml");
			logger.info("Target file is: " + targetFile.getAbsolutePath());

			writer = new KMLWriter();
			writer.createFolder("Squares");

			entries = gridService.getGridEntries(grid, s);
			values = new int[entries.size()];
			for (int i = 0; i < entries.size(); i++) {
				values[i] = entries.get(i).getNumberOfRecords();
			}

			classifier = new ColourClassifier(values, 10, new Color(255, 0, 0),
					50, 230);
			for (Color c : classifier.colours) {
				writer.createStylePoly(convertColourToID(c), c);
			}

			for (GridEntry entry : entries) {
				writer.createPlacemark("Squares", "L" + entry.getId(), entry
						.getBoundary(), convertColourToID(classifier
						.getColour(entry.getNumberOfRecords())));
			}

			fo = new FileOutputStream(targetFile);
			try {
				writer.write(false, fo);
			} finally {
				fo.close();
			}

		} catch (Exception e) {
			logger.error("Encountered exception whilst writing KML", e);
			//rethrow
			throw e;
		}
	}

	private String convertColourToID(Color c) {
		return Integer.toString(c.getAlpha()) + Integer.toString(c.getBlue())
				+ Integer.toString(c.getGreen()) + Integer.toString(c.getRed());
	}

	void generate(Grid grid, IndicatorSpecies s) throws Exception {
		/**
		logger.info("Generating grid image for: " + grid);

		// Generate image for all species.
		File targetFile = fileService.createTargetFile(Grid.class,
				grid.getId(), "grid-sm.png");
		logger.info("Target file is: " + targetFile.getAbsolutePath());

		// Create a BufferedImage to write the result to.
		BufferedImage targetImage = new BufferedImage(880, 680,
				BufferedImage.TYPE_INT_ARGB);

		// Create a feature type for the data
		SimpleFeatureType featureType = DataUtilities.createType("Feature",
				"*geom:Polygon,count:0");
		MemoryFeatureCollection coll = new MemoryFeatureCollection(featureType);

		for (GridEntry entry : gridService.getGridEntries(grid, null)) {
			List<Object> atts = new ArrayList<Object>();
			atts.add(entry.getBoundary()); // geometryTransformer.transform(entry.getBoundary(),
											// GeometryTransformer.GOOGLE_SRID));
			atts.add(entry.getNumberOfRecords());
			coll.add(new SimpleFeatureImpl(atts, featureType,
					new FeatureIdImpl(entry.getId().toString())));
		}

		Color[] colours = new Color[] { new TransparentColor(255, 0, 0, 50),
				new TransparentColor(255, 0, 0, 90),
				new TransparentColor(255, 0, 0, 130),
				new TransparentColor(255, 0, 0, 170),
				new TransparentColor(255, 0, 0, 210) };
		Style cs = buildClassifiedStyle(coll, "count", colours, featureType);

		// ImageBuilder image = new ImageBuilder(440, 340, targetFile);

		CoordinateReferenceSystem googleCRS = geometryTransformer
				.getCoordinateReferenceSystem(GeometryTransformer.GOOGLE_SRID);
		CoordinateReferenceSystem wgs84CRS = geometryTransformer
				.getCoordinateReferenceSystem(GeometryTransformer.WGS84_SRID);
		MapContext mapContext = new DefaultMapContext(wgs84CRS);
		mapContext.addLayer(coll, cs);

		StreamingRenderer renderer = new StreamingRenderer();
		renderer.setContext(mapContext);

		// 111, -44, 155, -10
		Polygon p = new GeometryBuilder().createRectangle(111, -44, 44, 34);
		Geometry g = p; // geometryTransformer.transform(p,
						// GeometryTransformer.GOOGLE_SRID);
		logger.info(p);
		logger.info(JTS.toEnvelope(g));

		ReferencedEnvelope re = new ReferencedEnvelope(GeometryUtils
				.findMinX(g), GeometryUtils.findMaxX(g), GeometryUtils
				.findMinY(g), GeometryUtils.findMaxY(g), wgs84CRS);
		renderer.paint(targetImage.createGraphics(), new Rectangle(880, 680),
				re, null);

		OutputStream fo = createOutputStream(targetFile);
		try {
			ImageIO.write(targetImage, "png", fo);
		} finally {
			fo.close();
		}

		// TODO now need to generate species specific one.
		targetFile = fileService.createTargetFile(Grid.class, grid.getId(), s
				.getCommonName()
				+ "-grid-sm.png");
		logger.info("Target file is: " + targetFile.getAbsolutePath());

		// Create a BufferedImage to write the result to.
		targetImage = new BufferedImage(880, 680, BufferedImage.TYPE_INT_ARGB);

		// Create a feature type for the data
		featureType = DataUtilities.createType("Feature",
				"*geom:Polygon,count:0");
		coll = new MemoryFeatureCollection(featureType);

		for (GridEntry entry : gridService.getGridEntries(grid, s)) {
			List<Object> atts = new ArrayList<Object>();
			atts.add(entry.getBoundary()); // geometryTransformer.transform(entry.getBoundary(),
											// GeometryTransformer.GOOGLE_SRID));
			atts.add(entry.getNumberOfRecords());
			coll.add(new SimpleFeatureImpl(atts, featureType,
					new FeatureIdImpl(entry.getId().toString())));
		}

		colours = new Color[] { new TransparentColor(255, 0, 0, 50),
				new TransparentColor(255, 0, 0, 90),
				new TransparentColor(255, 0, 0, 130),
				new TransparentColor(255, 0, 0, 170),
				new TransparentColor(255, 0, 0, 210) };
		cs = buildClassifiedStyle(coll, "count", colours, featureType);

		// ImageBuilder image = new ImageBuilder(440, 340, targetFile);

		googleCRS = geometryTransformer
				.getCoordinateReferenceSystem(GeometryTransformer.GOOGLE_SRID);
		wgs84CRS = geometryTransformer
				.getCoordinateReferenceSystem(GeometryTransformer.WGS84_SRID);
		mapContext = new DefaultMapContext(wgs84CRS);
		mapContext.addLayer(coll, cs);

		renderer = new StreamingRenderer();
		renderer.setContext(mapContext);

		// 111, -44, 155, -10
		p = new GeometryBuilder().createRectangle(111, -44, 44, 34);
		g = p; // geometryTransformer.transform(p,
				// GeometryTransformer.GOOGLE_SRID);
		logger.info(p);
		logger.info(JTS.toEnvelope(g));

		re = new ReferencedEnvelope(GeometryUtils.findMinX(g), GeometryUtils
				.findMaxX(g), GeometryUtils.findMinY(g), GeometryUtils
				.findMaxY(g), wgs84CRS);
		renderer.paint(targetImage.createGraphics(), new Rectangle(880, 680),
				re , null);

		fo = createOutputStream(targetFile);
		try {
			ImageIO.write(targetImage, "png", fo);
		} finally {
			fo.close();
		}
*/
		/**
		 * int transMinX = (int)
		 * Math.floor(GeometryUtils.findMaxX(transformedBottomLeft)); int
		 * transMaxX = (int)
		 * Math.ceil(GeometryUtils.findMaxX(transformedTopRight)); int transMinY
		 * = (int) Math.floor(GeometryUtils.findMaxY(transformedBottomLeft));
		 * int transMaxY = (int)
		 * Math.ceil(GeometryUtils.findMaxY(transformedTopRight));
		 * 
		 * int targetWidth = 500; double factor = (double) targetWidth /
		 * (transMaxX - transMinX); ImageBuilder image2 = new ImageBuilder(500,
		 * (int) Math.floor(factor * (transMaxY - transMinY)), f2);
		 * logger.info("Generating image: " + image);
		 * logger.info("Generating image: " + image2);
		 * 
		 * List<? extends GridEntry> entries = gridDAO.getGridEntries(grid);
		 * Collections.sort(entries, new Comparator<GridEntry>() { public int
		 * compare(GridEntry ge1, GridEntry ge2) { return new
		 * Integer(ge1.getNumberOfRecords
		 * ()).compareTo(ge2.getNumberOfRecords()); } }); int maxRecordsInSquare
		 * = 0; int minRecordsInSquare = 0; if (entries.size() > 0) {
		 * maxRecordsInSquare = entries.get(entries.size() -
		 * 1).getNumberOfRecords(); minRecordsInSquare =
		 * entries.get(0).getNumberOfRecords(); }
		 * 
		 * if (minRecordsInSquare == maxRecordsInSquare) { minRecordsInSquare =
		 * 0; }
		 * 
		 * int imageCellWidth = grid.getPrecision().multiply(new
		 * BigDecimal(10)).intValue(); logger.info("Max records in square: " +
		 * maxRecordsInSquare + ", min: " + minRecordsInSquare +
		 * ", img cell width: " + imageCellWidth);
		 * 
		 * for (GridEntry entry : entries) {
		 * 
		 * double alpha = (double) entry.getNumberOfRecords() /
		 * maxRecordsInSquare; Color c = new Color(255, 0, 0, (int)
		 * Math.floor(alpha * 255)); Polygon p = entry.getBoundary(); // Top
		 * left in coordinate system double minGeomX =
		 * GeometryUtils.findMinX(p); double maxGeomY =
		 * GeometryUtils.findMaxY(p); // Top left in image coordinates double
		 * minImageX = minGeomX - this.minX; double minImageY = this.maxY -
		 * maxGeomY; image.drawRectangle((int) minImageX * 10, (int) minImageY *
		 * 10, imageCellWidth, imageCellWidth, c);
		 * 
		 * 
		 * //////////
		 * 
		 * Geometry transGrid =
		 * geometryTransformer.transform(entry.getBoundary(), 900913); // Top
		 * left in coordinate system double tMinGeomX =
		 * GeometryUtils.findMinX(transGrid); double tMaxGeomX =
		 * GeometryUtils.findMaxX(transGrid); double tMinGeomY =
		 * GeometryUtils.findMinY(transGrid); double tMaxGeomY =
		 * GeometryUtils.findMaxY(transGrid); // Top left in image coordinates
		 * double tMinImageX = Math.floor(tMinGeomX - transMinX); double
		 * tMaxImageX = Math.floor(tMaxGeomX - transMinX); double tMinImageY =
		 * Math.floor(transMaxY - tMaxGeomY); double tMaxImageY =
		 * Math.floor(transMaxY - tMinGeomY); // Width in img coords double
		 * tWidth = factor * (tMaxImageX - tMinImageX); double tHeight = factor
		 * * (tMaxImageY - tMinImageY);
		 * 
		 * image2.drawRectangle((int) Math.floor(factor * tMinImageX), (int)
		 * Math.floor(factor * tMinImageY), (int) Math.floor(tWidth), (int)
		 * Math.floor(tHeight), c); }
		 * 
		 * image.draw(); image2.draw();
		 */
	}

	protected OutputStream createOutputStream(File targetFile)
			throws IOException {
		return new FileOutputStream(targetFile);
	}

	@SuppressWarnings("deprecation")
	public Style buildClassifiedStyle(
			FeatureCollection<SimpleFeatureType, SimpleFeature> fc,
			String name, Color[] colors, SimpleFeatureType schema)
			throws IllegalFilterException {
		// grab attribute col
		PropertyName value = filterFactory.property(name);
		String geomName = schema.getGeometryDescriptor().getLocalName();

		double[] values = new double[fc.size()];
		Iterator<SimpleFeature> it = fc.iterator();
		int count = 0;

		while (it.hasNext()) {
			SimpleFeature f = (SimpleFeature) it.next();
			values[count++] = ((Number) f.getAttribute(name)).doubleValue();
		}

		// pass to classification algorithm
		EqualClasses ec = new EqualClasses(colors.length, values);

		// build style
		double[] breaks = ec.getBreaks();
		Style ret = styleFactory.createStyle();

		// ret.setName(name);
		Rule[] rules = new Rule[colors.length + 1];

		PropertyIsLessThan cf1 = filterFactory.less(value, filterFactory
				.literal(breaks[0]));

		logger.info(cf1.toString());
		rules[0] = styleFactory.createRule();
		rules[0].setFilter(cf1);

		// rules[0].setName("lowest");
		Color c = colors[0];
		PolygonSymbolizer symb1 = createPolygonSymbolizer(c);

		// @todo: this should set the geometry name but currently this breaks
		// the legend
		// symb1.setGeometryPropertyName(geomName);
		rules[0].setSymbolizers(new Symbolizer[] { symb1 });
		logger.info("added low class " + breaks[0] + " " + colors[0]);

		// LOGGER.fine(rules[0].toString());
		for (int i = 1; i < (colors.length - 1); i++) {
			rules[i] = styleFactory.createRule();

			Expression expr = value;
			Expression lower = filterFactory.literal(breaks[i - 1]);
			Expression upper = filterFactory.literal(breaks[i]);
			PropertyIsBetween cf = filterFactory.between(expr, lower, upper);

			logger.info(cf.toString());
			c = colors[i];

			PolygonSymbolizer symb = createPolygonSymbolizer(c);

			// symb.setGeometryPropertyName(geomName);
			rules[i].setSymbolizers(new Symbolizer[] { symb });
			rules[i].setFilter(cf);

			// rules[i].setName("class "+i);
			logger.info("added class " + breaks[i - 1] + "->" + breaks[i] + " "
					+ colors[i]);
		}

		PropertyIsGreaterThan cf2 = filterFactory.greater(value, filterFactory
				.literal(breaks[colors.length - 2]));

		logger.info(cf2.toString());
		rules[colors.length - 1] = styleFactory.createRule();
		rules[colors.length - 1].setFilter(cf2);
		rules[colors.length - 1].setName(geomName);
		c = colors[colors.length - 1];

		PolygonSymbolizer symb2 = createPolygonSymbolizer(c);

		// symb2.setGeometryPropertyName(geomName);
		rules[colors.length - 1].setSymbolizers(new Symbolizer[] { symb2 });
		logger.info("added upper class " + breaks[colors.length - 2] + "  "
				+ colors[colors.length - 1]);
		rules[colors.length] = styleFactory.createRule();

		PolygonSymbolizer elsePoly = createPolygonSymbolizer(Color.black, 1.0);
		rules[colors.length].setSymbolizers(new Symbolizer[] { elsePoly });
		rules[colors.length].setIsElseFilter(true);

		FeatureTypeStyle ft = styleFactory.createFeatureTypeStyle(rules);
		ft.setFeatureTypeName("Feature");
		ft.setName(name);
		ret.addFeatureTypeStyle(ft);

		return ret;
	}

	@SuppressWarnings("deprecation")
	public PolygonSymbolizer createPolygonSymbolizer() {
		PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();
		ps.setFill(createFill());
		ps.setStroke(createStroke());

		return ps;
	}

	public Fill createFill() {
		Fill f = styleFactory.createFill(literalExpression("#808080"),
				literalExpression(1.0));
		// Fill f = styleFactory.getDefaultFill();
		// f.setColor(literalExpression("#808080"));
		// f.setBackgroundColor(literalExpression("#808080"));
		// f.setOpacity(literalExpression(1.0));

		return f;
	}

	public Expression literalExpression(String value) {
		Expression result = null;

		if (value != null) {
			result = filterFactory.literal(value);
		}

		return result;
	}

	public Expression literalExpression(double value) {
		return filterFactory.literal(value);
	}

	public Stroke createStroke() {
		return styleFactory.getDefaultStroke();
	}

	public PolygonSymbolizer createPolygonSymbolizer(Color borderColor,
			double borderWidth) {
		return createPolygonSymbolizer(createStroke(borderColor, borderWidth),
				null);
	}

	public PolygonSymbolizer createPolygonSymbolizer(Color fillColor,
			Color borderColor, double borderWidth) {
		return createPolygonSymbolizer(createStroke(borderColor, borderWidth),
				createFill(fillColor));
	}

	/**
	 * create a polygon symbolizer
	 * 
	 * @param stroke
	 *            - the stroke to use to outline the polygon
	 * @param fill
	 *            - the fill to use to color the polygon
	 * 
	 * @return the new polygon symbolizer
	 */
	public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill) {
		return createPolygonSymbolizer(stroke, fill, null);
	}

	public PolygonSymbolizer createPolygonSymbolizer(Color fillColor) {
		return createPolygonSymbolizer(null, createFill(fillColor));
	}

	/**
	 * create a polygon symbolizer
	 * 
	 * @param stroke
	 *            - the stroke to use to outline the polygon
	 * @param fill
	 *            - the fill to use to color the polygon
	 * @param geometryPropertyName
	 *            - the name of the geometry to be drawn
	 * 
	 * @return the new polygon symbolizer
	 */
	public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill,
			String geometryPropertyName) {
		return styleFactory.createPolygonSymbolizer(stroke, fill,
				geometryPropertyName);
	}

	public Stroke createStroke(Color color, double width) {
		double opacity = color.getAlpha() / 255;
		return styleFactory.createStroke(colorExpression(color),
				literalExpression(width), literalExpression(opacity));
	}

	public Expression colorExpression(Color color) {
		if (color == null) {
			return null;
		}

		String redCode = Integer.toHexString(color.getRed());
		String greenCode = Integer.toHexString(color.getGreen());
		String blueCode = Integer.toHexString(color.getBlue());

		if (redCode.length() == 1) {
			redCode = "0" + redCode;
		}

		if (greenCode.length() == 1) {
			greenCode = "0" + greenCode;
		}

		if (blueCode.length() == 1) {
			blueCode = "0" + blueCode;
		}

		String colorCode = "#" + redCode + greenCode + blueCode;

		return filterFactory.literal(colorCode.toUpperCase());
	}

	public Fill createFill(Color fillColor) {
		// return createFill(colorExpression(fillColor));
		double opacity = ((double) fillColor.getAlpha()) / 255D;
		return styleFactory.createFill(colorExpression(fillColor),
				literalExpression(opacity));
	}

	/**
	 * create a fill of color
	 * 
	 * @param fillColor
	 *            an Expression representing the color of the fill
	 * 
	 * @return the fill constructed
	 */
	public Fill createFill(Expression fillColor) {
		return styleFactory.createFill(fillColor);
	}

	public class EqualClasses {
		int numberClasses;
		double[] breaks;
		double[] collection;

		/**
		 * Creates a new instance of EqualClasses
		 * 
		 * @param numberClasses
		 * @param fc
		 */
		public EqualClasses(int numberClasses, double[] fc) {
			if (numberClasses >= fc.length) {
				int sections = numberClasses * 2;
				double[] newValues = new double[sections];
				double min = fc[0] - 1;
				double max = fc[fc.length - 1] + 1;
				for (int i = 0; i < sections; i++) {
					newValues[i] = (i + 1) * (max - min) / sections;
				}
				setCollection(newValues);
			} else {
				setCollection(fc);
			}
			setNumberClasses(numberClasses);
		}

		/**
		 * Getter for property numberClasses.
		 * 
		 * @return Value of property numberClasses.
		 * 
		 */
		public int getNumberClasses() {
			return numberClasses;
		}

		/**
		 * Setter for property numberClasses.
		 * 
		 * @param numberClasses
		 *            New value of property numberClasses.
		 * 
		 */
		public void setNumberClasses(int numberClasses) {
			this.numberClasses = numberClasses;
			if (breaks == null) {
				if (numberClasses > collection.length) {
					breaks = new double[collection.length];
				} else {
					breaks = new double[numberClasses - 1];
				}
			}

			Arrays.sort(collection);

			int step = collection.length / numberClasses;
			int firstCollectionIndex = step;
			if (step == 0) {
				// Case where the number of items in the collection is less than
				// the number of classes (colours).
				step = 1;
			}
			for (int i = firstCollectionIndex, j = 0; j < breaks.length; j++, i += step) {
				breaks[j] = collection[i];
			}
		}

		/**
		 * returns the the break points between the classes <b>Note</b> You get
		 * one less breaks than number of classes.
		 * 
		 * @return Value of property breaks.
		 * 
		 */
		public double[] getBreaks() {
			return this.breaks;
		}

		/**
		 * Setter for property collection.
		 * 
		 * @param collection
		 *            New value of property collection.
		 * 
		 */
		public void setCollection(double[] collection) {
			this.collection = collection;
		}

	}

//	private class TransparentColor extends Color {
//		private TransparentColor(int r, int g, int b, int a) {
//			super(r, g, b, a);
//		}
//
//		public String toString() {
//			return "Color [red: " + getRed() + ", green: " + getGreen()
//					+ ", blue: " + getBlue() + ", alpha: " + getAlpha() + "]";
//		}
//	}

	private class ColourClassifier {
		private Color baseColour;
		private int startOpacity;
		private int endOpacity;

		private Color[] colours;
		private int[] values;
		private double step;

		private ColourClassifier(int[] values, int levels, Color baseColour,
				int startOpacity, int endOpacity) {
			this.colours = new Color[levels];
			this.values = values;
			Arrays.sort(this.values);

			this.baseColour = baseColour;
			this.startOpacity = startOpacity;
			this.endOpacity = endOpacity;

			this.step = calculateStep();

			initialiseColours();
		}

		private double calculateStep() {
			int minValue = values[0];
			int maxValue = values[values.length - 1];

			return ((double) (maxValue - minValue)) / (double) colours.length;
		}

		private void initialiseColours() {
			int opacityStep = (endOpacity - startOpacity) / colours.length;
			for (int o = startOpacity, i = 0; o < endOpacity; o += opacityStep, i++) {
				colours[i] = new Color(baseColour.getRed(), baseColour
						.getGreen(), baseColour.getBlue(), startOpacity
						+ (i * opacityStep));
			}
		}

		private Color getColour(int value) {
			int colourIndex = (int) Math.round((value - values[0]) / step);
			return colours[Math.max(0, colourIndex - 1)];
		}
	}
}
