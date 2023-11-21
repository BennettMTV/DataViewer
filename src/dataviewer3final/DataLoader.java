package dataviewer3final;

import java.io.IOException;
import java.util.SortedMap;
import java.util.List;
import java.util.ArrayList;

public abstract class DataLoader {
	protected List<DataObserver> observers;

	public DataLoader() {
		this.observers = new ArrayList<DataObserver>();
	}

	public abstract void loadData() throws IOException;
	public abstract SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>> fetchData();

	public void attach(DataObserver observer) {
		observers.add(observer);
	}

	public void detatch(DataObserver observer) {
		observers.remove(observer);
	}

	public void notifyObservers() {
		for (DataObserver observer : observers) {
			observer.updateObserver();
		}
	}
}
