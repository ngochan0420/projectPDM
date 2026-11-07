package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.ApplicationDao;
import com.pdm.loan.dao.CustomerDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

public class CustomerDetailPage extends Page {

    private final OfficerApp.Ctx ctx;
    private final String customerId;
    private final CustomerDao customers = new CustomerDao();
    private final ApplicationDao applications = new ApplicationDao();

    public CustomerDetailPage(Shell shell, OfficerApp.Ctx ctx, String customerId) {
        super(shell);
        this.ctx = ctx;
        this.customerId = customerId;
    }

    static void fillFinancials(Stack c, CustomerDao customers, String customerId) throws Exception {
        Section inc = c.push(new Section("Income sources"));
        SimpleTable it = inc.push(new SimpleTable(SimpleTable.Col.left("Type", 1.2), SimpleTable.Col.left("Employer", 2),
                SimpleTable.Col.right("Monthly amount (VND)", 1.4)));
        for (Rec r : customers.incomes(customerId)) {
            it.addRow(Ui.bold(r.s("IncomeType")), r.s("EmployerName"), Fmt.money(r.n("MonthlyAmount")));
        }
        it.emptyMessage("No income source.");
        Section acc = c.push(new Section("Existing accounts"));
        SimpleTable at = acc.push(new SimpleTable(SimpleTable.Col.left("Bank", 2), SimpleTable.Col.left("Account type", 2)));
        for (Rec r : customers.accounts(customerId)) {
            at.addRow(Ui.bold(r.s("BankName")), r.s("AccountType"));
        }
        at.emptyMessage("No existing account.");
    }

    @Override
    protected void build() throws Exception {
        Rec cu = customers.profile(customerId);
        crumb("Customers", () -> shell.show("Customers", new CustomersPage(shell, ctx)));
        crumb(customerId, null);
        title(cu.s("FullName"));
        titleExtra(new Badge("#" + customerId, Theme.Tone.GRAY));
        Section p = body.push(new Section("Personal details"));
        p.push(Ui.keyValues(2, "Full name", cu.s("FullName"), "Income", Fmt.vnd(cu.n("Income")) + " / month",
                "Employment", cu.s("EmploymentStatus"), "Phone", Fmt.or(cu.s("Phones"))));
        Stack s = body.push(new Stack(18));
        fillFinancials(s, customers, customerId);
        Section o = body.push(new Section("Applications"));
        SimpleTable t = o.push(new SimpleTable(SimpleTable.Col.left("Application", 0.8), SimpleTable.Col.left("Product", 1.4),
                SimpleTable.Col.right("Amount (VND)", 1.1), SimpleTable.Col.left("Submitted", 1), SimpleTable.Col.left("Status", 1.2)));
        for (Rec r : applications.listForCustomer(customerId)) {
            t.addClickableRow(() -> shell.show("Applications", new ApplicationDetailPage(shell, ctx, r.s("ApplicationID"), 0)),
                    Ui.bold(r.s("ApplicationID")), r.s("Product"), Fmt.money(r.n("Amount")), Fmt.date(r.d("Submitted")),
                    Badge.status(r.s("Stage")));
        }
        t.emptyMessage("No application.");
    }
}
