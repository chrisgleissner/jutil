package com.github.chrisgleissner.jutil.table.provider;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

@Slf4j
public class ResultSetTableProviderTest {

    private ResultSet resultSet;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE MagnumPiCast(id INT, first_name VARCHAR(255), last_name VARCHAR(255), PRIMARY KEY (id))");

        PreparedStatement prepStmt = conn.prepareStatement("INSERT INTO MagnumPiCast VALUES (?, ?, ?)");
        prepStmt.setInt(1, 1);
        prepStmt.setString(2, "Tom");
        prepStmt.setString(3, "Selleck");
        prepStmt.addBatch();

        prepStmt.setInt(1, 2);
        prepStmt.setString(2, "John");
        prepStmt.setString(3, "Hillerman");
        prepStmt.addBatch();
        prepStmt.executeBatch();

        stmt.executeUpdate("INSERT INTO MagnumPiCast VALUES (3, 'Roger E.', 'Mosley'), (4, 'Larry', 'Manetti')");
        resultSet = stmt.executeQuery("SELECT * FROM MagnumPiCast");
    }

    @Test
    public void works() {
        assertTable("resultSet", DefaultTablePrinter.print(new ResultSetTableProvider(resultSet)));
    }
}