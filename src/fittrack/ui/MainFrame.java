package fittrack.ui;

import fittrack.auth.AuthService;
import fittrack.command.CommandManager;
import fittrack.model.*;
import fittrack.observer.ActivityLogger;
import fittrack.persistence.FitnessPersistenceService;
import fittrack.ui.components.Theme;
import fittrack.ui.dashboard.DashboardPanel;
import fittrack.ui.habit.HabitPanel;
import fittrack.ui.login.LoginPanel;
import fittrack.ui.progress.ProgressPanel;
import fittrack.ui.settings.SettingsPanel;
import fittrack.ui.workout.WorkoutPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {

    private final FitnessRepository       repo        = new FitnessRepository();
    private final CommandManager          cmdMgr      = new CommandManager();
    private final FitnessPersistenceService persistence = new FitnessPersistenceService();
    private final ActivityLogger          logger       = new ActivityLogger();

    private CardLayout rootLayout = new CardLayout();
    private JPanel     rootPanel  = new JPanel(rootLayout);

    // Post-login components
    private SidebarPanel   sidebar;
    private DashboardPanel dashPanel;
    private WorkoutPanel   workoutPanel;
    private HabitPanel     habitPanel;
    private ProgressPanel  progressPanel;
    private SettingsPanel  settingsPanel;
    private JPanel         contentArea;

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_APP   = "APP";
    private static final String PAGE_DASH  = "DASH";
    private static final String PAGE_WO    = "WORKOUT";
    private static final String PAGE_HAB   = "HABIT";
    private static final String PAGE_PROG  = "PROGRESS";
    private static final String PAGE_SETT  = "SETTINGS";

    public MainFrame() {
        super("FitTrack Pro — SET11103");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { onExit(); }
        });
        repo.addObserver(logger);
        loadData();
        buildRoot();
    }

    private void buildRoot() {
        rootPanel.setBackground(Theme.BG_DEEPEST);
        rootPanel.add(new LoginPanel(this::onLogin), CARD_LOGIN);
        setContentPane(rootPanel);
        rootLayout.show(rootPanel, CARD_LOGIN);
    }

    private void onLogin(User user) {
        buildWorkspace(user);
        rootPanel.add(buildAppPanel(user), CARD_APP);
        rootLayout.show(rootPanel, CARD_APP);
        setTitle("FitTrack Pro — " + user.getDisplayName() + " · " + user.getFitnessGoal());
        if (repo.getAllWorkouts(user.getUsername()).isEmpty()) seedDemoData(user);
        refreshAll();
    }

    private void buildWorkspace(User user) {
        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(Theme.BG_DARK);

        dashPanel     = new DashboardPanel(repo, user);
        workoutPanel  = new WorkoutPanel(repo, cmdMgr, user);
        habitPanel    = new HabitPanel(repo, cmdMgr, user);
        progressPanel = new ProgressPanel(repo, user);
        settingsPanel = new SettingsPanel(user);

        repo.addObserver(dashPanel);
        repo.addObserver(workoutPanel);
        repo.addObserver(habitPanel);
        repo.addObserver(progressPanel);

        contentArea.add(dashPanel,     PAGE_DASH);
        contentArea.add(workoutPanel,  PAGE_WO);
        contentArea.add(habitPanel,    PAGE_HAB);
        contentArea.add(progressPanel, PAGE_PROG);
        contentArea.add(settingsPanel, PAGE_SETT);
    }

    private JPanel buildAppPanel(User user) {
        sidebar = new SidebarPanel(user, this::navigate, this::onLogout);
        JPanel app = new JPanel(new BorderLayout());
        app.add(sidebar,     BorderLayout.WEST);
        app.add(contentArea, BorderLayout.CENTER);
        return app;
    }

    private void navigate(SidebarPanel.Page page) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        switch (page) {
            case DASHBOARD -> cl.show(contentArea, PAGE_DASH);
            case WORKOUTS  -> cl.show(contentArea, PAGE_WO);
            case HABITS    -> cl.show(contentArea, PAGE_HAB);
            case PROGRESS  -> cl.show(contentArea, PAGE_PROG);
            case SETTINGS  -> cl.show(contentArea, PAGE_SETT);
        }
    }

    private void onLogout() {
        int ans = JOptionPane.showConfirmDialog(this, "Save data and logout?",
                "Logout", JOptionPane.YES_NO_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) return;
        if (ans == JOptionPane.YES_OPTION) saveData();

        repo.removeObserver(dashPanel);
        repo.removeObserver(workoutPanel);
        repo.removeObserver(habitPanel);
        repo.removeObserver(progressPanel);
        AuthService.getInstance().logout();

        rootPanel.removeAll();
        rootPanel.add(new LoginPanel(this::onLogin), CARD_LOGIN);
        rootLayout.show(rootPanel, CARD_LOGIN);
        setTitle("FitTrack Pro — SET11103");
    }

    private void loadData() {
        try { repo.setWorkouts(persistence.loadWorkouts()); } catch (IOException ignored) {}
        try { repo.setHabits(persistence.loadHabits()); } catch (IOException ignored) {}
    }

    private void saveData() {
        try { persistence.saveWorkouts(repo.getAllWorkoutsRaw()); } catch (IOException e) { e.printStackTrace(); }
        try { persistence.saveHabits(repo.getAllHabitsRaw()); } catch (IOException e) { e.printStackTrace(); }
    }

    private void refreshAll() {
        if (dashPanel     != null) dashPanel.refresh();
        if (workoutPanel  != null) workoutPanel.refresh();
        if (habitPanel    != null) habitPanel.refresh();
        if (progressPanel != null) progressPanel.refresh();
    }

    private void onExit() {
        int ans = JOptionPane.showConfirmDialog(this, "Save your fitness data before exiting?",
                "Exit FitTrack", JOptionPane.YES_NO_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) return;
        if (ans == JOptionPane.YES_OPTION) saveData();
        System.exit(0);
    }

    // ── Demo data seed ────────────────────────────────────────────────────────

    private void seedDemoData(User user) {
        LocalDate today = LocalDate.now();
        String u = user.getUsername();

        // Workouts
        WorkoutSession w1 = repo.addWorkout(u, WorkoutSession.WorkoutType.RUNNING,     today.minusDays(1), 45, 6.5, 420, 158, 7.5, "Morning run, felt great!");
        WorkoutSession w2 = repo.addWorkout(u, WorkoutSession.WorkoutType.WEIGHTLIFTING,today.minusDays(2), 60, 0.0, 380, 135, 8.0, "Chest and back day");
        WorkoutSession w3 = repo.addWorkout(u, WorkoutSession.WorkoutType.CYCLING,     today.minusDays(3), 75, 22.0,550, 145, 6.5, "Evening ride");
        WorkoutSession w4 = repo.addWorkout(u, WorkoutSession.WorkoutType.HIIT,        today.minusDays(5), 30, 0.0, 340, 172, 9.0, "Tabata intervals");
        WorkoutSession w5 = repo.addWorkout(u, WorkoutSession.WorkoutType.YOGA,        today.minusDays(6), 50, 0.0, 180, 95,  4.0, "Yin yoga, very relaxing");
        WorkoutSession w6 = repo.addWorkout(u, WorkoutSession.WorkoutType.RUNNING,     today.minusDays(8), 35, 5.0, 310, 152, 6.5, "Easy 5k");
        WorkoutSession w7 = repo.addWorkout(u, WorkoutSession.WorkoutType.SWIMMING,    today.minusDays(10),40, 1.5, 400, 140, 7.0, "Lap swimming");
        WorkoutSession w8 = repo.addWorkout(u, WorkoutSession.WorkoutType.WEIGHTLIFTING,today.minusDays(12),55, 0.0, 360, 130, 7.5, "Leg day");

        // Habits
        Habit h1 = repo.addHabit(u, "Drink 8 Glasses of Water", "Stay hydrated throughout the day",
                Habit.Frequency.DAILY, Habit.Category.HYDRATION, "💧", 8, "glasses");
        Habit h2 = repo.addHabit(u, "Sleep 8 Hours", "Get proper recovery sleep",
                Habit.Frequency.DAILY, Habit.Category.SLEEP, "💤", 8, "hours");
        Habit h3 = repo.addHabit(u, "Eat 5 Servings of Veg", "Maintain healthy nutrition",
                Habit.Frequency.DAILY, Habit.Category.NUTRITION, "🥗", 5, "servings");
        Habit h4 = repo.addHabit(u, "10 Min Meditation", "Morning mindfulness practice",
                Habit.Frequency.DAILY, Habit.Category.MINDFULNESS, "🧘", 10, "minutes");
        Habit h5 = repo.addHabit(u, "30 Min Walk", "Daily step goal",
                Habit.Frequency.DAILY, Habit.Category.EXERCISE, "🚶", 30, "minutes");

        // Mark some habits completed on past days to show streaks
        for (int i = 0; i < 12; i++) {
            h1.markCompleted(today.minusDays(i));
        }
        for (int i = 0; i < 7; i++) {
            h2.markCompleted(today.minusDays(i));
            h4.markCompleted(today.minusDays(i * 2));
        }
        for (int i = 1; i < 5; i++) {
            h3.markCompleted(today.minusDays(i));
            h5.markCompleted(today.minusDays(i));
        }
        // Mark today's habits partially done
        h1.markCompleted(today);
        h5.markCompleted(today);
    }
}
