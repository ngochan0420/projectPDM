package com.pdm.loan.ui.shared;

import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.service.FileStore;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public final class Views {

    private Views() {
    }

    public static int suppliedCount(List<Rec> docs) {
        int n = 0;
        for (Rec d : docs) {
            if (d.b("Supplied")) {
                n++;
            }
        }
        return n;
    }

    /**
     * Document list: green dot + "1 file" + date when supplied, red "Missing" otherwise.
     *
     * @param onMissing  called with the DocTypeID when the "upload now" link is clicked (null = no link)
     * @param extraRight optional extra component per row (officer buttons), may return null
     */
    public static ItemList documents(String applicationId, List<Rec> docs, Integer daysWaiting,
                                     Consumer<String> onMissing,
                                     java.util.function.Function<Rec, JComponent> extraRight) {
        ItemList list = new ItemList();
        if (docs.isEmpty()) {
            list.empty("This product does not require any document.");
        }
        for (Rec d : docs) {
            boolean ok = d.b("Supplied");
            String file = ok ? FileStore.fileName(applicationId, d.s("DocumentID")) : null;
            JComponent[] tags = ok
                    ? new JComponent[]{Ui.chip("1 file")}
                    : (d.b("Mandatory") ? new JComponent[0] : new JComponent[]{Ui.chip("optional")});
            JComponent status;
            if (ok) {
                status = Ui.muted(file != null ? file + "   " + Fmt.date(d.d("SuppliedDate")) : Fmt.date(d.d("SuppliedDate")));
            } else if (onMissing != null) {
                JButton link = Ui.link("Missing · upload now", Theme.RED);
                link.addActionListener(e -> onMissing.accept(d.s("DocTypeID")));
                status = link;
            } else {
                status = Ui.colored(daysWaiting != null ? "Missing · waiting " + daysWaiting + " days" : "Missing",
                        Theme.RED);
            }
            JComponent extra = extraRight == null ? null : extraRight.apply(d);
            if (extra != null) {
                list.add(ok ? Theme.GREEN : Theme.RED, d.s("TypeName"), tags, status, extra);
            } else {
                list.add(ok ? Theme.GREEN : Theme.RED, d.s("TypeName"), tags, status);
            }
        }
        return list;
    }

    public static ItemList steps(List<Rec> steps, boolean showOfficer) {
        ItemList list = new ItemList();
        if (steps.isEmpty()) {
            list.empty("No verification step has been assigned yet.");
        }
        for (Rec s : steps) {
            String result = s.s("Result");
            Color dot = result == null ? Theme.BLUE : (result.startsWith("Failed") ? Theme.RED : Theme.GREEN);
            JComponent[] tags = showOfficer ? new JComponent[]{Ui.chip(s.s("Officer"))} : new JComponent[0];
            JLabel date = Ui.muted(result == null ? "Pending" : Fmt.date(s.d("VerificationDate")));
            date.setPreferredSize(new Dimension(90, date.getPreferredSize().height));
            date.setHorizontalAlignment(SwingConstants.RIGHT);
            list.add(dot, s.s("StepName"), tags, Badge.status(result == null ? "Pending" : result), date);
        }
        return list;
    }

    public static JPanel withStepper(Stepper stepper, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(28, 0));
        p.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(stepper, BorderLayout.NORTH);
        left.setPreferredSize(new Dimension(200, 10));
        p.add(left, BorderLayout.WEST);
        p.add(content, BorderLayout.CENTER);
        return p;
    }
}
