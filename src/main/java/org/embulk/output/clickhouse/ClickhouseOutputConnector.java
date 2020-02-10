package org.embulk.output.clickhouse;

import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.embulk.output.jdbc.AbstractJdbcOutputConnector;
import org.embulk.output.jdbc.JdbcOutputConnection;
import org.embulk.output.jdbc.TransactionIsolation;

import com.google.common.base.Optional;

public class ClickhouseOutputConnector
        extends AbstractJdbcOutputConnector
{

    private final String url;
    private final Properties properties;

    public ClickhouseOutputConnector(String url, Properties properties,
            Optional<TransactionIsolation> transactionIsolation)
    {
        super(transactionIsolation);

        this.url = url;
        this.properties = properties;
    }

    @Override
    protected JdbcOutputConnection connect() throws SQLException
    {
        Connection c = DriverManager.getConnection(url, properties);
        try {
          ClickhouseOutputConnection con = new ClickhouseOutputConnection(c);
            c = null;
            return con;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
