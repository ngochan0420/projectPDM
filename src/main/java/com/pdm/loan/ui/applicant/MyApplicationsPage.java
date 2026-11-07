package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.ApplicationDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import java.util.List;

public class MyApplicationsPage extends Page {

    private final String customerId;
    private final ApplicationDao applications = new ApplicationDao();

    public MyApplicationsPage(Shell shell, String customerId) {
        super(shell);
        this.customerId = customerId;
    }

    @Override
    protected void build() throws Exception {
        crumb("My applications", null);
        title("My applications");
        JButton add = Ui.primary("New application");
        add.addActionListener(e -> shell.show("New application", new NewApplicationPage(shell, customerId, null)));
        action(add);

        List<Rec> apps = applications.listForCustomer(customerId);
        for (Rec a : apps) {
            if (ApplicationDao.STAGE_WAITING_DOCS.equals(a.s("Stage")) && a.i("DaysWaiting") > 10) {
                body.push(new Banner(Theme.Tone.RED, Icons.Kind.WARNING, a.s("ApplicationID") + " has been waiting for "
                        + a.s("WaitingFor") + " for " + a.i("DaysWaiting") + " days. Upload it to continue."));
            }
        }

        SimpleTable t = body.push(new SimpleTable(
                SimpleTable.Col.left("Application", 0.8), SimpleTable.Col.left("Product", 1.3),
                SimpleTable.Col.right("Amount (VND)", 1.1), SimpleTable.Col.left("Submitted", 1),
                SimpleTable.Col.left("Status", 1.5), SimpleTable.Col.left("Waiting for", 1.5),
                SimpleTable.Col.left("Days waiting", 0.8), SimpleTable.Col.right("", 0.6)));
        for (Rec a : apps) {
            Runnable open = () -> shell.show("My applications",
                    new ApplicationStatusPage(shell, customerId, a.s("ApplicationID")));
            JButton o = Ui.small("Open", false);
            o.addActionListener(e -> open.run());
            boolean docs = ApplicationDao.STAGE_WAITING_DOCS.equals(a.s("Stage"));
            t.addClickableRow(open, Ui.bold(a.s("ApplicationID")), a.s("Product"), Fmt.money(a.n("Amount")),
                    Fmt.date(a.d("Submitted")), Badge.status(a.s("Stage")), Fmt.or(a.s("WaitingFor")),
                    Badge.days(a.iOrNull("DaysWaiting"), docs), o);
        }
        t.emptyMessage("You have not submitted any application yet.");
    }
}
