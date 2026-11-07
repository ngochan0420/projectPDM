package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.Rec;
import com.pdm.loan.db.TableData;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ApplicationDao {

    public static final String SUBMITTED = "Submitted";
    public static final String IN_VERIFICATION = "In Verification";
    public static final String DECIDED = "Decided";

    public static final String STAGE_WAITING_DOCS = "Waiting documents";
    public static final String STAGE_IN_VERIFICATION = "In verification";
    public static final String STAGE_NEW = "New";
    public static final String STAGE_READY = "Ready for decision";

    private static final String SUMMARY_SQL = """
            SELECT x.*,
                   CASE
                     WHEN x.Decision IS NOT NULL THEN x.Decision
                     WHEN x.MissingDocs IS NOT NULL THEN 'Waiting documents'
                     WHEN x.PendingSteps IS NOT NULL THEN 'In verification'
                     WHEN x.StepCount = 0 THEN 'New'
                     ELSE 'Ready for decision'
                   END AS Stage,
                   CASE
                     WHEN x.Decision IS NOT NULL THEN NULL
                     ELSE COALESCE(x.MissingDocs, x.PendingSteps, 'Decision')
                   END AS WaitingFor,
                   CASE
                     WHEN x.Decision IS NOT NULL THEN NULL
                     ELSE DATEDIFF(CURRENT_DATE, x.LastActivity)
                   END AS DaysWaiting
            FROM (
                SELECT a.ApplicationID, a.CustomerID, c.FullName AS Customer,
                       a.ProductId AS ProductID, p.ProductName AS Product, p.InterestRate,
                       p.MinTerm, p.MaxTerm,
                       a.RequestedAmount AS Amount, a.SubmissionDate AS Submitted,
                       a.Purpose, a.Status,
                       (SELECT dc.DecisionStatus FROM Decision dc
                        WHERE dc.ApplicationID = a.ApplicationID) AS Decision,
                       (SELECT GROUP_CONCAT(dt.TypeName ORDER BY dt.DocTypeID SEPARATOR ', ')
                        FROM RequiresDocType r JOIN DocumentType dt ON dt.DocTypeID = r.DocTypeID
                        WHERE r.ProductID = a.ProductId AND r.isMandatory = 1
                          AND NOT EXISTS (SELECT 1 FROM Document d
                                          WHERE d.ApplicationID = a.ApplicationID
                                            AND d.DocTypeID = r.DocTypeID AND d.IsSupplied = 1)) AS MissingDocs,
                       (SELECT GROUP_CONCAT(st.StepName ORDER BY v.StepID SEPARATOR ', ')
                        FROM VerificationStep v JOIN StepType st ON st.StepTypeID = v.StepTypeID
                        WHERE v.ApplicationID = a.ApplicationID AND v.Result IS NULL) AS PendingSteps,
                       (SELECT COUNT(*) FROM VerificationStep v
                        WHERE v.ApplicationID = a.ApplicationID) AS StepCount,
                       GREATEST(a.SubmissionDate,
                                COALESCE((SELECT MAX(d.SuppliedDate) FROM Document d
                                          WHERE d.ApplicationID = a.ApplicationID), a.SubmissionDate),
                                COALESCE((SELECT MAX(v.VerificationDate) FROM VerificationStep v
                                          WHERE v.ApplicationID = a.ApplicationID), a.SubmissionDate)) AS LastActivity
                FROM Application a
                JOIN Customer c ON c.CustomerID = a.CustomerID
                JOIN LoanProduct p ON p.ProductID = a.ProductId
                %s
            ) x
            ORDER BY x.ApplicationID DESC
            """;

    public List<Rec> listForCustomer(String customerId) throws SQLException {
        return Database.query(SUMMARY_SQL.formatted("WHERE a.CustomerID = ?"), customerId).recs();
    }

    public List<Rec> listAll() throws SQLException {
        return Database.query(SUMMARY_SQL.formatted("")).recs();
    }

    public Rec get(String applicationId) throws SQLException {
        return Database.query(SUMMARY_SQL.formatted("WHERE a.ApplicationID = ?"), applicationId).first();
    }

    public String submit(String customerId, String productId, BigDecimal amount, String purpose,
                         java.util.Set<String> suppliedDocTypes) throws SQLException {
        return Database.transaction(con -> {
            String appId = Database.nextId(con, "Application", "ApplicationID", "A");
            Database.update(con, """
                    INSERT INTO Application
                        (ApplicationID, CustomerID, ProductId, SubmissionDate, RequestedAmount, Purpose, Status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)""",
                    appId, customerId, productId, LocalDate.now(), amount, purpose, SUBMITTED);

            TableData required = Database.query(con,
                    "SELECT DocTypeID FROM RequiresDocType WHERE ProductID = ? ORDER BY DocTypeID", productId);
            for (Object[] row : required.rows()) {
                String docType = row[0].toString();
                boolean supplied = suppliedDocTypes.contains(docType);
                String docId = Database.nextId(con, "Document", "DocumentID", "F");
                Database.update(con, """
                        INSERT INTO Document (DocumentID, ApplicationID, DocTypeID, IsSupplied, SuppliedDate)
                        VALUES (?, ?, ?, ?, ?)""", docId, appId, docType, supplied,
                        supplied ? LocalDate.now() : null);
            }
            return appId;
        });
    }

    public static Map<String, Integer> countByStage(List<Rec> apps) {
        Map<String, Integer> m = new java.util.LinkedHashMap<>();
        for (Rec r : apps) {
            m.merge(r.s("Stage"), 1, Integer::sum);
        }
        return m;
    }
}
