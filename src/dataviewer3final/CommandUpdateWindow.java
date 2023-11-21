package dataviewer3final;

public class CommandUpdateWindow extends KeyCommand{

	public CommandUpdateWindow(DataViewerUI ui) {
		super(ui);
	}

	@Override
	public void execute() {
		ui.getState().display(ui);
		ui.getWindow().show();	
	}
}
