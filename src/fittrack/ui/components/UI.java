package fittrack.ui.components;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.*;

public final class UI {
    private UI() {}

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(Theme.FONT_SUBHEAD);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 38));
        return b;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    public static JPanel glowCard(LayoutManager layout, Color glowColor) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // Top glow accent
                g2.setColor(glowColor);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 14, 14));
                // Bottom bar
                g2.setColor(Theme.withAlpha(glowColor, 80));
                g2.fillRoundRect(0, getHeight()-4, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setBackground(Theme.BG_ELEVATED);
        tf.setCaretColor(Theme.NEON_GREEN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));
        tf.setOpaque(true);
        return tf;
    }

    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setForeground(Theme.TEXT_PRIMARY);
        pf.setBackground(Theme.BG_ELEVATED);
        pf.setCaretColor(Theme.NEON_GREEN);
        pf.setEchoChar('●');
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));
        return pf;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }

    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.withAlpha(color, 35));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),getHeight(),getHeight()));
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,getHeight(),getHeight()));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        l.setFont(Theme.FONT_LABEL); l.setForeground(color); l.setOpaque(false);
        l.setBorder(new EmptyBorder(2, 10, 2, 10));
        return l;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(Theme.BG_DARK);
        sp.getViewport().setBackground(Theme.BG_DARK);
        sp.getVerticalScrollBar().setUI(new FitScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new FitScrollBarUI());
        return sp;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBackground(Theme.BG_ELEVATED);
        cb.setBorder(new LineBorder(Theme.BORDER, 1, true));
        return cb;
    }

    public static JProgressBar progressBar(int val, Color color) {
        JProgressBar pb = new JProgressBar(0, 100) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),getHeight(),getHeight()));
                if (getValue() > 0) {
                    float w = getWidth() * getValue() / 100f;
                    GradientPaint gp = new GradientPaint(0,0, color.brighter(), w,0, color);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0,0,w,getHeight(),getHeight(),getHeight()));
                }
                g2.dispose();
            }
        };
        pb.setValue(val); pb.setOpaque(false); pb.setBorderPainted(false); pb.setStringPainted(false);
        pb.setPreferredSize(new Dimension(100, 8));
        return pb;
    }

    public static JSeparator sep() {
        JSeparator s = new JSeparator(); s.setForeground(Theme.BORDER); s.setBackground(Theme.BORDER); return s;
    }

    static class FitScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(40, 55, 80); trackColor = Theme.BG_DARK;
        }
        @Override protected JButton createDecreaseButton(int o) { return inv(); }
        @Override protected JButton createIncreaseButton(int o) { return inv(); }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? Theme.NEON_GREEN : thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height);
        }
        private JButton inv() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); return b;
        }
    }
}
