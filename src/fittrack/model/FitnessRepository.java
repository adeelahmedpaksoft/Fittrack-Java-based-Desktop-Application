package fittrack.model;

import fittrack.observer.FitnessObserver;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central data repository. Subject in the Observer pattern.
 */
public class FitnessRepository {

    private final List<WorkoutSession>   workouts  = new ArrayList<>();
    private final List<Habit>            habits    = new ArrayList<>();
    private final List<FitnessObserver>  observers = new ArrayList<>();
    private int nextWorkoutId = 1;
    private int nextHabitId   = 1;

    // ── Observer ────────────────────────────────────────────────────────────
    public void addObserver(FitnessObserver o)    { observers.add(o); }
    public void removeObserver(FitnessObserver o) { observers.remove(o); }
    private void notify(String event, Object data) {
        for (FitnessObserver o : observers) o.onDataChanged(event, data);
    }

    // ── Workout CRUD ─────────────────────────────────────────────────────────
    public WorkoutSession addWorkout(String username, WorkoutSession.WorkoutType type,
                                     LocalDate date, int durationMin, double distKm,
                                     int calories, int hr, double intensity, String notes) {
        WorkoutSession w = new WorkoutSession(nextWorkoutId++, username, type, date,
                durationMin, distKm, calories, hr, intensity, notes);
        workouts.add(w);
        notify("WORKOUT_ADDED", w);
        return w;
    }

    public boolean removeWorkout(int id) {
        WorkoutSession w = findWorkoutById(id);
        if (w == null) return false;
        workouts.remove(w);
        notify("WORKOUT_REMOVED", w);
        return true;
    }

    public boolean updateWorkout(WorkoutSession updated) {
        WorkoutSession ex = findWorkoutById(updated.getId());
        if (ex == null) return false;
        ex.setType(updated.getType());
        ex.setDate(updated.getDate());
        ex.setDurationMinutes(updated.getDurationMinutes());
        ex.setDistanceKm(updated.getDistanceKm());
        ex.setCaloriesBurned(updated.getCaloriesBurned());
        ex.setHeartRateAvg(updated.getHeartRateAvg());
        ex.setIntensityScore(updated.getIntensityScore());
        ex.setNotes(updated.getNotes());
        notify("WORKOUT_UPDATED", ex);
        return true;
    }

    public WorkoutSession findWorkoutById(int id) {
        return workouts.stream().filter(w -> w.getId() == id).findFirst().orElse(null);
    }

    public List<WorkoutSession> getAllWorkouts(String username) {
        return workouts.stream().filter(w -> w.getUsername().equals(username))
                .sorted(Comparator.comparing(WorkoutSession::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<WorkoutSession> getAllWorkoutsAdmin() {
        return new ArrayList<>(workouts);
    }

    // ── Habit CRUD ────────────────────────────────────────────────────────────
    public Habit addHabit(String username, String name, String description,
                           Habit.Frequency freq, Habit.Category cat, String icon,
                           int target, String unit) {
        Habit h = new Habit(nextHabitId++, username, name, description, freq, cat, icon, target, unit);
        habits.add(h);
        notify("HABIT_ADDED", h);
        return h;
    }

    public boolean removeHabit(int id) {
        Habit h = findHabitById(id);
        if (h == null) return false;
        habits.remove(h);
        notify("HABIT_REMOVED", h);
        return true;
    }

    public boolean updateHabit(Habit updated) {
        Habit ex = findHabitById(updated.getId());
        if (ex == null) return false;
        ex.setName(updated.getName());
        ex.setDescription(updated.getDescription());
        ex.setIcon(updated.getIcon());
        ex.setTargetCount(updated.getTargetCount());
        ex.setUnit(updated.getUnit());
        notify("HABIT_UPDATED", ex);
        return true;
    }

    public Habit findHabitById(int id) {
        return habits.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
    }

    public List<Habit> getAllHabits(String username) {
        return habits.stream().filter(h -> h.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public void toggleHabitToday(int id) {
        Habit h = findHabitById(id);
        if (h != null) { h.toggleToday(); notify("HABIT_TOGGLED", h); }
    }

    // ── Stats helpers ─────────────────────────────────────────────────────────
    public int totalCaloriesThisWeek(String username) {
        LocalDate weekAgo = LocalDate.now().minusDays(6);
        return getAllWorkouts(username).stream()
                .filter(w -> !w.getDate().isBefore(weekAgo))
                .mapToInt(WorkoutSession::getCaloriesBurned).sum();
    }

    public int totalWorkoutsThisMonth(String username) {
        LocalDate monthAgo = LocalDate.now().minusDays(29);
        return (int) getAllWorkouts(username).stream()
                .filter(w -> !w.getDate().isBefore(monthAgo)).count();
    }

    public double totalDistanceThisMonth(String username) {
        LocalDate monthAgo = LocalDate.now().minusDays(29);
        return getAllWorkouts(username).stream()
                .filter(w -> !w.getDate().isBefore(monthAgo))
                .mapToDouble(WorkoutSession::getDistanceKm).sum();
    }

    public int habitsCompletedToday(String username) {
        return (int) getAllHabits(username).stream().filter(Habit::isCompletedToday).count();
    }

    public void setWorkouts(List<WorkoutSession> list) {
        workouts.clear(); workouts.addAll(list);
        nextWorkoutId = workouts.stream().mapToInt(WorkoutSession::getId).max().orElse(0) + 1;
        notify("DATA_LOADED", null);
    }

    public void setHabits(List<Habit> list) {
        habits.clear(); habits.addAll(list);
        nextHabitId = habits.stream().mapToInt(Habit::getId).max().orElse(0) + 1;
    }

    public List<WorkoutSession> getAllWorkoutsRaw()  { return new ArrayList<>(workouts); }
    public List<Habit>          getAllHabitsRaw()    { return new ArrayList<>(habits); }
}
