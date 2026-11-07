package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Section extends JPanel {

    private final Stack body = new Stack(10);
    private boolean open = true;

    public Section(String title) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        JLabel t = Ui.heading(title, 15f);
        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.BORDER);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        JButton toggle = new JButton(Icons.of(Icons.Kind.CHEVRON_DOWN, Theme.MUTED, 16));
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setBorder(new EmptyBorder(0, 6, 0, 0));
        toggle.addActionListener(e -> {
            open = !open;
            body.setVisible(open);
            toggle.setIcon(Icons.of(open ? Icons.Kind.CHEVRON_DOWN : Icons.Kind.CHEVRON_RIGHT, Theme.MUTED, 16));
            revalidate();
        });
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(t, BorderLayout.WEST);
        header.add(line, BorderLayout.CENTER);
        header.add(toggle, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public <T extends Component> T push(T c) {
        return body.push(c);
    }
}
