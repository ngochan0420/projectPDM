package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.TableData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Lists for the combo boxes (customers, employees, products, verification step types). */
public class LookupDao {

    private List<Item> items(String sql, Object... params) throws SQLException {
        TableData t = Database.query(sql, params);
        List<Item> list = new ArrayList<>();
        for (Object[] r : t.rows()) {
            list.add(new Item(r[0].toString(), r[1].toString()));
        }
        return list;
    }

    public List<Item> customers() throws SQLException {
        return items("SELECT CustomerID, FullName FROM Customer ORDER BY CustomerID");
    }

    public List<Item> employees() throws SQLException {
        return items("SELECT EmployeeID, CONCAT(FullName, ' (', Role, ')') FROM Employee ORDER BY EmployeeID");
    }

    public List<Item> products() throws SQLException {
        return items("SELECT ProductID, ProductName FROM LoanProduct ORDER BY ProductID");
    }

    public List<Item> stepTypes() throws SQLException {
        return items("SELECT StepTypeID, StepName FROM StepType ORDER BY StepTypeID");
    }

    public List<Item> applicationsOf(String customerId) throws SQLException {
        return items("""
                SELECT a.ApplicationID, CONCAT(p.ProductName, ' - ', a.Status)
                FROM Application a JOIN LoanProduct p ON p.ProductID = a.ProductId
                WHERE a.CustomerID = ?
                ORDER BY a.ApplicationID DESC""", customerId);
    }
}
