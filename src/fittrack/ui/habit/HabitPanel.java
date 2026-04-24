package fittrack.ui.habit;

import fittrack.command.CommandManager;
import fittrack.model.*;
import fittrack.observer.FitnessObserver;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HabitPanel extends JPanel implements FitnessObserver {

    private final FitnessRepository repo;
    private final CommandManager    cmdMgr;
    private final User              user;

    private JPanel habitsGrid;
    private JLabel completedLabel, streakLabel;

    public HabitPanel(FitnessRepository repo, CommandManager cmdMgr, User user) {
        this.repo = repo; this.cmdMgr = cmdMgr; this.user = user;
        setLayout(new BorderLayout()); setBackground(Theme.BG_DARK);
        build(); refresh();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 14, 28));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titleCol = new JPanel(); titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS)); titleCol.setOpaque(false);
        titleCol.add(UI.label("Habit Tracker", Theme.FONT_TITLE, Theme.TEXT_PRIMARY));
        completedLabel = UI.label("", Theme.FONT_BODY, Theme.NEON_GREEN);
        titleCol.add(completedLabel);
        titleRow.add(titleCol, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); rightBtns.setOpaque(false);
        JButton addBtn = UI.button("＋  New Habit", Theme.NEON_PURPLE);
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e -> showAddHabitDialog());
        rightBtns.add(addBtn);
        titleRow.add(rightBtns, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        // Date strip
        JPanel dateStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8)); dateStrip.setOpaque(false);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            JPanel dayPill = makeDatePill(d, i == 0);
            dateStrip.add(dayPill);
        }
        header.add(dateStrip, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Habits grid
        habitsGrid = new JPanel();
        habitsGrid.setLayout(new BoxLayout(habitsGrid, BoxLayout.Y_AXIS));
        habitsGrid.setBackground(Theme.BG_DARK);
        habitsGrid.setBorder(new EmptyBorder(0, 20, 20, 20));
        add(UI.scroll(habitsGrid), BorderLayout.CENTER);
    }

    public void refresh() {
        List<Habit> habits = repo.getAllHabits(user.getUsername());
        long done = habits.stream().filter(Habit::isCompletedToday).count();
        completedLabel.setText(done + " of " + habits.size() + " completed today");

        habitsGrid.removeAll();

        if (habits.isEmpty()) {
            habitsGrid.add(Box.createVerticalStrut(60));
            JPanel emptyPanel = new JPanel(new BorderLayout()); emptyPanel.setOpaque(false);
            JLabel e1 = UI.label("🌱 Start building habits!", new Font("Segoe UI", Font.BOLD, 28), Theme.NEON_GREEN);
            e1.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel e2 = UI.label("Click '+ New Habit' to add your first habit", Theme.FONT_BODY, Theme.TEXT_MUTED);
            e2.setHorizontalAlignment(SwingConstants.CENTER);
            emptyPanel.add(e1, BorderLayout.CENTER); emptyPanel.add(e2, BorderLayout.SOUTH);
            habitsGrid.add(emptyPanel);
        } else {
            for (Habit h : habits) {
                habitsGrid.add(makeHabitCard(h));
                habitsGrid.add(Box.createVerticalStrut(10));
            }
        }
        habitsGrid.revalidate(); habitsGrid.repaint();
    }

    @Override public void onDataChanged(String event, Object data) { refresh(); }

    // ── Habit card ────────────────────────────────────────────────────────────

    private JPanel makeHabitCard(Habit h) {
        boolean done = h.isCompletedToday();
        Color accent = categoryColor(h.getCategory());

        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = done ? Theme.withAlpha(accent, 18) : Theme.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // Left stripe
                g2.setColor(done ? accent : Theme.withAlpha(accent, 80));
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 5, 5));
                // Border
                g2.setColor(done ? Theme.withAlpha(accent, 100) : Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left: icon + name
        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setOpaque(false);
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); nameRow.setOpaque(false);
        JLabel iconLbl = new JLabel(h.getIcon()); iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        JLabel nameLbl = UI.label(h.getName(), Theme.FONT_HEADING,
                done ? accent : Theme.TEXT_PRIMARY);
        if (done) nameLbl.setText("<html><strike>" + h.getName() + "</strike></html>");
        nameRow.add(iconLbl); nameRow.add(nameLbl);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2)); metaRow.setOpaque(false);
        metaRow.add(UI.badge(h.getCategory().toString(), accent));
        metaRow.add(UI.badge(h.getFrequency().toString(), Theme.TEXT_MUTED));
        metaRow.add(UI.label("Target: " + h.getTargetCount() + " " + h.getUnit(), Theme.FONT_SMALL, Theme.TEXT_MUTED));

        left.add(nameRow); left.add(metaRow);

        // Center: mini streak heatmap (last 14 days)
        JPanel heatmap = makeHeatmap(h);

        // Right: stats + toggle button
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS)); right.setOpaque(false);
        right.setPreferredSize(new Dimension(130, 0));

        JLabel streakLbl = UI.label("🔥 " + h.getCurrentStreak() + " day streak",
                Theme.FONT_SMALL, h.getCurrentStreak() > 0 ? Theme.NEON_ORANGE : Theme.TEXT_MUTED);
        JLabel totalLbl  = UI.label("✓ " + h.getTotalCompletions() + " total",
                Theme.FONT_SMALL, Theme.TEXT_SECONDARY);
        JLabel rateLbl   = UI.label(String.format("%.0f%% (30d)", h.getCompletionRate30Days()),
                Theme.FONT_SMALL, Theme.TEXT_MUTED);

        JButton toggleBtn = UI.button(done ? "✓ Done" : "Mark Done", done ? accent : Theme.BG_ELEVATED);
        toggleBtn.setForeground(done ? Theme.TEXT_INVERSE : accent);
        toggleBtn.setPreferredSize(new Dimension(110, 34)); toggleBtn.setMaximumSize(new Dimension(110, 34));
        toggleBtn.addActionListener(e -> {
            cmdMgr.execute(CommandManager.toggleHabit(repo, h.getId()));
        });

        // Delete button (admin only)
        if (user.isAdmin()) {
            JButton del = UI.button("✕", Theme.BG_ELEVATED); del.setForeground(Theme.NEON_PINK);
            del.setPreferredSize(new Dimension(34, 34)); del.setMaximumSize(new Dimension(34, 34));
            del.addActionListener(e -> {
                int ans = JOptionPane.showConfirmDialog(this,
                        "Remove habit \"" + h.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.YES_OPTION) repo.removeHabit(h.getId());
            });
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0)); btnRow.setOpaque(false);
            btnRow.add(toggleBtn); btnRow.add(del);
            right.add(streakLbl); right.add(Box.createVerticalStrut(3));
            right.add(totalLbl);  right.add(Box.createVerticalStrut(2));
            right.add(rateLbl);   right.add(Box.createVerticalStrut(6));
            right.add(btnRow);
        } else {
            right.add(streakLbl); right.add(Box.createVerticalStrut(3));
            right.add(totalLbl);  right.add(Box.createVerticalStrut(2));
            right.add(rateLbl);   right.add(Box.createVerticalStrut(6));
            right.add(toggleBtn);
        }

        card.add(left,    BorderLayout.CENTER);
        card.add(heatmap, BorderLayout.WEST);
        card.add(right,   BorderLayout.EAST);

        // Hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { card.repaint(); }
        });

        return card;
    }

    // ── Mini heatmap (14-day grid) ────────────────────────────────────────────

    private JPanel makeHeatmap(Habit h) {
        JPanel hm = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color accent = categoryColor(h.getCategory());
                int cellSize = 14, gap = 3;
                int cols = 7, rows = 2;
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        int dayOffset = (rows - 1 - row) * cols + (cols - 1 - col);
                        LocalDate d = LocalDate.now().minusDays(dayOffset);
                        boolean done = h.isCompletedOn(d);
                        int x = col * (cellSize + gap);
                        int y = row * (cellSize + gap);
                        g2.setColor(done ? accent : Theme.BG_ELEVATED);
                        g2.fill(new RoundRectangle2D.Float(x, y, cellSize, cellSize, 4, 4));
                        if (done) {
                            // glow
                            g2.setColor(Theme.withAlpha(accent, 60));
                            g2.setStroke(new BasicStroke(1f));
                            g2.draw(new RoundRectangle2D.Float(x-1, y-1, cellSize+2, cellSize+2, 4, 4));
                        }
                    }
                }
                g2.dispose();
            }
        };
        hm.setOpaque(false);
        hm.setPreferredSize(new Dimension(7 * (14+3), 2 * (14+3)));
        return hm;
    }

    // ── Date pill ─────────────────────────────────────────────────────────────

    private JPanel makeDatePill(LocalDate date, boolean isToday) {
        JPanel pill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isToday) {
                    g2.setColor(Theme.withAlpha(Theme.NEON_GREEN, 30));
                    g2.fill(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()-4, 10, 10));
                    g2.setColor(Theme.NEON_GREEN);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()-4, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setLayout(new BoxLayout(pill, BoxLayout.Y_AXIS));
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(46, 52));
        pill.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel dayName = UI.label(date.format(DateTimeFormatter.ofPattern("EEE")),
                Theme.FONT_SMALL, isToday ? Theme.NEON_GREEN : Theme.TEXT_MUTED);
        dayName.setAlignmentX(Component.CENTER_ALIGNMENT);
        dayName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel dayNum  = UI.label(String.valueOf(date.getDayOfMonth()),
                new Font("Segoe UI", Font.BOLD, 16), isToday ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        dayNum.setAlignmentX(Component.CENTER_ALIGNMENT);
        dayNum.setHorizontalAlignment(SwingConstants.CENTER);

        pill.add(dayName); pill.add(dayNum);
        return pill;
    }

    // ── Add habit dialog ──────────────────────────────────────────────────────

    private void showAddHabitDialog() {
        JDialog dlg = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "New Habit", true);
        dlg.setSize(460, 500); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Theme.BG_DARK); dlg.setContentPane(root);

        JPanel titleBar = new JPanel(new BorderLayout()); titleBar.setBackground(Theme.BG_SIDEBAR);
        titleBar.setBorder(new EmptyBorder(16,20,16,20));
        titleBar.add(UI.label("Create New Habit", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        root.add(titleBar, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Theme.BG_DARK);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; gc.gridx = 0; gc.insets = new Insets(5, 0, 5, 0);

        int y = 0;
        gc.gridy = y++; form.add(lbl("Habit Name *"), gc);
        gc.gridy = y++; JTextField nameF = UI.textField(""); nameF.setPreferredSize(new Dimension(0,40)); form.add(nameF, gc);

        gc.gridy = y++;
        String[] icons = {"💧","🏃","💤","🥗","🧘","💪","📚","🧠","🚶","🍎","☀","🌙"};
        JPanel row1 = twoCol(lbl("Icon (emoji)"), new JComboBox<>(icons),
                             lbl("Target"), makeTwoField("8", "glasses"));
        form.add(row1, gc);

        JComboBox<String> iconBox = (JComboBox<String>)((JPanel)row1.getComponent(2)).getComponent(0);

        JTextField targetF = (JTextField)((JPanel)((JPanel)row1.getComponent(3)).getComponent(0)).getComponent(0);
        JTextField unitF   = (JTextField)((JPanel)((JPanel)row1.getComponent(3)).getComponent(0)).getComponent(1);

        gc.gridy = y++;
        JPanel row2 = twoCol(lbl("Category"), new JComboBox<>(Habit.Category.values()),
                             lbl("Frequency"), new JComboBox<>(Habit.Frequency.values()));
        form.add(row2, gc);
        JComboBox<Habit.Category>  catBox  = (JComboBox<Habit.Category>)  ((JPanel)row2.getComponent(2)).getComponent(0);
        JComboBox<Habit.Frequency> freqBox = (JComboBox<Habit.Frequency>) ((JPanel)row2.getComponent(3)).getComponent(0);

        gc.gridy = y++; form.add(lbl("Description"), gc);
        gc.gridy = y++; JTextField descF = UI.textField("Optional description"); descF.setPreferredSize(new Dimension(0,40)); form.add(descF, gc);

        root.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btns.setBackground(Theme.BG_SIDEBAR);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        JButton cancel = UI.button("Cancel", Theme.BG_ELEVATED);
        JButton save   = UI.button("Create Habit", Theme.NEON_PURPLE);
        save.setPreferredSize(new Dimension(130, 40)); cancel.setPreferredSize(new Dimension(90, 40));
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) { nameF.setBorder(BorderFactory.createLineBorder(Theme.NEON_PINK)); return; }
            String icon = (String) iconBox.getSelectedItem();
            int target  = 1;
            try { target = Integer.parseInt(targetF.getText().trim()); } catch (Exception ignored) {}
            cmdMgr.execute(CommandManager.addHabit(repo, user.getUsername(),
                    nameF.getText().trim(), descF.getText().trim(),
                    (Habit.Frequency) freqBox.getSelectedItem(),
                    (Habit.Category)  catBox.getSelectedItem(),
                    icon, target, unitF.getText().trim()));
            dlg.dispose();
        });
        btns.add(cancel); btns.add(save);
        root.add(btns, BorderLayout.SOUTH);

        // Style combos
        for (JComboBox<?> cb : new JComboBox[]{iconBox, catBox, freqBox}) {
            cb.setBackground(Theme.BG_ELEVATED); cb.setForeground(Theme.TEXT_PRIMARY);
            cb.setFont(Theme.FONT_BODY); cb.setPreferredSize(new Dimension(0, 38));
        }
        dlg.setVisible(true);
    }

    private Color categoryColor(Habit.Category cat) {
        return switch (cat) {
            case HYDRATION   -> Theme.NEON_BLUE;
            case SLEEP       -> Theme.NEON_PURPLE;
            case NUTRITION   -> Theme.NEON_GREEN;
            case EXERCISE    -> Theme.NEON_ORANGE;
            case MINDFULNESS -> Theme.NEON_CYAN;
            default          -> Theme.NEON_YELLOW;
        };
    }

    private JLabel lbl(String t) {
        JLabel l = UI.label(t, Theme.FONT_LABEL, Theme.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(2,0,2,0)); return l;
    }

    private JPanel twoCol(JLabel l1, JComponent c1, JLabel l2, JComponent c2) {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 4)); p.setOpaque(false);
        if (c1.getPreferredSize().height < 38) c1.setPreferredSize(new Dimension(0, 38));
        if (c2.getPreferredSize().height < 38) c2.setPreferredSize(new Dimension(0, 38));
        p.add(l1); p.add(l2); p.add(c1); p.add(c2); return p;
    }

    private JPanel makeTwoField(String val1, String val2) {
        JPanel p = new JPanel(new GridLayout(1, 1)); p.setOpaque(false);
        JPanel inner = new JPanel(new GridLayout(1, 2, 6, 0)); inner.setOpaque(false);
        JTextField f1 = UI.textField(val1); f1.setPreferredSize(new Dimension(0, 38));
        JTextField f2 = UI.textField(val2); f2.setPreferredSize(new Dimension(0, 38));
        inner.add(f1); inner.add(f2);
        p.add(inner); return p;
    }
}
