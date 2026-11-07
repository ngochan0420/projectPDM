package com.pdm.loan.ui.kit;

import javax.swing.*;
import java.awt.*;

public class RoundPanel extends JPanel {

    private final Color fill;
    private final Color line;
    private final boolean dashed;

    public RoundPanel(LayoutManager layout) {
        this(layout, Theme.BG, Theme.BORDER, false);
    }

    public RoundPanel(LayoutManager layout, Color fill, Color line, boolean dashed) {
        super(layout);
        this.fill = fill;
        this.line = line;
        this.dashed = dashed;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(line);
        if (dashed) {
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f));
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }
}
