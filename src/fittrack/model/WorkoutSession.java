package fittrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class WorkoutSession implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum WorkoutType {
        RUNNING, CYCLING, SWIMMING, WEIGHTLIFTING, YOGA, HIIT, WALKING, PILATES, BOXING, OTHER
    }

    private int         id;
    private String      username;
    private WorkoutType type;
    private LocalDate   date;
    private LocalTime   startTime;
    private int         durationMinutes;
    private double      distanceKm;      // 0 if not applicable
    private int         caloriesBurned;
    private int         heartRateAvg;    // bpm, 0 if unknown
    private String      notes;
    private double      intensityScore;  // 1-10

    public WorkoutSession(int id, String username, WorkoutType type, LocalDate date,
                          int durationMinutes, double distanceKm, int caloriesBurned,
                          int heartRateAvg, double intensityScore, String notes) {
        this.id               = id;
        this.username         = username;
        this.type             = type;
        this.date             = date;
        this.startTime        = LocalTime.now();
        this.durationMinutes  = durationMinutes;
        this.distanceKm       = distanceKm;
        this.caloriesBurned   = caloriesBurned;
        this.heartRateAvg     = heartRateAvg;
        this.intensityScore   = intensityScore;
        this.notes            = notes;
    }

    public int         getId()               { return id; }
    public String      getUsername()         { return username; }
    public WorkoutType getType()             { return type; }
    public LocalDate   getDate()             { return date; }
    public int         getDurationMinutes()  { return durationMinutes; }
    public double      getDistanceKm()       { return distanceKm; }
    public int         getCaloriesBurned()   { return caloriesBurned; }
    public int         getHeartRateAvg()     { return heartRateAvg; }
    public double      getIntensityScore()   { return intensityScore; }
    public String      getNotes()            { return notes; }

    public void setType(WorkoutType t)            { this.type = t; }
    public void setDate(LocalDate d)              { this.date = d; }
    public void setDurationMinutes(int d)         { this.durationMinutes = d; }
    public void setDistanceKm(double d)           { this.distanceKm = d; }
    public void setCaloriesBurned(int c)          { this.caloriesBurned = c; }
    public void setHeartRateAvg(int h)            { this.heartRateAvg = h; }
    public void setIntensityScore(double s)       { this.intensityScore = s; }
    public void setNotes(String n)                { this.notes = n; }

    @Override public String toString() {
        return String.format("[%d] %s | %s | %dmin | %dcal", id, type, date, durationMinutes, caloriesBurned);
    }
}
