package fittrack.command;

import fittrack.model.*;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;

/** Command Pattern – executable action contract. */

// ── LogWorkoutCommand ────────────────────────────────────────────────────────
class LogWorkoutCommand implements Command {
    private final FitnessRepository repo;
    private final String username;
    private final WorkoutSession.WorkoutType type;
    private final LocalDate date;
    private final int dur, cal, hr;
    private final double dist, intensity;
    private final String notes;
    private WorkoutSession logged;

    LogWorkoutCommand(FitnessRepository repo, String username,
                      WorkoutSession.WorkoutType type, LocalDate date,
                      int dur, double dist, int cal, int hr, double intensity, String notes) {
        this.repo = repo; this.username = username; this.type = type;
        this.date = date; this.dur = dur; this.dist = dist;
        this.cal = cal; this.hr = hr; this.intensity = intensity; this.notes = notes;
    }

    public void execute() {
        logged = repo.addWorkout(username, type, date, dur, dist, cal, hr, intensity, notes);
    }
    public void undo() { if (logged != null) repo.removeWorkout(logged.getId()); }
    public String getDescription() { return "Log " + type + " workout"; }
}

// ── RemoveWorkoutCommand ─────────────────────────────────────────────────────
class RemoveWorkoutCommand implements Command {
    private final FitnessRepository repo;
    private final int id;
    private WorkoutSession snapshot;

    RemoveWorkoutCommand(FitnessRepository repo, int id) { this.repo = repo; this.id = id; }

    public void execute() {
        WorkoutSession w = repo.findWorkoutById(id);
        if (w != null) {
            snapshot = new WorkoutSession(w.getId(), w.getUsername(), w.getType(), w.getDate(),
                    w.getDurationMinutes(), w.getDistanceKm(), w.getCaloriesBurned(),
                    w.getHeartRateAvg(), w.getIntensityScore(), w.getNotes());
            repo.removeWorkout(id);
        }
    }
    public void undo() {
        if (snapshot != null)
            repo.addWorkout(snapshot.getUsername(), snapshot.getType(), snapshot.getDate(),
                    snapshot.getDurationMinutes(), snapshot.getDistanceKm(),
                    snapshot.getCaloriesBurned(), snapshot.getHeartRateAvg(),
                    snapshot.getIntensityScore(), snapshot.getNotes());
    }
    public String getDescription() { return "Remove workout #" + id; }
}

// ── AddHabitCommand ──────────────────────────────────────────────────────────
class AddHabitCommand implements Command {
    private final FitnessRepository repo;
    private final String username, name, desc, icon, unit;
    private final Habit.Frequency freq;
    private final Habit.Category  cat;
    private final int target;
    private Habit added;

    AddHabitCommand(FitnessRepository repo, String username, String name, String desc,
                    Habit.Frequency freq, Habit.Category cat, String icon, int target, String unit) {
        this.repo = repo; this.username = username; this.name = name;
        this.desc = desc; this.freq = freq; this.cat = cat;
        this.icon = icon; this.target = target; this.unit = unit;
    }
    public void execute() { added = repo.addHabit(username, name, desc, freq, cat, icon, target, unit); }
    public void undo()    { if (added != null) repo.removeHabit(added.getId()); }
    public String getDescription() { return "Add habit: " + name; }
}

// ── ToggleHabitCommand ───────────────────────────────────────────────────────
class ToggleHabitCommand implements Command {
    private final FitnessRepository repo;
    private final int habitId;
    private boolean wasDone;

    ToggleHabitCommand(FitnessRepository repo, int habitId) {
        this.repo = repo; this.habitId = habitId;
    }
    public void execute() {
        Habit h = repo.findHabitById(habitId);
        if (h != null) { wasDone = h.isCompletedToday(); repo.toggleHabitToday(habitId); }
    }
    public void undo() {
        Habit h = repo.findHabitById(habitId);
        if (h != null) {
            if (wasDone) h.markCompleted(LocalDate.now());
            else         h.unmarkCompleted(LocalDate.now());
        }
    }
    public String getDescription() { return "Toggle habit #" + habitId; }
}

// ── CommandManager ────────────────────────────────────────────────────────────
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd) { cmd.execute(); undoStack.push(cmd); redoStack.clear(); }
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    public String  undoLabel() { return canUndo() ? undoStack.peek().getDescription() : ""; }
    public String  redoLabel() { return canRedo() ? redoStack.peek().getDescription() : ""; }
    public void undo() { if (canUndo()) { Command c = undoStack.pop(); c.undo(); redoStack.push(c); } }
    public void redo() { if (canRedo()) { Command c = redoStack.pop(); c.execute(); undoStack.push(c); } }

    // ── Factories ─────────────────────────────────────────────────────────────
    public static Command logWorkout(FitnessRepository r, String user,
                                     WorkoutSession.WorkoutType type, LocalDate date,
                                     int dur, double dist, int cal, int hr, double intensity, String notes) {
        return new LogWorkoutCommand(r, user, type, date, dur, dist, cal, hr, intensity, notes);
    }
    public static Command removeWorkout(FitnessRepository r, int id) { return new RemoveWorkoutCommand(r, id); }
    public static Command addHabit(FitnessRepository r, String user, String name, String desc,
                                   Habit.Frequency freq, Habit.Category cat, String icon, int target, String unit) {
        return new AddHabitCommand(r, user, name, desc, freq, cat, icon, target, unit);
    }
    public static Command toggleHabit(FitnessRepository r, int id) { return new ToggleHabitCommand(r, id); }
}
