package fittrack.observer;

/** Observer Pattern – contract for all fitness data observers. */
public interface FitnessObserver {
    void onDataChanged(String event, Object data);
}
