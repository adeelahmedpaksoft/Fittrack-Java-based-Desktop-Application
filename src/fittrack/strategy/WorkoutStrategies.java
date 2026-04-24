package fittrack.strategy;

import fittrack.model.WorkoutSession;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Strategy Pattern – contract for workout filtering/sorting strategies. */

// ── Concrete Strategies ──────────────────────────────────────────────────────

class AllWorkoutsStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) { return w; }
    public String getName() { return "All Workouts"; }
    public String getIcon() { return "⊞"; }
}

class ThisWeekStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        LocalDate weekAgo = LocalDate.now().minusDays(6);
        return w.stream().filter(x -> !x.getDate().isBefore(weekAgo)).collect(Collectors.toList());
    }
    public String getName() { return "This Week"; }
    public String getIcon() { return "📅"; }
}

class ThisMonthStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        LocalDate monthAgo = LocalDate.now().minusDays(29);
        return w.stream().filter(x -> !x.getDate().isBefore(monthAgo)).collect(Collectors.toList());
    }
    public String getName() { return "This Month"; }
    public String getIcon() { return "📆"; }
}

class HighIntensityStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        return w.stream().filter(x -> x.getIntensityScore() >= 7.0).collect(Collectors.toList());
    }
    public String getName() { return "High Intensity"; }
    public String getIcon() { return "🔥"; }
}

class CardioOnlyStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        return w.stream().filter(x ->
                x.getType() == WorkoutSession.WorkoutType.RUNNING ||
                x.getType() == WorkoutSession.WorkoutType.CYCLING ||
                x.getType() == WorkoutSession.WorkoutType.SWIMMING ||
                x.getType() == WorkoutSession.WorkoutType.WALKING)
                .collect(Collectors.toList());
    }
    public String getName() { return "Cardio"; }
    public String getIcon() { return "🏃"; }
}

class StrengthOnlyStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        return w.stream().filter(x ->
                x.getType() == WorkoutSession.WorkoutType.WEIGHTLIFTING ||
                x.getType() == WorkoutSession.WorkoutType.HIIT ||
                x.getType() == WorkoutSession.WorkoutType.BOXING)
                .collect(Collectors.toList());
    }
    public String getName() { return "Strength"; }
    public String getIcon() { return "💪"; }
}

class SortByCaloriesStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        return w.stream().sorted((a, b) -> b.getCaloriesBurned() - a.getCaloriesBurned())
                .collect(Collectors.toList());
    }
    public String getName() { return "Most Calories"; }
    public String getIcon() { return "🔆"; }
}

class SortByDurationStrategy implements WorkoutStrategy {
    public List<WorkoutSession> apply(List<WorkoutSession> w) {
        return w.stream().sorted((a, b) -> b.getDurationMinutes() - a.getDurationMinutes())
                .collect(Collectors.toList());
    }
    public String getName() { return "Longest First"; }
    public String getIcon() { return "⏱"; }
}

/** Factory exposing all strategies. */
public class WorkoutStrategies {
    private WorkoutStrategies() {}
    public static WorkoutStrategy[] all() {
        return new WorkoutStrategy[]{
            new AllWorkoutsStrategy(),
            new ThisWeekStrategy(),
            new ThisMonthStrategy(),
            new CardioOnlyStrategy(),
            new StrengthOnlyStrategy(),
            new HighIntensityStrategy(),
            new SortByCaloriesStrategy(),
            new SortByDurationStrategy()
        };
    }
}
