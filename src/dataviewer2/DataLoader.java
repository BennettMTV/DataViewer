package dataviewer2;

import java.io.IOException;

public abstract class DataLoader extends DataViewer {    
	public abstract void loadData() throws IOException;
    public abstract void updatePlotData();
}
