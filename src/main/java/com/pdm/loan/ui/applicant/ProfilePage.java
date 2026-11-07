package com.pdm.loan.ui.applicant;

import com.pdm.loan.dao.CustomerDao;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.Fmt;
import com.pdm.loan.ui.kit.*;

import javax.swing.*;

public class ProfilePage extends Page {

    private static final String[] EMPLOYMENT = {"Employed", "Self-employed", "Unemployed", "Retired", "Student"};

    private final String customerId;
    private final CustomerDao customers = new CustomerDao();

    public ProfilePage(Shell shell, String customerId) {
        super(shell);
        this.customerId = customerId;
    }

    @Override
    protected void build() throws Exception {
        Rec prof = customers.profile(customerId);
        crumb("My profile", null);
        title("My profile");
        titleExtra(Ui.avatar(Fmt.initials(prof.s("FullName"))));

        JTextField name = Ui.field(prof.s("FullName"));
        JComboBox<String> employment = new JComboBox<>(EMPLOYMENT);
        employment.setEditable(true);
        employment.setSelectedItem(prof.s("EmploymentStatus"));
        JButton save = Ui.primary("Save profile");
        save.addActionListener(Ui.on(this, () -> {
            customers.updateProfile(customerId, Ui.require(name.getText(), "enter your full name"),
                    Ui.require((String) employment.getSelectedItem(), "enter your employment status"));
            Ui.info(this, "Profile saved.");
            reload();
        }));
        action(save);

        // ---- Personal details + phones
        Section personal = body.push(new Section("Personal details"));
        java.util.List<Rec> phones = customers.phones(customerId);
        JPanel phoneBox = new JPanel();
        phoneBox.setLayout(new BoxLayout(phoneBox, BoxLayout.Y_AXIS));
        phoneBox.setOpaque(false);
        if (phones.isEmpty()) {
            phoneBox.add(Ui.readOnly("No phone number"));
        }
        for (Rec ph : phones) {
            String number = ph.s("PhoneNumber");
            JButton del = Ui.small("Remove", false);
            del.addActionListener(Ui.on(this, () -> {
                if (Ui.confirm(this, "Remove phone number " + number + "?")) {
                    customers.deletePhone(customerId, number);
                    reload();
                }
            }));
            JTextField f = Ui.readOnly(number);
            f.setForeground(Theme.TEXT);
            phoneBox.add(Ui.leftRight(f, del));
            phoneBox.add(Box.createVerticalStrut(4));
        }
        personal.push(Ui.formGrid(3, Ui.labeled("Full name", name), Ui.labeled("Employment status", employment),
                Ui.labeled("Phone number", phoneBox)));
        JButton addPhone = Ui.small("Add phone number", false);
        addPhone.addActionListener(Ui.on(this, () -> {
            String number = JOptionPane.showInputDialog(this, "Phone number:");
            if (number != null && !number.isBlank()) {
                customers.addPhone(customerId, number.trim());
                reload();
            }
        }));
        personal.push(Ui.flow(0, addPhone));

        // ---- Income sources
        Section income = body.push(new Section("Income sources"));
        SimpleTable it = income.push(new SimpleTable(
                SimpleTable.Col.left("Type", 1.2), SimpleTable.Col.left("Employer", 2),
                SimpleTable.Col.right("Monthly amount (VND)", 1.4), SimpleTable.Col.right("", 0.6)));
        for (Rec r : customers.incomes(customerId)) {
            JButton edit = Ui.small("Edit", false);
            edit.addActionListener(Ui.on(this, () -> editIncome(r)));
            it.addRow(Ui.bold(r.s("IncomeType")), r.s("EmployerName"), Fmt.money(r.n("MonthlyAmount")), edit);
        }
        it.emptyMessage("No income source yet.");
        JButton addIncome = Ui.small("Add income source", false);
        addIncome.addActionListener(Ui.on(this, () -> editIncome(null)));
        income.push(Ui.flow(0, addIncome));

        // ---- Existing accounts
        Section acc = body.push(new Section("Existing accounts"));
        SimpleTable at = acc.push(new SimpleTable(
                SimpleTable.Col.left("Bank", 2), SimpleTable.Col.left("Account type", 2), SimpleTable.Col.right("", 0.6)));
        for (Rec r : customers.accounts(customerId)) {
            JButton edit = Ui.small("Edit", false);
            edit.addActionListener(Ui.on(this, () -> editAccount(r)));
            at.addRow(Ui.bold(r.s("BankName")), r.s("AccountType"), edit);
        }
        at.emptyMessage("No existing account yet.");
        JButton addAcc = Ui.small("Add account", false);
        addAcc.addActionListener(Ui.on(this, () -> editAccount(null)));
        acc.push(Ui.flow(0, addAcc));
    }

    private void editIncome(Rec r) throws Exception {
        JTextField type = Ui.field(r == null ? "" : r.s("IncomeType"));
        JTextField employer = Ui.field(r == null ? "" : r.s("EmployerName"));
        JTextField amount = Ui.field(r == null ? "" : Fmt.money(r.n("MonthlyAmount")));
        JPanel form = Ui.formGrid(1, Ui.labeled("Type (e.g. Salary)", type), Ui.labeled("Employer", employer),
                Ui.labeled("Monthly amount (VND)", amount));
        String[] options = r == null ? new String[]{"Add", "Cancel"} : new String[]{"Save", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, form, r == null ? "Add income source" : "Edit income source",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            String t = Ui.require(type.getText(), "enter the income type");
            String e = Ui.require(employer.getText(), "enter the employer");
            java.math.BigDecimal m = Fmt.parseMoney(amount.getText());
            if (m.signum() <= 0) {
                throw new IllegalArgumentException("Monthly amount must be greater than 0.");
            }
            if (r == null) {
                customers.addIncome(customerId, t, e, m);
            } else {
                customers.updateIncome(customerId, r.s("IncomeID"), t, e, m);
            }
            reload();
        } else if (choice == 1 && r != null && Ui.confirm(this, "Delete this income source?")) {
            customers.deleteIncome(customerId, r.s("IncomeID"));
            reload();
        }
    }

    private void editAccount(Rec r) throws Exception {
        JTextField bank = Ui.field(r == null ? "" : r.s("BankName"));
        JTextField type = Ui.field(r == null ? "" : r.s("AccountType"));
        JPanel form = Ui.formGrid(1, Ui.labeled("Bank", bank), Ui.labeled("Account type (e.g. Savings)", type));
        String[] options = r == null ? new String[]{"Add", "Cancel"} : new String[]{"Save", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, form, r == null ? "Add account" : "Edit account",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            String b = Ui.require(bank.getText(), "enter the bank");
            String t = Ui.require(type.getText(), "enter the account type");
            if (r == null) {
                customers.addAccount(customerId, t, b);
            } else {
                customers.updateAccount(customerId, r.i("ExistingAccountsID"), t, b);
            }
            reload();
        } else if (choice == 1 && r != null && Ui.confirm(this, "Delete this account?")) {
            customers.deleteAccount(customerId, r.i("ExistingAccountsID"));
            reload();
        }
    }
}
