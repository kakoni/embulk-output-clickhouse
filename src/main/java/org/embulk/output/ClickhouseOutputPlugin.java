package org.embulk.output;

import java.util.Properties;
import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.embulk.spi.Exec;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.output.jdbc.AbstractJdbcOutputPlugin;
import org.embulk.output.jdbc.BatchInsert;
import org.embulk.output.jdbc.JdbcOutputConnection;
import org.embulk.output.jdbc.JdbcOutputConnector;
import org.embulk.output.jdbc.MergeConfig;
import org.embulk.output.jdbc.TableIdentifier;
import org.embulk.output.jdbc.TransactionIsolation;
import org.embulk.output.jdbc.Ssl;
import org.embulk.output.clickhouse.ClickhouseOutputConnector;

public class ClickhouseOutputPlugin extends AbstractJdbcOutputPlugin
{

    public interface ClickhousePluginTask
            extends PluginTask
    {
        @Config("host")
        public String getHost();

        @Config("port")
        @ConfigDefault("8123")
        public int getPort();

        @Config("user")
        public String getUser();

        @Config("password")
        @ConfigDefault("\"\"")
        public String getPassword();

        @Config("database")
        public String getDatabase();

        @Config("schema")
        @ConfigDefault("\"public\"")
        public String getSchema();
    }

    @Override
    protected Class<? extends PluginTask> getTaskClass()
    {
        return ClickhousePluginTask.class;
    }

    @Override
    protected Features getFeatures(PluginTask task)
    {
        return new Features()
            .setMaxTableNameLength(127)
            .setSupportedModes(ImmutableSet.of(Mode.INSERT, Mode.INSERT_DIRECT, Mode.TRUNCATE_INSERT, Mode.REPLACE, Mode.MERGE))
            .setIgnoreMergeKeys(false);
    }

    @Override
    protected JdbcOutputConnector getConnector(PluginTask task, boolean retryableMetadataOperation)
    {
        ClickhousePluginTask t = (ClickhousePluginTask) task;

        String url = String.format("jdbc:clickhouse://%s:%d/%s",
                t.getHost(), t.getPort(), t.getDatabase());

        Properties props = new Properties();

        props.setProperty("connectionTimeout", "300000"); // milliseconds
        props.setProperty("socketTimeout", "1800000"); // smillieconds

        props.putAll(t.getOptions());

        props.setProperty("user", t.getUser());
        props.setProperty("password", t.getPassword());
        logConnectionProperties(url, props);

        return new ClickhouseOutputConnector(url, props, t.getTransactionIsolation());
    }

    @Override
    protected BatchInsert newBatchInsert(PluginTask task, Optional<MergeConfig> mergeConfig)
            throws IOException, SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
