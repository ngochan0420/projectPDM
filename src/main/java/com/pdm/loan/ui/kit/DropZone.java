package com.pdm.loan.ui.kit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class DropZone extends RoundPanel {

    public DropZone(String title, String hint, Consumer<List<File>> onFiles) {
        super(new BorderLayout(12, 0), Theme.BG, new Color(0xD4D4D8), true);
        setBorder(new EmptyBorder(12, 14, 12, 14));
        add(new JLabel(Icons.of(Icons.Kind.UPLOAD, Theme.TEXT, 16)), BorderLayout.WEST);
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        JLabel t = Ui.bold(title);
        t.setFont(Theme.font(Font.BOLD, 12.5f));
        texts.add(t);
        texts.add(Ui.small(hint));
        add(texts, BorderLayout.CENTER);
        JButton browse = Ui.small("Choose files", false);
        add(browse, BorderLayout.EAST);

        Runnable choose = () -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onFiles.accept(List.of(fc.getSelectedFiles()));
            }
        };
        browse.addActionListener(e -> choose.run());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                choose.run();
            }
        });
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport s) {
                return s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport s) {
                try {
                    List<File> files = (List<File>) s.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    SwingUtilities.invokeLater(() -> onFiles.accept(files));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }
}
