package dataviewer3final;

public abstract class DataObserver {
    protected DataLoader loader;

    public DataObserver(DataLoader source) {
        this.loader = source;
        source.attach(this);
    }

    public abstract void updateObserver();
}