package org.embulk.output.clickhouse.setter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import org.embulk.output.jdbc.BatchInsert;
import org.embulk.output.jdbc.JdbcColumn;
import org.embulk.output.jdbc.setter.DefaultValueSetter;
import org.embulk.output.jdbc.setter.SqlTimeColumnSetter;
import org.embulk.spi.time.Timestamp;

public class ClickhouseSqlTimeColumnSetter
        extends SqlTimeColumnSetter
{

    public ClickhouseSqlTimeColumnSetter(BatchInsert batch, JdbcColumn column,
                                         DefaultValueSetter defaultValue,
                                         Calendar calendar)
    {
        super(batch, column, defaultValue, calendar);
    }

    @Override
    public void timestampValue(Timestamp v) throws IOException, SQLException
    {
        batch.setSqlTimestamp(v, calendar);
    }

}
