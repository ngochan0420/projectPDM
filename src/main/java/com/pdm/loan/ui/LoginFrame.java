package com.pdm.loan.ui;

import com.pdm.loan.dao.Item;
import com.pdm.loan.dao.LookupDao;
import com.pdm.loan.db.Database;
import com.pdm.loan.ui.applicant.ApplicantApp;
import com.pdm.loan.ui.kit.*;
import com.pdm.loan.ui.officer.OfficerApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class LoginFrame extends JFrame {

    private static final String APPLICANT = "Applicant";
    private static final String OFFICER = "Loan Officer";
    private static final String ADMIN = "Admin";

    private final LookupDao lookup = new LookupDao();
    private final JComboBox<String> role = new JComboBox<>(new String[]{APPLICANT, OFFICER, ADMIN});
    private final JComboBox<Item> user = new JComboBox<>();
    private final JLabel status = Ui.small(" ");

    public LoginFrame() {
        super("LoanDesk - Log in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        RoundPanel card = new RoundPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(26, 28, 24, 28));
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 4));
        head.setOpaque(false);
        head.add(Ui.heading("LoanDesk", 22f));
        head.add(Ui.muted("Loan application & verification"));
        card.add(head, BorderLayout.NORTH);
        card.add(Ui.formGrid(1, Ui.labeled("Role", role), Ui.labeled("User", user)), BorderLayout.CENTER);
        JButton login = Ui.primary("Log in");
        login.addActionListener(Ui.on(this, this::login));
        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setOpaque(false);
        south.add(login, BorderLayout.NORTH);
        south.add(status, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(380, 330));

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(Theme.SIDEBAR);
        bg.add(card);
        setContentPane(bg);
        getRootPane().setDefaultButton(login);
        role.addActionListener(e -> Ui.run(this, this::loadUsers));

        setSize(560, 480);
        setLocationRelativeTo(null);
        Ui.run(this, this::checkConnectionAndLoad);
    }

    private void checkConnectionAndLoad() throws Exception {
        try (Connection con = Database.getConnection()) {
            status.setText("Connected to MySQL " + con.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            status.setText("Not connected - check db.properties");
            status.setForeground(Theme.RED);
            throw e;
        }
        loadUsers();
    }

    private void loadUsers() throws Exception {
        user.removeAllItems();
        String r = (String) role.getSelectedItem();
        user.setEnabled(!ADMIN.equals(r));
        List<Item> items = APPLICANT.equals(r) ? lookup.customers()
                : OFFICER.equals(r) ? lookup.employees() : List.of();
        for (Item i : items) {
            user.addItem(i);
        }
    }

    private void login() {
        String r = (String) role.getSelectedItem();
        Item u = (Item) user.getSelectedItem();
        JFrame next;
        if (ADMIN.equals(r)) {
            next = OfficerApp.open(new OfficerApp.Ctx(null, "Administrator", true));
        } else if (u == null) {
            throw new IllegalArgumentException("Please choose a user.");
        } else if (APPLICANT.equals(r)) {
            next = ApplicantApp.open(u.id(), u.label());
        } else {
            String name = u.label().replaceAll(" \\(.*\\)$", "");
            next = OfficerApp.open(new OfficerApp.Ctx(u.id(), name, false));
        }
        next.setVisible(true);
        dispose();
    }

    public static void logout(JFrame from) {
        from.dispose();
        new LoginFrame().setVisible(true);
    }
}
