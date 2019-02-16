package com.github.chrisgleissner.jutil.table.provider;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.MethodExecutionContext;
import net.ttddyy.dsproxy.listener.MethodExecutionListener;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.jdk.JdkJdbcProxyFactory;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

@Slf4j
public class ResultSetTableProviderTest {

    private ResultSet resultSet;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection conn = proxy(DriverManager.getConnection("jdbc:h2:mem:test", "sa", ""));
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

    private Connection proxy(Connection connection) {
        DefaultQueryLogEntryCreator logCreator = new DefaultQueryLogEntryCreator() {
            protected void writeTimeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
            }
        };
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .queryListener(new NoOpQueryExecutionListener() {
                    @Override
                    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
                        log.info("{}", logCreator.getLogEntry(executionInfo, list, false, false));
                    }
                })
                .build();
        return new JdkJdbcProxyFactory().createConnection(connection, new ConnectionInfo(), proxyConfig);
    }

    @Test
    public void works() {
        assertTable("resultSet", DefaultTablePrinter.print(new ResultSetTableProvider(resultSet)));
    }
}