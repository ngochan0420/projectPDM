package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.ApplicationDao;
import com.pdm.loan.dao.ReportDao;
import com.pdm.loan.dao.VerificationDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class OverviewPage extends Page {

    private final OfficerApp.Ctx ctx;
    private final ApplicationDao applications = new ApplicationDao();
    private final ReportDao reports = new ReportDao();
    private final VerificationDao verification = new VerificationDao();

    public OverviewPage(Shell shell, OfficerApp.Ctx ctx) {
        super(shell);
        this.ctx = ctx;
    }

    private void open(String appId) {
        shell.show("Applications", new ApplicationDetailPage(shell, ctx, appId, 0));
    }

    @Override
    protected void build() throws Exception {
        crumb("Overview", null);
        title("Overview");
        titleExtra(Ui.muted("Welcome, " + ctx.name()));

        List<Rec> apps = applications.listAll();
        Map<String, Integer> counts = ApplicationDao.countByStage(apps);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(Ui.statCard("Total applications", String.valueOf(apps.size())));
        cards.add(Ui.statCard("Waiting for documents", String.valueOf(counts.getOrDefault(ApplicationDao.STAGE_WAITING_DOCS, 0))));
        cards.add(Ui.statCard("In verification", String.valueOf(counts.getOrDefault(ApplicationDao.STAGE_IN_VERIFICATION, 0))));
        cards.add(Ui.statCard("Ready for decision", String.valueOf(counts.getOrDefault(ApplicationDao.STAGE_READY, 0)
                + counts.getOrDefault(ApplicationDao.STAGE_NEW, 0))));
        body.push(cards);

        // S2
        List<Rec> late = reports.waitingForDocuments(10);
        if (!late.isEmpty()) {
            body.push(new Banner(Theme.Tone.RED, Icons.Kind.WARNING, late.size()
                    + " application(s) have been waiting for a required document for more than 10 days."));
        }
        Section s2 = body.push(new Section("Waiting for a document over 10 days"));
        SimpleTable t = s2.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8), SimpleTable.Col.left("Customer", 1.4),
                SimpleTable.Col.left("Missing document", 2), SimpleTable.Col.left("Submitted", 1),
                SimpleTable.Col.left("Days waiting", 0.8)));
        for (Rec r : late) {
            t.addClickableRow(() -> open(r.s("ApplicationID")), Ui.bold(r.s("ApplicationID")), r.s("Customer"),
                    r.s("MissingDocument"), Fmt.date(r.d("SubmissionDate")), Badge.days(r.i("DaysWaiting"), true));
        }
        t.emptyMessage("No application is late. 🎉".replace(" 🎉", ""));

        if (!ctx.readOnly()) {
            Section mine = body.push(new Section("My pending verification steps"));
            SimpleTable mt = mine.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8),
                    SimpleTable.Col.left("Customer", 1.4), SimpleTable.Col.left("Step", 1.6),
                    SimpleTable.Col.left("Submitted", 1), SimpleTable.Col.right("", 0.6)));
            for (Rec r : verification.pendingFor(ctx.employeeId())) {
                JButton o = Ui.small("Open", false);
                o.addActionListener(e -> shell.show("Applications",
                        new ApplicationDetailPage(shell, ctx, r.s("ApplicationID"), ApplicationDetailPage.TAB_STEPS)));
                mt.addClickableRow(() -> shell.show("Applications",
                                new ApplicationDetailPage(shell, ctx, r.s("ApplicationID"), ApplicationDetailPage.TAB_STEPS)),
                        Ui.bold(r.s("ApplicationID")), r.s("Customer"), r.s("StepName"), Fmt.date(r.d("SubmissionDate")), o);
            }
            mt.emptyMessage("Nothing assigned to you right now.");
        }
    }
}
