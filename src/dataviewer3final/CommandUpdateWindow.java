package dataviewer3final;

public class CommandUpdateWindow extends KeyCommand{

	public CommandUpdateWindow(DataViewerUI ui) {
		super(ui);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		ui.getState().display(ui);
		ui.getWindow().show();	
	}
}
