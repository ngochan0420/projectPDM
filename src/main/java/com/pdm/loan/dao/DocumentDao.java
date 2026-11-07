package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.TableData;

import java.sql.SQLException;
import java.time.LocalDate;

/** A4, A5, L2, S1: documents of an application (supplied / missing). */
public class DocumentDao {

    public java.util.List<com.pdm.loan.db.Rec> checklist(String applicationId) throws SQLException {
        return Database.query("""
                SELECT dt.DocTypeID, dt.TypeName, dt.Description, r.isMandatory AS Mandatory,
                       d.DocumentID, COALESCE(d.IsSupplied, 0) AS Supplied, d.SuppliedDate
                FROM Application a
                JOIN RequiresDocType r ON r.ProductID = a.ProductId
                JOIN DocumentType dt ON dt.DocTypeID = r.DocTypeID
                LEFT JOIN Document d ON d.ApplicationID = a.ApplicationID AND d.DocTypeID = r.DocTypeID
                WHERE a.ApplicationID = ?
                ORDER BY dt.DocTypeID""", applicationId).recs();
    }

    public void setSupplied(String applicationId, String docTypeId, boolean supplied) throws SQLException {
        Database.transaction(con -> {
            Object docId = Database.scalar(con,
                    "SELECT DocumentID FROM Document WHERE ApplicationID = ? AND DocTypeID = ?",
                    applicationId, docTypeId);
            LocalDate date = supplied ? LocalDate.now() : null;
            if (docId == null) {
                String newId = Database.nextId(con, "Document", "DocumentID", "F");
                return Database.update(con, """
                        INSERT INTO Document (DocumentID, ApplicationID, DocTypeID, IsSupplied, SuppliedDate)
                        VALUES (?, ?, ?, ?, ?)""", newId, applicationId, docTypeId, supplied, date);
            }
            return Database.update(con,
                    "UPDATE Document SET IsSupplied = ?, SuppliedDate = ? WHERE DocumentID = ?",
                    supplied, date, docId);
        });
    }

    public int missingMandatoryCount(String applicationId) throws SQLException {
        Object n = Database.scalar("""
                SELECT COUNT(*)
                FROM Application a
                JOIN RequiresDocType r ON r.ProductID = a.ProductId AND r.isMandatory = 1
                WHERE a.ApplicationID = ?
                  AND NOT EXISTS (SELECT 1 FROM Document d
                                  WHERE d.ApplicationID = a.ApplicationID
                                    AND d.DocTypeID = r.DocTypeID AND d.IsSupplied = 1)""", applicationId);
        return ((Number) n).intValue();
    }

    /** Get the DocumentID used to name the uploaded file. */
    public String documentId(String applicationId, String docTypeId) throws SQLException {
        Object id = Database.scalar("SELECT DocumentID FROM Document WHERE ApplicationID = ? AND DocTypeID = ?",
                applicationId, docTypeId);
        return id == null ? null : id.toString();
    }
}
