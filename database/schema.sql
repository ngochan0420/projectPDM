CREATE TABLE Customer (
                          CustomerID CHAR(4) NOT NULL PRIMARY KEY,
                          FullName VARCHAR(100) NOT NULL,
                          EmploymentStatus VARCHAR(100) NOT NULL
);

CREATE TABLE CustomerPhone (
                               CustomerID CHAR(4) NOT NULL,
                               PhoneNumber VARCHAR(20) NOT NULL,

                               PRIMARY KEY(CustomerID, PhoneNumber),
                               FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID)
                                   ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CustomerExistingAccounts (
                                          ExistingAccountsID INT AUTO_INCREMENT PRIMARY KEY,
                                          CustomerID CHAR(4) NOT NULL,
                                          AccountType VARCHAR(100) NOT NULL,
                                          BankName VARCHAR(100) NOT NULL,

                                          FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID)
                                              ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IncomeSource (
                              IncomeID CHAR(4) NOT NULL PRIMARY KEY,
                              CustomerID CHAR(4) NOT NULL,
                              IncomeType VARCHAR(100) NOT NULL,
                              EmployerName VARCHAR(100) NOT NULL,
                              MonthlyAmount DECIMAL(15,2) NOT NULL CHECK(MonthlyAmount > 0),

                              FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID)
                                  ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Employee (
                          EmployeeID CHAR(4) NOT NULL PRIMARY KEY,
                          SupervisorID CHAR(4) NULL,
                          FullName VARCHAR(100) NOT NULL,
                          Role VARCHAR(50) NOT NULL,

                          FOREIGN KEY(SupervisorID) REFERENCES Employee(EmployeeID)
                              ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE StepType (
                          StepTypeID CHAR(4) NOT NULL PRIMARY KEY,
                          StepName VARCHAR(100) NOT NULL,
                          Description VARCHAR(100) NULL
);

CREATE TABLE Application (
                             ApplicationID CHAR(4) NOT NULL PRIMARY KEY,
                             CustomerID CHAR(4) NOT NULL,
    -- ProductId CHAR(4) NOT NULL,
                             SubmissionDate DATE NOT NULL,
                             RequestedAmount DECIMAL(15,2) NOT NULL CHECK(RequestedAmount > 0),
                             Purpose VARCHAR(100) NOT NULL,
                             Status VARCHAR(100) NOT NULL,

                             FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID)
                                 ON DELETE RESTRICT ON UPDATE CASCADE
    -- FOREIGN KEY(ProductId) REFERENCES LoanProduct(ProductId)
);

CREATE TABLE VerificationStep (
                                  StepID CHAR(4) NOT NULL PRIMARY KEY,
                                  ApplicationID CHAR(4) NOT NULL,
                                  StepTypeID CHAR(4) NOT NULL,
                                  EmployeeID CHAR(4) NOT NULL,
                                  Result VARCHAR(100) NOT NULL,
                                  VerificationDate DATE NOT NULL,
                                  Comments VARCHAR(100) NULL,

                                  FOREIGN KEY(ApplicationID) REFERENCES Application(ApplicationID)
                                      ON DELETE CASCADE ON UPDATE CASCADE,
                                  FOREIGN KEY(EmployeeID) REFERENCES Employee(EmployeeID)
                                      ON DELETE RESTRICT ON UPDATE CASCADE,
                                  FOREIGN KEY(StepTypeID) REFERENCES StepType(StepTypeID)
                                      ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Decision (
                          DecisionID CHAR(4) NOT NULL PRIMARY KEY,
                          ApplicationID CHAR(4) NOT NULL UNIQUE,
                          DecisionStatus VARCHAR(100) NOT NULL
                              CHECK (DecisionStatus IN ('Approved','Rejected','Approved with conditions')),
                          Reason VARCHAR(100) NOT NULL,
                          DecisionDate DATE NOT NULL,

                          FOREIGN KEY(ApplicationID) REFERENCES Application(ApplicationID)
                              ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Application (
    ApplicationID INT PRIMARY KEY,
    ApplicantName VARCHAR(255)
);

CREATE TABLE LoanProduct (
    ProductID INT PRIMARY KEY,
    ProductName VARCHAR(255) NOT NULL,
    InterestRate DECIMAL(5,2), -- Ví dụ: 10.50%
    MinTerm INT,
    MaxTerm INT,
    EligibilityRules TEXT
);

CREATE TABLE DocumentType (
    DocTypeID INT PRIMARY KEY,
    TypeName VARCHAR(255) NOT NULL,
    Description TEXT
);

CREATE TABLE Document (
    DocumentID INT PRIMARY KEY,
    ApplicationID INT,
    DocTypeID INT,
    IsSupplied BOOLEAN,
    SuppliedDate DATE,
    FOREIGN KEY (ApplicationID) REFERENCES Application(ApplicationID),
    FOREIGN KEY (DocTypeID) REFERENCES DocumentType(DocTypeID)
);

CREATE TABLE RequiresDocType (
    ProductID INT,
    DocTypeID INT,
    isMandatory BOOLEAN,
    PRIMARY KEY (ProductID, DocTypeID),
    FOREIGN KEY (ProductID) REFERENCES LoanProduct(ProductID),
    FOREIGN KEY (DocTypeID) REFERENCES DocumentType(DocTypeID)
);

CREATE TABLE Loan (
    LoanID INT PRIMARY KEY,
    ApplicationID INT UNIQUE, 
    LoanAmount DECIMAL(15,2),
    LoanDate DATE,
    FOREIGN KEY (ApplicationID) REFERENCES Application(ApplicationID)
);

CREATE TABLE RepaymentSchedule (
    LoanID INT,
    InstallmentID INT,
    DueDate DATE,
    AmountDue DECIMAL(15,2),
    Status VARCHAR(50),
    PRIMARY KEY (LoanID, InstallmentID),
    FOREIGN KEY (LoanID) REFERENCES Loan(LoanID)
);

CREATE TABLE Disbursement (
    DisbursementID INT PRIMARY KEY,
    LoanID INT,
    DisbursementAmount DECIMAL(15,2),
    DisbursementDate DATE,
    FOREIGN KEY (LoanID) REFERENCES Loan(LoanID)
);
