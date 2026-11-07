package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.sql.SQLException;

public final class Ui {

    private Ui() {
    }

    // ------------------------------------------------------------------ errors

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    public static void run(Component parent, Action action) {
        try {
            action.run();
        } catch (Exception e) {
            error(parent, e);
        }
    }

    public static ActionListener on(Component parent, Action action) {
        return e -> run(parent, action);
    }

    public static void error(Component parent, Exception e) {
        if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof SQLException) {
            JOptionPane.showMessageDialog(parent, "Database error:\n" + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        } else {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    public static String require(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Please " + what + ".");
        }
        return value.trim();
    }

    // ------------------------------------------------------------------ labels

    public static JLabel text(String s) {
        JLabel l = new JLabel(s == null ? "—" : s);
        l.setFont(Theme.font(Font.PLAIN, 13f));
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel bold(String s) {
        JLabel l = text(s);
        l.setFont(Theme.font(Font.BOLD, 13f));
        return l;
    }

    public static JLabel muted(String s) {
        JLabel l = text(s);
        l.setForeground(Theme.MUTED);
        return l;
    }

    public static JLabel small(String s) {
        JLabel l = text(s);
        l.setFont(Theme.font(Font.PLAIN, 11.5f));
        l.setForeground(Theme.MUTED);
        return l;
    }

    public static JLabel colored(String s, Color c) {
        JLabel l = text(s);
        l.setForeground(c);
        return l;
    }

    public static JLabel heading(String s, float size) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.font(Font.BOLD, size));
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JComponent chip(String s) {
        return new Badge(s, Theme.Tone.GRAY);
    }

    // ----------------------------------------------------------------- buttons

    public static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(Theme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.font(Font.BOLD, 12.5f));
        b.setFocusPainted(false);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondary(String text) {
        JButton b = new JButton(text);
        b.setBackground(Theme.BG);
        b.setForeground(Theme.TEXT);
        b.setFont(Theme.font(Font.PLAIN, 12.5f));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    public static JButton small(String text, boolean primary) {
        JButton b = primary ? primary(text) : secondary(text);
        b.setFont(Theme.font(primary ? Font.BOLD : Font.PLAIN, 11.5f));
        b.setMargin(new Insets(2, 8, 2, 8));
        if (primary) {
            b.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }
        return b;
    }

    public static JButton link(String text, Color color) {
        JButton b = new JButton(text);
        b.setForeground(color);
        b.setFont(Theme.font(Font.PLAIN, 12f));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ------------------------------------------------------------------ fields

    public static JTextField field(String value) {
        JTextField f = new JTextField(value == null ? "" : value);
        f.setFont(Theme.font(Font.PLAIN, 13f));
        f.putClientProperty("JComponent.minimumHeight", 34);
        return f;
    }

    public static JTextField readOnly(String value) {
        JTextField f = field(value);
        f.setEditable(false);
        f.setBackground(new Color(0xF4F4F5));
        f.setForeground(Theme.MUTED);
        return f;
    }

    public static JTextArea area(String value, int rows) {
        JTextArea a = new JTextArea(value == null ? "" : value, rows, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Theme.font(Font.PLAIN, 13f));
        a.setBorder(new EmptyBorder(8, 10, 8, 10));
        return a;
    }

    public static JScrollPane areaScroll(JTextArea a) {
        JScrollPane s = new JScrollPane(a);
        s.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        // keep the height even when the row is too narrow for every preferred width
        s.setMinimumSize(new Dimension(60, a.getPreferredSize().height + 4));
        return s;
    }

    public static JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.add(small(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static JPanel formGrid(int columns, JComponent... labeledFields) {
        JPanel p = new JPanel(new GridLayout(0, columns, 14, 12));
        p.setOpaque(false);
        for (JComponent c : labeledFields) {
            p.add(c);
        }
        return p;
    }

    // ----------------------------------------------------------------- layouts

    public static JPanel flow(int gap, Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        for (Component c : items) {
            p.add(c);
        }
        return p;
    }

    public static JPanel flowRight(int gap, Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, gap, 0));
        p.setOpaque(false);
        for (Component c : items) {
            p.add(c);
        }
        return p;
    }

    public static JPanel leftRight(JComponent left, JComponent right) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(left, BorderLayout.CENTER);
        if (right != null) {
            p.add(right, BorderLayout.EAST);
        }
        return p;
    }

    public static JPanel keyValues(int pairsPerRow, String... kv) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < kv.length / 2; i++) {
            int row = i / pairsPerRow;
            int col = (i % pairsPerRow) * 2;
            g.gridy = row;
            g.gridx = col;
            g.weightx = 0;
            JLabel k = small(kv[2 * i]);
            k.setPreferredSize(new Dimension(120, k.getPreferredSize().height));
            p.add(k, g);
            g.gridx = col + 1;
            g.weightx = 1;
            JLabel v = text(kv[2 * i + 1] == null ? "—" : kv[2 * i + 1]);
            v.setPreferredSize(new Dimension(10, v.getPreferredSize().height));
            p.add(v, g);
        }
        return p;
    }

    public static JComponent statCard(String label, String value) {
        RoundPanel p = new RoundPanel(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        p.add(small(label), BorderLayout.NORTH);
        p.add(heading(value, 17f), BorderLayout.CENTER);
        return p;
    }

    public static JComponent avatar(String initials) {
        JLabel l = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setForeground(Color.WHITE);
        l.setFont(Theme.font(Font.BOLD, 10f));
        l.setPreferredSize(new Dimension(26, 26));
        return l;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(null);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }
}
