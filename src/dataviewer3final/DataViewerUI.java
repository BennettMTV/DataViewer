package dataviewer3final;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import edu.du.dudraw.Draw;
import edu.du.dudraw.DrawListener;

public class DataViewerUI implements DrawListener, DisplayMode {
	private DataLoader loader;
	private Draw m_window;
	private State theState =  new MenuState();
	
	
	// new user created variables
	SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>> data;
	ArrayList<KeyCommand> commandList;


	// GUI Settings
	protected final static double 	DATA_WINDOW_BORDER = 50.0;
	protected final static String 	DEFAULT_COUNTRY = "United States";
	protected final static boolean	DO_DEBUG = true;
	protected final static boolean	DO_TRACE = false;
	protected final static double 	EXTREMA_PCT = 0.1;
	protected final static int 		GUI_MODE_MAIN_MENU = 0;
	protected final static int 		GUI_MODE_DATA = 1;
	protected final static double		MENU_STARTING_X = 40.0;
	protected final static double 	MENU_STARTING_Y = 90.0;
	protected final static double 	MENU_ITEM_SPACING = 5.0;
	protected final static String[] 	MONTH_NAMES = { "", // 1-based
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	protected final static double		TEMPERATURE_MAX_C = 30.0;
	protected final static double		TEMPERATURE_MIN_C = -10.0;
	protected final static double		TEMPERATURE_RANGE = TEMPERATURE_MAX_C - TEMPERATURE_MIN_C;
	protected final static String[] 	VISUALIZATION_MODES = { "Raw", "Extrema (within 10% of min/max)" };
	protected final static int 		VISUALIZATION_RAW_IDX = 0;
	protected final static int		VISUALIZATION_EXTREMA_IDX = 1;

	// user selections
	protected static String selectedCountry = DEFAULT_COUNTRY;
	protected static String selectedState;
	protected static Integer selectedStartYear;
	protected static Integer selectedEndYear;
	protected static String selectedVisualization = VISUALIZATION_MODES[0];
	private static int guiMode = GUI_MODE_MAIN_MENU; // Menu by default
	protected static int 		WINDOW_HEIGHT = 720;
	protected static String 	WINDOW_TITLE = "DataViewer Application";
	protected static int 		WINDOW_WIDTH = 1320; // should be a multiple of 12

	// plot-related data
	protected static TreeMap<Integer, SortedMap<Integer,Double>> m_plotData = null;
	protected static TreeMap<Integer,Double> m_plotMonthlyMaxValue = null;
	protected static TreeMap<Integer,Double> m_plotMonthlyMinValue = null;
	/**
	 * Constructor sets up the window and loads the specified data file.
	 */
	public DataViewerUI(String dataFile) throws IOException {
		commandList = new ArrayList<KeyCommand>();
		loader = new DataLoaderCSV(dataFile);

		// Setup the DuDraw board
		m_window = new Draw(WINDOW_TITLE);
		m_window.setCanvasSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		m_window.enableDoubleBuffering(); // Too slow otherwise -- need to use .show() later

		// Add the mouse/key listeners
		m_window.addListener(this);

		// Load data
		data = loader.loadData();

		// Set initial selections to first in data
		selectedState = data.get(selectedCountry).firstKey();
		selectedStartYear = data.get(selectedCountry).get(selectedState).firstKey();
		selectedEndYear = data.get(selectedCountry).get(selectedState).lastKey();

		KeyCommand updatePlotData = new CommandUpdatePlot(this);
		commandList.add(updatePlotData);
		
		// draw the screen for the first time -- this will be the main menu
		KeyCommand updateWindow = new CommandUpdateWindow(this);
		commandList.add(updateWindow);
		
		for (KeyCommand command : commandList) {
			command.execute();
		}
		commandList.clear();
	}
	
	// new getter methods for command pattern
	public State getState() {
		return theState;
	}
	public Draw getWindow() {
		return m_window;
	}
	public String getSelectedCountry() {
		return selectedCountry;
	}
	public String getSelectedState() {
		return selectedState;
	}
	public Integer getStartYear() {
		return selectedStartYear;
	}
	public Integer getEndYear() {
		return selectedEndYear;
	}
	
	public void drawMainMenu() {
		m_window.clear(Color.WHITE);

		String[] menuItems = {
				"Type the menu number to select that option:",
				"",
				String.format("C     Set country: [%s]", selectedCountry),
				String.format("T     Set state: [%s]", selectedState),
				String.format("S     Set start year [%d]", selectedStartYear),
				String.format("E     Set end year [%d]", selectedEndYear),
				String.format("V     Set visualization [%s]", selectedVisualization),
				String.format("P     Plot data"),
				String.format("Q     Quit"),
		};

		// enable drawing by "percentage" with the menu drawing
		m_window.setXscale(0, 100);
		m_window.setYscale(0, 100);

		// draw the menu
		m_window.setPenColor(Color.BLACK);

		drawMenuItems(menuItems);
	}

	private void drawMenuItems(String[] menuItems) {
		double yCoord = MENU_STARTING_Y;

		for(int i=0; i<menuItems.length; i++) {
			m_window.textLeft(MENU_STARTING_X, yCoord, menuItems[i]);
			yCoord -= MENU_ITEM_SPACING;
		}
	}

	public void drawData() {
		// Give a buffer around the plot window
		m_window.setXscale(-DATA_WINDOW_BORDER, WINDOW_WIDTH+DATA_WINDOW_BORDER);
		m_window.setYscale(-DATA_WINDOW_BORDER, WINDOW_HEIGHT+DATA_WINDOW_BORDER);

		// gray background
		m_window.clear(Color.LIGHT_GRAY);

		// white plot area
		m_window.setPenColor(Color.WHITE);
		m_window.filledRectangle(WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0, WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0);  

		m_window.setPenColor(Color.BLACK);

		double nCols = 12; // one for each month
		double nRows = selectedEndYear - selectedStartYear + 1; // for the years

		Logger.debug("nCols = %f, nRows = %f", nCols, nRows);

		double cellWidth = WINDOW_WIDTH / nCols;
		double cellHeight = WINDOW_HEIGHT / nRows;

		Logger.debug("cellWidth = %f, cellHeight = %f", cellWidth, cellHeight);

		boolean extremaVisualization = selectedVisualization.equals(VISUALIZATION_MODES[VISUALIZATION_EXTREMA_IDX]);
		Logger.info("visualization: %s (extrema == %b)", selectedVisualization, extremaVisualization);

		for(int month = 1; month <= 12; month++) {
			double fullRange = m_plotMonthlyMaxValue.get(month) - m_plotMonthlyMinValue.get(month);
			double extremaMinBound = m_plotMonthlyMinValue.get(month) + EXTREMA_PCT * fullRange;
			double extremaMaxBound = m_plotMonthlyMaxValue.get(month) - EXTREMA_PCT * fullRange;

			// draw the line separating the months and the month label
			m_window.setPenColor(Color.BLACK);
			double lineX = (month-1.0)*cellWidth;
			m_window.line(lineX, 0.0, lineX, WINDOW_HEIGHT);
			m_window.text(lineX+cellWidth/2.0, -DATA_WINDOW_BORDER/2.0, MONTH_NAMES[month]);

			// there should always be a map for the month
			SortedMap<Integer,Double> monthData = m_plotData.get(month);

			for(int year = selectedStartYear; year <= selectedEndYear; year++) {

				// month data structure might not have every year
				if(monthData.containsKey(year)) {
					Double value = monthData.get(year);

					double x = (month-1.0)*cellWidth + 0.5 * cellWidth;
					double y = (year-selectedStartYear)*cellHeight + 0.5 * cellHeight;

					Color cellColor = null;

					// get either color or grayscale depending on visualization mode
					if(extremaVisualization && value > extremaMinBound && value < extremaMaxBound) {
						cellColor = getDataColor(value, true);
					}
					else if(extremaVisualization) {
						// doing extrema visualization, show "high" values in red "low" values in blue.
						if(value >= extremaMaxBound) {
							cellColor = Color.RED;
						}
						else {
							cellColor = Color.BLUE;
						}
					}
					else {
						cellColor = getDataColor(value, false);
					}

					// draw the rectangle for this data point
					m_window.setPenColor(cellColor);
					Logger.trace("month = %d, year = %d -> (%f, %f) with %s", month, year, x, y, cellColor.toString());
					m_window.filledRectangle(x, y, cellWidth/2.0, cellHeight/2.0);
				}
			}
		}

		// draw the labels for the y-axis
		m_window.setPenColor(Color.BLACK);

		double labelYearSpacing = (selectedEndYear - selectedStartYear) / 5.0;
		double labelYSpacing = WINDOW_HEIGHT/5.0;
		// spaced out by 5, but need both the first and last label, so iterate 6
		for(int i=0; i<6; i++) {
			int year = (int)Math.round(i * labelYearSpacing + selectedStartYear);
			String text = String.format("%4d", year);

			m_window.textRight(0.0, i*labelYSpacing, text);
			m_window.textLeft(WINDOW_WIDTH, i*labelYSpacing, text);
		}

		// draw rectangle around the whole data plot window
		m_window.rectangle(WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0, WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0);

		// put in the title
		String title = String.format("%s, %s from %d to %d. Press 'M' for Main Menu.  Press 'Q' to Quit.",
				selectedState, selectedCountry, selectedStartYear, selectedEndYear);
		m_window.text(WINDOW_WIDTH/2.0, WINDOW_HEIGHT+DATA_WINDOW_BORDER/2.0, title);
	}

	/**
	 * Return a Color object based on the value passed in.
	 * @param value - controls the color
	 * @param doGrayscale - if true, return a grayscale value (r, g, b are all equal);
	 * 	otherwise return a range of red to green.
	 * @return null is value is null, otherwise return a Color object
	 */
	private Color getDataColor(Double value, boolean doGrayscale) {
		if(null == value) {
			return null;
		}
		double pct = (value - TEMPERATURE_MIN_C) / TEMPERATURE_RANGE;
		Logger.trace("converted %f raw value to %f %%", value, pct);

		if (pct > 1.0) {
			pct = 1.0;
		}
		else if (pct < 0.0) {
			pct = 0.0;
		}
		int r, g, b;
		// Replace the color scheme with my own
		if (!doGrayscale) {
			r = (int)(255.0 * pct);
			g = 0;
			b = (int)(255.0 * (1.0-pct));

		} else {
			// Grayscale for the middle extema
			r = g = b = (int)(255.0 * pct);
		}

		Logger.trace("converting %f to [%d, %d, %d]", value, r, g, b);

		return new Color(r, g, b);
	}

	public void updatePlotData(String country, String state, int startYear, int endYear) {
		//debug("raw data: %s", m_rawData.toString());
		// plot data is a map where the key is the Month, and the value is a sorted map where the key
		// is the year. 
		m_plotData = new TreeMap<Integer,SortedMap<Integer,Double>>();
		for(int month = 1; month <= 12; month++) {
			// any year/months not filled in will be null
			m_plotData.put(month, new TreeMap<Integer,Double>());
		}
		// now run through the raw data and if it is related to the current state and within the current
		// years, put it in a sorted data structure, so that we 
		// find min/max year based on data 
		m_plotMonthlyMaxValue = new TreeMap<Integer,Double>();
		m_plotMonthlyMinValue = new TreeMap<Integer,Double>();

		SortedMap<Integer, SortedMap<Integer, Record>> selectedData = data.get(country).get(state);

		for (Integer year : selectedData.keySet()) {
			if (year.compareTo(startYear) >= 0 && year.compareTo(endYear) <= 0) {
				SortedMap<Integer, Record> yearData = selectedData.get(year);

				for (Integer month : yearData.keySet()) {
					Double value = yearData.get(month).temperature;

					if(!m_plotMonthlyMinValue.containsKey(month) || value.compareTo(m_plotMonthlyMinValue.get(month)) < 0) {
						m_plotMonthlyMinValue.put(month, value);
					}
					if(!m_plotMonthlyMaxValue.containsKey(month) || value.compareTo(m_plotMonthlyMaxValue.get(month)) > 0) {
						m_plotMonthlyMaxValue.put(month, value);
					}

					m_plotData.get(month).put(year, value);
				}
			}
		}
		//debug("plot data: %s", m_plotData.toString());
	}


	// Below are the mouse/key listeners
	/**
	 * Handle key press.  Q always quits.  Otherwise process based on GUI mode.
	 */
	@Override public void keyPressed(int key) {
		boolean needsUpdate = false;
		boolean needsUpdatePlotData = false;
		Logger.trace("key pressed '%c'", (char)key);
		// regardless of draw mode, 'Q' or 'q' means quit:
		if(key == 'Q') {
			Logger.info("Exiting...");
			System.exit(0);
		}
		else if(theState.isMenu()) {
			if(key == 'P') {
				// plot the data
				theState =  theState.transiton();
				if(m_plotData == null) {
					// first time going to render data need to generate the plot data
					needsUpdatePlotData = true;
				}
				needsUpdate = true;
			}
			else if(key == 'C') {
				// set the Country
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Choose a Country", "Input",
						JOptionPane.INFORMATION_MESSAGE, null,
						data.keySet().toArray(), selectedCountry);

				if(selectedValue != null) {
					Logger.info("User selected: '%s'", selectedValue);
					if(!selectedValue.equals(selectedCountry)) {
						// change in data, update selections
						selectedCountry = (String)selectedValue;

						SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>> stateMap = data.get(selectedCountry);
						selectedState = stateMap.firstKey();

						// clamp start/end year if current selection is out of range
						if (selectedStartYear < stateMap.get(selectedState).firstKey()) {
							selectedStartYear = stateMap.get(selectedState).firstKey();
						}
						if (selectedStartYear > stateMap.get(selectedState).lastKey()) {
							selectedEndYear = stateMap.get(selectedState).lastKey();
						}
						needsUpdate = true;
						needsUpdatePlotData = true;
					}
				}
			}

			else if(key == 'T') {
				// set the state
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Choose a State", "Input",
						JOptionPane.INFORMATION_MESSAGE, null,
						data.get(selectedCountry).keySet().toArray(), selectedState);

				if(selectedValue != null) {
					Logger.info("User selected: '%s'", selectedValue);
					if(!selectedValue.equals(selectedState)) {
						// change in data
						selectedState = (String)selectedValue;

						SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>> stateMap = data.get(selectedCountry);

						// clamp start/end year if current selection is out of range
						if (selectedStartYear < stateMap.get(selectedState).firstKey()) {
							selectedStartYear = stateMap.get(selectedState).firstKey();
						}
						if (selectedStartYear > stateMap.get(selectedState).lastKey()) {
							selectedEndYear = stateMap.get(selectedState).lastKey();
						}

						needsUpdate = true;
						needsUpdatePlotData = true;
					}
				}
			}
			else if(key == 'S') {
				// set the start year
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Choose the start year", "Input",
						JOptionPane.INFORMATION_MESSAGE, null,
						data.get(selectedCountry).get(selectedState).keySet().toArray(), selectedStartYear);

				if(selectedValue != null) {
					Logger.info("User seleted: '%s'", selectedValue);
					Integer year = (Integer)selectedValue;
					if(year.compareTo(selectedEndYear) > 0) {
						Logger.error("new start year (%d) must not be after end year (%d)", year, selectedEndYear);
					}
					else {
						if(!selectedStartYear.equals(year)) {
							selectedStartYear = year;
							needsUpdate = true;
							needsUpdatePlotData = true;
						}
					}
				}
			}
			else if(key == 'E') {
				// set the end year
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Choose the end year", "Input",
						JOptionPane.INFORMATION_MESSAGE, null,
						data.get(selectedCountry).get(selectedState).keySet().toArray(), selectedEndYear);

				if(selectedValue != null) {
					Logger.info("User seleted: '%s'", selectedValue);
					Integer year = (Integer)selectedValue;
					if(year.compareTo(selectedStartYear) < 0) {
						Logger.error("new end year (%d) must be not be before start year (%d)", year, selectedStartYear);
					}
					else {
						if(!selectedEndYear.equals(year)) {
							selectedEndYear = year;
							needsUpdate = true;
							needsUpdatePlotData = true;
						}
					}
				}
			}
			else if(key == 'V') {
				// set the visualization
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Choose the visualization mode", "Input",
						JOptionPane.INFORMATION_MESSAGE, null,
						VISUALIZATION_MODES, selectedVisualization);

				if(selectedValue != null) {
					Logger.info("User seleted: '%s'", selectedValue);
					String visualization = (String)selectedValue;
					if(!selectedVisualization.equals(visualization)) {
						selectedVisualization = visualization;
						needsUpdate = true;
					}
				}
			}

		} else if(!theState.isMenu()) { 
			if(key == 'M') {

				theState = theState.transiton();
				needsUpdate = true;
			}

		}
		else {
			throw new IllegalStateException(String.format("unexpected mode: %d", guiMode));
		}
		if(needsUpdatePlotData) {
			// something changed with the data that needs to be plotted
			KeyCommand updatePlotData = new CommandUpdatePlot(this);
			commandList.add(updatePlotData);
			//updatePlotData(selectedCountry, selectedState, selectedStartYear, selectedEndYear);
		}
		if(needsUpdate) {
			KeyCommand updateWindow = new CommandUpdateWindow(this);
			commandList.add(updateWindow);
			//update();
		}
		
		for (KeyCommand command : commandList) {
			command.execute();
		}
		commandList.clear();
	}

	@Override
	public void keyReleased(int key) {}

	@Override
	public void keyTyped(char key) {}

	@Override
	public void mouseClicked(double x, double y) {}
	@Override
	public void mouseDragged(double x, double y) {}

	@Override
	public void mousePressed(double x, double y) {}

	@Override
	public void mouseReleased(double x, double y) {}


	@Override
	public void update() {
		// does nothing
	}    
}
