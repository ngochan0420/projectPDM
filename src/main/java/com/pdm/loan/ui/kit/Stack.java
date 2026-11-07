package com.pdm.loan.ui.kit;

import javax.swing.*;
import java.awt.*;

public class Stack extends JPanel implements Scrollable {

    private final int gap;
    private int row;
    private final GridBagConstraints filler = new GridBagConstraints();
    private final JPanel glue = new JPanel();

    public Stack(int gap) {
        super(new GridBagLayout());
        this.gap = gap;
        setOpaque(false);
        glue.setOpaque(false);
        clear();
    }

    public final void clear() {
        removeAll();
        row = 0;
        filler.gridx = 0;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;
        relayoutGlue();
    }

    private void relayoutGlue() {
        remove(glue);
        filler.gridy = 10_000;
        super.add(glue, filler);
    }

    public <T extends Component> T push(T c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row++;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(row == 1 ? 0 : gap, 0, 0, 0);
        super.add(c, g);
        return c;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
        return r.height - 32;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container p = getParent();
        return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
    }
}
