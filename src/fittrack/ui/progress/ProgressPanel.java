package fittrack.ui.progress;

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
import java.util.*;
import java.util.List;

public class ProgressPanel extends JPanel implements FitnessObserver {

    private final FitnessRepository repo;
    private final User              user;
    private JPanel contentPanel;

    public ProgressPanel(FitnessRepository repo, User user) {
        this.repo = repo; this.user = user;
        setLayout(new BorderLayout()); setBackground(Theme.BG_DARK);
        build(); refresh();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 14, 28));
        header.add(UI.label("Progress & Analytics", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        JLabel sub = UI.label("Your fitness journey over time", Theme.FONT_BODY, Theme.TEXT_MUTED);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        add(UI.scroll(contentPanel), BorderLayout.CENTER);
    }

    public void refresh() {
        contentPanel.removeAll();
        List<WorkoutSession> all = repo.getAllWorkouts(user.getUsername());

        // ── Row 1: Goal rings ─────────────────────────────────────────────
        JPanel goalsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        goalsRow.setOpaque(false); goalsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        int weeklyCalGoal    = AppConfig.getInstance().getWeeklyCalGoal();
        int monthlyWkGoal    = AppConfig.getInstance().getMonthlyWorkoutGoal();
        int weeklyCalActual  = repo.totalCaloriesThisWeek(user.getUsername());
        int monthlyWkActual  = repo.totalWorkoutsThisMonth(user.getUsername());
        double monthlyDistKm = repo.totalDistanceThisMonth(user.getUsername());

        goalsRow.add(ringCard("Weekly Calories", weeklyCalActual, weeklyCalGoal, "kcal", Theme.NEON_ORANGE));
        goalsRow.add(ringCard("Monthly Workouts", monthlyWkActual, monthlyWkGoal, "sessions", Theme.NEON_GREEN));
        goalsRow.add(ringCard("Monthly Distance", (int)monthlyDistKm, 50, "km", Theme.NEON_BLUE));
        contentPanel.add(goalsRow);
        contentPanel.add(Box.createVerticalStrut(18));

        // ── Row 2: 30-day activity heatmap ───────────────────────────────
        JPanel heatCard = UI.card(new BorderLayout(0, 10));
        heatCard.setBorder(new EmptyBorder(16,16,16,16));
        heatCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        heatCard.add(UI.label("Activity Heatmap — Last 30 Days", Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
        heatCard.add(new ActivityHeatmap(all), BorderLayout.CENTER);
        contentPanel.add(heatCard);
        contentPanel.add(Box.createVerticalStrut(18));

        // ── Row 3: Personal bests ─────────────────────────────────────────
        JPanel pbCard = UI.card(new BorderLayout(0, 12));
        pbCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        pbCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        pbCard.add(UI.label("Personal Bests 🏆", Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);

        JPanel pbGrid = new JPanel(new GridLayout(2, 3, 14, 10)); pbGrid.setOpaque(false);

        // Best single session stats
        OptionalInt bestCal = all.stream().mapToInt(WorkoutSession::getCaloriesBurned).max();
        OptionalInt bestDur = all.stream().mapToInt(WorkoutSession::getDurationMinutes).max();
        OptionalDouble bestDist = all.stream().mapToDouble(WorkoutSession::getDistanceKm).max();
        OptionalDouble bestInt  = all.stream().mapToDouble(WorkoutSession::getIntensityScore).max();
        OptionalInt totalSessions = OptionalInt.of(all.size());
        int totalCal = all.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();

        pbGrid.add(pbTile("🔥 Best Session Calories", bestCal.isPresent() ? bestCal.getAsInt() + " kcal" : "—", Theme.NEON_ORANGE));
        pbGrid.add(pbTile("⏱ Longest Session",        bestDur.isPresent()  ? bestDur.getAsInt()  + " min"  : "—", Theme.NEON_GREEN));
        pbGrid.add(pbTile("🛣 Longest Distance",       bestDist.isPresent() && bestDist.getAsDouble() > 0 ? String.format("%.1f km", bestDist.getAsDouble()) : "—", Theme.NEON_BLUE));
        pbGrid.add(pbTile("⚡ Peak Intensity",         bestInt.isPresent()  ? String.format("%.0f/10", bestInt.getAsDouble()) : "—", Theme.NEON_PINK));
        pbGrid.add(pbTile("📊 Total Sessions",         totalSessions.getAsInt() + " workouts", Theme.NEON_PURPLE));
        pbGrid.add(pbTile("🔥 Total Calories Burned",  totalCal + " kcal", Theme.NEON_YELLOW));

        pbCard.add(pbGrid, BorderLayout.CENTER);
        contentPanel.add(pbCard);
        contentPanel.add(Box.createVerticalStrut(18));

        // ── Row 4: Workout type breakdown bar ────────────────────────────
        if (!all.isEmpty()) {
            JPanel typeCard = UI.card(new BorderLayout(0, 10));
            typeCard.setBorder(new EmptyBorder(16,16,16,16));
            typeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
            typeCard.add(UI.label("Workout Type Breakdown", Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
            typeCard.add(new TypeBreakdownChart(all), BorderLayout.CENTER);
            contentPanel.add(typeCard);
        }

        contentPanel.revalidate(); contentPanel.repaint();
    }

    @Override public void onDataChanged(String event, Object data) { refresh(); }

    // ── Ring / goal card ──────────────────────────────────────────────────────

    private JPanel ringCard(String title, int actual, int goal, String unit, Color color) {
        JPanel card = UI.glowCard(new BorderLayout(10, 0), color);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Ring chart
        double pct = goal > 0 ? Math.min(1.0, (double) actual / goal) : 0;
        JPanel ring = new JPanel() {
            float anim = 0;
            { javax.swing.Timer t = new javax.swing.Timer(16, null);
              t.addActionListener(e -> { anim = Math.min(1f, anim+0.04f); repaint(); if(anim>=1)((javax.swing.Timer)e.getSource()).stop(); });
              t.start(); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth()-s)/2, y = (getHeight()-s)/2;
                g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Theme.BG_ELEVATED);
                g2.drawArc(x, y, s, s, -90, 360);
                g2.setColor(color);
                int sweep = (int)(pct * 360 * anim);
                g2.drawArc(x, y, s, s, -90, -sweep);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(Theme.TEXT_PRIMARY);
                String pctStr = (int)(pct*100) + "%";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pctStr, getWidth()/2 - fm.stringWidth(pctStr)/2, getHeight()/2 + fm.getAscent()/2 - 4);
                g2.dispose();
            }
        };
        ring.setOpaque(false); ring.setPreferredSize(new Dimension(90, 90));

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setOpaque(false);
        info.add(UI.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED));
        info.add(Box.createVerticalStrut(6));
        JLabel valLbl = UI.label(String.valueOf(actual), new Font("Segoe UI", Font.BOLD, 26), color);
        info.add(valLbl);
        info.add(UI.label("/ " + goal + " " + unit, Theme.FONT_SMALL, Theme.TEXT_MUTED));
        info.add(Box.createVerticalStrut(4));
        info.add(UI.progressBar((int)(pct*100), color));

        card.add(ring, BorderLayout.WEST); card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel pbTile(String title, String value, Color color) {
        JPanel tile = new JPanel(new BorderLayout(0, 4)); tile.setOpaque(false);
        tile.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        tile.add(UI.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED), BorderLayout.NORTH);
        tile.add(UI.label(value, Theme.FONT_HEADING, color), BorderLayout.CENTER);
        return tile;
    }
}

// ── Activity Heatmap (30-day) ─────────────────────────────────────────────────

class ActivityHeatmap extends JPanel {
    private final List<WorkoutSession> workouts;
    ActivityHeatmap(List<WorkoutSession> workouts) {
        this.workouts = workouts; setOpaque(false);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int days = 30, cellW = (getWidth() - 4*(days-1)) / days, cellH = getHeight() - 20;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate d = today.minusDays(days - 1 - i);
            long count = workouts.stream().filter(w -> w.getDate().equals(d)).count();
            int totalCal = workouts.stream().filter(w->w.getDate().equals(d)).mapToInt(WorkoutSession::getCaloriesBurned).sum();
            Color c = count == 0 ? Theme.BG_ELEVATED :
                      totalCal < 200 ? Theme.withAlpha(Theme.NEON_GREEN, 120) :
                      totalCal < 400 ? Theme.withAlpha(Theme.NEON_GREEN, 180) : Theme.NEON_GREEN;
            int x = i * (cellW + 4);
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(x, 0, cellW, cellH, 4, 4));
            // Day label every 5 days
            if (i % 5 == 0) {
                g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
                g2.drawString(String.valueOf(d.getDayOfMonth()), x, cellH + 14);
            }
        }
        g2.dispose();
    }
}

// ── Type breakdown horizontal bar chart ──────────────────────────────────────

class TypeBreakdownChart extends JPanel {
    private final List<WorkoutSession> workouts;
    private float anim = 0f;
    TypeBreakdownChart(List<WorkoutSession> workouts) {
        this.workouts = workouts; setOpaque(false);
        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> { anim = Math.min(1f, anim+0.04f); repaint(); if(anim>=1)((javax.swing.Timer)e.getSource()).stop(); });
        t.start();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (workouts.isEmpty()) return;
        Map<WorkoutSession.WorkoutType, Long> counts = new LinkedHashMap<>();
        for (WorkoutSession w : workouts) counts.merge(w.getType(), 1L, Long::sum);
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(1);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 0, rowH = getHeight() / counts.size() - 4;
        int labelW = 110, barAreaW = getWidth() - labelW - 50;
        for (var entry : counts.entrySet()) {
            Color c = Theme.workoutColor(entry.getKey());
            int barW = (int)(barAreaW * entry.getValue() / (float)max * anim);
            // Label
            g2.setColor(Theme.TEXT_SECONDARY); g2.setFont(Theme.FONT_SMALL);
            g2.drawString(entry.getKey().toString(), 0, y + rowH/2 + 4);
            // Bar
            g2.setColor(Theme.BG_ELEVATED);
            g2.fill(new RoundRectangle2D.Float(labelW, y+2, barAreaW, rowH-4, rowH-4, rowH-4));
            GradientPaint gp = new GradientPaint(labelW, 0, c.brighter(), labelW+barW, 0, c);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(labelW, y+2, Math.max(barW,8), rowH-4, rowH-4, rowH-4));
            // Count
            g2.setColor(Theme.TEXT_PRIMARY); g2.setFont(Theme.FONT_LABEL);
            g2.drawString(String.valueOf(entry.getValue()), labelW + barW + 8, y + rowH/2 + 4);
            y += rowH + 4;
        }
        g2.dispose();
    }
}
