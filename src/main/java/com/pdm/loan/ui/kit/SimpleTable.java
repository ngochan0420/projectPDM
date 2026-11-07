package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimpleTable extends RoundPanel {

    public record Col(String name, double weight, boolean right) {
        public static Col left(String name, double weight) {
            return new Col(name, weight, false);
        }

        public static Col right(String name, double weight) {
            return new Col(name, weight, true);
        }
    }

    private final Col[] cols;
    private final JPanel rows = new JPanel();
    private int count;

    public SimpleTable(Col... cols) {
        super(new BorderLayout());
        this.cols = cols;
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        JComponent[] header = new JComponent[cols.length];
        for (int i = 0; i < cols.length; i++) {
            JLabel l = Ui.small(cols[i].name());
            l.setFont(Theme.font(Font.BOLD, 11f));
            header[i] = l;
        }
        JPanel h = line(header, 34);
        h.setOpaque(true);
        h.setBackground(Theme.HEADER_BG);
        rows.add(h);
        add(rows, BorderLayout.CENTER);
        setBorder(new EmptyBorder(1, 1, 1, 1));
    }

    private JPanel line(JComponent[] cells, int height) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new MatteBorder(count == 0 && height == 34 ? 0 : 1, 0, 0, 0, Theme.BORDER));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 12, 0, 12);
        for (int i = 0; i < cols.length; i++) {
            g.gridx = i;
            g.weightx = cols[i].weight();
            JComponent cell = i < cells.length && cells[i] != null ? cells[i] : new JLabel("");
            // GridBagLayout centres the cell vertically; anchor keeps it left/right aligned
            JPanel holder = new JPanel(new GridBagLayout());
            holder.setOpaque(false);
            GridBagConstraints hc = new GridBagConstraints();
            hc.weightx = 1;
            hc.anchor = cols[i].right() ? GridBagConstraints.LINE_END : GridBagConstraints.LINE_START;
            holder.add(cell, hc);
            if (cell instanceof JLabel l && !(cell instanceof Badge)) {
                // let long text shrink with "..." instead of pushing the columns
                holder.remove(cell);
                holder.setLayout(new BorderLayout());
                l.setHorizontalAlignment(cols[i].right() ? SwingConstants.RIGHT : SwingConstants.LEFT);
                holder.add(l, BorderLayout.CENTER);
            }
            holder.setPreferredSize(new Dimension(0, Math.max(height - 12, cell.getPreferredSize().height)));
            holder.setMinimumSize(new Dimension(0, 0));
            p.add(holder, g);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        p.setPreferredSize(new Dimension(100, height));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** Add a row. Strings become labels; components are used as they are. */
    public JPanel addRow(Object... cells) {
        JComponent[] comps = new JComponent[cells.length];
        for (int i = 0; i < cells.length; i++) {
            Object c = cells[i];
            comps[i] = c instanceof JComponent jc ? jc : Ui.text(c == null ? "—" : c.toString());
        }
        count++;
        JPanel row = line(comps, 44);
        row.setOpaque(true);
        row.setBackground(Theme.BG);
        rows.add(row);
        return row;
    }

    /** Add a row that reacts to clicks (hover highlight + hand cursor). */
    public JPanel addClickableRow(Runnable onClick, Object... cells) {
        JPanel row = addRow(cells);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(Theme.HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!row.contains(e.getPoint())) {
                    row.setBackground(Theme.BG);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return row;
    }

    public void emptyMessage(String text) {
        if (count == 0) {
            JLabel l = Ui.muted(text);
            l.setBorder(new EmptyBorder(14, 12, 14, 12));
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
            p.add(l);
            p.setAlignmentX(LEFT_ALIGNMENT);
            rows.add(p);
        }
    }

    public int rowCount() {
        return count;
    }
}
