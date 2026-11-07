package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.*;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.EligibilityChecker;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.service.LoanCalculator;
import com.pdm.loan.ui.applicant.RepaymentsPage;
import com.pdm.loan.ui.kit.*;
import com.pdm.loan.ui.shared.Uploads;
import com.pdm.loan.ui.shared.Views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDetailPage extends Page {

    public static final int TAB_OVERVIEW = 0, TAB_CUSTOMER = 1, TAB_PRODUCT = 2, TAB_DOCS = 3, TAB_STEPS = 4,
            TAB_DECISION = 5, TAB_DISBURSEMENT = 6, TAB_REPAYMENT = 7;
    private static final String[] TABS = {"Application Overview", "Customer Profile", "Loan Product",
            "Documents Needed", "Verification Steps", "Final Decision", "Disbursement", "Repayment Schedule"};

    private final OfficerApp.Ctx ctx;
    private final String appId;
    private int tab;

    private final ApplicationDao applications = new ApplicationDao();
    private final CustomerDao customers = new CustomerDao();
    private final ProductDao products = new ProductDao();
    private final DocumentDao documents = new DocumentDao();
    private final VerificationDao verification = new VerificationDao();
    private final DecisionDao decisions = new DecisionDao();
    private final LoanDao loans = new LoanDao();
    private final LookupDao lookup = new LookupDao();

    // loaded in build()
    private Rec a;
    private List<Rec> docs;
    private List<Rec> steps;
    private Rec decision;
    private Rec loan;

    public ApplicationDetailPage(Shell shell, OfficerApp.Ctx ctx, String appId, int tab) {
        super(shell);
        this.ctx = ctx;
        this.appId = appId;
        this.tab = tab;
    }

    private boolean canEdit() {
        return !ctx.readOnly();
    }

    private void goTab(int t) {
        tab = t;
        reload();
    }

    @Override
    protected void build() throws Exception {
        a = applications.get(appId);
        docs = documents.checklist(appId);
        steps = verification.steps(appId);
        decision = decisions.decisionOf(appId);
        loan = loans.loanOf(appId);

        crumb("Applications", () -> shell.show("Applications", new ApplicationsPage(shell, ctx, ApplicationsPage.ALL)));
        crumb(appId, null);
        title(a.s("Customer") + " — " + a.s("Product"));
        titleExtra(new Badge("#" + appId, Theme.Tone.GRAY));
        titleExtra(Badge.status(a.s("Stage")));
        if (canEdit() && decision == null) {
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

        boolean missing = a.s("MissingDocs") != null;
        boolean pending = a.s("PendingSteps") != null;
        boolean approved = decision != null && decision.s("DecisionStatus").startsWith("Approved");
        List<Stepper.Step> list = new ArrayList<>();
        list.add(new Stepper.Step(TABS[0], Icons.Kind.DONE));
        list.add(new Stepper.Step(TABS[1], Icons.Kind.DONE));
        list.add(new Stepper.Step(TABS[2], Icons.Kind.DONE));
        list.add(new Stepper.Step(TABS[3], missing ? Icons.Kind.CURRENT : Icons.Kind.DONE));
        list.add(new Stepper.Step(TABS[4], pending || steps.isEmpty() ? Icons.Kind.CURRENT : Icons.Kind.DONE));
        list.add(new Stepper.Step(TABS[5], decision != null ? Icons.Kind.DONE
                : !missing && !pending ? Icons.Kind.CURRENT : Icons.Kind.DISABLED));
        list.add(new Stepper.Step(TABS[6], loan != null ? Icons.Kind.DONE
                : approved ? Icons.Kind.CURRENT : Icons.Kind.DISABLED));
        list.add(new Stepper.Step(TABS[7], loan == null ? Icons.Kind.DISABLED
                : loan.n("Outstanding").signum() > 0 ? Icons.Kind.CURRENT : Icons.Kind.DONE));
        Stepper stepper = new Stepper(list, this::goTab);
        stepper.select(tab);

        Stack c = new Stack(16);
        switch (tab) {
            case TAB_CUSTOMER -> customerTab(c);
            case TAB_PRODUCT -> productTab(c);
            case TAB_DOCS -> documentsTab(c);
            case TAB_STEPS -> stepsTab(c);
            case TAB_DECISION -> decisionTab(c);
            case TAB_DISBURSEMENT -> disbursementTab(c);
            case TAB_REPAYMENT -> repaymentTab(c);
            default -> overviewTab(c);
        }
        body.push(Views.withStepper(stepper, c));
    }

    private void waitingBanner(Stack c) {
        Integer days = a.iOrNull("DaysWaiting");
        if (decision != null || days == null) {
            return;
        }
        boolean late = a.s("MissingDocs") != null && days > 10;
        c.push(new Banner(late ? Theme.Tone.RED : Theme.Tone.BLUE, late ? Icons.Kind.WARNING : Icons.Kind.INFO,
                "Waiting for: " + a.s("WaitingFor") + " · " + days + " days" + (late ? " (over 10 days)" : "")));
    }

    // ------------------------------------------------------------ overview

    private void overviewTab(Stack c) throws Exception {
        waitingBanner(c);
        Rec cu = customers.profile(a.s("CustomerID"));
        Section cs = c.push(new Section("Customer"));
        cs.push(Ui.keyValues(2,
                "Full name", cu.s("FullName"), "Income", Fmt.vnd(cu.n("Income")) + " / month",
                "Phone", Fmt.or(cu.s("Phones")), "Income sources", Fmt.or(cu.s("IncomeTypes")),
                "Employment", cu.s("EmploymentStatus"), "Accounts", Fmt.or(cu.s("Banks"))));
        Section as = c.push(new Section("Application"));
        as.push(Ui.keyValues(2,
                "Product", a.s("Product") + " · " + Fmt.rate(a.n("InterestRate")), "Submitted", Fmt.date(a.d("Submitted")),
                "Amount", Fmt.vnd(a.n("Amount")), "Status", a.s("Stage"),
                "Purpose", a.s("Purpose"), "Documents", Views.suppliedCount(docs) + " of " + docs.size() + " supplied"));
        if (decision != null) {
            Section d = c.push(new Section("Decision"));
            d.push(Ui.keyValues(1, "Decision", decision.s("DecisionStatus"), "Date", Fmt.date(decision.d("DecisionDate")),
                    "Reason", decision.s("Reason")));
        }
        Section vs = c.push(new Section("Verification steps"));
        vs.push(Views.steps(steps, true));
    }

    // ------------------------------------------------------------ customer

    private void customerTab(Stack c) throws Exception {
        String cid = a.s("CustomerID");
        Rec cu = customers.profile(cid);
        Section p = c.push(new Section("Personal details"));
        p.push(Ui.keyValues(2, "Customer ID", cid, "Full name", cu.s("FullName"),
                "Employment", cu.s("EmploymentStatus"), "Phone", Fmt.or(cu.s("Phones"))));
        CustomerDetailPage.fillFinancials(c, customers, cid);
        Section o = c.push(new Section("All applications of this customer"));
        SimpleTable t = o.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8), SimpleTable.Col.left("Product", 1.4),
                SimpleTable.Col.right("Amount (VND)", 1.1), SimpleTable.Col.left("Submitted", 1), SimpleTable.Col.left("Status", 1.2)));
        for (Rec r : applications.listForCustomer(cid)) {
            t.addClickableRow(() -> shell.show("Applications", new ApplicationDetailPage(shell, ctx, r.s("ApplicationID"), 0)),
                    Ui.bold(r.s("ApplicationID")), r.s("Product"), Fmt.money(r.n("Amount")), Fmt.date(r.d("Submitted")),
                    Badge.status(r.s("Stage")));
        }
    }

    // ------------------------------------------------------------- product

    private void productTab(Stack c) throws Exception {
        Rec p = products.product(a.s("ProductID"));
        Section s = c.push(new Section("Loan product"));
        s.push(Ui.keyValues(2, "Product", p.s("ProductName"), "Interest rate", Fmt.rate(p.n("InterestRate")),
                "Term range", p.i("MinTerm") + "–" + p.i("MaxTerm") + " months", "Requested amount", Fmt.vnd(a.n("Amount"))));
        Section e = c.push(new Section("Eligibility check"));
        ItemList el = e.push(new ItemList());
        Rec cu = customers.profile(a.s("CustomerID"));
        for (EligibilityChecker.Check ch : EligibilityChecker.check(p.s("EligibilityRules"), cu.n("Income"))) {
            Color dot = ch.passed() == null ? Theme.FAINT : ch.passed() ? Theme.GREEN : Theme.RED;
            el.add(dot, ch.rule(), null, Ui.muted(ch.detail()),
                    ch.passed() == null ? Badge.status("Manual check") : Badge.status(ch.passed() ? "Passed" : "Failed"));
        }
        Section d = c.push(new Section("Required documents"));
        ItemList dl = d.push(new ItemList());
        for (Rec r : products.requiredDocuments(a.s("ProductID"))) {
            dl.add(Theme.FAINT, r.s("TypeName"), new JComponent[]{Ui.chip(r.s("Description"))},
                    Ui.muted(r.b("Mandatory") ? "Required" : "Optional"));
        }
    }

    // ----------------------------------------------------------- documents

    private void documentsTab(Stack c) throws Exception {
        if (canEdit() && decision == null) {
            c.push(new DropZone("Upload a document by dragging and dropping",
                    "Drag and drop anywhere on this box to attach a document to this application",
                    files -> Ui.run(this, () -> {
                        Uploads.dropped(this, appId, files);
                        reload();
                    })));
        }
        int missing = documents.missingMandatoryCount(appId);
        if (decision == null) {
            c.push(missing > 0
                    ? new Banner(Theme.Tone.PURPLE, Icons.Kind.LOCK, "Final decision is locked: " + missing + " of "
                    + docs.size() + " required documents is missing")
                    : new Banner(Theme.Tone.GREEN, Icons.Kind.CHECK, "All " + docs.size() + " required documents are supplied."));
        }
        Section s = c.push(new Section("Required documents"));
        Integer days = a.iOrNull("DaysWaiting");
        s.push(Views.documents(appId, docs, days, null, !canEdit() || decision != null ? null : d -> {
            boolean ok = d.b("Supplied");
            JButton b = Ui.small(ok ? "Mark missing" : "Mark supplied", false);
            b.addActionListener(Ui.on(this, () -> {
                documents.setSupplied(appId, d.s("DocTypeID"), !ok);
                reload();
            }));
            return b;
        }));
    }

    // --------------------------------------------------------------- steps

    private void stepsTab(Stack c) throws Exception {
        boolean allDone = !steps.isEmpty() && a.s("PendingSteps") == null;
        if (allDone) {
            boolean docsOk = a.s("MissingDocs") == null;
            c.push(new Banner(docsOk ? Theme.Tone.GREEN : Theme.Tone.BLUE, docsOk ? Icons.Kind.CHECK : Icons.Kind.INFO,
                    "All " + steps.size() + " verification steps completed · " + Views.suppliedCount(docs) + " of "
                            + docs.size() + " required documents supplied"));
            Section r = c.push(new Section("Verification results"));
            SimpleTable t = r.push(new SimpleTable(SimpleTable.Col.left("Step type", 1.3), SimpleTable.Col.left("Officer", 1.2),
                    SimpleTable.Col.left("Date", 0.9), SimpleTable.Col.left("Result", 1.1), SimpleTable.Col.left("Comment", 2)));
            int passed = 0;
            for (Rec s : steps) {
                if (s.s("Result").startsWith("Passed")) {
                    passed++;
                }
                JLabel com = Ui.text(Fmt.or(s.s("Comments")));
                com.setToolTipText(s.s("Comments"));
                t.addRow(Ui.bold(s.s("StepName")), s.s("Officer"), Fmt.date(s.d("VerificationDate")),
                        Badge.status(s.s("Result")), com);
            }
            Section sum = c.push(new Section("Summary"));
            sum.push(Ui.keyValues(2, "Steps passed", passed + " of " + steps.size(),
                    "Overall result", docsOk ? "Ready for final decision" : "Waiting for documents",
                    "Documents supplied", Views.suppliedCount(docs) + " of " + docs.size(),
                    "Last updated", Fmt.date(a.d("LastActivity"))));
            JButton go = Ui.primary("Go to final decision");
            go.addActionListener(e -> goTab(TAB_DECISION));
            c.push(Ui.flow(8, go));
        } else {
            Section s = c.push(new Section("Verification steps"));
            s.push(Views.steps(steps, true));
        }
        if (!canEdit() || decision != null) {
            return;
        }

        // L4: record a result for a pending step
        List<Rec> pendingSteps = steps.stream().filter(s -> s.s("Result") == null).toList();
        if (!pendingSteps.isEmpty()) {
            JComboBox<String> which = new JComboBox<>();
            for (Rec s : pendingSteps) {
                which.addItem(s.s("StepID") + " · " + s.s("StepName") + " (" + s.s("Officer") + ")");
            }
            Section rec = c.push(new Section("Record result"));
            JComboBox<String> result = new JComboBox<>(VerificationDao.RESULTS);
            JTextArea comments = Ui.area("", 3);
            rec.push(Ui.formGrid(3, Ui.labeled("Step", which), Ui.labeled("Result", result),
                    Ui.labeled("Verification date", Ui.readOnly(Fmt.date(LocalDate.now())))));
            rec.push(Ui.labeled("Comments", Ui.areaScroll(comments)));
            JButton save = Ui.primary("Save result");
            save.addActionListener(Ui.on(this, () -> {
                Rec s = pendingSteps.get(which.getSelectedIndex());
                verification.record(s.s("StepID"), ctx.employeeId(), (String) result.getSelectedItem(), comments.getText());
                reload();
            }));
            rec.push(Ui.flow(0, save));
        }

        // L3: assign a new step
        Section as = c.push(new Section("Assign a new step"));
        JComboBox<Item> type = new JComboBox<>(lookup.stepTypes().toArray(new Item[0]));
        JComboBox<Item> officer = new JComboBox<>(lookup.employees().toArray(new Item[0]));
        for (int i = 0; i < officer.getItemCount(); i++) {
            if (officer.getItemAt(i).id().equals(ctx.employeeId())) {
                officer.setSelectedIndex(i);
            }
        }
        as.push(Ui.formGrid(2, Ui.labeled("Step type", type), Ui.labeled("Officer", officer)));
        JButton assign = Ui.secondary("Assign step");
        assign.addActionListener(Ui.on(this, () -> {
            verification.assign(appId, ((Item) type.getSelectedItem()).id(), ((Item) officer.getSelectedItem()).id());
            reload();
        }));
        as.push(Ui.flow(0, assign));
    }

    // ------------------------------------------------------------ decision

    private void decisionTab(Stack c) throws Exception {
        if (decision != null) {
            boolean ok = decision.s("DecisionStatus").startsWith("Approved");
            c.push(new Banner(ok ? Theme.Tone.GREEN : Theme.Tone.RED, ok ? Icons.Kind.CHECK : Icons.Kind.WARNING,
                    "Decision recorded: " + decision.s("DecisionStatus")));
            Section s = c.push(new Section("Decision"));
            s.push(Ui.keyValues(1, "Decision", decision.s("DecisionStatus"), "Date", Fmt.date(decision.d("DecisionDate")),
                    "Reason", decision.s("Reason")));
            if (ok) {
                JButton next = Ui.primary("Go to disbursement");
                next.addActionListener(e -> goTab(TAB_DISBURSEMENT));
                c.push(Ui.flow(0, next));
            }
            return;
        }
        int missing = documents.missingMandatoryCount(appId);
        int pending = verification.pendingCount(appId);
        boolean locked = missing > 0 || pending > 0;
        if (locked) {
            String why = missing > 0 ? missing + " required document(s) missing (" + a.s("MissingDocs") + ")"
                    : pending + " verification step(s) pending (" + a.s("PendingSteps") + ")";
            c.push(new Banner(Theme.Tone.PURPLE, Icons.Kind.LOCK, "Decision locked: " + why));
        } else {
            c.push(new Banner(Theme.Tone.GREEN, Icons.Kind.CHECK,
                    "All " + docs.size() + " required documents are supplied. You can make a final decision."));
        }

        Section s = c.push(new Section("Decision"));
        ButtonGroup group = new ButtonGroup();
        JPanel options = new JPanel(new GridLayout(1, 3, 12, 0));
        options.setOpaque(false);
        JRadioButton[] radios = new JRadioButton[DecisionDao.STATUSES.length];
        JTextField conditions = Ui.field("");
        conditions.putClientProperty("JTextField.placeholderText", "e.g. add a guarantor");
        conditions.setEnabled(false);
        for (int i = 0; i < radios.length; i++) {
            JRadioButton r = new JRadioButton(DecisionDao.STATUSES[i]);
            r.setOpaque(false);
            r.setFont(Theme.font(Font.PLAIN, 12.5f));
            r.setEnabled(!locked && canEdit());
            r.addActionListener(e -> conditions.setEnabled(r.getText().equals("Approved with conditions")));
            group.add(r);
            radios[i] = r;
            RoundPanel box = new RoundPanel(new BorderLayout());
            box.setBorder(new EmptyBorder(8, 10, 8, 10));
            box.add(r);
            options.add(box);
        }
        s.push(options);
        JTextArea reason = Ui.area("", 4);
        reason.setEnabled(!locked && canEdit());
        s.push(Ui.labeled("Reason", Ui.areaScroll(reason)));
        s.push(Ui.labeled("Conditions (only for approval with conditions)", conditions));
        JButton back = Ui.secondary("Back");
        back.addActionListener(e -> goTab(TAB_STEPS));
        JButton submit = Ui.primary("Submit decision");
        submit.setEnabled(!locked && canEdit());
        submit.addActionListener(Ui.on(this, () -> {
            String status = null;
            for (JRadioButton r : radios) {
                if (r.isSelected()) {
                    status = r.getText();
                }
            }
            if (status == null) {
                throw new IllegalArgumentException("Please choose Approved, Rejected or Approved with conditions.");
            }
            String why = Ui.require(reason.getText(), "explain the decision");
            if (status.equals("Approved with conditions")) {
                why += " | Conditions: " + Ui.require(conditions.getText(), "enter the conditions");
            }
            if (Ui.confirm(this, "Record '" + status + "' for " + appId + "? This cannot be changed.")) {
                decisions.decide(appId, status, why);
                goTab(TAB_DECISION);
            }
        }));
        s.push(Ui.flow(8, back, submit));
        s.push(Ui.small(locked ? "Available once all required documents are supplied and all steps are done."
                : "The decision and its reason are recorded with today's date."));
    }

    // -------------------------------------------------------- disbursement

    private void disbursementTab(Stack c) throws Exception {
        boolean approved = decision != null && decision.s("DecisionStatus").startsWith("Approved");
        if (loan == null) {
            if (!approved) {
                c.push(new Banner(Theme.Tone.GRAY, Icons.Kind.LOCK, "Available after the application is approved."));
                return;
            }
            Rec p = products.product(a.s("ProductID"));
            c.push(new Banner(Theme.Tone.GREEN, Icons.Kind.CHECK, "Application approved. Create the loan and disburse it."));
            Section s = c.push(new Section("Create loan & disburse"));
            JTextField amount = Ui.field(Fmt.money(a.n("Amount")));
            int def = Math.min(Math.max(12, p.i("MinTerm")), p.i("MaxTerm"));
            JSpinner term = new JSpinner(new SpinnerNumberModel(def, p.i("MinTerm"), p.i("MaxTerm"), 1));
            JLabel est = Ui.muted("");
            Runnable calc = () -> {
                try {
                    est.setText("≈ " + Fmt.money(LoanCalculator.monthlyPayment(Fmt.parseMoney(amount.getText()),
                            p.n("InterestRate"), (Integer) term.getValue())) + " VND / month at "
                            + Fmt.rate(p.n("InterestRate")));
                } catch (Exception ex) {
                    est.setText("Enter a valid amount");
                }
            };
            term.addChangeListener(e -> calc.run());
            amount.addActionListener(e -> calc.run());
            amount.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    calc.run();
                }
            });
            calc.run();
            s.push(Ui.formGrid(2, Ui.labeled("Loan amount (VND)", amount),
                    Ui.labeled("Term (months, " + p.i("MinTerm") + "–" + p.i("MaxTerm") + ")", term)));
            s.push(est);
            JButton create = Ui.primary("Create loan & disburse");
            create.setEnabled(canEdit());
            create.addActionListener(Ui.on(this, () -> {
                BigDecimal amt = Fmt.parseMoney(amount.getText());
                if (Ui.confirm(this, "Disburse " + Fmt.vnd(amt) + " over " + term.getValue() + " months?")) {
                    loans.createLoan(appId, amt, (Integer) term.getValue());
                    goTab(TAB_DISBURSEMENT);
                }
            }));
            s.push(Ui.flow(0, create));
            return;
        }
        Section sum = c.push(new Section("Loan summary"));
        sum.push(Ui.keyValues(2, "Approved amount", Fmt.vnd(loan.n("LoanAmount")),
                "Disbursed on", Fmt.date(loan.d("DisbursedOn")),
                "Interest rate", Fmt.rate(loan.n("InterestRate")), "Outstanding balance", Fmt.vnd(loan.n("Outstanding")),
                "Installments", String.valueOf(loan.i("Installments")),
                "Loan status", loan.n("Outstanding").signum() > 0 ? "Active" : "Closed"));
        Section d = c.push(new Section("Disbursement"));
        ItemList dl = d.push(new ItemList());
        for (Rec r : loans.disbursements(loan.s("LoanID"))) {
            dl.add(Theme.GREEN, Fmt.vnd(r.n("DisbursementAmount")) + " disbursed", new JComponent[]{Ui.chip(r.s("DisbursementID"))},
                    Ui.muted(Fmt.date(r.d("DisbursementDate"))));
        }
        Section rs = c.push(new Section("Repayment schedule"));
        ItemList rl = rs.push(new ItemList());
        List<Rec> sched = loans.schedule(loan.s("LoanID"));
        for (int i = 0; i < Math.min(5, sched.size()); i++) {
            Rec r = sched.get(i);
            boolean paid = "Paid".equals(r.s("Status"));
            rl.add(paid ? Theme.GREEN : Theme.BLUE, "Installment " + r.i("InstallmentID") + " · " + Fmt.vnd(r.n("AmountDue")),
                    null, Badge.status(paid ? "Paid" : "Upcoming"), Ui.muted(Fmt.date(r.d("DueDate"))));
        }
        if (sched.size() > 5) {
            JButton more = Ui.link("... " + (sched.size() - 5) + " more installments", Theme.MUTED);
            more.addActionListener(e -> goTab(TAB_REPAYMENT));
            rs.push(more);
        }
    }

    // ----------------------------------------------------------- repayment

    private void repaymentTab(Stack c) throws Exception {
        if (loan == null) {
            c.push(new Banner(Theme.Tone.GRAY, Icons.Kind.LOCK, "Available after the loan is disbursed."));
            return;
        }
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(Ui.statCard("Loan amount", Fmt.vnd(loan.n("LoanAmount"))));
        cards.add(Ui.statCard("Interest rate", Fmt.rate(loan.n("InterestRate"))));
        cards.add(Ui.statCard("Installments", String.valueOf(loan.i("Installments"))));
        cards.add(Ui.statCard("Outstanding balance", Fmt.vnd(loan.n("Outstanding"))));
        c.push(cards);
        c.push(RepaymentsPage.scheduleTable(loans.schedule(loan.s("LoanID")), !canEdit() ? null : inst -> Ui.run(this, () -> {
            loans.markPaid(loan.s("LoanID"), inst);
            reload();
        })));
    }
}
