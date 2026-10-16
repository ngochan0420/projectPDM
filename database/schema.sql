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
