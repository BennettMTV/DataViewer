package dataviewer3final;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class DataLoaderCSV extends DataLoader {
    private String m_dataFile;

    /**
	 * Utility function to pull a year integer out of a date string.  Supports M/D/Y and Y-M-D formats only.
	 * 
	 * @param dateString
	 * @return
	 */

    public DataLoaderCSV(String dataFile) throws FileNotFoundException {
        this.m_dataFile = dataFile;
    }
	
	private static Integer parseYear(String dateString) {
		Integer ret = null;
		if(dateString.indexOf("/") != -1) {
			// Assuming something like 1/20/1823
			String[] parts = dateString.split("/");
			if(parts.length == 3) {
				ret = Integer.parseInt(parts[2]);
			}
		}
		else if(dateString.indexOf("-") != -1) {
			// Assuming something like 1823-01-20
			String[] parts = dateString.split("-");
			if(parts.length == 3) {
				ret = Integer.parseInt(parts[0]);
			}
		}
		else {
			throw new RuntimeException(String.format("Unexpected date delimiter: '%s'", dateString));
		}
		if(ret == null) {
			Logger.trace("Unable to parse year from date: '%s'", dateString);
		}
		return ret;
	}

	private static List<Object> getRecordFromLine(String line) {
		List<String> rawValues = new ArrayList<String>();
		try (Scanner rowScanner = new Scanner(line)) {
			rowScanner.useDelimiter(",");
			while (rowScanner.hasNext()) {
				rawValues.add(rowScanner.next());
			}
		}
		m_dataCountries.add(rawValues.get(FILE_COUNTRY_IDX));
		if(rawValues.size() != FILE_NUM_COLUMNS) {
			Logger.trace("malformed line '%s'...skipping", line);
			return null;
		}
		else if(!rawValues.get(FILE_COUNTRY_IDX).equals(m_selectedCountry)) {
			Logger.trace("skipping non-USA record: %s", rawValues);
			return null;
		}
		else {
			Logger.trace("processing raw data: %s", rawValues.toString());
		}
		try {
			// Parse these into more useful objects than String
			List<Object> values = new ArrayList<Object>(4);

			Integer year = parseYear(rawValues.get(FILE_DATE_IDX));
			if(year == null) {
				return null;
			}
			values.add(year);

			Integer month = parseMonth(rawValues.get(FILE_DATE_IDX));
			if(month == null) {
				return null;
			}
			values.add(month);
			values.add(Double.parseDouble(rawValues.get(FILE_TEMPERATURE_IDX)));
			//not going to use UNCERTAINTY yet
			//values.add(Double.parseDouble(rawValues.get(FILE_UNCERTAINTY_IDX)));
			values.add(rawValues.get(FILE_STATE_IDX));
			// since all are the same country
			//values.add(rawValues.get(FILE_COUNTRY_IDX));

			// if we got here, add the state to the list of states
			m_dataStates.add(rawValues.get(FILE_STATE_IDX));
			m_dataYears.add(year);
			return values;
		}
		catch(NumberFormatException e) {
			Logger.trace("unable to parse data line, skipping...'%s'", line);
			return null;
		}
	}

	private static Integer parseMonth(String dateString) {
		Integer ret = null;
		if(dateString.indexOf("/") != -1) {
			// Assuming something like 1/20/1823
			String[] parts = dateString.split("/");
			if(parts.length == 3) {
				ret = Integer.parseInt(parts[0]);
			}
		}
		else if(dateString.indexOf("-") != -1) {
			// Assuming something like 1823-01-20
			String[] parts = dateString.split("-");
			if(parts.length == 3) {
				ret = Integer.parseInt(parts[1]);
			}
		}
		else {
			throw new RuntimeException(String.format("Unexpected date delimiter: '%s'", dateString));
		}
		if(ret == null || ret.intValue() < 1 || ret.intValue() > 12) {
			Logger.trace("Unable to parse month from date: '%s'", dateString);
			return null;
		}
		return ret;
	}

	public void loadData() throws FileNotFoundException {
		// reset the data storage in case this is a re-load
		m_dataRaw = new ArrayList<List<Object>>();
		m_dataStates = new TreeSet<String>();
		m_dataCountries = new TreeSet<String>();
		m_dataYears = new TreeSet<Integer>();
		m_plotData = null;

		try (Scanner scanner = new Scanner(new File(m_dataFile))) {
			while (scanner.hasNextLine()) {
				List<Object> record = getRecordFromLine(scanner.nextLine());
				if(record != null) {
					m_dataRaw.add(record);
				}
			}
			// update selections (not including country) for the newly loaded data
			m_selectedState = m_dataStates.first();
			m_selectedStartYear = m_dataYears.first();
			m_selectedEndYear = m_dataYears.last();

			Logger.info("loaded %d data records", m_dataRaw.size());
			Logger.info("loaded data for %d states", m_dataStates.size());
			Logger.info("loaded data for %d years [%d, %d]", m_dataYears.size(), m_selectedStartYear, m_selectedEndYear);
		}
	}

	public void updatePlotData() {
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
		for(List<Object> rec : m_dataRaw) {
			String state = (String)rec.get(RECORD_STATE_IDX);
			Integer year = (Integer)rec.get(RECORD_YEAR_IDX);

			// Check to see if they are the state and year range we care about
			if (state.equals(m_selectedState) && 
					((year.compareTo(m_selectedStartYear) >= 0 && year.compareTo(m_selectedEndYear) <= 0))) {

				// Ok, we need to add this to the list of values for the month
				Integer month = (Integer)rec.get(RECORD_MONTH_IDX);
				Double value = (Double)rec.get(RECORD_TEMPERATURE_IDX);

				if(!m_plotMonthlyMinValue.containsKey(month) || value.compareTo(m_plotMonthlyMinValue.get(month)) < 0) {
					m_plotMonthlyMinValue.put(month, value);
				}
				if(!m_plotMonthlyMaxValue.containsKey(month) || value.compareTo(m_plotMonthlyMaxValue.get(month)) > 0) {
					m_plotMonthlyMaxValue.put(month, value);
				}

				m_plotData.get(month).put(year, value);
			}
		}
		//debug("plot data: %s", m_plotData.toString());
	}
}
