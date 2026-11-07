package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Coloured message box (info / warning / success / locked) shown at the top of a page section. */
public class Banner extends JPanel {

    private final Theme.Tone tone;

    public Banner(Theme.Tone tone, Icons.Kind icon, String text) {
        this(tone, icon, text, null);
    }

    public Banner(Theme.Tone tone, Icons.Kind icon, String text, String subText) {
        super(new BorderLayout(10, 0));
        this.tone = tone;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel ic = new JLabel(Icons.of(icon, tone.fg, 16));
        ic.setVerticalAlignment(SwingConstants.TOP);
        add(ic, BorderLayout.WEST);
        JPanel texts = new JPanel(new GridLayout(subText == null ? 1 : 2, 1));
        texts.setOpaque(false);
        JLabel main = new JLabel(text);
        main.setForeground(tone == Theme.Tone.GRAY ? Theme.TEXT : tone.fg);
        main.setFont(Theme.font(subText == null ? Font.PLAIN : Font.BOLD, 12.5f));
        texts.add(main);
        if (subText != null) {
            texts.add(Ui.small(subText));
        }
        add(texts, BorderLayout.CENTER);
    }

    /** Add a component (e.g. a close button) on the right side. */
    public Banner withRight(JComponent c) {
        add(c, BorderLayout.EAST);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(tone.bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.setColor(tone.border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
