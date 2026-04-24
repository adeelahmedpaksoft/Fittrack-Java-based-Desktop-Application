package fittrack.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Habit implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Frequency { DAILY, WEEKLY, WEEKDAYS }
    public enum Category  { HYDRATION, SLEEP, NUTRITION, EXERCISE, MINDFULNESS, OTHER }

    private int        id;
    private String     username;
    private String     name;
    private String     description;
    private Frequency  frequency;
    private Category   category;
    private String     icon;          // emoji icon
    private int        targetCount;   // e.g. 8 glasses of water
    private String     unit;          // e.g. "glasses", "minutes", "hours"
    private LocalDate  createdDate;
    private Set<LocalDate> completedDates = new HashSet<>();

    public Habit(int id, String username, String name, String description,
                 Frequency frequency, Category category, String icon,
                 int targetCount, String unit) {
        this.id          = id;
        this.username    = username;
        this.name        = name;
        this.description = description;
        this.frequency   = frequency;
        this.category    = category;
        this.icon        = icon;
        this.targetCount = targetCount;
        this.unit        = unit;
        this.createdDate = LocalDate.now();
    }

    public int       getId()           { return id; }
    public String    getUsername()     { return username; }
    public String    getName()         { return name; }
    public String    getDescription()  { return description; }
    public Frequency getFrequency()    { return frequency; }
    public Category  getCategory()     { return category; }
    public String    getIcon()         { return icon; }
    public int       getTargetCount()  { return targetCount; }
    public String    getUnit()         { return unit; }
    public LocalDate getCreatedDate()  { return createdDate; }
    public Set<LocalDate> getCompletedDates() { return completedDates; }

    public void setName(String n)         { this.name = n; }
    public void setDescription(String d)  { this.description = d; }
    public void setIcon(String i)         { this.icon = i; }
    public void setTargetCount(int t)     { this.targetCount = t; }
    public void setUnit(String u)         { this.unit = u; }
    public void setCompletedDates(Set<LocalDate> d) { this.completedDates = d; }

    public boolean isCompletedOn(LocalDate date)  { return completedDates.contains(date); }
    public boolean isCompletedToday()             { return isCompletedOn(LocalDate.now()); }

    public void markCompleted(LocalDate date)   { completedDates.add(date); }
    public void unmarkCompleted(LocalDate date) { completedDates.remove(date); }
    public void toggleToday() {
        LocalDate today = LocalDate.now();
        if (isCompletedToday()) unmarkCompleted(today);
        else markCompleted(today);
    }

    /** Current streak in days */
    public int getCurrentStreak() {
        int streak = 0;
        LocalDate d = LocalDate.now();
        while (completedDates.contains(d)) { streak++; d = d.minusDays(1); }
        return streak;
    }

    /** Total completions */
    public int getTotalCompletions() { return completedDates.size(); }

    /** Completion rate over last 30 days */
    public double getCompletionRate30Days() {
        LocalDate today = LocalDate.now();
        long done = completedDates.stream()
                .filter(d -> !d.isBefore(today.minusDays(29)))
                .count();
        return done / 30.0 * 100;
    }
}
