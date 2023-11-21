package dataviewer3final;

public class CommandUpdatePlot extends KeyCommand{

	public CommandUpdatePlot(DataViewerUI ui) {
		super(ui);
	}

	@Override
	public void execute() {
		ui.updatePlotData(ui.getSelectedCountry(), ui.getSelectedState(), ui.getStartYear(), ui.getEndYear());
	}

}
