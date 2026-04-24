package fittrack.ui.settings;

import fittrack.auth.AuthService;
import fittrack.model.User;
import fittrack.singleton.AppConfig;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final User user;

    public SettingsPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout()); setBackground(Theme.BG_DARK);
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK); header.setBorder(new EmptyBorder(24,28,14,28));
        header.add(UI.label("Settings", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_DARK); content.setBorder(new EmptyBorder(0,20,20,20));

        // ── Profile card ──────────────────────────────────────────────────
        JPanel profile = UI.card(new GridBagLayout());
        profile.setBorder(new EmptyBorder(20,20,20,20));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx=1; gc.gridx=0; gc.insets=new Insets(6,0,6,0);

        gc.gridy=0; gc.gridwidth=2;
        profile.add(UI.label("Your Profile", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), gc);
        gc.gridwidth=1;

        gc.gridy=1; gc.gridx=0; gc.weightx=0;
        profile.add(UI.label("Display Name:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        profile.add(UI.label(user.getDisplayName(), Theme.FONT_BODY, Theme.TEXT_PRIMARY), gc);

        gc.gridy=2; gc.gridx=0; gc.weightx=0;
        profile.add(UI.label("Role:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        Color roleColor = user.isAdmin() ? Theme.NEON_ORANGE : Theme.NEON_PURPLE;
        profile.add(UI.badge(user.getRole().toString(), roleColor), gc);

        gc.gridy=3; gc.gridx=0; gc.weightx=0;
        profile.add(UI.label("Fitness Goal:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        profile.add(UI.badge(user.getFitnessGoal(), Theme.NEON_GREEN), gc);

        gc.gridy=4; gc.gridx=0; gc.weightx=0;
        profile.add(UI.label("BMI:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        profile.add(UI.label(String.format("%.1f (%.0f kg, %.0f cm)", user.getBmi(), user.getWeightKg(), user.getHeightCm()),
                Theme.FONT_BODY, Theme.TEXT_PRIMARY), gc);

        content.add(profile); content.add(Box.createVerticalStrut(14));

        // ── Goals card ────────────────────────────────────────────────────
        AppConfig cfg = AppConfig.getInstance();
        JPanel goals = UI.card(new GridBagLayout());
        goals.setBorder(new EmptyBorder(20,20,20,20));
        goals.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        gc = new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; gc.gridx=0; gc.insets=new Insets(6,0,6,0);

        gc.gridy=0; gc.gridwidth=2;
        goals.add(UI.label("Fitness Goals", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), gc);
        gc.gridwidth=1;

        gc.gridy=1; gc.gridx=0; gc.weightx=0;
        goals.add(UI.label("Weekly Calorie Burn Goal:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JTextField calGoalField = UI.textField(String.valueOf(cfg.getWeeklyCalGoal()));
        calGoalField.setPreferredSize(new Dimension(0,36)); goals.add(calGoalField, gc);

        gc.gridy=2; gc.gridx=0; gc.weightx=0;
        goals.add(UI.label("Monthly Workout Sessions Goal:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JTextField wkGoalField = UI.textField(String.valueOf(cfg.getMonthlyWorkoutGoal()));
        wkGoalField.setPreferredSize(new Dimension(0,36)); goals.add(wkGoalField, gc);

        gc.gridy=3; gc.gridx=0; gc.weightx=0; gc.gridwidth=2;
        JPanel saveBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT)); saveBtnWrap.setOpaque(false);
        JButton saveBtn = UI.button("Save Goals", Theme.NEON_GREEN);
        saveBtn.setForeground(Theme.TEXT_INVERSE); saveBtn.setPreferredSize(new Dimension(130,38));
        saveBtn.addActionListener(e -> {
            try {
                cfg.setWeeklyCalGoal(Integer.parseInt(calGoalField.getText().trim()));
                cfg.setMonthlyWorkoutGoal(Integer.parseInt(wkGoalField.getText().trim()));
                JOptionPane.showMessageDialog(this, "Goals saved!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        saveBtnWrap.add(saveBtn); goals.add(saveBtnWrap, gc);
        content.add(goals); content.add(Box.createVerticalStrut(14));

        // ── Users card (admin only) ───────────────────────────────────────
        if (user.isAdmin()) {
            JPanel usersCard = UI.card(new BorderLayout(0,12));
            usersCard.setBorder(new EmptyBorder(20,20,20,20));
            usersCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            usersCard.add(UI.label("All Users", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
            JPanel usersGrid = new JPanel(new GridLayout(0,1,0,8)); usersGrid.setOpaque(false);
            for (User u : AuthService.getInstance().getAllUsers()) {
                JPanel row = new JPanel(new BorderLayout(12,0));
                row.setBackground(Theme.BG_ELEVATED); row.setOpaque(true);
                row.setBorder(new EmptyBorder(10,12,10,12));
                // Avatar
                JPanel av = makeAvatar(u);
                JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setOpaque(false);
                info.add(UI.label(u.getDisplayName(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY));
                info.add(UI.label("@" + u.getUsername() + " · " + u.getFitnessGoal(), Theme.FONT_SMALL, Theme.TEXT_MUTED));
                Color rc = u.isAdmin() ? Theme.NEON_ORANGE : Theme.NEON_PURPLE;
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); right.setOpaque(false);
                right.add(UI.badge(u.getRole().toString(), rc));
                right.add(UI.label(String.format("BMI %.1f", u.getBmi()), Theme.FONT_SMALL, Theme.TEXT_MUTED));
                row.add(av, BorderLayout.WEST); row.add(info, BorderLayout.CENTER); row.add(right, BorderLayout.EAST);
                usersGrid.add(row);
            }
            usersCard.add(usersGrid, BorderLayout.CENTER);
            content.add(usersCard); content.add(Box.createVerticalStrut(14));
        }

        // ── About card ────────────────────────────────────────────────────
        JPanel about = UI.card(new GridBagLayout());
        about.setBorder(new EmptyBorder(16,20,16,20));
        about.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        gc = new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; gc.gridx=0; gc.insets=new Insets(3,0,3,0);
        gc.gridy=0; about.add(UI.label("About FitTrack Pro", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), gc);
        gc.gridy=1; about.add(UI.label("SET11103 Coursework · Fitness, Habit & Activity Tracker", Theme.FONT_BODY, Theme.TEXT_SECONDARY), gc);
        gc.gridy=2; about.add(UI.label("Version " + cfg.getVersion() + " · Patterns: Singleton · Observer · Strategy · Command",
                Theme.FONT_SMALL, Theme.TEXT_MUTED), gc);
        content.add(about);

        add(UI.scroll(content), BorderLayout.CENTER);
    }

    private JPanel makeAvatar(User u) {
        Color c = u.isAdmin() ? Theme.NEON_ORANGE : Theme.NEON_PURPLE;
        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.withAlpha(c, 35)); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(c); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                FontMetrics fm=g2.getFontMetrics(); String init=u.getAvatarInitials();
                g2.drawString(init,(getWidth()-fm.stringWidth(init))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        av.setPreferredSize(new Dimension(36,36)); av.setOpaque(false); return av;
    }
}
