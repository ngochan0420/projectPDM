package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.ApplicationDao;
import com.pdm.loan.dao.CustomerDao;
import com.pdm.loan.dao.DocumentDao;
import com.pdm.loan.dao.ProductDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.EligibilityChecker;
import com.pdm.loan.service.FileStore;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;
import com.pdm.loan.ui.shared.Uploads;
import com.pdm.loan.ui.shared.Views;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class NewApplicationPage extends Page {

    private static final String[] STEPS = {"Choose product", "Application details", "Required documents",
            "Upload documents", "Review & submit"};

    private final String customerId;
    private final ProductDao products = new ProductDao();
    private final CustomerDao customers = new CustomerDao();
    private final ApplicationDao applications = new ApplicationDao();

    // wizard state (kept while moving between steps)
    private int step;
    private String productId;
    private String amountText = "";
    private String purposeText = "";
    private final Map<String, File> files = new LinkedHashMap<>(); // DocTypeID -> chosen file

    public NewApplicationPage(Shell shell, String customerId, String productId) {
        super(shell);
        this.customerId = customerId;
        this.productId = productId;
        this.step = productId == null ? 0 : 1;
    }

    private void go(int newStep) {
        step = newStep;
        reload();
    }

    @Override
    protected void build() throws Exception {
        Rec me = customers.profile(customerId);
        crumb("New application", () -> go(0));
        crumb(STEPS[step], null);
        title("New loan application");
        titleExtra(new Badge("Draft", Theme.Tone.GRAY));
        titleExtra(Ui.avatar(Fmt.initials(me.s("FullName"))));

        List<Stepper.Step> steps = new ArrayList<>();
        for (int i = 0; i < STEPS.length; i++) {
            steps.add(new Stepper.Step(STEPS[i],
                    i < step ? Icons.Kind.DONE : i == step ? Icons.Kind.CURRENT : Icons.Kind.DISABLED));
        }
        Stepper stepper = new Stepper(steps, i -> {
            if (i < step) {
                go(i);
            }
        });
        stepper.select(step);

        Stack content = new Stack(16);
        switch (step) {
            case 0 -> chooseProduct(content);
            case 1 -> details(content, me);
            case 2 -> requiredDocs(content);
            case 3 -> upload(content);
            default -> review(content, me);
        }
        body.push(Views.withStepper(stepper, content));
    }

    // ------------------------------------------------------------- step 1

    private void chooseProduct(Stack c) throws Exception {
        c.push(Ui.muted("Choose the loan product you want to apply for."));
        SimpleTable t = c.push(new SimpleTable(SimpleTable.Col.left("Product", 1.6), SimpleTable.Col.left("Interest rate", 1),
                SimpleTable.Col.left("Term range", 1), SimpleTable.Col.right("", 0.8)));
        for (Rec p : products.listProducts()) {
            boolean chosen = p.s("ProductID").equals(productId);
            JButton pick = Ui.small(chosen ? "Selected" : "Choose", chosen);
            Runnable choose = () -> {
                if (!p.s("ProductID").equals(productId)) {
                    files.clear();
                }
                productId = p.s("ProductID");
                go(1);
            };
            pick.addActionListener(e -> choose.run());
            t.addClickableRow(choose, Ui.bold(p.s("ProductName")), Fmt.rate(p.n("InterestRate")),
                    p.i("MinTerm") + "–" + p.i("MaxTerm") + " months", pick);
        }
    }

    // ------------------------------------------------------------- step 2

    private void details(Stack c, Rec me) throws Exception {
        Rec p = products.product(productId);
        c.push(new Banner(Theme.Tone.BLUE, Icons.Kind.INFO, p.s("ProductName") + " · " + Fmt.rate(p.n("InterestRate"))
                + " · " + p.i("MinTerm") + "–" + p.i("MaxTerm") + " months"));
        JComboBox<String> product = new JComboBox<>();
        List<Rec> all = products.listProducts();
        for (Rec r : all) {
            product.addItem(r.s("ProductName"));
            if (r.s("ProductID").equals(productId)) {
                product.setSelectedIndex(product.getItemCount() - 1);
            }
        }
        JTextField amount = Ui.field(amountText);
        JTextArea purpose = Ui.area(purposeText, 4);
        c.push(Ui.formGrid(2,
                Ui.labeled("Applicant", Ui.readOnly(me.s("FullName"))),
                Ui.labeled("Application date", Ui.readOnly(Fmt.date(LocalDate.now()))),
                Ui.labeled("Loan product", product),
                Ui.labeled("Requested amount (VND)", amount)));
        c.push(Ui.labeled("Purpose", Ui.areaScroll(purpose)));

        JButton cancel = Ui.secondary("Cancel");
        cancel.addActionListener(e -> shell.show("My applications", new MyApplicationsPage(shell, customerId)));
        JButton next = Ui.primary("Next: required documents");
        next.addActionListener(Ui.on(this, () -> {
            BigDecimal a = Fmt.parseMoney(amount.getText());
            if (a.signum() <= 0) {
                throw new IllegalArgumentException("Requested amount must be greater than 0.");
            }
            String pid = all.get(product.getSelectedIndex()).s("ProductID");
            if (!pid.equals(productId)) {
                files.clear();
            }
            productId = pid;
            amountText = Fmt.money(a);
            purposeText = Ui.require(purpose.getText(), "enter the purpose of the loan");
            go(2);
        }));
        c.push(Ui.flow(8, cancel, next));
    }

    // ------------------------------------------------------------- step 3

    private void requiredDocs(Stack c) throws Exception {
        Rec p = products.product(productId);
        List<Rec> docs = products.requiredDocuments(productId);
        c.push(new Banner(Theme.Tone.BLUE, Icons.Kind.INFO,
                p.s("ProductName") + " requires " + docs.size() + " document(s). Prepare them before you upload."));
        Section s = c.push(new Section("Required documents · " + docs.size()));
        ItemList list = s.push(new ItemList());
        for (Rec d : docs) {
            list.add(Theme.FAINT, d.s("TypeName"), new JComponent[]{Ui.chip(d.s("Description"))},
                    Badge.status(d.b("Mandatory") ? "Required" : "Optional"));
        }
        if (docs.isEmpty()) {
            list.empty("No document is required for this product.");
        }
        Section acc = c.push(new Section("Accepted files"));
        ItemList al = acc.push(new ItemList());
        al.add(Theme.FAINT, "PDF, JPG or PNG", null, Ui.muted("Up to 10 MB each"));
        c.push(Ui.small("You can submit with missing documents and upload them later. "
                + "A decision is made only when all required documents are supplied."));
        c.push(nav(1, "Next: upload documents", 3));
    }

    // ------------------------------------------------------------- step 4

    private void upload(Stack c) throws Exception {
        List<Rec> docs = products.requiredDocuments(productId);
        c.push(new DropZone("Upload documents by dragging and dropping",
                "PDF, JPG or PNG · up to 10 MB each · drop files here or click to choose",
                dropped -> Ui.run(this, () -> {
                    for (File f : dropped) {
                        FileStore.validate(f);
                        Rec doc = Uploads.askDocType(this, f, withSupplied(docs));
                        if (doc != null) {
                            files.put(doc.s("DocTypeID"), f);
                        }
                    }
                    reload();
                })));
        Section s = c.push(new Section("Required documents · " + files.size() + " of " + docs.size() + " supplied"));
        ItemList list = s.push(new ItemList());
        for (Rec d : docs) {
            File f = files.get(d.s("DocTypeID"));
            JButton choose = Ui.small(f == null ? "Upload" : "Replace", false);
            choose.addActionListener(Ui.on(this, () -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    FileStore.validate(fc.getSelectedFile());
                    files.put(d.s("DocTypeID"), fc.getSelectedFile());
                    reload();
                }
            }));
            if (f != null) {
                list.add(Theme.GREEN, d.s("TypeName"), new JComponent[]{Ui.chip("1 file")}, Ui.muted(f.getName()), choose);
            } else {
                list.add(Theme.RED, d.s("TypeName"), new JComponent[0], Ui.colored("Not uploaded yet", Theme.RED), choose);
            }
        }
        c.push(Ui.small("You can submit with missing documents and upload them later. "
                + "A decision is made only when all required documents are supplied."));
        c.push(nav(2, "Next: review & submit", 4));
    }

    private List<Rec> withSupplied(List<Rec> docs) {
        List<Rec> list = new ArrayList<>();
        for (Rec d : docs) {
            Map<String, Object> m = new LinkedHashMap<>(d.values());
            m.put("Supplied", files.containsKey(d.s("DocTypeID")));
            list.add(new Rec(m));
        }
        return list;
    }

    // ------------------------------------------------------------- step 5

    private void review(Stack c, Rec me) throws Exception {
        Rec p = products.product(productId);
        List<Rec> docs = products.requiredDocuments(productId);
        c.push(new Banner(Theme.Tone.BLUE, Icons.Kind.INFO, "You can submit now and upload the missing documents later. "
                + "A decision is made only when all are supplied."));

        Section sum = c.push(new Section("Application summary"));
        sum.push(Ui.keyValues(2,
                "Applicant", me.s("FullName"),
                "Purpose", purposeText,
                "Product", p.s("ProductName") + " · " + Fmt.rate(p.n("InterestRate")),
                "Application date", Fmt.date(LocalDate.now()),
                "Amount", amountText + " VND",
                "Term", p.i("MinTerm") + "–" + p.i("MaxTerm") + " months"));

        // S9: automatic eligibility check against the product rules
        Section elig = c.push(new Section("Eligibility check"));
        ItemList el = elig.push(new ItemList());
        List<EligibilityChecker.Check> checks = EligibilityChecker.check(p.s("EligibilityRules"), me.n("Income"));
        for (EligibilityChecker.Check ch : checks) {
            Color dot = ch.passed() == null ? Theme.FAINT : ch.passed() ? Theme.GREEN : Theme.RED;
            JComponent badge = ch.passed() == null ? Badge.status("Manual check")
                    : Badge.status(ch.passed() ? "Passed" : "Failed");
            el.add(dot, ch.rule(), null, Ui.muted(ch.detail()), badge);
        }
        if (checks.isEmpty()) {
            el.empty("No eligibility rules for this product.");
        }

        int supplied = 0;
        for (Rec d : docs) {
            if (files.containsKey(d.s("DocTypeID"))) {
                supplied++;
            }
        }
        Section ds = c.push(new Section("Documents · " + supplied + " of " + docs.size() + " supplied"));
        ItemList dl = ds.push(new ItemList());
        for (Rec d : docs) {
            File f = files.get(d.s("DocTypeID"));
            if (f != null) {
                dl.add(Theme.GREEN, d.s("TypeName"), new JComponent[]{Ui.chip("1 file")}, Ui.muted(f.getName()));
            } else {
                dl.add(Theme.RED, d.s("TypeName"), new JComponent[0], Ui.colored("Missing · upload later", Theme.RED));
            }
        }

        JButton back = Ui.secondary("Back");
        back.addActionListener(e -> go(3));
        JButton submit = Ui.primary("Submit application");
        submit.addActionListener(Ui.on(this, () -> {
            String appId = applications.submit(customerId, productId, Fmt.parseMoney(amountText), purposeText,
                    files.keySet());
            DocumentDao docDao = new DocumentDao();
            for (Map.Entry<String, File> e : files.entrySet()) {
                FileStore.save(appId, docDao.documentId(appId, e.getKey()), e.getValue());
            }
            Ui.info(this, "Application " + appId + " has been submitted.");
            shell.show("My applications", new ApplicationStatusPage(shell, customerId, appId));
        }));
        c.push(Ui.flow(8, back, submit));
    }

    private JPanel nav(int backTo, String nextText, int nextStep) {
        JButton back = Ui.secondary("Back");
        back.addActionListener(e -> go(backTo));
        JButton next = Ui.primary(nextText);
        next.addActionListener(e -> go(nextStep));
        return Ui.flow(8, back, next);
    }
}
