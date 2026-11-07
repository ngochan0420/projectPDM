package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Shell extends JFrame {

    private final JPanel nav = new JPanel();
    private final Map<String, JPanel> navItems = new LinkedHashMap<>();
    private final JPanel content = new JPanel(new BorderLayout());
    private final String userName;
    private final String userInitials;

    public Shell(String windowTitle, String role, String userName, String userInitials, Runnable onLogout) {
        super(windowTitle);
        this.userName = userName;
        this.userInitials = userInitials;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SIDEBAR);
        side.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));
        side.setPreferredSize(new Dimension(210, 100));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logo.setOpaque(false);
        logo.setBorder(new EmptyBorder(16, 12, 18, 8));
        JLabel mark = new JLabel("LD", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mark.setForeground(Color.WHITE);
        mark.setFont(Theme.font(Font.BOLD, 10f));
        mark.setPreferredSize(new Dimension(24, 24));
        logo.add(mark);
        logo.add(Ui.heading("LoanDesk", 13.5f));
        logo.add(new Badge(role, Theme.Tone.GRAY));
        side.add(logo, BorderLayout.NORTH);

        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(0, 10, 0, 10));
        side.add(nav, BorderLayout.CENTER);

        JPanel user = new JPanel(new BorderLayout(8, 0));
        user.setOpaque(false);
        user.setBorder(new CompoundBorderTop());
        user.add(Ui.avatar(userInitials), BorderLayout.WEST);
        JLabel name = Ui.text(userName);
        name.setFont(Theme.font(Font.PLAIN, 12f));
        user.add(name, BorderLayout.CENTER);
        JButton out = Ui.small("Log out", false);
        out.addActionListener(e -> onLogout.run());
        user.add(out, BorderLayout.EAST);
        side.add(user, BorderLayout.SOUTH);

        content.setBackground(Theme.BG);
        add(side, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
    }

    private static class CompoundBorderTop extends javax.swing.border.CompoundBorder {
        CompoundBorderTop() {
            super(new MatteBorder(1, 0, 0, 0, Theme.BORDER), new EmptyBorder(12, 12, 12, 10));
        }
    }

    public String userName() {
        return userName;
    }

    public String userInitials() {
        return userInitials;
    }

    /** Add a sidebar menu item that opens a page. */
    public void addNav(String label, Supplier<Page> page) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                if (Boolean.TRUE.equals(getClientProperty("selected"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.SELECTED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(7, 10, 7, 10));
        JLabel l = Ui.text(label);
        l.setFont(Theme.font(Font.PLAIN, 12.5f));
        item.add(l);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                show(label, page.get());
            }
        });
        navItems.put(label, item);
        nav.add(item);
        nav.add(Box.createVerticalStrut(2));
    }

    /** Show a page and highlight its menu item (navLabel may be null to keep the current one). */
    public void show(String navLabel, Page page) {
        if (navLabel != null) {
            navItems.forEach((k, v) -> {
                v.putClientProperty("selected", k.equals(navLabel));
                v.repaint();
            });
        }
        content.removeAll();
        content.add(page, BorderLayout.CENTER);
        page.reload();
        content.revalidate();
        content.repaint();
    }
}
