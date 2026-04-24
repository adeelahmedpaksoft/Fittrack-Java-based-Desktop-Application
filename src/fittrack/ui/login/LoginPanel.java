package fittrack.ui.login;

import fittrack.auth.AuthService;
import fittrack.model.User;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final Consumer<User> onSuccess;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;

    public LoginPanel(Consumer<User> onSuccess) {
        this.onSuccess = onSuccess;
        setBackground(Theme.BG_DEEPEST);
        setLayout(new GridBagLayout());
        buildUI();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // Diagonal energy lines
        g2.setColor(new Color(255,255,255, 5));
        g2.setStroke(new BasicStroke(1));
        for (int i = -getHeight(); i < getWidth()+getHeight(); i += 60)
            g2.drawLine(i, 0, i + getHeight(), getHeight());
        // Neon glow circles
        RadialGradientPaint r1 = new RadialGradientPaint(new Point(getWidth()/4, getHeight()/2), getHeight()*0.5f,
                new float[]{0f,1f}, new Color[]{new Color(57,255,130,30), new Color(0,0,0,0)});
        g2.setPaint(r1); g2.fillRect(0,0,getWidth(),getHeight());
        RadialGradientPaint r2 = new RadialGradientPaint(new Point(getWidth()*3/4, getHeight()/3), getHeight()*0.4f,
                new float[]{0f,1f}, new Color[]{new Color(255,107,53,20), new Color(0,0,0,0)});
        g2.setPaint(r2); g2.fillRect(0,0,getWidth(),getHeight());
        g2.dispose();
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(18,24,38), getWidth(),getHeight(),new Color(10,14,22));
                g2.setPaint(gp); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.setColor(new Color(57,255,130, 50)); g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,20,20));
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setPreferredSize(new Dimension(440, 580));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 0, 8, 0); gc.fill = GridBagConstraints.HORIZONTAL; gc.gridx = 0; gc.weightx = 1;

        // Logo
        gc.gridy = 0; gc.insets = new Insets(0,0,4,0);
        JLabel logo = new JLabel("⚡", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 52));
        logo.setForeground(Theme.NEON_GREEN);
        card.add(logo, gc);

        gc.gridy = 1; gc.insets = new Insets(0,0,4,0);
        JLabel title = new JLabel("FitTrack Pro", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Theme.TEXT_PRIMARY);
        card.add(title, gc);

        gc.gridy = 2; gc.insets = new Insets(0,0,28,0);
        JLabel sub = new JLabel("Track. Train. Transform.", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(Theme.NEON_GREEN);
        card.add(sub, gc);

        // Quick login buttons
        gc.gridy = 3; gc.insets = new Insets(0,0,14,0);
        JPanel quick = new JPanel(new GridLayout(1, 2, 10, 0));
        quick.setOpaque(false);
        quick.add(quickBtn("👑 Admin",   "admin",  "admin123", Theme.NEON_ORANGE));
        quick.add(quickBtn("🏃 Athlete", "alex",   "fit123",   Theme.NEON_GREEN));
        card.add(quick, gc);

        // Divider
        gc.gridy = 4; gc.insets = new Insets(0,0,20,0);
        JPanel div = new JPanel(new BorderLayout(10,0)); div.setOpaque(false);
        JSeparator s1 = UI.sep(), s2 = UI.sep();
        JLabel or = UI.label("or sign in manually", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        or.setHorizontalAlignment(SwingConstants.CENTER);
        s1.setPreferredSize(new Dimension(80,1)); s2.setPreferredSize(new Dimension(80,1));
        div.add(s1, BorderLayout.WEST); div.add(or, BorderLayout.CENTER); div.add(s2, BorderLayout.EAST);
        card.add(div, gc);

        gc.gridy = 5; gc.insets = new Insets(0,0,4,0);
        card.add(fieldLabel("Username"), gc);
        gc.gridy = 6; gc.insets = new Insets(0,0,14,0);
        usernameField = UI.textField("");
        usernameField.setPreferredSize(new Dimension(0, 44));
        card.add(usernameField, gc);

        gc.gridy = 7; gc.insets = new Insets(0,0,4,0);
        card.add(fieldLabel("Password"), gc);
        gc.gridy = 8; gc.insets = new Insets(0,0,6,0);
        passwordField = UI.passwordField();
        passwordField.setPreferredSize(new Dimension(0, 44));
        card.add(passwordField, gc);

        gc.gridy = 9; gc.insets = new Insets(0,0,16,0);
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL); errorLabel.setForeground(Theme.NEON_PINK);
        card.add(errorLabel, gc);

        gc.gridy = 10; gc.insets = new Insets(0,0,16,0);
        JButton loginBtn = UI.button("START TRAINING  →", Theme.NEON_GREEN);
        loginBtn.setForeground(Theme.TEXT_INVERSE);
        loginBtn.setPreferredSize(new Dimension(0, 48));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(loginBtn, gc);

        gc.gridy = 11;
        JLabel hint = new JLabel("admin/admin123 | alex/fit123 | sam/gym456 | morgan/yoga789", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(Theme.TEXT_MUTED);
        card.add(hint, gc);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        add(card);
    }

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        if (u.isEmpty()||p.isEmpty()) { errorLabel.setText("Enter username and password."); return; }
        User user = AuthService.getInstance().login(u, p);
        if (user != null) onSuccess.accept(user);
        else { errorLabel.setText("Invalid credentials. Try again."); passwordField.setText(""); }
    }

    private JButton quickBtn(String label, String user, String pass, Color col) {
        JButton b = UI.button(label, Theme.BG_ELEVATED);
        b.setForeground(col); b.setPreferredSize(new Dimension(0, 42));
        b.addActionListener(e -> { usernameField.setText(user); passwordField.setText(pass); doLogin(); });
        return b;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = UI.label(text, Theme.FONT_LABEL, Theme.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(0,0,2,0)); return l;
    }
}
