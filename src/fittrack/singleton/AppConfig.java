package fittrack.singleton;

import java.io.*;
import java.util.Properties;

/**
 * Singleton Pattern – single shared application configuration.
 */
public class AppConfig {
    private static AppConfig instance;
    private static final String CONFIG_FILE = "fittrack_config.properties";
    private final Properties props = new Properties();

    private AppConfig() {
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) { props.load(fis); }
            catch (IOException ignored) {}
        } else {
            props.setProperty("data.workouts", "workouts.dat");
            props.setProperty("data.habits",   "habits.dat");
            props.setProperty("log.file",      "fittrack.log");
            props.setProperty("app.version",   "1.0");
            props.setProperty("calorie.goal",  "500");
            props.setProperty("workout.goal",  "5");
            save();
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    public String getWorkoutsFile()  { return props.getProperty("data.workouts", "workouts.dat"); }
    public String getHabitsFile()    { return props.getProperty("data.habits",   "habits.dat"); }
    public String getLogFilePath()   { return props.getProperty("log.file",      "fittrack.log"); }
    public String getVersion()       { return props.getProperty("app.version",   "1.0"); }
    public int    getWeeklyCalGoal() { return Integer.parseInt(props.getProperty("calorie.goal", "500")); }
    public int    getMonthlyWorkoutGoal() { return Integer.parseInt(props.getProperty("workout.goal", "5")); }

    public void setWeeklyCalGoal(int g)      { props.setProperty("calorie.goal", String.valueOf(g)); save(); }
    public void setMonthlyWorkoutGoal(int g) { props.setProperty("workout.goal", String.valueOf(g)); save(); }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "FitTrack Configuration");
        } catch (IOException ignored) {}
    }
}
