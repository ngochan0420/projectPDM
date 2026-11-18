USE loandb;

-- Query 1: Display one application's status and every completed verification step, including the employee who performed each step.
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
  
-- Query 2: Find applications waiting for a required document for more than 10 days.
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
  
-- Query 3: Calculate the approval rate for each loan product this year.
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

-- Query 4: Find applications that have supplied every document required by their loan product.
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

-- Query 5: Find customers with more than one active loan and calculate their total outstanding balance.
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

-- Query 6: List all customers and their applications, including customers who have not submitted any applications.
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

-- 7. Find applications whose requested amount exceeds the average requested amount across all applications.
SELECT
    ApplicationID,
    CustomerID,
    ProductID,
    RequestedAmount
FROM Application
WHERE RequestedAmount > (
        SELECT AVG(RequestedAmount)
        FROM Application
)
ORDER BY RequestedAmount DESC;

-- 8. Find loan products that have never received an application.
SELECT
    p.ProductID,
    p.ProductName,
    p.InterestRate
FROM LoanProduct p
WHERE NOT EXISTS (
        SELECT 1
        FROM Application a
        WHERE a.ProductID = p.ProductID
)
ORDER BY p.ProductID;

-- 9. Display a combined list of customer names and employee names.
SELECT FullName, 'Customer' AS PersonType
FROM Customer
UNION
SELECT FullName, 'Employee' AS PersonType
FROM Employee
ORDER BY FullName;

-- 10. Create a view showing application details together with customer names and loan product names.
CREATE OR REPLACE VIEW vw_ApplicationDetails AS
SELECT
    a.ApplicationID,
    a.CustomerID,
    c.FullName          AS CustomerName,
    a.ProductID,
    p.ProductName       AS LoanProductName,
    a.SubmissionDate,
    a.RequestedAmount,
    a.Purpose,
    a.Status
FROM Application a
JOIN Customer    c ON c.CustomerID = a.CustomerID
JOIN LoanProduct p ON p.ProductID  = a.ProductID;

-- Dùng thử view:
SELECT * FROM vw_ApplicationDetails ORDER BY ApplicationID;

-- 11. List each loan product and the number of applications it has received, including products with zero applications.
SELECT
    p.ProductID,
    p.ProductName,
    COUNT(a.ApplicationID) AS TotalApplications
FROM LoanProduct p
LEFT JOIN Application a
       ON a.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName
ORDER BY TotalApplications DESC, p.ProductID;

-- 12. List verification steps that have not yet been completed, grouped or filtered by application.
SELECT
    vs.ApplicationID,
    c.FullName      AS CustomerName,
    vs.StepID,
    st.StepName,
    e.FullName      AS AssignedEmployee,
    vs.Comments
FROM VerificationStep vs
JOIN StepType    st ON st.StepTypeID  = vs.StepTypeID
JOIN Employee    e  ON e.EmployeeID   = vs.EmployeeID
JOIN Application a  ON a.ApplicationID = vs.ApplicationID
JOIN Customer    c  ON c.CustomerID   = a.CustomerID
WHERE vs.Result IS NULL
  AND vs.VerificationDate IS NULL
ORDER BY vs.ApplicationID, vs.StepID;
