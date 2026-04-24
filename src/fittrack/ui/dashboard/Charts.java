package fittrack.ui.dashboard;

import fittrack.model.WorkoutSession;
import fittrack.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/** Weekly calorie bar chart */
class WeeklyCalChart extends JPanel {
    private final List<WorkoutSession> workouts;
    private float anim = 0f;

    WeeklyCalChart(List<WorkoutSession> workouts) {
        this.workouts = workouts;
        setOpaque(false);
        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> { anim = Math.min(1f, anim + 0.04f); repaint(); if (anim>=1) ((javax.swing.Timer)e.getSource()).stop(); });
        t.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LocalDate today = LocalDate.now();
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        int[] cals = new int[7];
        for (WorkoutSession w : workouts) {
            for (int i = 0; i < 7; i++) {
                LocalDate d = today.minusDays(6-i);
                if (w.getDate().equals(d)) cals[i] += w.getCaloriesBurned();
            }
        }
        int max = Arrays.stream(cals).max().orElse(1);
        if (max == 0) max = 500;

        int w = getWidth(), h = getHeight(), padL=10, padR=10, padT=14, padB=28;
        int barW = (w-padL-padR)/7 - 6, chartH = h-padT-padB;

        for (int i = 0; i < 7; i++) {
            int bh = (int)(cals[i] * 1f / max * chartH * anim);
            int x = padL + i*((w-padL-padR)/7) + 3;
            int y = padT + chartH - bh;
            Color c = i == 6 ? Theme.NEON_GREEN : Theme.NEON_ORANGE;
            GradientPaint gp = new GradientPaint(x,y,c.brighter(), x,y+bh,Theme.withAlpha(c,100));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, Math.max(bh,3), 5, 5));
            if (cals[i]>0) {
                g2.setColor(Theme.TEXT_PRIMARY); g2.setFont(Theme.FONT_SMALL);
                String lv = String.valueOf(cals[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lv, x+(barW-fm.stringWidth(lv))/2, y-3);
            }
            g2.setColor(i==6?Theme.NEON_GREEN:Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(days[i], x+(barW-fm.stringWidth(days[i]))/2, h-8);
        }
        g2.dispose();
    }
}

/** Workout type donut chart */
class WorkoutTypeChart extends JPanel {
    private final List<WorkoutSession> workouts;
    private float anim = 0f;

    WorkoutTypeChart(List<WorkoutSession> workouts) {
        this.workouts = workouts;
        setOpaque(false);
        javax.swing.Timer t = new javax.swing.Timer(16, null);
        t.addActionListener(e -> { anim = Math.min(1f, anim + 0.035f); repaint(); if(anim>=1)((javax.swing.Timer)e.getSource()).stop(); });
        t.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (workouts.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_BODY);
            g2.drawString("No workouts yet", getWidth()/2-50, getHeight()/2); g2.dispose(); return;
        }
        Map<WorkoutSession.WorkoutType, Integer> counts = new LinkedHashMap<>();
        for (WorkoutSession w : workouts) counts.merge(w.getType(), 1, Integer::sum);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        int cx = getWidth()/3, cy = getHeight()/2, r = Math.min(cx,cy)-14, ir = (int)(r*0.55);
        float start = -90f;

        for (var entry : counts.entrySet()) {
            float sweep = entry.getValue() * 360f / total * anim;
            g2.setColor(Theme.workoutColor(entry.getKey()));
            g2.fill(new Arc2D.Float(cx-r,cy-r,r*2,r*2,start,sweep,Arc2D.PIE));
            start += sweep;
        }
        g2.setColor(Theme.BG_CARD); g2.fillOval(cx-ir,cy-ir,ir*2,ir*2);
        g2.setColor(Theme.TEXT_PRIMARY); g2.setFont(new Font("Segoe UI",Font.BOLD,20));
        String tot = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tot, cx-fm.stringWidth(tot)/2, cy+fm.getAscent()/2-4);
        g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
        g2.drawString("sessions", cx-22, cy+14);

        // Legend
        int lx = cx+r+14, ly = cy-(counts.size()*20)/2;
        int i=0;
        for (var entry : counts.entrySet()) {
            g2.setColor(Theme.workoutColor(entry.getKey()));
            g2.fillRoundRect(lx, ly+i*22, 10, 10, 4, 4);
            g2.setColor(Theme.TEXT_SECONDARY); g2.setFont(Theme.FONT_SMALL);
            g2.drawString(entry.getKey().toString()+" ("+entry.getValue()+")", lx+14, ly+i*22+10);
            i++;
        }
        g2.dispose();
    }
}
