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
        stmt.executeUpdate("CREATE TABLE MagnumPiCast(id INT, first_name VARCHAR(255), last_name VARCHAR(255), PRIMARY KEY (id))");
        stmt.executeUpdate("INSERT INTO MagnumPiCast VALUES (1, 'Tom', 'Selleck'), (2, 'John', 'Hillerman'), (3, 'Roger E.', 'Mosley'), (4, 'Larry', 'Manetti')");
        resultSet = stmt.executeQuery("SELECT * FROM MagnumPiCast");
    }

    @Test
    public void works() {
        assertTable("resultSet", DefaultTablePrinter.print(new ResultSetTable(resultSet)));
    }
}