package fittrack.persistence;

import fittrack.model.*;
import fittrack.singleton.AppConfig;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** File-based CSV persistence for workouts and habits. */
public class FitnessPersistenceService {

    public void saveWorkouts(List<WorkoutSession> workouts) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(AppConfig.getInstance().getWorkoutsFile()))) {
            pw.println("id,username,type,date,duration,distance,calories,hr,intensity,notes");
            for (WorkoutSession w : workouts) {
                pw.printf("%d,%s,%s,%s,%d,%.2f,%d,%d,%.1f,%s%n",
                        w.getId(), w.getUsername(), w.getType(), w.getDate(),
                        w.getDurationMinutes(), w.getDistanceKm(), w.getCaloriesBurned(),
                        w.getHeartRateAvg(), w.getIntensityScore(), esc(w.getNotes()));
            }
        }
    }

    public List<WorkoutSession> loadWorkouts() throws IOException {
        List<WorkoutSession> list = new ArrayList<>();
        File f = new File(AppConfig.getInstance().getWorkoutsFile());
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    String[] p = line.split(",", 10);
                    WorkoutSession w = new WorkoutSession(
                            Integer.parseInt(p[0].trim()), p[1].trim(),
                            WorkoutSession.WorkoutType.valueOf(p[2].trim()),
                            LocalDate.parse(p[3].trim()),
                            Integer.parseInt(p[4].trim()), Double.parseDouble(p[5].trim()),
                            Integer.parseInt(p[6].trim()), Integer.parseInt(p[7].trim()),
                            Double.parseDouble(p[8].trim()), unesc(p.length > 9 ? p[9] : ""));
                    list.add(w);
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    public void saveHabits(List<Habit> habits) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(AppConfig.getInstance().getHabitsFile()))) {
            pw.println("id,username,name,description,frequency,category,icon,target,unit,createdDate,completedDates");
            for (Habit h : habits) {
                String dates = h.getCompletedDates().stream()
                        .map(LocalDate::toString).reduce("", (a, b) -> a.isEmpty() ? b : a + "|" + b);
                pw.printf("%d,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s%n",
                        h.getId(), h.getUsername(), esc(h.getName()), esc(h.getDescription()),
                        h.getFrequency(), h.getCategory(), esc(h.getIcon()),
                        h.getTargetCount(), esc(h.getUnit()), h.getCreatedDate(), dates);
            }
        }
    }

    public List<Habit> loadHabits() throws IOException {
        List<Habit> list = new ArrayList<>();
        File f = new File(AppConfig.getInstance().getHabitsFile());
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    String[] p = line.split(",", 11);
                    Habit h = new Habit(Integer.parseInt(p[0].trim()), p[1].trim(),
                            unesc(p[2]), unesc(p[3]),
                            Habit.Frequency.valueOf(p[4].trim()),
                            Habit.Category.valueOf(p[5].trim()),
                            unesc(p[6]),
                            Integer.parseInt(p[7].trim()), unesc(p[8]));
                    if (p.length > 10 && !p[10].isBlank()) {
                        Set<LocalDate> dates = new java.util.HashSet<>();
                        for (String d : p[10].split("\\|")) {
                            try { dates.add(LocalDate.parse(d.trim())); } catch (Exception ignored) {}
                        }
                        h.setCompletedDates(dates);
                    }
                    list.add(h);
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    private String esc(String s)   { return s == null ? "" : s.replace(",", "\\,").replace("\n", "\\n"); }
    private String unesc(String s) { return s == null ? "" : s.replace("\\,", ",").replace("\\n", "\n"); }
}
