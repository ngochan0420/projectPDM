package com.pdm.loan.ui.kit;

import javax.swing.*;
import java.awt.*;

public class Chip extends JToggleButton {

    private final Color normalText;

    public Chip(String text, Color textColor) {
        super(text);
        this.normalText = textColor;
        setFont(Theme.font(Font.PLAIN, 12f));
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty("JButton.buttonType", "roundRect");
        addItemListener(e -> restyle());
        restyle();
    }

    private void restyle() {
        setBackground(isSelected() ? Theme.PRIMARY : Theme.BG);
        setForeground(isSelected() ? Color.WHITE : normalText);
        putClientProperty("JToggleButton.selectedBackground", Theme.PRIMARY);
        putClientProperty("JButton.selectedBackground", Theme.PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isSelected()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}
