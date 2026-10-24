-- ---------------------------------------------------------------------
-- 6. List all customers and their applications, 
-- including customers who have not submitted any applications.
-- ---------------------------------------------------------------------
SELECT
    c.CustomerID,
    c.FullName,
    a.ApplicationID,
    a.SubmissionDate,
    a.RequestedAmount,
    a.Status
FROM Customer c
LEFT JOIN Application a
       ON a.CustomerID = c.CustomerID
ORDER BY c.CustomerID, a.ApplicationID;


-- ---------------------------------------------------------------------
-- 7. Find applications whose requested amount exceeds 
-- the average requested amount across all applications.
-- ---------------------------------------------------------------------
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


-- ---------------------------------------------------------------------
-- 8. Find loan products that have never received an application.
-- ---------------------------------------------------------------------
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


-- ---------------------------------------------------------------------
-- 9. Display a combined list of customer names and employee names.
-- ---------------------------------------------------------------------
SELECT FullName, 'Customer' AS PersonType
FROM Customer
UNION
SELECT FullName, 'Employee' AS PersonType
FROM Employee
ORDER BY FullName;


-- ---------------------------------------------------------------------
-- 10. Create a view showing application details together
-- with customer names and loan product names.
-- ---------------------------------------------------------------------
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


-- ---------------------------------------------------------------------
-- 11. List each loan product and the number of applications it has received, 
-- including products with zero applications.
-- ---------------------------------------------------------------------
SELECT
    p.ProductID,
    p.ProductName,
    COUNT(a.ApplicationID) AS TotalApplications
FROM LoanProduct p
LEFT JOIN Application a
       ON a.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName
ORDER BY TotalApplications DESC, p.ProductID;


-- ---------------------------------------------------------------------
-- 12. List verification steps that have not yet been completed, 
-- grouped or filtered by application.
-- ---------------------------------------------------------------------
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


