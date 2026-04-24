package fittrack.ui.workout;

import fittrack.command.CommandManager;
import fittrack.model.*;
import fittrack.observer.FitnessObserver;
import fittrack.strategy.*;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class WorkoutPanel extends JPanel implements FitnessObserver {

    private final FitnessRepository repo;
    private final CommandManager    cmdMgr;
    private final User              user;
    private final WorkoutStrategy[] strategies = WorkoutStrategies.all();

    private WorkoutTableModel tableModel = new WorkoutTableModel();
    private JTable table = new JTable(tableModel);
    private JComboBox<WorkoutStrategy> filterBox;
    private JLabel countLabel, totalCalLabel;

    public WorkoutPanel(FitnessRepository repo, CommandManager cmdMgr, User user) {
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
        titleRow.add(UI.label("Workout Log", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        JButton addBtn = UI.button("🏋  Log Workout", Theme.NEON_GREEN);
        addBtn.setForeground(Theme.TEXT_INVERSE);
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> showLogDialog());
        titleRow.add(addBtn, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false); filterBar.setBorder(new EmptyBorder(12,0,0,0));

        filterBox = new JComboBox<>(strategies);
        filterBox.setRenderer((list, val, idx, sel, focus) -> {
            JLabel l = new JLabel(val != null ? val.getIcon() + "  " + val.getName() : "");
            l.setFont(Theme.FONT_BODY); l.setForeground(sel ? Theme.TEXT_INVERSE : Theme.TEXT_PRIMARY);
            l.setBackground(sel ? Theme.NEON_GREEN : Theme.BG_ELEVATED); l.setOpaque(true);
            l.setBorder(new EmptyBorder(4,10,4,10)); return l;
        });
        filterBox.setPreferredSize(new Dimension(170, 36));
        filterBox.addActionListener(e -> refresh());

        countLabel   = UI.label("", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        totalCalLabel = UI.label("", Theme.FONT_SMALL, Theme.NEON_ORANGE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        right.add(totalCalLabel); right.add(countLabel); right.add(filterBox);

        filterBar.add(right, BorderLayout.EAST);
        header.add(filterBar, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table
        styleTable();
        JScrollPane scroll = UI.scroll(table);
        scroll.setBorder(new EmptyBorder(0,20,0,20));
        add(scroll, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        bottom.setBackground(Theme.BG_DARK);
        bottom.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER));
        JButton editBtn   = UI.button("✎ Edit",   Theme.BG_ELEVATED);
        JButton delBtn    = UI.button("✕ Delete", Theme.BG_ELEVATED);
        JButton undoBtn   = UI.button("↩ Undo",   Theme.BG_ELEVATED);
        JButton redoBtn   = UI.button("↪ Redo",   Theme.BG_ELEVATED);
        delBtn.setForeground(Theme.NEON_PINK);
        editBtn.addActionListener(e  -> editSelected());
        delBtn.addActionListener(e   -> deleteSelected());
        undoBtn.addActionListener(e  -> { cmdMgr.undo(); refresh(); });
        redoBtn.addActionListener(e  -> { cmdMgr.redo(); refresh(); });
        if (!user.isAdmin()) delBtn.setEnabled(false);
        bottom.add(editBtn); bottom.add(delBtn);
        bottom.add(Box.createHorizontalStrut(12)); bottom.add(undoBtn); bottom.add(redoBtn);
        add(bottom, BorderLayout.SOUTH);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount()==2) editSelected();
            }
        });
    }

    private void styleTable() {
        table.setFont(Theme.FONT_BODY); table.setForeground(Theme.TEXT_PRIMARY);
        table.setBackground(Theme.BG_DARK); table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY); table.setGridColor(Theme.BORDER);
        table.setRowHeight(34); table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(10,0)); table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        JTableHeader th = table.getTableHeader();
        th.setBackground(Theme.BG_SIDEBAR); th.setForeground(Theme.TEXT_MUTED);
        th.setFont(Theme.FONT_LABEL); th.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER));

        int[] widths = {40,100,80,70,80,80,80,80,60};
        for (int i=0; i<widths.length && i<table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Type renderer with colour
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                try {
                    WorkoutSession.WorkoutType type = WorkoutSession.WorkoutType.valueOf(val.toString());
                    JLabel l = UI.badge(val.toString(), Theme.workoutColor(type));
                    l.setBackground(sel ? Theme.BG_HOVER : Theme.BG_DARK); l.setOpaque(true);
                    return l;
                } catch (Exception ex) { return super.getTableCellRendererComponent(t,val,sel,foc,r,c); }
            }
        });
        // Intensity renderer
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                try {
                    double score = Double.parseDouble(val.toString());
                    JLabel l = UI.badge(String.format("%.1f", score), Theme.intensityColor(score));
                    l.setBackground(sel ? Theme.BG_HOVER : Theme.BG_DARK); l.setOpaque(true);
                    return l;
                } catch (Exception ex) { return super.getTableCellRendererComponent(t,val,sel,foc,r,c); }
            }
        });
    }

    public void refresh() {
        WorkoutStrategy strat = (WorkoutStrategy) filterBox.getSelectedItem();
        List<WorkoutSession> all      = repo.getAllWorkouts(user.getUsername());
        List<WorkoutSession> filtered = strat != null ? strat.apply(all) : all;
        tableModel.setWorkouts(filtered);
        int totalCal = filtered.stream().mapToInt(WorkoutSession::getCaloriesBurned).sum();
        countLabel.setText(filtered.size() + " sessions  ");
        totalCalLabel.setText("🔥 " + totalCal + " kcal total  ");
    }

    @Override public void onDataChanged(String event, Object data) { refresh(); }

    private void showLogDialog() {
        WorkoutFormDialog dlg = new WorkoutFormDialog((JFrame)SwingUtilities.getWindowAncestor(this), null, user);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            cmdMgr.execute(CommandManager.logWorkout(repo, user.getUsername(),
                    dlg.getWorkoutType(), dlg.getDate(), dlg.getDuration(), dlg.getDistance(),
                    dlg.getCalories(), dlg.getHeartRate(), dlg.getIntensity(), dlg.getNotes()));
        }
    }

    private void editSelected() {
        WorkoutSession w = selectedWorkout();
        if (w == null) { noSel(); return; }
        WorkoutFormDialog dlg = new WorkoutFormDialog((JFrame)SwingUtilities.getWindowAncestor(this), w, user);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            w.setType(dlg.getWorkoutType()); w.setDate(dlg.getDate());
            w.setDurationMinutes(dlg.getDuration()); w.setDistanceKm(dlg.getDistance());
            w.setCaloriesBurned(dlg.getCalories()); w.setHeartRateAvg(dlg.getHeartRate());
            w.setIntensityScore(dlg.getIntensity()); w.setNotes(dlg.getNotes());
            repo.updateWorkout(w);
        }
    }

    private void deleteSelected() {
        WorkoutSession w = selectedWorkout();
        if (w == null) { noSel(); return; }
        int ans = JOptionPane.showConfirmDialog(this, "Delete this workout session?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) cmdMgr.execute(CommandManager.removeWorkout(repo, w.getId()));
    }

    private WorkoutSession selectedWorkout() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getAt(table.convertRowIndexToModel(row));
    }

    private void noSel() {
        JOptionPane.showMessageDialog(this, "Please select a workout.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Table model ───────────────────────────────────────────────────────────
    static class WorkoutTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#","Type","Date","Duration","Distance","Calories","Heart Rate","Intensity","Notes"};
        private java.util.List<WorkoutSession> data = new java.util.ArrayList<>();
        void setWorkouts(java.util.List<WorkoutSession> d) { this.data = new java.util.ArrayList<>(d); fireTableDataChanged(); }
        WorkoutSession getAt(int r) { return r>=0&&r<data.size()?data.get(r):null; }
        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return COLS.length; }
        public String getColumnName(int c) { return COLS[c]; }
        public Object getValueAt(int r, int c) {
            WorkoutSession w = data.get(r);
            return switch(c) {
                case 0 -> w.getId();
                case 1 -> w.getType().toString();
                case 2 -> w.getDate().toString();
                case 3 -> w.getDurationMinutes() + " min";
                case 4 -> w.getDistanceKm() > 0 ? String.format("%.1f km", w.getDistanceKm()) : "—";
                case 5 -> w.getCaloriesBurned() + " kcal";
                case 6 -> w.getHeartRateAvg() > 0 ? w.getHeartRateAvg() + " bpm" : "—";
                case 7 -> w.getIntensityScore();
                case 8 -> w.getNotes() != null ? w.getNotes() : "";
                default -> "";
            };
        }
    }
}
