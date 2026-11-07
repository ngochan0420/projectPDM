package com.pdm.loan.ui.kit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public final class Icons {

    public enum Kind { DONE, CURRENT, TODO, DISABLED, INFO, WARNING, LOCK, CHECK, UPLOAD, CHEVRON_DOWN, CHEVRON_RIGHT, DOT }

    private Icons() {
    }

    public static Icon of(Kind kind, Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.setColor(color);
                float s = size;
                g2.setStroke(new BasicStroke(Math.max(1.3f, s / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (kind) {
                    case DONE -> {
                        g2.drawOval(1, 1, size - 3, size - 3);
                        tick(g2, s);
                    }
                    case CURRENT -> {
                        g2.drawOval(1, 1, size - 3, size - 3);
                        int d = Math.max(4, size / 4);
                        g2.fillOval(size / 2 - d / 2 - 1, size / 2 - d / 2 - 1, d, d);
                    }
                    case TODO -> g2.drawOval(1, 1, size - 3, size - 3);
                    case DISABLED -> {
                        g2.drawOval(1, 1, size - 3, size - 3);
                        g2.drawLine((int) (s * 0.27), (int) (s * 0.70), (int) (s * 0.70), (int) (s * 0.27));
                    }
                    case INFO -> {
                        g2.drawOval(1, 1, size - 3, size - 3);
                        g2.drawLine(size / 2 - 1, (int) (s * 0.45), size / 2 - 1, (int) (s * 0.7));
                        g2.fillOval(size / 2 - 2, (int) (s * 0.25), 2, 2);
                    }
                    case WARNING -> {
                        Path2D p = new Path2D.Float();
                        p.moveTo(s / 2 - 1, 1.5);
                        p.lineTo(s - 2, s - 2.5);
                        p.lineTo(1, s - 2.5);
                        p.closePath();
                        g2.draw(p);
                        g2.drawLine(size / 2 - 1, (int) (s * 0.38), size / 2 - 1, (int) (s * 0.6));
                        g2.fillOval(size / 2 - 2, (int) (s * 0.68), 2, 2);
                    }
                    case LOCK -> {
                        g2.drawRoundRect((int) (s * 0.2), (int) (s * 0.45), (int) (s * 0.58), (int) (s * 0.45), 3, 3);
                        g2.drawArc((int) (s * 0.3), (int) (s * 0.12), (int) (s * 0.38), (int) (s * 0.6), 0, 180);
                    }
                    case CHECK -> tick(g2, s);
                    case UPLOAD -> {
                        g2.drawLine(size / 2, 2, size / 2, (int) (s * 0.65));
                        g2.drawLine(size / 2, 2, (int) (s * 0.25), (int) (s * 0.3));
                        g2.drawLine(size / 2, 2, (int) (s * 0.75), (int) (s * 0.3));
                        g2.drawPolyline(new int[]{2, 2, size - 2, size - 2},
                                new int[]{(int) (s * 0.6), size - 2, size - 2, (int) (s * 0.6)}, 4);
                    }
                    case CHEVRON_DOWN -> g2.drawPolyline(new int[]{(int) (s * 0.25), size / 2, (int) (s * 0.75)},
                            new int[]{(int) (s * 0.38), (int) (s * 0.63), (int) (s * 0.38)}, 3);
                    case CHEVRON_RIGHT -> g2.drawPolyline(new int[]{(int) (s * 0.38), (int) (s * 0.63), (int) (s * 0.38)},
                            new int[]{(int) (s * 0.25), size / 2, (int) (s * 0.75)}, 3);
                    case DOT -> {
                        int d = Math.max(6, size / 2);
                        g2.fillOval((size - d) / 2, (size - d) / 2, d, d);
                    }
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static void tick(Graphics2D g2, float s) {
        g2.drawPolyline(new int[]{(int) (s * 0.3), (int) (s * 0.45), (int) (s * 0.72)},
                new int[]{(int) (s * 0.52), (int) (s * 0.66), (int) (s * 0.36)}, 3);
    }

    public static Icon step(Kind kind) {
        Color c = switch (kind) {
            case DONE -> Theme.GREEN;
            case CURRENT -> Theme.TEXT;
            default -> Theme.FAINT;
        };
        return of(kind, c, 16);
    }

    public static Icon dot(Color c) {
        return of(Kind.DOT, c, 12);
    }
}
