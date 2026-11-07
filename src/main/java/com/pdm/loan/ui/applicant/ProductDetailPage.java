package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.ProductDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.service.LoanCalculator;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;

public class ProductDetailPage extends Page {

    private final String customerId;
    private final String productId;
    private final ProductDao products = new ProductDao();

    public ProductDetailPage(Shell shell, String customerId, String productId) {
        super(shell);
        this.customerId = customerId;
        this.productId = productId;
    }

    @Override
    protected void build() throws Exception {
        Rec p = products.product(productId);
        crumb("Loan products", () -> shell.show("Loan products", new ProductsPage(shell, customerId)));
        crumb(p.s("ProductName"), null);
        title(p.s("ProductName"));
        titleExtra(new Badge(Fmt.rate(p.n("InterestRate")), Theme.Tone.GRAY));
        JButton back = Ui.secondary("Back to products");
        back.addActionListener(e -> shell.show("Loan products", new ProductsPage(shell, customerId)));
        action(back);
        if (customerId != null) {
            JButton apply = Ui.primary("Apply now");
            apply.addActionListener(e -> shell.show("New application", new NewApplicationPage(shell, customerId, productId)));
            action(apply);
        }

        Section terms = body.push(new Section("Terms"));
        terms.push(Ui.keyValues(2,
                "Interest rate", Fmt.rate(p.n("InterestRate")).replace(" / year", " per year"),
                "Repayment", "Monthly installments",
                "Term range", p.i("MinTerm") + "–" + p.i("MaxTerm") + " months",
                "Processing", "Decision after all documents are supplied"));

        Section elig = body.push(new Section("Eligibility"));
        ItemList rules = elig.push(new ItemList());
        String text = p.s("EligibilityRules");
        if (text == null || text.isBlank()) {
            rules.empty("No special rules.");
        } else {
            for (String r : text.split(";")) {
                rules.add(Theme.GREEN, r.trim(), null);
            }
        }

        Section docs = body.push(new Section("Required documents"));
        ItemList dl = docs.push(new ItemList());
        for (Rec d : products.requiredDocuments(productId)) {
            dl.add(Theme.FAINT, d.s("TypeName"), new JComponent[]{Ui.chip(d.s("Description"))},
                    Ui.muted(d.b("Mandatory") ? "Required" : "Optional"));
        }

        // Estimated monthly payment (interest calculation)
        Section est = body.push(new Section("Estimated monthly payment"));
        JTextField amount = Ui.field("100,000,000");
        JTextField term = Ui.field(String.valueOf(Math.min(Math.max(36, p.i("MinTerm")), p.i("MaxTerm"))));
        JLabel result = new Badge("", Theme.Tone.BLUE);
        result.setFont(Theme.font(Font.BOLD, 13f));
        Runnable calc = () -> {
            try {
                int months = Integer.parseInt(term.getText().trim());
                BigDecimal pay = LoanCalculator.monthlyPayment(Fmt.parseMoney(amount.getText()), p.n("InterestRate"), months);
                result.setText("≈ " + Fmt.money(pay) + " VND / month");
            } catch (Exception ex) {
                result.setText("Enter an amount and a term");
            }
        };
        DocumentListener dlistener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calc.run(); }
            public void removeUpdate(DocumentEvent e) { calc.run(); }
            public void changedUpdate(DocumentEvent e) { calc.run(); }
        };
        amount.getDocument().addDocumentListener(dlistener);
        term.getDocument().addDocumentListener(dlistener);
        calc.run();
        JPanel holder = Ui.flow(0, result);
        est.push(Ui.formGrid(3, Ui.labeled("Amount (VND)", amount), Ui.labeled("Term (months)", term),
                Ui.labeled(" ", holder)));
        est.push(Ui.small("Estimate only. The final installment schedule is created after approval."));
    }
}
