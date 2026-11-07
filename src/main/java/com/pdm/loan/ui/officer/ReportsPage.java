package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.ReportDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.time.Year;

public class ReportsPage extends Page {

    private static final String[] REPORTS = {"Complete documents", "Waiting over N days", "Approval rate", "Multiple active loans"};

    private final OfficerApp.Ctx ctx;
    private final ReportDao reports = new ReportDao();
    private int report;
    private int days = 10;
    private int year = Year.now().getValue();

    public ReportsPage(Shell shell, OfficerApp.Ctx ctx) {
        super(shell);
        this.ctx = ctx;
    }

    private void open(String appId) {
        shell.show("Applications", new ApplicationDetailPage(shell, ctx, appId, 0));
    }

    @Override
    protected void build() throws Exception {
        crumb("Reports", null);
        crumb(REPORTS[report], null);
        title("Reports");

        JPanel chips = Ui.flow(8);
        ButtonGroup g = new ButtonGroup();
        for (int i = 0; i < REPORTS.length; i++) {
            int idx = i;
            Chip c = new Chip("S" + (i + 1) + " · " + REPORTS[i], Theme.TEXT);
            c.setSelected(i == report);
            c.addActionListener(e -> {
                report = idx;
                reload();
            });
            g.add(c);
            chips.add(c);
        }
        body.push(chips);

        switch (report) {
            case 0 -> {
                body.push(Ui.muted("Applications that have supplied every document their product requires."));
                SimpleTable t = body.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8),
                        SimpleTable.Col.left("Customer", 1.5), SimpleTable.Col.left("Product", 1.5), SimpleTable.Col.left("Status", 1)));
                for (Rec r : reports.completeApplications()) {
                    t.addClickableRow(() -> open(r.s("ApplicationID")), Ui.bold(r.s("ApplicationID")), r.s("Customer"),
                            r.s("Product"), r.s("Status"));
                }
                t.emptyMessage("No application is complete.");
            }
            case 1 -> {
                JSpinner sp = new JSpinner(new SpinnerNumberModel(days, 0, 365, 1));
                JButton run = Ui.small("Run", true);
                run.addActionListener(e -> {
                    days = (Integer) sp.getValue();
                    reload();
                });
                body.push(Ui.flow(8, Ui.muted("Waiting for a required document for more than"), sp, Ui.muted("days"), run));
                SimpleTable t = body.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8),
                        SimpleTable.Col.left("Customer", 1.4), SimpleTable.Col.left("Missing document", 2),
                        SimpleTable.Col.left("Submitted", 1), SimpleTable.Col.left("Days waiting", 0.8)));
                for (Rec r : reports.waitingForDocuments(days)) {
                    t.addClickableRow(() -> open(r.s("ApplicationID")), Ui.bold(r.s("ApplicationID")), r.s("Customer"),
                            r.s("MissingDocument"), Fmt.date(r.d("SubmissionDate")), Badge.days(r.i("DaysWaiting"), true));
                }
                t.emptyMessage("No application is waiting that long.");
            }
            case 2 -> {
                JSpinner sp = new JSpinner(new SpinnerNumberModel(year, 2000, 2100, 1));
                sp.setEditor(new JSpinner.NumberEditor(sp, "#"));
                JButton run = Ui.small("Run", true);
                run.addActionListener(e -> {
                    year = (Integer) sp.getValue();
                    reload();
                });
                body.push(Ui.flow(8, Ui.muted("Year"), sp, run));
                SimpleTable t = body.push(new SimpleTable(SimpleTable.Col.left("Product", 1.8),
                        SimpleTable.Col.right("Applications", 1), SimpleTable.Col.right("Approved", 1),
                        SimpleTable.Col.right("Approval rate", 1)));
                for (Rec r : reports.approvalRate(year)) {
                    t.addRow(Ui.bold(r.s("ProductName")), String.valueOf(r.i("TotalApplications")),
                            String.valueOf(r.i("ApprovedCount")),
                            r.get("ApprovalRate") == null ? "—" : r.n("ApprovalRate").stripTrailingZeros().toPlainString() + "%");
                }
            }
            default -> {
                body.push(Ui.muted("Customers with more than one active loan and their total outstanding balance."));
                SimpleTable t = body.push(new SimpleTable(SimpleTable.Col.left("Customer", 0.8),
                        SimpleTable.Col.left("Full name", 1.8), SimpleTable.Col.right("Active loans", 1),
                        SimpleTable.Col.right("Outstanding balance (VND)", 1.5)));
                for (Rec r : reports.customersWithMultipleLoans()) {
                    t.addClickableRow(() -> shell.show("Customers", new CustomerDetailPage(shell, ctx, r.s("CustomerID"))),
                            Ui.bold(r.s("CustomerID")), r.s("FullName"), String.valueOf(r.i("ActiveLoans")),
                            Fmt.money(r.n("Outstanding")));
                }
                t.emptyMessage("No customer has more than one active loan.");
            }
        }
    }
}
