package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ItemList extends JPanel {

    public ItemList() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    /**
     * @param dot   colour of the status dot
     * @param title main text
     * @param tags  small components after the title (chips)
     * @param right components on the right (badge, date, link ...)
     */
    public void add(Color dot, String title, JComponent[] tags, JComponent... right) {
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(Icons.of(Icons.Kind.CHEVRON_RIGHT, Theme.MUTED, 12)));
        left.add(new JLabel(Icons.dot(dot)));
        left.add(Ui.text(title));
        if (tags != null) {
            for (JComponent t : tags) {
                left.add(t);
            }
        }
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        r.setOpaque(false);
        for (JComponent c : right) {
            r.add(c);
        }
        JPanel row = Ui.leftRight(left, r);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        super.add(row);
    }

    public void empty(String text) {
        JLabel l = Ui.muted(text);
        l.setBorder(new EmptyBorder(6, 4, 6, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        super.add(l);
    }
}
