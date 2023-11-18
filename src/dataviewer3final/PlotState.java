package dataviewer3final;

public class PlotState implements State  {
	public void display(DisplayMode DM) {
		DM.drawData();
	}
	public State transiton() {
		return new MenuState();
	}
	@Override
	public boolean isMenu() {
		// TODO Auto-generated method stub
		return false;
	}
}
