package org.embulk.output;

import ru.yandex.clickhouse.settings.ClickHouseConnectionSettings;

import java.util.Properties;
import java.io.IOException;
import java.sql.SQLException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.output.jdbc.AbstractJdbcOutputPlugin;
import org.embulk.output.jdbc.BatchInsert;
import org.embulk.output.jdbc.JdbcOutputConnector;
import org.embulk.output.jdbc.setter.ColumnSetterFactory;
import org.embulk.output.clickhouse.ClickhouseBatchInsert;
import org.embulk.output.clickhouse.setter.ClickhouseColumnSetterFactory;
import org.embulk.output.jdbc.MergeConfig;
import org.embulk.output.clickhouse.ClickhouseOutputConnector;
import org.joda.time.DateTimeZone;


public class ClickhouseOutputPlugin extends AbstractJdbcOutputPlugin
{

    public interface ClickhousePluginTask
            extends PluginTask
    {
        @Config("driver_path")
        @ConfigDefault("null")
        public Optional<String> getDriverPath();

        @Config("host")
        public String getHost();

        @Config("port")
        @ConfigDefault("8123")
        public int getPort();

        @Config("user")
        @ConfigDefault("null")
        public Optional<String> getUser();

        @Config("password")
        @ConfigDefault("null")
        public Optional<String> getPassword();

        @Config("database")
        public String getDatabase();

        @Config("buffer_size")
        @ConfigDefault("65536")
        public Optional<Integer> getBufferSize();

        @Config("apache_buffer_size")
        @ConfigDefault("65536")
        public Optional<Integer> getApacheBufferSize();

        @Config("connect_timeout")
        @ConfigDefault("30000")
        public int getConnectTimeout();

        @Config("socket_timeout")
        @ConfigDefault("30000")
        public int getSocketTimeout();

        @Config("data_transfer_timeout")
        @ConfigDefault("10000")
        public Optional<Integer> getDataTransferTimeout();

        @Config("keep_alive_timeout")
        @ConfigDefault("30000")
        public Optional<Integer> getKeepAliveTimeout();

        @Config("create_table_option")
        @ConfigDefault("\"ENGINE = TinyLog\"")
        public Optional<String> getCreateTableOption();

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
            .setSupportedModes(ImmutableSet.of(Mode.INSERT, Mode.INSERT_DIRECT, Mode.TRUNCATE_INSERT, Mode.REPLACE))
            .setIgnoreMergeKeys(false);
    }

    @Override
    protected JdbcOutputConnector getConnector(PluginTask task, boolean retryableMetadataOperation)
    {
        ClickhousePluginTask t = (ClickhousePluginTask) task;

        loadDriver("ru.yandex.clickhouse.ClickHouseDriver", t.getDriverPath());

        String url = String.format("jdbc:clickhouse://%s:%d/%s",
                t.getHost(), t.getPort(), t.getDatabase());

        Properties props = new Properties();

        if (t.getUser().isPresent()) {
            props.setProperty("user", t.getUser().get());
        }
        if (t.getPassword().isPresent()) {
            props.setProperty("password", t.getPassword().get());
        }

        // ClickHouse Connection Options
        if (t.getApacheBufferSize().isPresent()) {
            props.setProperty(ClickHouseConnectionSettings.APACHE_BUFFER_SIZE.getKey(), String.valueOf(t.getApacheBufferSize().get()));
        }
        if (t.getBufferSize().isPresent()) {
            props.setProperty(ClickHouseConnectionSettings.BUFFER_SIZE.getKey(), String.valueOf(t.getBufferSize().get()));
        }
        if (t.getDataTransferTimeout().isPresent()) {
            props.setProperty(ClickHouseConnectionSettings.DATA_TRANSFER_TIMEOUT.getKey(), String.valueOf(t.getDataTransferTimeout().get()));
        }
        if (t.getKeepAliveTimeout().isPresent()) {
            props.setProperty(ClickHouseConnectionSettings.KEEP_ALIVE_TIMEOUT.getKey(), String.valueOf(t.getKeepAliveTimeout().get()));
        }

        props.setProperty(ClickHouseConnectionSettings.SOCKET_TIMEOUT.getKey(), String.valueOf(t.getSocketTimeout()));
        props.setProperty(ClickHouseConnectionSettings.CONNECTION_TIMEOUT.getKey(), String.valueOf(t.getConnectTimeout()));

        props.putAll(t.getOptions());

        logConnectionProperties(url, props);

        return new ClickhouseOutputConnector(url, props, t.getTransactionIsolation());
    }

    @Override
    protected BatchInsert newBatchInsert(PluginTask task, Optional<MergeConfig> mergeConfig) throws IOException, SQLException
    {
        return new ClickhouseBatchInsert(getConnector(task, true), mergeConfig);
    }

    @Override
    protected ColumnSetterFactory newColumnSetterFactory(BatchInsert batch, DateTimeZone defaultTimeZone)
    {
        return new ClickhouseColumnSetterFactory(batch, defaultTimeZone);
    }
}
