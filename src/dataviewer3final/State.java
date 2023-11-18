package dataviewer3final;

public interface State {
	State transiton();
	void display(DisplayMode DM);
	boolean isMenu();

}