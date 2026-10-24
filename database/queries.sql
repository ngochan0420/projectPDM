-- Query 1: Multi-table JOIN
SELECT
    a.ApplicationID,
    a.Status AS ApplicationStatus,
    st.StepName,
    vs.Result AS StepResult,
    vs.VerificationDate,
    e.FullName AS VerifiedByEmployee
FROM Application a
JOIN VerificationStep vs
    ON a.ApplicationID = vs.ApplicationID
JOIN StepType st
    ON vs.StepTypeID = st.StepTypeID
JOIN Employee e
    ON vs.EmployeeID = e.EmployeeID
WHERE a.ApplicationID = 'A002'
  AND vs.Result IS NOT NULL;
  
-- Query 2: JOIN, date conditions
SELECT DISTINCT
    a.ApplicationID,
    a.CustomerID,
    a.SubmissionDate,
    DATEDIFF(CURRENT_DATE, a.SubmissionDate) AS DaysWaiting
FROM Application a
JOIN RequiresDocType rdt
    ON a.ProductId = rdt.ProductID
LEFT JOIN Document d
    ON d.ApplicationID = a.ApplicationID
    AND d.DocTypeID = rdt.DocTypeID
    AND d.IsSupplied = 1
WHERE rdt.isMandatory = 1
  AND d.DocumentID IS NULL
  AND DATEDIFF(CURRENT_DATE, a.SubmissionDate) > 10;
  
-- Query 3: GROUP BY, aggregation
SELECT 
    lp.ProductID,
    lp.ProductName,
    COUNT(a.ApplicationID) AS TotalApplications,
    SUM(CASE WHEN d.DecisionStatus LIKE 'Approved%' THEN 1 ELSE 0 END) AS ApprovedCount,
    ROUND(
        (SUM(CASE WHEN d.DecisionStatus LIKE 'Approved%' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(a.ApplicationID), 0), 
        2
    ) AS ApprovalRatePercentage
FROM LoanProduct lp
LEFT JOIN Application a ON lp.ProductID = a.ProductId 
    AND YEAR(a.SubmissionDate) = YEAR(CURRENT_DATE)
LEFT JOIN Decision d ON a.ApplicationID = d.ApplicationID
GROUP BY lp.ProductID, lp.ProductName;

-- Query 4: NOT EXISTS — "for all"
SELECT
    a.ApplicationID,
    a.CustomerID,
    a.ProductId
FROM Application a
WHERE NOT EXISTS (
    SELECT 1
    FROM RequiresDocType rdt
    WHERE rdt.ProductID = a.ProductId
      AND rdt.isMandatory = 1
      AND NOT EXISTS (
          SELECT 1
          FROM Document d
          WHERE d.ApplicationID = a.ApplicationID
            AND d.DocTypeID = rdt.DocTypeID
            AND d.IsSupplied = 1
      )
);

-- Query 5: GROUP BY, HAVING, aggregation
SELECT
    c.CustomerID,
    c.FullName,
    COUNT(DISTINCT l.LoanID) AS ActiveLoansCount,
    SUM(rs.AmountDue) AS TotalOutstandingBalance
FROM Customer c
JOIN Application a
    ON c.CustomerID = a.CustomerID
JOIN Loan l
    ON a.ApplicationID = l.ApplicationID
JOIN RepaymentSchedule rs
    ON l.LoanID = rs.LoanID
WHERE rs.Status = 'Unpaid'
GROUP BY c.CustomerID, c.FullName
HAVING COUNT(DISTINCT l.LoanID) > 1;

-- Query 6: LEFT JOIN (outer join)
SELECT 
    c.CustomerID,
    c.FullName,
    c.EmploymentStatus,
    a.ApplicationID,
    a.SubmissionDate,
    a.RequestedAmount,
    a.Status AS ApplicationStatus
FROM Customer c
LEFT JOIN Application a ON c.CustomerID = a.CustomerID
ORDER BY c.CustomerID;
