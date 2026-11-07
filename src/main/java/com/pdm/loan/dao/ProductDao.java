package com.pdm.loan.dao;

import com.pdm.loan.db.Database;
import com.pdm.loan.db.Rec;

import java.sql.SQLException;
import java.util.List;

public class ProductDao {

    public List<Rec> listProducts() throws SQLException {
        return Database.query("""
                SELECT ProductID, ProductName, InterestRate, MinTerm, MaxTerm, EligibilityRules
                FROM LoanProduct
                ORDER BY ProductID""").recs();
    }

    public Rec product(String productId) throws SQLException {
        return Database.query("""
                SELECT ProductID, ProductName, InterestRate, MinTerm, MaxTerm, EligibilityRules
                FROM LoanProduct WHERE ProductID = ?""", productId).first();
    }

    public List<Rec> requiredDocuments(String productId) throws SQLException {
        return Database.query("""
                SELECT dt.DocTypeID, dt.TypeName, dt.Description, r.isMandatory AS Mandatory
                FROM RequiresDocType r
                JOIN DocumentType dt ON dt.DocTypeID = r.DocTypeID
                WHERE r.ProductID = ?
                ORDER BY dt.DocTypeID""", productId).recs();
    }
}
