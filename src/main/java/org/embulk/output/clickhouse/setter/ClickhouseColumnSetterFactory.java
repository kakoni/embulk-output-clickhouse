package org.embulk.output.clickhouse.setter;

import org.embulk.output.jdbc.BatchInsert;
import org.embulk.output.jdbc.JdbcColumn;
import org.embulk.output.jdbc.JdbcColumnOption;
import org.embulk.output.jdbc.setter.ColumnSetter;
import org.embulk.output.jdbc.setter.ColumnSetterFactory;
import org.embulk.output.jdbc.setter.SqlTimestampColumnSetter;
import org.embulk.output.jdbc.setter.StringColumnSetter;
import org.joda.time.DateTimeZone;

public class ClickhouseColumnSetterFactory
        extends ColumnSetterFactory
{
    public ClickhouseColumnSetterFactory(BatchInsert batch, DateTimeZone defaultTimeZone)
    {
        super(batch, defaultTimeZone);
    }

    @Override
    public ColumnSetter newCoalesceColumnSetter(JdbcColumn column, JdbcColumnOption option)
    {
        if (column.getSimpleTypeName().equalsIgnoreCase("datetime64(3)")) {
            // actually "timestamp"
            return new ClickhouseSqlTimeColumnSetter(batch, column, newDefaultValueSetter(column, option), newCalendar(option));
        } else {
            return super.newCoalesceColumnSetter(column, option);
        }
    }

}
