package fittrack.ui.workout;

import fittrack.model.*;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class WorkoutFormDialog extends JDialog {

    private JComboBox<WorkoutSession.WorkoutType> typeBox;
    private JTextField dateField, durationField, distField, calField, hrField, notesField;
    private JSlider intensitySlider;
    private JLabel  intensityLabel;
    private boolean confirmed = false;

    public WorkoutFormDialog(JFrame parent, WorkoutSession edit, User user) {
        super(parent, edit == null ? "Log New Workout" : "Edit Workout", true);
        setSize(480, 560); setLocationRelativeTo(parent); setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK); setContentPane(root);

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.BG_SIDEBAR); titleBar.setBorder(new EmptyBorder(16,20,16,20));
        titleBar.add(UI.label(edit==null?"Log New Workout":"Edit Workout Session", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        root.add(titleBar, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_DARK); form.setBorder(new EmptyBorder(20,24,16,24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; gc.gridx = 0; gc.insets = new Insets(4,0,4,0);

        int y=0;
        gc.gridy=y++; form.add(lbl("Workout Type"), gc);
        gc.gridy=y++; typeBox = UI.combo(WorkoutSession.WorkoutType.values());
        typeBox.setPreferredSize(new Dimension(0,40)); form.add(typeBox, gc);

        gc.gridy=y++;
        JPanel row1 = twoCol(lbl("Date (YYYY-MM-DD)"), dateField=UI.textField(LocalDate.now().toString()),
                             lbl("Duration (minutes)"), durationField=UI.textField("30"));
        form.add(row1, gc);

        gc.gridy=y++;
        JPanel row2 = twoCol(lbl("Distance (km, 0 if N/A)"), distField=UI.textField("0"),
                             lbl("Calories Burned"), calField=UI.textField("300"));
        form.add(row2, gc);

        gc.gridy=y++;
        JPanel row3 = twoCol(lbl("Avg Heart Rate (bpm)"), hrField=UI.textField("140"),
                             lbl("Notes"), notesField=UI.textField("Optional notes"));
        form.add(row3, gc);

        gc.gridy=y++;
        JPanel intHeader = new JPanel(new BorderLayout()); intHeader.setOpaque(false);
        intHeader.add(lbl("Intensity (1-10)"), BorderLayout.WEST);
        intensityLabel = UI.label("5", Theme.FONT_HEADING, Theme.NEON_ORANGE);
        intHeader.add(intensityLabel, BorderLayout.EAST);
        form.add(intHeader, gc);

        gc.gridy=y++;
        intensitySlider = new JSlider(1,10,5);
        intensitySlider.setBackground(Theme.BG_DARK); intensitySlider.setForeground(Theme.NEON_ORANGE);
        intensitySlider.setMajorTickSpacing(1); intensitySlider.setPaintTicks(true); intensitySlider.setPaintLabels(true);
        intensitySlider.setFont(Theme.FONT_SMALL);
        intensitySlider.addChangeListener(e -> {
            intensityLabel.setText(String.valueOf(intensitySlider.getValue()));
            intensityLabel.setForeground(Theme.intensityColor(intensitySlider.getValue()));
        });
        form.add(intensitySlider, gc);

        if (edit != null) populate(edit);
        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14));
        btns.setBackground(Theme.BG_SIDEBAR);
        btns.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER));
        JButton cancel = UI.button("Cancel", Theme.BG_ELEVATED);
        JButton save   = UI.button(edit==null?"Log Workout":"Save Changes", Theme.NEON_GREEN);
        save.setForeground(Theme.TEXT_INVERSE);
        save.setPreferredSize(new Dimension(140,40)); cancel.setPreferredSize(new Dimension(90,40));
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> { confirmed=true; dispose(); });
        btns.add(cancel); btns.add(save);
        root.add(btns, BorderLayout.SOUTH);
    }

    private void populate(WorkoutSession w) {
        typeBox.setSelectedItem(w.getType());
        dateField.setText(w.getDate().toString());
        durationField.setText(String.valueOf(w.getDurationMinutes()));
        distField.setText(String.format("%.1f", w.getDistanceKm()));
        calField.setText(String.valueOf(w.getCaloriesBurned()));
        hrField.setText(String.valueOf(w.getHeartRateAvg()));
        notesField.setText(w.getNotes() != null ? w.getNotes() : "");
        intensitySlider.setValue((int)w.getIntensityScore());
    }

    public boolean isConfirmed()   { return confirmed; }
    public WorkoutSession.WorkoutType getWorkoutType() { return (WorkoutSession.WorkoutType) typeBox.getSelectedItem(); }
    public int    getDuration()    { try { return Integer.parseInt(durationField.getText().trim()); } catch(Exception e){return 30;} }
    public double getDistance()    { try { return Double.parseDouble(distField.getText().trim()); } catch(Exception e){return 0;} }
    public int    getCalories()    { try { return Integer.parseInt(calField.getText().trim()); } catch(Exception e){return 0;} }
    public int    getHeartRate()   { try { return Integer.parseInt(hrField.getText().trim()); } catch(Exception e){return 0;} }
    public double getIntensity()   { return intensitySlider.getValue(); }
    public String getNotes()       { return notesField.getText().trim(); }
    public LocalDate getDate() {
        try { return LocalDate.parse(dateField.getText().trim()); }
        catch (DateTimeParseException e) { return LocalDate.now(); }
    }

    private JLabel lbl(String t) {
        JLabel l = UI.label(t, Theme.FONT_LABEL, Theme.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(2,0,2,0)); return l;
    }
    private JPanel twoCol(JLabel l1, JComponent c1, JLabel l2, JComponent c2) {
        JPanel p = new JPanel(new GridLayout(2,2,10,4)); p.setOpaque(false);
        c1.setPreferredSize(new Dimension(0,38)); c2.setPreferredSize(new Dimension(0,38));
        p.add(l1); p.add(l2); p.add(c1); p.add(c2); return p;
    }
}
