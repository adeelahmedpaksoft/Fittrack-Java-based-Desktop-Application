package fittrack.strategy;
import fittrack.model.WorkoutSession;
import java.util.List;
/** Strategy Pattern – contract for workout filtering/sorting. */
public interface WorkoutStrategy {
    List<WorkoutSession> apply(List<WorkoutSession> workouts);
    String getName();
    String getIcon();
}
