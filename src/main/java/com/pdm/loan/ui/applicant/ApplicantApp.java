package com.pdm.loan.ui.applicant;

import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.LoginFrame;
import com.pdm.loan.ui.kit.Shell;

/** Window for the APPLICANT (customer): features A1-A7. */
public final class ApplicantApp {

    private ApplicantApp() {
    }

    public static Shell open(String customerId, String customerName) {
        Shell[] holder = new Shell[1];
        Shell shell = new Shell("LoanDesk - " + customerName, "Applicant", customerName, Fmt.initials(customerName),
                () -> LoginFrame.logout(holder[0]));
        holder[0] = shell;
        shell.addNav("Loan products", () -> new ProductsPage(shell, customerId));
        shell.addNav("My profile", () -> new ProfilePage(shell, customerId));
        shell.addNav("New application", () -> new NewApplicationPage(shell, customerId, null));
        shell.addNav("My applications", () -> new MyApplicationsPage(shell, customerId));
        shell.addNav("Repayments", () -> new RepaymentsPage(shell, customerId, null));
        shell.show("My applications", new MyApplicationsPage(shell, customerId));
        return shell;
    }
}
