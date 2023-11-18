package dataviewer3final;

public class PlotState implements State  {
<<<<<<< HEAD
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
=======
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
		


>>>>>>> ee7612547ff475c1b4e597bb9cedaa5692687823
