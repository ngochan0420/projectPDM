package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.TableData;

import java.sql.SQLException;
import java.time.LocalDate;

/** L5: make the final decision on an application. */
public class DecisionDao {

    public static final String[] STATUSES = {"Approved", "Rejected", "Approved with conditions"};

    private final DocumentDao documents = new DocumentDao();
    private final VerificationDao verification = new VerificationDao();

    public com.pdm.loan.db.Rec decisionOf(String applicationId) throws SQLException {
        return Database.query(
                "SELECT DecisionID, DecisionStatus, Reason, DecisionDate FROM Decision WHERE ApplicationID = ?",
                applicationId).first();
    }

    public void decide(String applicationId, String status, String reason) throws SQLException {
        int missing = documents.missingMandatoryCount(applicationId);
        if (missing > 0) {
            throw new IllegalStateException(
                    "Cannot decide: " + missing + " required document(s) are still missing.");
        }
        int pending = verification.pendingCount(applicationId);
        if (pending > 0) {
            throw new IllegalStateException(
                    "Cannot decide: " + pending + " verification step(s) are still pending.");
        }
        Database.transaction(con -> {
            if (Database.scalar(con, "SELECT 1 FROM Decision WHERE ApplicationID = ?", applicationId) != null) {
                throw new IllegalStateException("This application already has a decision.");
            }
            String id = Database.nextId(con, "Decision", "DecisionID", "D");
            Database.update(con, """
                    INSERT INTO Decision (DecisionID, ApplicationID, DecisionStatus, Reason, DecisionDate)
                    VALUES (?, ?, ?, ?, ?)""", id, applicationId, status, reason, LocalDate.now());
            return Database.update(con, "UPDATE Application SET Status = ? WHERE ApplicationID = ?",
                    ApplicationDao.DECIDED, applicationId);
        });
    }
}
