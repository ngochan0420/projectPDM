package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Rounded "pill" label used for statuses (Approved, Waiting documents, 12 days ...). */
public class Badge extends JLabel {

    private final Theme.Tone tone;

    public Badge(String text, Theme.Tone tone) {
        super(text);
        this.tone = tone;
        setFont(Theme.font(Font.PLAIN, 11.5f));
        setForeground(tone.fg);
        setBorder(new EmptyBorder(2, 8, 2, 8));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(tone.bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.setColor(tone.border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    /** Pick the colour from the status text. */
    public static Badge status(String status) {
        if (status == null) {
            return new Badge("—", Theme.Tone.GRAY);
        }
        return switch (status) {
            case "Approved", "Passed", "Paid", "Supplied", "Required" -> new Badge(status, Theme.Tone.GREEN);
            case "Approved with conditions" -> new Badge("With conditions", Theme.Tone.GREEN);
            case "Rejected", "Failed", "Overdue", "Missing" -> new Badge(status, Theme.Tone.RED);
            case "Waiting documents", "Pending", "Upcoming", "Unpaid", "Waiting for document" ->
                    new Badge(status, Theme.Tone.BLUE);
            case "In verification", "Active" -> new Badge(status, Theme.Tone.PURPLE);
            case "Ready for decision", "Passed with remarks" -> new Badge(status, Theme.Tone.AMBER);
            default -> new Badge(status, Theme.Tone.GRAY);
        };
    }

    /** "12 days" in red when the application has waited more than 10 days for a document. */
    public static JComponent days(Integer days, boolean waitingForDocuments) {
        if (days == null) {
            return Ui.muted("—");
        }
        boolean late = waitingForDocuments && days > 10;
        return new Badge(days + (days == 1 ? " day" : " days"), late ? Theme.Tone.RED : Theme.Tone.GRAY);
    }
}
