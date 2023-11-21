package dataviewer3final;

import java.io.IOException;
 
public class Main {
    public static void main(String[] args) throws IOException {
    	String dataFile = "data/GlobalLandTemperaturesByState.csv";

        DataLoader loader = new DataLoaderCSV(dataFile);
        loader.loadData();

        // Create UI from CSV data
        new DataViewerUI(loader);
    }
}
