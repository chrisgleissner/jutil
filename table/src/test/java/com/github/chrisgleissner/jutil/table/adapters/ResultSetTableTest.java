package com.github.chrisgleissner.jutil.table.adapters;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

public class ResultSetTableTest {

    private ResultSet resultSet;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE MagnumPiCast(first_name VARCHAR(255), last_name VARCHAR(255), PRIMARY KEY (first_name, last_name))");
        stmt.executeUpdate("INSERT INTO MagnumPiCast(first_name, last_name) VALUES ('Tom', 'Selleck')");
        stmt.executeUpdate("INSERT INTO MagnumPiCast(first_name, last_name) VALUES ('John', 'Hillerman')");
        stmt.executeUpdate("INSERT INTO MagnumPiCast(first_name, last_name) VALUES ('Roger E.', 'Mosley')");
        stmt.executeUpdate("INSERT INTO MagnumPiCast(first_name, last_name) VALUES ('Larry', 'Manetti')");
        resultSet = stmt.executeQuery("select * from MagnumPiCast");
    }

    @Test
    public void works() {
        assertTable("resultSet", DefaultTablePrinter.print(new ResultSetTable(resultSet)));
    }
}