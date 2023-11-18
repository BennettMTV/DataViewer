package dataviewer3final;

public class CommandUpdatePlot extends KeyCommand{

	public CommandUpdatePlot(DataViewerUI ui) {
		super(ui);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		ui.updatePlotData(ui.getSelectedCountry(), ui.getSelectedState(), ui.getStartYear(), ui.getEndYear());
	}

}
