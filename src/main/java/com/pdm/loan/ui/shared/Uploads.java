package com.pdm.loan.ui.shared;

import com.pdm.loan.dao.DocumentDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.FileStore;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public final class Uploads {

    private static final DocumentDao DOCS = new DocumentDao();

    private Uploads() {
    }

    public static void upload(String applicationId, String docTypeId, File file) throws Exception {
        FileStore.validate(file);
        DOCS.setSupplied(applicationId, docTypeId, true);
        String docId = DOCS.documentId(applicationId, docTypeId);
        FileStore.save(applicationId, docId, file);
    }

    public static boolean chooseAndUpload(Component parent, String applicationId, String docTypeId) throws Exception {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        upload(applicationId, docTypeId, fc.getSelectedFile());
        return true;
    }

    public static Rec askDocType(Component parent, File file, List<Rec> checklist) {
        if (checklist.isEmpty()) {
            throw new IllegalStateException("This product does not require any document.");
        }
        String[] names = checklist.stream()
                .map(r -> r.s("TypeName") + (r.b("Supplied") ? "  (already supplied)" : ""))
                .toArray(String[]::new);
        int def = 0;
        for (int i = 0; i < checklist.size(); i++) {
            if (!checklist.get(i).b("Supplied")) {
                def = i;
                break;
            }
        }
        JComboBox<String> box = new JComboBox<>(names);
        box.setSelectedIndex(def);
        int ok = JOptionPane.showConfirmDialog(parent, new Object[]{"Which document is \"" + file.getName() + "\"?", box},
                "Upload document", JOptionPane.OK_CANCEL_OPTION);
        return ok == JOptionPane.OK_OPTION ? checklist.get(box.getSelectedIndex()) : null;
    }

    public static void dropped(Component parent, String applicationId, List<File> files) throws Exception {
        for (File f : files) {
            FileStore.validate(f);
            Rec doc = askDocType(parent, f, DOCS.checklist(applicationId));
            if (doc != null) {
                upload(applicationId, doc.s("DocTypeID"), f);
            }
        }
    }
}
