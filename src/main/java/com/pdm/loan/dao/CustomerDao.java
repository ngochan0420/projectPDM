package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.Rec;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** A2: personal info, phone numbers, existing bank accounts, income sources. */
public class CustomerDao {

    /** All customers with a short summary (officer "Customers" page). */
    public List<Rec> listCustomers() throws SQLException {
        return Database.query("""
                SELECT c.CustomerID, c.FullName, c.EmploymentStatus,
                       (SELECT MIN(p.PhoneNumber) FROM CustomerPhone p WHERE p.CustomerID = c.CustomerID) AS Phone,
                       (SELECT SUM(i.MonthlyAmount) FROM IncomeSource i WHERE i.CustomerID = c.CustomerID) AS Income,
                       (SELECT COUNT(*) FROM Application a WHERE a.CustomerID = c.CustomerID) AS Applications
                FROM Customer c
                ORDER BY c.CustomerID""").recs();
    }

    /** FullName, EmploymentStatus, total monthly income, phones, banks, income types. */
    public Rec profile(String customerId) throws SQLException {
        return Database.query("""
                SELECT c.CustomerID, c.FullName, c.EmploymentStatus,
                       (SELECT SUM(i.MonthlyAmount) FROM IncomeSource i WHERE i.CustomerID = c.CustomerID) AS Income,
                       (SELECT GROUP_CONCAT(p.PhoneNumber SEPARATOR ', ')
                        FROM CustomerPhone p WHERE p.CustomerID = c.CustomerID) AS Phones,
                       (SELECT GROUP_CONCAT(e.BankName SEPARATOR ', ')
                        FROM CustomerExistingAccounts e WHERE e.CustomerID = c.CustomerID) AS Banks,
                       (SELECT GROUP_CONCAT(i.IncomeType SEPARATOR ', ')
                        FROM IncomeSource i WHERE i.CustomerID = c.CustomerID) AS IncomeTypes
                FROM Customer c WHERE c.CustomerID = ?""", customerId).first();
    }

    public void updateProfile(String customerId, String fullName, String employmentStatus) throws SQLException {
        Database.update("UPDATE Customer SET FullName = ?, EmploymentStatus = ? WHERE CustomerID = ?",
                fullName, employmentStatus, customerId);
    }

    // ---- Phone
    public List<Rec> phones(String customerId) throws SQLException {
        return Database.query("SELECT PhoneNumber FROM CustomerPhone WHERE CustomerID = ? ORDER BY PhoneNumber",
                customerId).recs();
    }

    public void addPhone(String customerId, String phone) throws SQLException {
        Database.update("INSERT INTO CustomerPhone (CustomerID, PhoneNumber) VALUES (?, ?)", customerId, phone);
    }

    public void deletePhone(String customerId, String phone) throws SQLException {
        Database.update("DELETE FROM CustomerPhone WHERE CustomerID = ? AND PhoneNumber = ?", customerId, phone);
    }

    // ---- Existing bank accounts
    public List<Rec> accounts(String customerId) throws SQLException {
        return Database.query("""
                SELECT ExistingAccountsID, AccountType, BankName
                FROM CustomerExistingAccounts WHERE CustomerID = ? ORDER BY ExistingAccountsID""", customerId).recs();
    }

    public void addAccount(String customerId, String type, String bank) throws SQLException {
        Database.update("INSERT INTO CustomerExistingAccounts (CustomerID, AccountType, BankName) VALUES (?, ?, ?)",
                customerId, type, bank);
    }

    public void updateAccount(String customerId, int accountId, String type, String bank) throws SQLException {
        Database.update("""
                UPDATE CustomerExistingAccounts SET AccountType = ?, BankName = ?
                WHERE ExistingAccountsID = ? AND CustomerID = ?""", type, bank, accountId, customerId);
    }

    public void deleteAccount(String customerId, int accountId) throws SQLException {
        Database.update("DELETE FROM CustomerExistingAccounts WHERE ExistingAccountsID = ? AND CustomerID = ?",
                accountId, customerId);
    }

    // ---- Income sources
    public List<Rec> incomes(String customerId) throws SQLException {
        return Database.query("""
                SELECT IncomeID, IncomeType, EmployerName, MonthlyAmount
                FROM IncomeSource WHERE CustomerID = ? ORDER BY IncomeID""", customerId).recs();
    }

    public void addIncome(String customerId, String type, String employer, BigDecimal monthly) throws SQLException {
        Database.transaction(con -> {
            String id = Database.nextId(con, "IncomeSource", "IncomeID", "I");
            return Database.update(con, """
                    INSERT INTO IncomeSource (IncomeID, CustomerID, IncomeType, EmployerName, MonthlyAmount)
                    VALUES (?, ?, ?, ?, ?)""", id, customerId, type, employer, monthly);
        });
    }

    public void updateIncome(String customerId, String incomeId, String type, String employer, BigDecimal monthly)
            throws SQLException {
        Database.update("""
                UPDATE IncomeSource SET IncomeType = ?, EmployerName = ?, MonthlyAmount = ?
                WHERE IncomeID = ? AND CustomerID = ?""", type, employer, monthly, incomeId, customerId);
    }

    public void deleteIncome(String customerId, String incomeId) throws SQLException {
        Database.update("DELETE FROM IncomeSource WHERE IncomeID = ? AND CustomerID = ?", incomeId, customerId);
    }
}
