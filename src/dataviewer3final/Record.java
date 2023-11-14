package dataviewer3final;

public class Record {
    public String country;
    public String state;
    public int year;
    public int month;
    public double temperature;
    public double uncertainty;

    public Record(String country, String state, int year, int month, double temp, double uncertainty) {
        this.country = country;
        this.state = state;
        this.year = year;
        this.month = month;
        this.temperature = temp;
        this.uncertainty = uncertainty;
    }
}
