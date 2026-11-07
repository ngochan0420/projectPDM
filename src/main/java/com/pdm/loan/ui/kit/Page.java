package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class Page extends JPanel {

    protected final Shell shell;
    private final JPanel crumbs = Ui.flow(6);
    private final JPanel titleRow = Ui.flow(10);
    private final JPanel actions = Ui.flowRight(8);
    protected final Stack body = new Stack(18);

    protected Page(Shell shell) {
        super(new BorderLayout());
        this.shell = shell;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(14, 24, 0, 24));

        JPanel head = new JPanel(new BorderLayout(0, 14));
        head.setOpaque(false);
        head.add(crumbs, BorderLayout.NORTH);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(titleRow, BorderLayout.WEST);
        titleBar.add(actions, BorderLayout.EAST);
        head.add(titleBar, BorderLayout.CENTER);
        head.setBorder(new EmptyBorder(0, 0, 18, 0));
        add(head, BorderLayout.NORTH);

        JScrollPane sp = Ui.scroll(body);
        sp.setBorder(new EmptyBorder(0, 0, 0, 0));
        body.setBorder(new EmptyBorder(0, 0, 24, 6));
        add(sp, BorderLayout.CENTER);
    }

    protected abstract void build() throws Exception;

    public final void reload() {
        crumbs.removeAll();
        titleRow.removeAll();
        actions.removeAll();
        body.clear();
        Ui.run(this, this::build);
        revalidate();
        repaint();
    }

    // ---- header helpers for subclasses

    protected void crumb(String label, Runnable go) {
        if (crumbs.getComponentCount() > 0) {
            crumbs.add(new JLabel(Icons.of(Icons.Kind.CHEVRON_RIGHT, Theme.FAINT, 10)));
        }
        if (go == null) {
            JLabel l = Ui.small(label);
            l.setForeground(Theme.TEXT);
            crumbs.add(l);
        } else {
            JButton b = Ui.link(label, Theme.FAINT);
            b.setFont(Theme.font(Font.PLAIN, 11.5f));
            b.addActionListener(e -> go.run());
            crumbs.add(b);
        }
    }

    protected void title(String text) {
        titleRow.add(Ui.heading(text, 22f));
    }

    protected void titleExtra(JComponent c) {
        titleRow.add(c);
    }

    protected void action(JButton b) {
        actions.add(b);
    }
}
