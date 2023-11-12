package Att2;

public class Record  implements Comparable<Record> {
	private String m_dataCountries;
	private String m_dataStates;
	private int m_dataYear;
	private String month;
	private double temperature;
	
	
	
	public Record(String country, String states, int year, double temperature, String month) {
		this.m_dataCountries = country;
		this.m_dataStates = states;
		this.m_dataYear = year;
		this.month = month;
		this.temperature = temperature;
		
	}
	
	public String getCuntries() {
		return m_dataCountries;
	}
	
	public String getStates() {
		return m_dataStates;
	}
	
	public int getYear() {
		return m_dataYear;
	}
	public String getMonth() {
		return month;
	}
	
	 public double getTemperature() {
	        return temperature;
	}
	 
	 @Override
	    public String toString() {
	        return "Record{" +
	               "Country='" + m_dataCountries + '\'' +
	               ", State='" + m_dataStates + '\'' +
	               ", Year=" + m_dataYear +
	               ", Month='" + month + '\'' +
	               ", Temperature=" + temperature +
	               '}';
	    }

	@Override
	public int compareTo(Record o) {
		// TODO Auto-generated method stub
		int compareCuntries = this.m_dataCountries.compareTo(o.m_dataCountries);
		if (compareCuntries !=0) {
			return compareCuntries;
		}
		
		int compareState = this.m_dataStates.compareTo(o.m_dataStates);
        if (compareState != 0) {
            return compareState;
        }
        
        int compareYear = Integer.compare(this.m_dataYear, o.m_dataYear);
        if (compareYear != 0) {
            return compareYear;
        }
        
        int compareMonth = this.month.compareTo(o.month);
        if (compareMonth != 0) {
            return compareMonth;
        }
        return Double.compare(this.temperature, o.temperature);
	}
}
