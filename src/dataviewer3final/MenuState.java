package dataviewer3final;

public class MenuState implements State  {
	public void display(DisplayMode DM) {
		DM.drawMainMenu();
	}
	public State transiton() {
		return new PlotState();
	}
	@Override
	public boolean isMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}







