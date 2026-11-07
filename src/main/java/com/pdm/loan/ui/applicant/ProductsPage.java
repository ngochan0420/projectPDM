package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.ProductDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;

public class ProductsPage extends Page {

    private final String customerId;
    private final ProductDao products = new ProductDao();

    public ProductsPage(Shell shell, String customerId) {
        super(shell);
        this.customerId = customerId;
    }

    @Override
    protected void build() throws Exception {
        crumb("Loan products", null);
        title("Loan products");
        if (customerId != null) {
            JButton mine = Ui.secondary("My applications");
            mine.addActionListener(e -> shell.show("My applications", new MyApplicationsPage(shell, customerId)));
            JButton apply = Ui.primary("New application");
            apply.addActionListener(e -> shell.show("New application", new NewApplicationPage(shell, customerId, null)));
            action(mine);
            action(apply);
        }
        body.push(Ui.muted("Compare interest rates, terms and eligibility before you apply."));

        SimpleTable t = body.push(new SimpleTable(
                SimpleTable.Col.left("Product", 1.6),
                SimpleTable.Col.left("Interest rate", 1),
                SimpleTable.Col.left("Term range", 1),
                SimpleTable.Col.left("Eligibility", 3.2),
                SimpleTable.Col.right("", 0.6)));
        for (Rec p : products.listProducts()) {
            Runnable open = () -> shell.show(null, new ProductDetailPage(shell, customerId, p.s("ProductID")));
            JButton view = Ui.small("View", false);
            view.addActionListener(e -> open.run());
            JLabel rules = Ui.text(p.s("EligibilityRules"));
            rules.setToolTipText(p.s("EligibilityRules"));
            t.addClickableRow(open, Ui.bold(p.s("ProductName")), Fmt.rate(p.n("InterestRate")),
                    p.i("MinTerm") + "–" + p.i("MaxTerm") + " months", rules, view);
        }
    }
}
