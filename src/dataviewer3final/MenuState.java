package dataviewer3final;

<<<<<<< HEAD
public class MenuState implements State  {
	public void display(DisplayMode DM) {
		DM.drawMainMenu();
	}
	public State transiton() {
		return new PlotState();
	}
=======


public class MenuState implements State  {
	  public void display(DisplayMode DM) {
		  DM.drawMainMenu();
	}
	   public State transiton() {
		  return new PlotState();
	  }
>>>>>>> ee7612547ff475c1b4e597bb9cedaa5692687823
	@Override
	public boolean isMenu() {
		// TODO Auto-generated method stub
		return true;
	}
}
<<<<<<< HEAD







=======
	
	       
		 		
			
	       
	 

	 
>>>>>>> ee7612547ff475c1b4e597bb9cedaa5692687823
