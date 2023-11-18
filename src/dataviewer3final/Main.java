package dataviewer3final;

import java.io.IOException;
 
public class Main {
    public static void main(String[] args) throws IOException {
    	String data = "data/GlobalLandTemperaturesByState.csv";
    	//String data = "data/sample.csv";
        new DataViewerUI(data);
    }
}