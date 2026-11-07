package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.ApplicationDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ApplicationsPage extends Page {

    public static final String ALL = "All";
    private static final String[] FILTERS = {ALL, "Waiting for documents", "In verification", "Ready for decision",
            "Over 10 days", "Approved", "Rejected"};

    private final OfficerApp.Ctx ctx;
    private String filter;
    private final ApplicationDao applications = new ApplicationDao();
    private List<Rec> shown = new ArrayList<>();

    public ApplicationsPage(Shell shell, OfficerApp.Ctx ctx, String filter) {
        super(shell);
        this.ctx = ctx;
        this.filter = filter;
    }

    private static boolean late(Rec a) {
        return ApplicationDao.STAGE_WAITING_DOCS.equals(a.s("Stage")) && a.i("DaysWaiting") > 10;
    }

    private static Predicate<Rec> predicate(String f) {
        return switch (f) {
            case "Waiting for documents" -> a -> ApplicationDao.STAGE_WAITING_DOCS.equals(a.s("Stage"));
            case "In verification" -> a -> ApplicationDao.STAGE_IN_VERIFICATION.equals(a.s("Stage"));
            case "Ready for decision" -> a -> ApplicationDao.STAGE_READY.equals(a.s("Stage"))
                    || ApplicationDao.STAGE_NEW.equals(a.s("Stage"));
            case "Over 10 days" -> ApplicationsPage::late;
            case "Approved" -> a -> a.s("Stage").startsWith("Approved");
            case "Rejected" -> a -> "Rejected".equals(a.s("Stage"));
            default -> a -> true;
        };
    }

    @Override
    protected void build() throws Exception {
        crumb("Applications", null);
        crumb(filter.equals(ALL) ? "All applications" : filter, null);
        title("Applications");
        JButton export = Ui.secondary("Export");
        export.addActionListener(Ui.on(this, this::export));
        action(export);

        List<Rec> all = applications.listAll();
        JPanel chips = Ui.flow(8);
        ButtonGroup group = new ButtonGroup();
        for (String f : FILTERS) {
            String label = f.equals(ALL) ? "All " + all.size() : f;
            Chip c = new Chip(label, f.equals("Over 10 days") ? Theme.RED : Theme.TEXT);
            c.setSelected(f.equals(filter));
            c.addActionListener(e -> {
                filter = f;
                reload();
            });
            group.add(c);
            chips.add(c);
        }
        body.push(chips);

        SimpleTable t = body.push(new SimpleTable(
                SimpleTable.Col.left("Application", 0.8), SimpleTable.Col.left("Customer", 1.4),
                SimpleTable.Col.left("Product", 1.3), SimpleTable.Col.right("Amount (VND)", 1.1),
                SimpleTable.Col.left("Submitted", 1), SimpleTable.Col.left("Status", 1.5),
                SimpleTable.Col.left("Waiting for", 1.5), SimpleTable.Col.left("Days waiting", 0.8)));
        shown = all.stream().filter(predicate(filter)).toList();
        for (Rec a : shown) {
            boolean docs = ApplicationDao.STAGE_WAITING_DOCS.equals(a.s("Stage"));
            t.addClickableRow(() -> shell.show("Applications", new ApplicationDetailPage(shell, ctx, a.s("ApplicationID"), 0)),
                    Ui.bold(a.s("ApplicationID")), a.s("Customer"), a.s("Product"), Fmt.money(a.n("Amount")),
                    Fmt.date(a.d("Submitted")), Badge.status(a.s("Stage")), Fmt.or(a.s("WaitingFor")),
                    Badge.days(a.iOrNull("DaysWaiting"), docs));
        }
        t.emptyMessage("No application matches this filter.");
        body.push(Ui.leftRight(Ui.small("Showing " + shown.size() + " of " + all.size() + " applications"),
                Ui.small("Red badge = waiting for a required document for more than 10 days")));
    }

    private void export() throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("applications.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter w = new PrintWriter(fc.getSelectedFile(), StandardCharsets.UTF_8)) {
            w.println("Application,Customer,Product,Amount,Submitted,Status,WaitingFor,DaysWaiting");
            for (Rec a : shown) {
                w.printf("%s,\"%s\",\"%s\",%s,%s,%s,\"%s\",%s%n", a.s("ApplicationID"), a.s("Customer"), a.s("Product"),
                        a.n("Amount").toPlainString(), a.d("Submitted"), a.s("Stage"), Fmt.or(a.s("WaitingFor")),
                        a.get("DaysWaiting") == null ? "" : a.s("DaysWaiting"));
            }
        }
        Ui.info(this, "Exported " + shown.size() + " applications to " + fc.getSelectedFile());
    }
}
