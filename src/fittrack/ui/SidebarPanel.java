package fittrack.ui;

import fittrack.model.User;
import fittrack.ui.components.Theme;
import fittrack.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {

    public enum Page { DASHBOARD, WORKOUTS, HABITS, PROGRESS, SETTINGS }

    private final Consumer<Page> onNavigate;
    private final User           user;
    private final Runnable       onLogout;

    private Page   active = Page.DASHBOARD;
    private JPanel navBox = new JPanel();

    record NavItem(String icon, String label, Page page) {}

    private static final NavItem[] NAV = {
        new NavItem("⊞",  "Dashboard",  Page.DASHBOARD),
        new NavItem("🏋", "Workouts",   Page.WORKOUTS),
        new NavItem("✓",  "Habits",     Page.HABITS),
        new NavItem("📈", "Progress",   Page.PROGRESS),
    };

    public SidebarPanel(User user, Consumer<Page> onNavigate, Runnable onLogout) {
        this.user = user; this.onNavigate = onNavigate; this.onLogout = onLogout;
        setPreferredSize(new Dimension(215, 0));
        setBackground(Theme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,0,0,1,Theme.BORDER));
        build();
    }

    private void build() {
        JPanel top = new JPanel(); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS)); top.setOpaque(false);

        // Logo
        JPanel logo = new JPanel(new BorderLayout(10,0)); logo.setOpaque(false);
        logo.setBorder(new EmptyBorder(22,18,18,18)); logo.setMaximumSize(new Dimension(Integer.MAX_VALUE,72));
        JLabel icon = new JLabel("⚡"); icon.setFont(new Font("Segoe UI",Font.PLAIN,30)); icon.setForeground(Theme.NEON_GREEN);
        JPanel logoText = new JPanel(); logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS)); logoText.setOpaque(false);
        logoText.add(UI.label("FitTrack Pro", Theme.FONT_HEADING, Theme.TEXT_PRIMARY));
        logoText.add(UI.label("Track. Train. Transform.", Theme.FONT_SMALL, Theme.NEON_GREEN));
        logo.add(icon, BorderLayout.WEST); logo.add(logoText, BorderLayout.CENTER);
        top.add(logo);

        // Role badge
        JPanel rb = new JPanel(new FlowLayout(FlowLayout.LEFT,18,0)); rb.setOpaque(false);
        rb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        rb.add(UI.badge(user.getRole().toString(), user.isAdmin() ? Theme.NEON_ORANGE : Theme.NEON_PURPLE));
        top.add(rb); top.add(Box.createVerticalStrut(10));

        // Nav
        navBox.setLayout(new BoxLayout(navBox, BoxLayout.Y_AXIS)); navBox.setOpaque(false);
        buildNav();
        top.add(navBox);
        add(top, BorderLayout.NORTH);
        add(buildProfile(), BorderLayout.SOUTH);
    }

    private void buildNav() {
        navBox.removeAll();
        for (NavItem item : NAV) navBox.add(makeBtn(item));
        if (user.isAdmin()) navBox.add(makeBtn(new NavItem("⚙","Settings",Page.SETTINGS)));
        navBox.revalidate(); navBox.repaint();
    }

    private JPanel makeBtn(NavItem item) {
        boolean isActive = active == item.page();
        JPanel btn = new JPanel(new BorderLayout(12,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(Theme.withAlpha(Theme.NEON_GREEN, 20));
                    g2.fill(new RoundRectangle2D.Float(8,2,getWidth()-16,getHeight()-4,8,8));
                    g2.setColor(Theme.NEON_GREEN);
                    g2.fill(new RoundRectangle2D.Float(0,4,4,getHeight()-8,4,4));
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setOpaque(false); btn.setBorder(new EmptyBorder(11,18,11,18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = UI.label(item.icon(), new Font("Segoe UI",Font.PLAIN,16), isActive ? Theme.NEON_GREEN : Theme.TEXT_MUTED);
        iconLbl.setPreferredSize(new Dimension(22,22));
        JLabel textLbl = UI.label(item.label(), Theme.FONT_SUBHEAD, isActive ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        btn.add(iconLbl, BorderLayout.WEST); btn.add(textLbl, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { active = item.page(); buildNav(); onNavigate.accept(item.page()); }
            @Override public void mouseEntered(MouseEvent e) { if (!isActive) { btn.setBackground(Theme.BG_HOVER); btn.setOpaque(true); btn.repaint(); } }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(null); btn.setOpaque(false); btn.repaint(); }
        });
        return btn;
    }

    public void setActive(Page p) { active = p; buildNav(); }

    private JPanel buildProfile() {
        JPanel panel = new JPanel(new BorderLayout(10,0));
        panel.setBackground(Theme.BG_SIDEBAR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER), new EmptyBorder(12,16,12,16)));

        // Avatar
        Color c = user.isAdmin() ? Theme.NEON_ORANGE : Theme.NEON_PURPLE;
        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.withAlpha(c,40)); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(c); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                FontMetrics fm=g2.getFontMetrics(); String init=user.getAvatarInitials();
                g2.drawString(init,(getWidth()-fm.stringWidth(init))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        av.setPreferredSize(new Dimension(38,38)); av.setOpaque(false);

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS)); info.setOpaque(false);
        info.add(UI.label(user.getDisplayName(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY));
        info.add(UI.label(user.getFitnessGoal(), Theme.FONT_SMALL, Theme.TEXT_MUTED));

        JButton logout = new JButton("⏻") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(getModel().isRollover()?Theme.NEON_PINK:Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,16));
                g2.drawString("⏻",4,18); g2.dispose();
            }
        };
        logout.setPreferredSize(new Dimension(28,28)); logout.setOpaque(false);
        logout.setContentAreaFilled(false); logout.setBorderPainted(false); logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setToolTipText("Logout"); logout.addActionListener(e -> onLogout.run());

        panel.add(av, BorderLayout.WEST); panel.add(info, BorderLayout.CENTER); panel.add(logout, BorderLayout.EAST);
        return panel;
    }
}
