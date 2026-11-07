package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.TableData;

import java.sql.SQLException;
import java.time.LocalDate;

public class VerificationDao {

    public static final String[] RESULTS = {"Passed", "Passed with remarks", "Failed"};

    public java.util.List<com.pdm.loan.db.Rec> steps(String applicationId) throws SQLException {
        return Database.query("""
                SELECT v.StepID, v.StepTypeID, st.StepName, v.EmployeeID, e.FullName AS Officer,
                       v.Result, v.VerificationDate, v.Comments
                FROM VerificationStep v
                JOIN StepType st ON st.StepTypeID = v.StepTypeID
                JOIN Employee e ON e.EmployeeID = v.EmployeeID
                WHERE v.ApplicationID = ?
                ORDER BY v.StepID""", applicationId).recs();
    }

    public java.util.List<com.pdm.loan.db.Rec> pendingFor(String employeeId) throws SQLException {
        return Database.query("""
                SELECT v.StepID, v.ApplicationID, st.StepName, c.FullName AS Customer, a.SubmissionDate
                FROM VerificationStep v
                JOIN StepType st ON st.StepTypeID = v.StepTypeID
                JOIN Application a ON a.ApplicationID = v.ApplicationID
                JOIN Customer c ON c.CustomerID = a.CustomerID
                WHERE v.EmployeeID = ? AND v.Result IS NULL
                ORDER BY v.ApplicationID""", employeeId).recs();
    }

    public TableData completedSteps(String applicationId) throws SQLException {
        return Database.query("""
                SELECT st.StepName, vs.Result AS StepResult, vs.VerificationDate,
                       e.FullName AS VerifiedByEmployee
                FROM Application a
                JOIN VerificationStep vs ON a.ApplicationID = vs.ApplicationID
                JOIN StepType st ON vs.StepTypeID = st.StepTypeID
                JOIN Employee e ON vs.EmployeeID = e.EmployeeID
                WHERE a.ApplicationID = ? AND vs.Result IS NOT NULL
                ORDER BY vs.VerificationDate""", applicationId);
    }

    public void assign(String applicationId, String stepTypeId, String employeeId) throws SQLException {
        Database.transaction(con -> {
            String stepId = Database.nextId(con, "VerificationStep", "StepID", "V");
            Database.update(con, """
                    INSERT INTO VerificationStep
                        (StepID, ApplicationID, StepTypeID, EmployeeID, Result, VerificationDate, Comments)
                    VALUES (?, ?, ?, ?, NULL, NULL, NULL)""", stepId, applicationId, stepTypeId, employeeId);
            return Database.update(con, "UPDATE Application SET Status = ? WHERE ApplicationID = ? AND Status = ?",
                    ApplicationDao.IN_VERIFICATION, applicationId, ApplicationDao.SUBMITTED);
        });
    }

    public void record(String stepId, String employeeId, String result, String comments) throws SQLException {
        Database.update("""
                UPDATE VerificationStep
                SET Result = ?, VerificationDate = ?, Comments = ?, EmployeeID = ?
                WHERE StepID = ?""", result, LocalDate.now(), comments, employeeId, stepId);
    }

    public int pendingCount(String applicationId) throws SQLException {
        Object n = Database.scalar(
                "SELECT COUNT(*) FROM VerificationStep WHERE ApplicationID = ? AND Result IS NULL", applicationId);
        return ((Number) n).intValue();
    }
}
