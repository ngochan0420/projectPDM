package com.pdm.loan.ui.officer;

import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.LoginFrame;
import com.pdm.loan.ui.applicant.ProductsPage;
import com.pdm.loan.ui.kit.Shell;

public final class OfficerApp {

    public record Ctx(String employeeId, String name, boolean readOnly) {
    }

    private OfficerApp() {
    }

    public static Shell open(Ctx ctx) {
        Shell[] holder = new Shell[1];
        Shell shell = new Shell("LoanDesk - " + ctx.name(), ctx.readOnly() ? "Admin" : "Officer", ctx.name(),
                Fmt.initials(ctx.name()), () -> LoginFrame.logout(holder[0]));
        holder[0] = shell;
        shell.addNav("Overview", () -> new OverviewPage(shell, ctx));
        shell.addNav("Applications", () -> new ApplicationsPage(shell, ctx, ApplicationsPage.ALL));
        shell.addNav("Customers", () -> new CustomersPage(shell, ctx));
        shell.addNav("Loan products", () -> new ProductsPage(shell, null));
        shell.addNav("Reports", () -> new ReportsPage(shell, ctx));
        shell.show("Overview", new OverviewPage(shell, ctx));
        return shell;
    }
}
