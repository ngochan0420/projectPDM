package com.pdm.loan.ui.officer;

import com.pdm.loan.dao.CustomerDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

public class CustomersPage extends Page {

    private final OfficerApp.Ctx ctx;
    private final CustomerDao customers = new CustomerDao();

    public CustomersPage(Shell shell, OfficerApp.Ctx ctx) {
        super(shell);
        this.ctx = ctx;
    }

    @Override
    protected void build() throws Exception {
        crumb("Customers", null);
        title("Customers");
        SimpleTable t = body.push(new SimpleTable(SimpleTable.Col.left("Customer", 0.7), SimpleTable.Col.left("Full name", 1.6),
                SimpleTable.Col.left("Employment", 1.1), SimpleTable.Col.left("Phone", 1.1),
                SimpleTable.Col.right("Monthly income (VND)", 1.3), SimpleTable.Col.right("Applications", 0.8)));
        for (Rec r : customers.listCustomers()) {
            t.addClickableRow(() -> shell.show("Customers", new CustomerDetailPage(shell, ctx, r.s("CustomerID"))),
                    Ui.bold(r.s("CustomerID")), r.s("FullName"), r.s("EmploymentStatus"), Fmt.or(r.s("Phone")),
                    Fmt.money(r.n("Income")), String.valueOf(r.i("Applications")));
        }
    }
}
