package dataviewer3final;

public abstract class KeyCommand {
	DataViewerUI ui;
	
	public KeyCommand(DataViewerUI ui) {
		this.ui = ui;
	}
	
	public abstract void execute();
}
