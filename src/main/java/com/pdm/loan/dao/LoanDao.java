package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.Rec;
import com.pdm.loan.service.LoanCalculator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LoanDao {

    private static final String LOAN_SQL = """
            SELECT l.LoanID, l.ApplicationID, l.LoanAmount, l.LoanDate,
                   p.ProductName, p.InterestRate,
                   (SELECT COUNT(*) FROM RepaymentSchedule rs WHERE rs.LoanID = l.LoanID) AS Installments,
                   (SELECT COALESCE(SUM(rs.AmountDue), 0) FROM RepaymentSchedule rs
                    WHERE rs.LoanID = l.LoanID AND rs.Status = 'Unpaid') AS Outstanding,
                   (SELECT MAX(d.DisbursementDate) FROM Disbursement d WHERE d.LoanID = l.LoanID) AS DisbursedOn,
                   (SELECT COALESCE(SUM(d.DisbursementAmount), 0) FROM Disbursement d
                    WHERE d.LoanID = l.LoanID) AS Disbursed
            FROM Loan l
            JOIN Application a ON a.ApplicationID = l.ApplicationID
            JOIN LoanProduct p ON p.ProductID = a.ProductId
            """;

    /** The loan created for an application, or null. */
    public Rec loanOf(String applicationId) throws SQLException {
        return Database.query(LOAN_SQL + " WHERE l.ApplicationID = ?", applicationId).first();
    }

    public List<Rec> loansOf(String customerId) throws SQLException {
        return Database.query(LOAN_SQL + " WHERE a.CustomerID = ? ORDER BY l.LoanID DESC", customerId).recs();
    }

    public List<Rec> schedule(String loanId) throws SQLException {
        return Database.query("""
                SELECT InstallmentID, DueDate, AmountDue, Status
                FROM RepaymentSchedule WHERE LoanID = ? ORDER BY InstallmentID""", loanId).recs();
    }

    public List<Rec> disbursements(String loanId) throws SQLException {
        return Database.query("""
                SELECT DisbursementID, DisbursementAmount, DisbursementDate
                FROM Disbursement WHERE LoanID = ? ORDER BY DisbursementDate""", loanId).recs();
    }

    public String createLoan(String applicationId, BigDecimal amount, int termMonths) throws SQLException {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than 0.");
        }
        return Database.transaction(con -> {
            Rec info = Database.query(con, """
                    SELECT d.DecisionStatus, p.InterestRate, p.MinTerm, p.MaxTerm
                    FROM Application a
                    JOIN LoanProduct p ON p.ProductID = a.ProductId
                    LEFT JOIN Decision d ON d.ApplicationID = a.ApplicationID
                    WHERE a.ApplicationID = ?""", applicationId).first();
            if (info == null || info.s("DecisionStatus") == null || !info.s("DecisionStatus").startsWith("Approved")) {
                throw new IllegalStateException("A loan can only be created for an approved application.");
            }
            if (termMonths < info.i("MinTerm") || termMonths > info.i("MaxTerm")) {
                throw new IllegalArgumentException("Term must be between " + info.i("MinTerm")
                        + " and " + info.i("MaxTerm") + " months for this product.");
            }
            if (Database.scalar(con, "SELECT 1 FROM Loan WHERE ApplicationID = ?", applicationId) != null) {
                throw new IllegalStateException("This application already has a loan.");
            }
            LocalDate today = LocalDate.now();
            String loanId = Database.nextId(con, "Loan", "LoanID", "L");
            Database.update(con, "INSERT INTO Loan (LoanID, ApplicationID, LoanAmount, LoanDate) VALUES (?, ?, ?, ?)",
                    loanId, applicationId, amount, today);

            String disbId = Database.nextId(con, "Disbursement", "DisbursementID", "B");
            Database.update(con, """
                    INSERT INTO Disbursement (DisbursementID, LoanID, DisbursementAmount, DisbursementDate)
                    VALUES (?, ?, ?, ?)""", disbId, loanId, amount, today);

            BigDecimal rate = info.n("InterestRate");
            BigDecimal payment = LoanCalculator.monthlyPayment(amount, rate, termMonths);
            BigDecimal balance = amount;
            for (int i = 1; i <= termMonths; i++) {
                BigDecimal interest = LoanCalculator.interestFor(balance, rate);
                BigDecimal due = payment;
                if (i == termMonths) {
                    due = balance.add(interest); // last installment absorbs rounding
                }
                balance = balance.add(interest).subtract(due);
                Database.update(con, """
                        INSERT INTO RepaymentSchedule (LoanID, InstallmentID, DueDate, AmountDue, Status)
                        VALUES (?, ?, ?, ?, 'Unpaid')""", loanId, i, today.plusMonths(i), due);
            }
            return loanId;
        });
    }

    public void markPaid(String loanId, int installmentId) throws SQLException {
        Database.update("UPDATE RepaymentSchedule SET Status = 'Paid' WHERE LoanID = ? AND InstallmentID = ?",
                loanId, installmentId);
    }
}
