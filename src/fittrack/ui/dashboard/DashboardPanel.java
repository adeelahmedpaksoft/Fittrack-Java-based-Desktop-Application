package fittrack.ui.dashboard;

import fittrack.model.*;
import fittrack.observer.FitnessObserver;
import fittrack.singleton.AppConfig;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel implements FitnessObserver {

    private final FitnessRepository repo;
    private final User currentUser;
    private JPanel statsRow, chartsRow, recentRow;
    private JLabel greetingLabel;

    public DashboardPanel(FitnessRepository repo, User user) {
        this.repo = repo; this.currentUser = user;
        setLayout(new BorderLayout()); setBackground(Theme.BG_DARK);
        build(); refresh();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(26, 28, 14, 28));

        greetingLabel = UI.label("", Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
        JLabel sub = UI.label("Your fitness summary · " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")),
                Theme.FONT_BODY, Theme.TEXT_MUTED);
        JPanel col = new JPanel(); col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false); col.add(greetingLabel); col.add(Box.createVerticalStrut(4)); col.add(sub);

        // BMI badge
        double bmi = currentUser.getBmi();
        String bmiLabel = bmi < 18.5 ? "Underweight" : bmi < 25 ? "Healthy" : bmi < 30 ? "Overweight" : "Obese";
        Color bmiColor  = bmi < 18.5 ? Theme.NEON_BLUE : bmi < 25 ? Theme.NEON_GREEN : bmi < 30 ? Theme.NEON_ORANGE : Theme.NEON_PINK;
        JLabel bmiLbl = UI.badge(String.format("BMI %.1f · %s", bmi, bmiLabel), bmiColor);

        header.add(col, BorderLayout.WEST); header.add(bmiLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_DARK);
        content.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Stats row
        statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false); statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        content.add(statsRow); content.add(Box.createVerticalStrut(18));

        // Charts row
        chartsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsRow.setOpaque(false); chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        content.add(chartsRow); content.add(Box.createVerticalStrut(18));

        // Recent workouts
        recentRow = new JPanel(); recentRow.setLayout(new BoxLayout(recentRow, BoxLayout.Y_AXIS));
        recentRow.setOpaque(false);
        JPanel recentCard = wrapCard("Recent Workouts", recentRow);
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        content.add(recentCard);

        add(UI.scroll(content), BorderLayout.CENTER);
    }

    public void refresh() {
        greetingLabel.setText("Hey, " + currentUser.getDisplayName().split(" ")[0] + "! 💪");
        List<WorkoutSession> workouts = repo.getAllWorkouts(currentUser.getUsername());

        int calories  = repo.totalCaloriesThisWeek(currentUser.getUsername());
        int sessions  = repo.totalWorkoutsThisMonth(currentUser.getUsername());
        double dist   = repo.totalDistanceThisMonth(currentUser.getUsername());
        int habDone   = repo.habitsCompletedToday(currentUser.getUsername());
        int habTotal  = repo.getAllHabits(currentUser.getUsername()).size();
        int calGoal   = AppConfig.getInstance().getWeeklyCalGoal();
        int wkGoal    = AppConfig.getInstance().getMonthlyWorkoutGoal();

        // Stats
        statsRow.removeAll();
        statsRow.add(statCard("Calories This Week",   calories + " kcal",  (double)calories/calGoal,  Theme.NEON_ORANGE, "🔥"));
        statsRow.add(statCard("Workouts This Month",  sessions + " sessions", (double)sessions/wkGoal, Theme.NEON_GREEN,  "🏋"));
        statsRow.add(statCard("Distance This Month",  String.format("%.1f km", dist), Math.min(dist/50,1), Theme.NEON_BLUE, "🛣"));
        statsRow.add(statCard("Habits Today",         habDone + " / " + habTotal, habTotal > 0 ? (double)habDone/habTotal : 0, Theme.NEON_PURPLE, "✅"));
        statsRow.revalidate(); statsRow.repaint();

        // Charts
        chartsRow.removeAll();
        // Weekly calorie bar chart
        WeeklyCalChart weekChart = new WeeklyCalChart(workouts);
        chartsRow.add(wrapCard("Calories This Week", weekChart));
        // Workout type donut
        WorkoutTypeChart typeChart = new WorkoutTypeChart(workouts);
        chartsRow.add(wrapCard("Workout Types", typeChart));
        chartsRow.revalidate(); chartsRow.repaint();

        // Recent
        recentRow.removeAll();
        List<WorkoutSession> recent = workouts.subList(0, Math.min(5, workouts.size()));
        if (recent.isEmpty()) {
            JLabel e = UI.label("No workouts logged yet. Add your first session!", Theme.FONT_BODY, Theme.TEXT_MUTED);
            e.setHorizontalAlignment(SwingConstants.CENTER); recentRow.add(e);
        } else {
            for (WorkoutSession w : recent) {
                recentRow.add(recentWorkoutRow(w));
                recentRow.add(Box.createVerticalStrut(6));
            }
        }
        recentRow.revalidate(); recentRow.repaint();
    }

    @Override public void onDataChanged(String event, Object data) { refresh(); }

    // ── Stat card ─────────────────────────────────────────────────────────────
    private JPanel statCard(String title, String value, double progress, Color color, String icon) {
        JPanel card = UI.glowCard(new BorderLayout(0, 8), color);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(UI.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED), BorderLayout.WEST);
        top.add(UI.label(icon, new Font("Segoe UI", Font.PLAIN, 18), color), BorderLayout.EAST);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(color);

        // Animated count (for numeric values)
        try {
            int target = Integer.parseInt(value.replaceAll("[^0-9]",""));
            Timer t = new Timer(20, null); final int[] cur = {0};
            t.addActionListener(e -> {
                cur[0] = Math.min(target, cur[0] + Math.max(1, target/25));
                val.setText(value.replaceFirst("\\d+", String.valueOf(cur[0])));
                if (cur[0] >= target) ((Timer)e.getSource()).stop();
            });
            t.start();
        } catch (Exception ignored) {}

        JProgressBar pb = UI.progressBar((int)(Math.min(progress, 1.0) * 100), color);

        card.add(top, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        card.add(pb,  BorderLayout.SOUTH);
        return card;
    }

    private JPanel recentWorkoutRow(WorkoutSession w) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(Theme.BG_ELEVATED);
        row.setOpaque(true);
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        Color c = Theme.workoutColor(w.getType());
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillOval(0,3,12,12); g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(16,16)); dot.setOpaque(false);
        JPanel dotWrap = new JPanel(new GridBagLayout()); dotWrap.setOpaque(false); dotWrap.add(dot);

        JLabel type = UI.label(w.getType().toString(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        JLabel meta = UI.label(w.getDurationMinutes() + " min · " + w.getCaloriesBurned() + " kcal" +
                (w.getDistanceKm() > 0 ? " · " + String.format("%.1f km", w.getDistanceKm()) : ""),
                Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel tc = new JPanel(); tc.setLayout(new BoxLayout(tc, BoxLayout.Y_AXIS)); tc.setOpaque(false);
        tc.add(type); tc.add(meta);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        right.add(UI.badge(String.format("%.0f/10", w.getIntensityScore()), Theme.intensityColor(w.getIntensityScore())));
        right.add(UI.label(w.getDate().format(DateTimeFormatter.ofPattern("MMM dd")), Theme.FONT_SMALL, Theme.TEXT_MUTED));

        row.add(dotWrap, BorderLayout.WEST); row.add(tc, BorderLayout.CENTER); row.add(right, BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(Theme.BG_HOVER); row.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { row.setBackground(Theme.BG_ELEVATED); row.repaint(); }
        });
        return row;
    }

    private JPanel wrapCard(String title, JComponent comp) {
        JPanel card = UI.card(new BorderLayout(0,10));
        card.setBorder(new EmptyBorder(16,16,16,16));
        card.add(UI.label(title, Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
        card.add(comp, BorderLayout.CENTER);
        return card;
    }
}
