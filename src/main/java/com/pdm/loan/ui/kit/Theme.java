package com.pdm.loan.ui.kit;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public final class Theme {

    public static final Color BG = Color.WHITE;
    public static final Color SIDEBAR = new Color(0xFAFAFA);
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color HEADER_BG = new Color(0xFAFAFA);
    public static final Color HOVER = new Color(0xF7F7F8);
    public static final Color SELECTED = new Color(0xEFEFF1);
    public static final Color TEXT = new Color(0x111827);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color FAINT = new Color(0xA1A1AA);
    public static final Color PRIMARY = new Color(0x18181B);
    public static final Color ACCENT = new Color(0xA855F7);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color RED = new Color(0xEF4444);
    public static final Color BLUE = new Color(0x38BDF8);

    /** Colour sets for badges and banners. */
    public enum Tone {
        GREEN(0x047857, 0xECFDF5, 0xA7F3D0),
        BLUE(0x0284C7, 0xF0F9FF, 0x7DD3FC),
        RED(0xDC2626, 0xFEF2F2, 0xFCA5A5),
        GRAY(0x6B7280, 0xF3F4F6, 0xE5E7EB),
        PURPLE(0x7C3AED, 0xF5F3FF, 0xC4B5FD),
        AMBER(0xB45309, 0xFFFBEB, 0xFCD34D);

        public final Color fg;
        public final Color bg;
        public final Color border;

        Tone(int fg, int bg, int border) {
            this.fg = new Color(fg);
            this.bg = new Color(bg);
            this.border = new Color(border);
        }
    }

    private Theme() {
    }

    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusedBorderColor", new Color(0x71717A));
        UIManager.put("Component.focusColor", new Color(0xE4E4E7));
        UIManager.put("Button.borderColor", BORDER);
        UIManager.put("Button.focusedBorderColor", BORDER);
        UIManager.put("Panel.background", BG);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", BG);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TextComponent.background", BG);
        UIManager.put("TextField.background", BG);
        UIManager.put("ComboBox.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("OptionPane.background", BG);
    }

    public static Font font(int style, float size) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new JLabel().getFont();
        }
        return base.deriveFont(style, size);
    }
}
