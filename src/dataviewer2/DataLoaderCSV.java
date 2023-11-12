package dataviewer2;

public class DataLoaderCSV extends DataLoader {
  private final static int 		FILE_COUNTRY_IDX = 4;
	private final static int 		FILE_NUM_COLUMNS = 5;
	private final static int 		FILE_DATE_IDX = 0;
	private final static int 		FILE_TEMPERATURE_IDX = 1;
	private final static int 		FILE_STATE_IDX = 3;
	private final static int		FILE_UNCERTAINTY_IDX =2;
	private final static String[] 	MONTH_NAMES = { "", // 1-based
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private String m_dataFile = "";
//	private List<List<Object>> m_dataRaw;
//	private TreeSet<String> m_dataStates;
//	private TreeSet<String> m_dataCountries;
//	private TreeSet<Integer> m_dataYears;
//	private String m_selectedCountry;
	private TreeSet<Record> records;
	
	public dataLoad(String dataFile) {
        this.m_dataFile = dataFile;
        records = new TreeSet<>();
	}

	public void loadData() throws FileNotFoundException {
		// reset the data storage in case this is a re-load
	    
		try (Scanner scanner = new Scanner(new File(m_dataFile))) {
			 if (scanner.hasNextLine()) { // Skip the header line
		            scanner.nextLine();
			 }
    	    while (scanner.hasNextLine()) {
    	    	
    	    	Record  recordsdata = getRecordFromLine(scanner.nextLine());
    	    	if(recordsdata != null) {
//    	    		Double temperature = (Double) recordsdata.get(2);
//    	    		String state = (String) recordsdata.get(3);  
//    	    		String country = (String) recordsdata.get(4);
//    	    		Integer year = (Integer) recordsdata.get(0);
//    	    		String month = (String) recordsdata.get(1);
//
//                    
//                    Record record = new Record(country, state, year, temperature, month);
                    System.out.println( recordsdata.toString());
                    records.add(recordsdata);
    	    	}
    	    }
		}
	}
		
		private Record getRecordFromLine(String line) {
	        List<String> rawValues = new ArrayList<String>();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(",");
	            while (rowScanner.hasNext()) {
	                rawValues.add(rowScanner.next());
	                
	                
	                
	            }
	        }

	     
	        if(rawValues.size() != FILE_NUM_COLUMNS) {
	        	//trace("malformed line '%s'...skipping", line);
	        	return null;
	        }
	        try {
	        	// Parse these into more useful objects than String
	        	List<Object> values = new ArrayList<Object>(4);
	        	
	        	Integer year = parseYear(rawValues.get(FILE_DATE_IDX));
	        	if(year == null) {
	        		return null;
	        	}

	        	
	        	String month = parseMonth(rawValues.get(FILE_DATE_IDX));
	        
	        	if(month == null) {
	        		return null;
	        	}
	        	
	        	Double temperature = Double.parseDouble(rawValues.get(FILE_TEMPERATURE_IDX));
	        	String state = rawValues.get(FILE_STATE_IDX);
	            String country = rawValues.get(FILE_COUNTRY_IDX);
	        	
	        
//	        	  values.add(rawValues.get(2));
//	              values.add(Double.parseDouble(rawValues.get(3)));
//	              values.add(rawValues.get(4));
//	              values.add(rawValues.get(0));
//	              values.add(rawValues.get(1));
	        	 return new Record(country, state, year, temperature, month);
	        }
	        catch(NumberFormatException e) {
	        //	trace("unable to parse data line, skipping...'%s'", line);
	        	return null;
	        }
	    }
		
		private Integer parseYear(String dateString) {
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
	    		//trace("Unable to parse year from date: '%s'", dateString);
	    	}
	    	return ret;
	    }
		
		 private String parseMonth(String dateString) {
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
		    		//trace("Unable to parse month from date: '%s'", dateString);
		    		return null;
		    	}
		    
		    	return MONTH_NAMES[ret];
			}
		 public void printRecords() {
			    for (Record record : records) {
			    	record.toString();
			    }
			}

}

}
