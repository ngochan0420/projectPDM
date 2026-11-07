package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.Rec;

import java.sql.SQLException;
import java.util.List;

public class ReportDao {

    public List<Rec> completeApplications() throws SQLException {
        return Database.query("""
                SELECT a.ApplicationID, c.FullName AS Customer, p.ProductName AS Product, a.Status
                FROM Application a
                JOIN Customer c ON c.CustomerID = a.CustomerID
                JOIN LoanProduct p ON p.ProductID = a.ProductId
                WHERE NOT EXISTS (
                    SELECT 1 FROM RequiresDocType rdt
                    WHERE rdt.ProductID = a.ProductId AND rdt.isMandatory = 1
                      AND NOT EXISTS (
                          SELECT 1 FROM Document d
                          WHERE d.ApplicationID = a.ApplicationID
                            AND d.DocTypeID = rdt.DocTypeID AND d.IsSupplied = 1))
                ORDER BY a.ApplicationID""").recs();
    }

    public List<Rec> waitingForDocuments(int days) throws SQLException {
        return Database.query("""
                SELECT a.ApplicationID, c.FullName AS Customer, a.SubmissionDate,
                       GROUP_CONCAT(dt.TypeName SEPARATOR ', ') AS MissingDocument,
                       DATEDIFF(CURRENT_DATE, a.SubmissionDate) AS DaysWaiting
                FROM Application a
                JOIN Customer c ON c.CustomerID = a.CustomerID
                JOIN RequiresDocType rdt ON a.ProductId = rdt.ProductID
                JOIN DocumentType dt ON dt.DocTypeID = rdt.DocTypeID
                LEFT JOIN Document d ON d.ApplicationID = a.ApplicationID
                     AND d.DocTypeID = rdt.DocTypeID AND d.IsSupplied = 1
                WHERE rdt.isMandatory = 1
                  AND d.DocumentID IS NULL
                  AND DATEDIFF(CURRENT_DATE, a.SubmissionDate) > ?
                GROUP BY a.ApplicationID, c.FullName, a.SubmissionDate
                ORDER BY DaysWaiting DESC""", days).recs();
    }

    public List<Rec> approvalRate(int year) throws SQLException {
        return Database.query("""
                SELECT lp.ProductID, lp.ProductName,
                       COUNT(a.ApplicationID) AS TotalApplications,
                       SUM(CASE WHEN d.DecisionStatus LIKE 'Approved%' THEN 1 ELSE 0 END) AS ApprovedCount,
                       ROUND(SUM(CASE WHEN d.DecisionStatus LIKE 'Approved%' THEN 1 ELSE 0 END) * 100.0
                             / NULLIF(COUNT(a.ApplicationID), 0), 2) AS ApprovalRate
                FROM LoanProduct lp
                LEFT JOIN Application a ON lp.ProductID = a.ProductId AND YEAR(a.SubmissionDate) = ?
                LEFT JOIN Decision d ON a.ApplicationID = d.ApplicationID
                GROUP BY lp.ProductID, lp.ProductName
                ORDER BY lp.ProductID""", year).recs();
    }

    public List<Rec> customersWithMultipleLoans() throws SQLException {
        return Database.query("""
                SELECT c.CustomerID, c.FullName,
                       COUNT(DISTINCT l.LoanID) AS ActiveLoans,
                       SUM(rs.AmountDue) AS Outstanding
                FROM Customer c
                JOIN Application a ON c.CustomerID = a.CustomerID
                JOIN Loan l ON a.ApplicationID = l.ApplicationID
                JOIN RepaymentSchedule rs ON l.LoanID = rs.LoanID
                WHERE rs.Status = 'Unpaid'
                GROUP BY c.CustomerID, c.FullName
                HAVING COUNT(DISTINCT l.LoanID) > 1""").recs();
    }
}
