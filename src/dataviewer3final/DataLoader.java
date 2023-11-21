package dataviewer3final;

import java.io.IOException;
import java.util.SortedMap;

public interface DataLoader {    
	public SortedMap<String, SortedMap<String, SortedMap<Integer, SortedMap<Integer, Record>>>> loadData() throws IOException;
}
