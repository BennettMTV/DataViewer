package dataviewer3final;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class DataLoaderCSV implements DataLoader {
    private final static int 		FILE_COUNTRY_IDX = 4;
	private final static int 		FILE_DATE_IDX = 0;
	private final static int 		FILE_NUM_COLUMNS = 5;
	private final static int 		FILE_STATE_IDX = 3;
	private final static int 		FILE_TEMPERATURE_IDX = 1;
	private final static int 		FILE_UNCERTAINTY_IDX = 2;

	private String filePath;

	public DataLoaderCSV(String file) {
		this.filePath = file;
	}

    /**
	 * Utility function to pull a year integer out of a date string.  Supports M/D/Y and Y-M-D formats only.
	 * 
	 * @param dateString
	 * @return
	 */
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

	private static Record getRecordFromLine(String line) {
		List<String> rawValues = new ArrayList<String>();
		try (Scanner rowScanner = new Scanner(line)) {
			rowScanner.useDelimiter(",");
			while (rowScanner.hasNext()) {
				rawValues.add(rowScanner.next());
			}
		}

		if(rawValues.size() != FILE_NUM_COLUMNS) {
			Logger.trace("malformed line '%s'...skipping", line);
			return null;
		}
		
		Logger.trace("processing raw data: %s", rawValues.toString());

		try {
			Integer year = parseYear(rawValues.get(FILE_DATE_IDX));
			if(year == null) {
				Logger.trace("unable to parse data line, skipping...'%s'", line);
				return null;
			}

			Integer month = parseMonth(rawValues.get(FILE_DATE_IDX));
			if(month == null) {
				Logger.trace("unable to parse data line, skipping...'%s'", line);
				return null;
			}
			
			Double temperature = Double.parseDouble(rawValues.get(FILE_TEMPERATURE_IDX));

			//not going to use UNCERTAINTY yet
			//Double uncertainty = Double.parseDouble(rawValues.get(FILE_UNCERTAINTY_IDX));

			String state = rawValues.get(FILE_STATE_IDX);

			String country = rawValues.get(FILE_COUNTRY_IDX);

			return new Record(country, state, year, month, temperature, 0.0);
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

	public SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>> loadData() throws FileNotFoundException {
		SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>> countryMap =
			new TreeMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>>();

		try (Scanner scanner = new Scanner(new File(filePath))) {
			while (scanner.hasNextLine()) {
				Record record = getRecordFromLine(scanner.nextLine());
				if(record != null) {
					if (!countryMap.containsKey(record.country)) {
						SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>> stateMap = new TreeMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>();
						countryMap.put(record.country, stateMap);
						Logger.debug("Added country %s", record.country);
					}

					SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>> stateMap = countryMap.get(record.country);

					if (!stateMap.containsKey(record.state)) {
						SortedMap<Integer, SortedMap<Integer, Record>> yearMap = new TreeMap<Integer, SortedMap<Integer, Record>>();
						stateMap.put(record.state, yearMap);
					}

					SortedMap<Integer, SortedMap<Integer, Record>> yearMap = stateMap.get(record.state);

					if (!yearMap.containsKey(record.year)) {
						SortedMap<Integer, Record> monthMap = new TreeMap<Integer, Record>();
						yearMap.put(record.year, monthMap);
					}

					SortedMap<Integer, Record> monthMap = yearMap.get(record.year);
					monthMap.put(record.month, record);
				}
			}
		}

		Logger.info("loaded data for %d countries", countryMap.size());
		return countryMap;
	}
}
