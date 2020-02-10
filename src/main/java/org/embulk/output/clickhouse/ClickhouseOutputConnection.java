package org.embulk.output.clickhouse;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.common.base.Optional;
import org.embulk.output.jdbc.JdbcColumn;
import org.embulk.output.jdbc.JdbcSchema;
import org.embulk.output.jdbc.JdbcOutputConnection;
import org.embulk.output.jdbc.MergeConfig;
import org.embulk.output.jdbc.TableIdentifier;
import ru.yandex.clickhouse.domain.ClickHouseDataType;

public class ClickhouseOutputConnection
        extends JdbcOutputConnection
{
    public ClickhouseOutputConnection(Connection connection)
            throws SQLException
    {
        super(connection, null);
    }

    @Override
    protected String buildColumnTypeName(JdbcColumn c)
    {
        switch(c.getSimpleTypeName()) {
            case "CLOB":
                return "String";
            case "DOUBLE PRECISION":
                return "Float64";
            case "DATETIME64(3)":
                return "DateTime64";

            default:
                return ClickHouseDataType.fromTypeString(c.getSimpleTypeName()).toString();
        }
    }

}
