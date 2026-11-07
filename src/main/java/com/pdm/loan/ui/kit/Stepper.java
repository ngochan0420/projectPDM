package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class Stepper extends JPanel {

    public record Step(String label, Icons.Kind state) {
    }

    private final List<JPanel> items = new ArrayList<>();
    private final List<Step> steps;
    private int selected = -1;

    public Stepper(List<Step> steps, IntConsumer onClick) {
        this.steps = steps;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        for (int i = 0; i < steps.size(); i++) {
            int index = i;
            Step s = steps.get(i);
            JPanel row = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(index == selected ? Theme.ACCENT : Theme.BORDER);
                    g.fillRect(0, 0, index == selected ? 2 : 1, getHeight());
                }
            };
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(8, 12, 8, 4));
            JLabel l = Ui.text(s.label());
            l.setFont(Theme.font(Font.PLAIN, 12f));
            if (s.state() == Icons.Kind.DISABLED) {
                l.setForeground(Theme.FAINT);
            }
            row.add(l, BorderLayout.CENTER);
            row.add(new JLabel(Icons.step(s.state())), BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            row.setPreferredSize(new Dimension(170, 34));
            row.setAlignmentX(LEFT_ALIGNMENT);
            if (onClick != null) {
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onClick.accept(index);
                    }
                });
            }
            items.add(row);
            add(row);
        }
    }

    public void select(int index) {
        selected = index;
        for (int i = 0; i < items.size(); i++) {
            JLabel l = (JLabel) ((BorderLayout) items.get(i).getLayout()).getLayoutComponent(BorderLayout.CENTER);
            l.setFont(Theme.font(i == index ? Font.BOLD : Font.PLAIN, 12f));
        }
        repaint();
    }

    public List<Step> steps() {
        return steps;
    }
}
