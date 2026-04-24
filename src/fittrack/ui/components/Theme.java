package fittrack.ui.components;

import fittrack.model.WorkoutSession;
import java.awt.*;

/** Fitness-themed energetic dark colour palette. */
public final class Theme {
    private Theme() {}

    // Backgrounds
    public static final Color BG_DEEPEST  = new Color(8,   11,  18);
    public static final Color BG_DARK     = new Color(12,  16,  26);
    public static final Color BG_CARD     = new Color(18,  24,  38);
    public static final Color BG_ELEVATED = new Color(26,  34,  52);
    public static final Color BG_HOVER    = new Color(34,  45,  68);
    public static final Color BG_SIDEBAR  = new Color(10,  14,  22);

    // Energetic Accents
    public static final Color NEON_GREEN   = new Color(57,  255, 130);
    public static final Color NEON_ORANGE  = new Color(255, 107,  53);
    public static final Color NEON_BLUE    = new Color(56,  189, 248);
    public static final Color NEON_PURPLE  = new Color(168, 85,  247);
    public static final Color NEON_PINK    = new Color(244, 63,  94);
    public static final Color NEON_YELLOW  = new Color(250, 204,  21);
    public static final Color NEON_CYAN    = new Color(34,  211, 238);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(240, 245, 250);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color TEXT_MUTED     = new Color(71,  85,  105);
    public static final Color TEXT_INVERSE   = new Color(8,   11,  18);

    // Borders
    public static final Color BORDER       = new Color(30,  41,  59);
    public static final Color BORDER_GLOW  = new Color(57,  255, 130);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD,  36);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 12);

    public static Color workoutColor(WorkoutSession.WorkoutType t) {
        return switch (t) {
            case RUNNING, WALKING   -> NEON_GREEN;
            case CYCLING            -> NEON_BLUE;
            case SWIMMING           -> NEON_CYAN;
            case WEIGHTLIFTING      -> NEON_ORANGE;
            case HIIT, BOXING       -> NEON_PINK;
            case YOGA, PILATES      -> NEON_PURPLE;
            default                 -> NEON_YELLOW;
        };
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color intensityColor(double score) {
        if (score >= 8) return NEON_PINK;
        if (score >= 6) return NEON_ORANGE;
        if (score >= 4) return NEON_YELLOW;
        return NEON_GREEN;
    }
}
