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
    Description TEXT NULL
);

CREATE TABLE LoanProduct (
    ProductID CHAR(4) NOT NULL PRIMARY KEY,
    ProductName VARCHAR(255) NOT NULL,
    InterestRate DECIMAL(5,2), -- Ví dụ: 10.50%
    MinTerm INT NOT NULL,
    MaxTerm INT NOT NULL,
    EligibilityRules TEXT,
    CONSTRAINT chk_term_range CHECK(MinTerm > 0 AND MinTerm <= MaxTerm)
);

CREATE TABLE Application (
    ApplicationID CHAR(4) NOT NULL PRIMARY KEY,
    CustomerID CHAR(4) NOT NULL,
    ProductId CHAR(4) NOT NULL,
    SubmissionDate DATE NOT NULL,
    RequestedAmount DECIMAL(15,2) NOT NULL CHECK(RequestedAmount > 0),
    Purpose TEXT NOT NULL,
    Status VARCHAR(100) NOT NULL,

    FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY(ProductId) REFERENCES LoanProduct(ProductId)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE VerificationStep (
    StepID CHAR(4) NOT NULL PRIMARY KEY,
    ApplicationID CHAR(4) NOT NULL,
    StepTypeID CHAR(4) NOT NULL,
    EmployeeID CHAR(4) NOT NULL,
    Result VARCHAR(100) NULL,
    VerificationDate DATE NULL,
    Comments TEXT NULL,
    CHECK ((Result IS NULL) = (VerificationDate IS NULL)),

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
    Reason TEXT NOT NULL,
    DecisionDate DATE NOT NULL,

    FOREIGN KEY(ApplicationID) REFERENCES Application(ApplicationID)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE DocumentType (
    DocTypeID CHAR(4) NOT NULL PRIMARY KEY,
    TypeName VARCHAR(255) NOT NULL,
    Description TEXT
);

CREATE TABLE Document (
    DocumentID CHAR(4) NOT NULL PRIMARY KEY,
    ApplicationID CHAR(4) NOT NULL,
    DocTypeID CHAR(4) NOT NULL,
    IsSupplied BOOLEAN NOT NULL,
    SuppliedDate DATE NULL,

    FOREIGN KEY (ApplicationID) REFERENCES Application(ApplicationID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (DocTypeID) REFERENCES DocumentType(DocTypeID)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE RequiresDocType (
    ProductID CHAR(4) NOT NULL,
    DocTypeID CHAR(4) NOT NULL,
    isMandatory BOOLEAN NOT NULL,

    PRIMARY KEY (ProductID, DocTypeID),
    FOREIGN KEY (ProductID) REFERENCES LoanProduct(ProductID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (DocTypeID) REFERENCES DocumentType(DocTypeID)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Loan (
    LoanID CHAR(4) NOT NULL PRIMARY KEY,
    ApplicationID CHAR(4) NOT NULL UNIQUE,
    LoanAmount DECIMAL(15,2) NOT NULL CHECK(LoanAmount > 0),
    LoanDate DATE NOT NULL,

    FOREIGN KEY (ApplicationID) REFERENCES Application(ApplicationID)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE RepaymentSchedule (
    LoanID CHAR(4) NOT NULL,
    InstallmentID INT NOT NULL ,
    DueDate DATE NOT NULL,
    AmountDue DECIMAL(15,2) NOT NULL CHECK(AmountDue > 0),
    Status VARCHAR(50) NOT NULL CHECK(Status IN ('Paid','Unpaid')),

    PRIMARY KEY (LoanID, InstallmentID),
    FOREIGN KEY (LoanID) REFERENCES Loan(LoanID)
    ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Disbursement (
    DisbursementID CHAR(4) NOT NULL PRIMARY KEY,
    LoanID CHAR(4) NOT NULL,
    DisbursementAmount DECIMAL(15,2) NOT NULL CHECK(DisbursementAmount > 0),
    DisbursementDate DATE NOT NULL,

    FOREIGN KEY (LoanID) REFERENCES Loan(LoanID)
        ON DELETE CASCADE ON UPDATE CASCADE
);
