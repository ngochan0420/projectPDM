package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.*;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;
import com.pdm.loan.ui.shared.Uploads;
import com.pdm.loan.ui.shared.Views;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationStatusPage extends Page {

    private final String customerId;
    private final String appId;
    private final ApplicationDao applications = new ApplicationDao();
    private final DocumentDao documents = new DocumentDao();
    private final VerificationDao verification = new VerificationDao();
    private final DecisionDao decisions = new DecisionDao();
    private final LoanDao loans = new LoanDao();

    public ApplicationStatusPage(Shell shell, String customerId, String appId) {
        super(shell);
        this.customerId = customerId;
        this.appId = appId;
    }

    @Override
    protected void build() throws Exception {
        Rec a = applications.get(appId);
        List<Rec> docs = documents.checklist(appId);
        List<Rec> steps = verification.steps(appId);
        Rec decision = decisions.decisionOf(appId);
        Rec loan = loans.loanOf(appId);

        crumb("My applications", () -> shell.show("My applications", new MyApplicationsPage(shell, customerId)));
        crumb(appId, null);
        title(a.s("Product") + " — " + Fmt.vnd(a.n("Amount")));
        titleExtra(new Badge("#" + appId, Theme.Tone.GRAY));
        titleExtra(Ui.avatar(shell.userInitials()));

        boolean missing = a.s("MissingDocs") != null;
        if (missing && decision == null) {
            JButton up = Ui.primary("Upload");
            up.addActionListener(Ui.on(this, () -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Uploads.dropped(this, appId, List.of(fc.getSelectedFile()));
                    reload();
                }
            }));
            action(up);
        }

        // ---- left progress list
        List<Stepper.Step> prog = new ArrayList<>();
        prog.add(new Stepper.Step("Application submitted", Icons.Kind.DONE));
        prog.add(new Stepper.Step("Documents", missing ? Icons.Kind.CURRENT : Icons.Kind.DONE));
        for (Rec s : steps) {
            prog.add(new Stepper.Step(s.s("StepName"), s.s("Result") != null ? Icons.Kind.DONE : Icons.Kind.CURRENT));
        }
        boolean ready = !missing && a.s("PendingSteps") == null;
        prog.add(new Stepper.Step("Decision", decision != null ? Icons.Kind.DONE
                : ready ? Icons.Kind.CURRENT : Icons.Kind.DISABLED));
        prog.add(new Stepper.Step("Disbursement", loan != null && loan.get("DisbursedOn") != null ? Icons.Kind.DONE
                : Icons.Kind.DISABLED));
        Stepper stepper = new Stepper(prog, null);
        stepper.select(missing ? 1 : -1);

        // ---- right side
        Stack c = new Stack(16);
        Integer days = a.iOrNull("DaysWaiting");
        if (decision == null && days != null) {
            boolean late = missing && days > 10;
            String text = "Waiting for: " + a.s("WaitingFor") + " · " + days + " days."
                    + (late ? " Over 10 days, please upload it." : "");
            c.push(new Banner(late ? Theme.Tone.RED : Theme.Tone.BLUE, late ? Icons.Kind.WARNING : Icons.Kind.INFO, text));
        } else if (decision != null) {
            boolean ok = decision.s("DecisionStatus").startsWith("Approved");
            c.push(new Banner(ok ? Theme.Tone.GREEN : Theme.Tone.RED, ok ? Icons.Kind.CHECK : Icons.Kind.WARNING,
                    decision.s("DecisionStatus") + " on " + Fmt.date(decision.d("DecisionDate")),
                    "Reason: " + decision.s("Reason")));
        }

        Section ds = c.push(new Section("Required documents · " + Views.suppliedCount(docs) + " of " + docs.size()
                + " supplied"));
        ds.push(Views.documents(appId, docs, null, decision != null ? null : docType -> Ui.run(this, () -> {
            if (Uploads.chooseAndUpload(this, appId, docType)) {
                reload();
            }
        }), null));
        if (decision == null) {
            ds.push(new DropZone("Upload a document by dragging and dropping",
                    "PDF, JPG or PNG · up to 10 MB each", files -> Ui.run(this, () -> {
                        Uploads.dropped(this, appId, files);
                        reload();
                    })));
        }

        Section vs = c.push(new Section("Verification progress"));
        ItemList list = Views.steps(steps, false);
        if (decision == null) {
            list.add(Theme.FAINT, "Decision", null, Ui.muted("Not decided yet"));
        } else {
            list.add(Theme.GREEN, "Decision", null, Badge.status(decision.s("DecisionStatus")),
                    Ui.muted(Fmt.date(decision.d("DecisionDate"))));
        }
        vs.push(list);

        if (loan != null) {
            JButton sched = Ui.secondary("View repayment schedule");
            sched.addActionListener(e -> shell.show("Repayments", new RepaymentsPage(shell, customerId, loan.s("LoanID"))));
            c.push(Ui.flow(0, sched));
        }
        body.push(Views.withStepper(stepper, c));
    }
}
