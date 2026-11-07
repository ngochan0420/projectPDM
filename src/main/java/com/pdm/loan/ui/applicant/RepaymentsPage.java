package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.LoanDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RepaymentsPage extends Page {

    private final String customerId;
    private String loanId;
    private final LoanDao loans = new LoanDao();

    public RepaymentsPage(Shell shell, String customerId, String loanId) {
        super(shell);
        this.customerId = customerId;
        this.loanId = loanId;
    }

    @Override
    protected void build() throws Exception {
        List<Rec> mine = loans.loansOf(customerId);
        crumb("Repayments", null);
        if (mine.isEmpty()) {
            title("Repayment schedule");
            body.push(new Banner(Theme.Tone.GRAY, Icons.Kind.INFO,
                    "You have no loan yet. The schedule appears here after an application is approved and disbursed."));
            return;
        }
        Rec loan = mine.get(0);
        for (Rec l : mine) {
            if (l.s("LoanID").equals(loanId)) {
                loan = l;
            }
        }
        loanId = loan.s("LoanID");
        crumb(loan.s("ApplicationID"), null);
        title("Repayment schedule");
        titleExtra(new Badge("#" + loan.s("ApplicationID"), Theme.Tone.GRAY));
        titleExtra(Ui.avatar(shell.userInitials()));

        if (mine.size() > 1) {
            ButtonGroup g = new ButtonGroup();
            JPanel chips = Ui.flow(8);
            for (Rec l : mine) {
                Chip c = new Chip(l.s("ApplicationID") + " · " + l.s("ProductName"), Theme.TEXT);
                c.setSelected(l.s("LoanID").equals(loanId));
                c.addActionListener(e -> {
                    loanId = l.s("LoanID");
                    reload();
                });
                g.add(c);
                chips.add(c);
            }
            body.push(chips);
        }

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(Ui.statCard("Loan amount", Fmt.vnd(loan.n("LoanAmount"))));
        cards.add(Ui.statCard("Interest rate", Fmt.rate(loan.n("InterestRate"))));
        cards.add(Ui.statCard("Installments", String.valueOf(loan.i("Installments"))));
        cards.add(Ui.statCard("Outstanding balance", Fmt.vnd(loan.n("Outstanding"))));
        body.push(cards);

        body.push(scheduleTable(loans.schedule(loanId), null));
    }

    public static SimpleTable scheduleTable(List<Rec> rows, java.util.function.IntConsumer onPay) {
        SimpleTable t = new SimpleTable(
                SimpleTable.Col.left("No.", 0.5), SimpleTable.Col.left("Due date", 1.2),
                SimpleTable.Col.right("Amount due (VND)", 1.3), SimpleTable.Col.right("Remaining balance", 1.4),
                SimpleTable.Col.left("Status", 0.9), SimpleTable.Col.right("", onPay == null ? 0.01 : 0.9));
        BigDecimal remaining = BigDecimal.ZERO;
        for (Rec r : rows) {
            if (!"Paid".equals(r.s("Status"))) {
                remaining = remaining.add(r.n("AmountDue"));
            }
        }
        for (Rec r : rows) {
            boolean paid = "Paid".equals(r.s("Status"));
            if (!paid) {
                remaining = remaining.subtract(r.n("AmountDue"));
            }
            LocalDate due = r.d("DueDate");
            String status = paid ? "Paid" : due.isBefore(LocalDate.now()) ? "Overdue" : "Upcoming";
            JComponent action = new JLabel("");
            if (onPay != null && !paid) {
                JButton pay = Ui.small("Mark as paid", false);
                pay.addActionListener(e -> onPay.accept(r.i("InstallmentID")));
                action = pay;
            }
            t.addRow(Ui.bold(String.valueOf(r.i("InstallmentID"))), Fmt.date(due), Fmt.money(r.n("AmountDue")),
                    Fmt.money(remaining.max(BigDecimal.ZERO)), Badge.status(status), action);
        }
        t.emptyMessage("No installments.");
        return t;
    }
}
