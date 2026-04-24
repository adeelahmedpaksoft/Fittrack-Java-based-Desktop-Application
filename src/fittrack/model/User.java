package fittrack.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { ADMIN, ATHLETE }

    private final String username;
    private final String passwordHash;
    private final Role   role;
    private final String displayName;
    private final String avatarInitials;
    // fitness profile
    private double weightKg;
    private double heightCm;
    private int    age;
    private String fitnessGoal; // "Weight Loss", "Muscle Gain", "Endurance", "General Fitness"

    public User(String username, String passwordHash, Role role,
                String displayName, double weightKg, double heightCm, int age, String goal) {
        this.username      = username;
        this.passwordHash  = passwordHash;
        this.role          = role;
        this.displayName   = displayName;
        this.weightKg      = weightKg;
        this.heightCm      = heightCm;
        this.age           = age;
        this.fitnessGoal   = goal;
        String[] p = displayName.split(" ");
        avatarInitials = p.length >= 2
                ? ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase()
                : displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
    }

    public String getUsername()       { return username; }
    public String getPasswordHash()   { return passwordHash; }
    public Role   getRole()           { return role; }
    public String getDisplayName()    { return displayName; }
    public String getAvatarInitials() { return avatarInitials; }
    public double getWeightKg()       { return weightKg; }
    public double getHeightCm()       { return heightCm; }
    public int    getAge()            { return age; }
    public String getFitnessGoal()    { return fitnessGoal; }
    public boolean isAdmin()          { return role == Role.ADMIN; }

    public double getBmi() {
        if (heightCm <= 0) return 0;
        double h = heightCm / 100.0;
        return weightKg / (h * h);
    }

    public void setWeightKg(double w)    { this.weightKg = w; }
    public void setFitnessGoal(String g) { this.fitnessGoal = g; }
}
