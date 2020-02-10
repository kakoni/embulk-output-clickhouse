# Clickhouse output plugin for Embulk

Alpha stage Clickhouse output plugin for embulk. Loads record to clickhouse server.

## Overview

* **Plugin type**: output

## Configuration
- **driver_path**: path to the jar file of the JDBC driver. If not set, the bundled JDBC driver (Clickhouse-jdbc) will be used. (string)
- **host**: database host name (string, required)
- **port**: database port number (integer, default: 8123)
- **user**: database login user name (string, optional)
- **password**: database login password (string, optional)
- **database**: destination database name (string, required)
- **table**: destination table name (string, required)
- **buffer_size**: see ClickHouse param **buffer_size** (integer, default: 65535)
- **apache_buffer_size**: see ClickHouse param **apache_buffer_size** (integer, default: 65535)
- **connect_timeout**: see ClickHouse param **connection_timeout** (integer, default: 30000)
- **socket_timeout**: see ClickHouse param **socket_timeout** (integer, default: 30000)
- **data_transfer_timeout**: see ClickHouse param **data_transfer_timeout** (integer, default: 10000)
- **keep_alive_timeout**: see ClickHouse param **keep_alive_timeout** (integer, default: 10000)
- **column_options**: advanced: a key-value pairs where key is a column name and value is options for the column.
  - **type**: type of a column when this plugin creates new tables (e.g. `VARCHAR(255)`, `INTEGER NOT NULL UNIQUE`). This used when this plugin creates intermediate tables (insert and truncate_insert modes), when it creates the target table (replace mode), and when it creates nonexistent target table automatically. (string, default: depends on input column type. `BIGINT` if input column type is long, `BOOLEAN` if boolean, `DOUBLE PRECISION` if double, `CLOB` if string, `TIMESTAMP` if timestamp)
  - **value_type**: This plugin converts input column type (embulk type) into a database type to build a INSERT statement. This value_type option controls the type of the value in a INSERT statement. (string, default: depends on the sql type of the column. Available values options are: `byte`, `short`, `int`, `long`, `double`, `float`, `boolean`, `string`, `nstring`, `date`, `time`, `timestamp`, `decimal`, `json`, `null`, `pass`)
  - **timestamp_format**: If input column type (embulk type) is timestamp and value_type is `string` or `nstring`, this plugin needs to format the timestamp value into a string. This timestamp_format option is used to control the format of the timestamp. (string, default: `%Y-%m-%d %H:%M:%S.%6N`)
  - **timezone**: If input column type (embulk type) is timestamp, this plugin needs to format the timestamp value into a SQL string. In this cases, this timezone option is used to control the timezone. (string, value of default_timezone option is used by default)
- **before_load**: if set, this SQL will be executed before loading all records. In truncate_insert mode, the SQL will be executed after truncating. replace mode doesn't support this option.
- **after_load**: if set, this SQL will be executed after loading all records.

### Modes

* **insert**:
  * Behavior: This mode writes rows to some intermediate tables first. If all those tasks run correctly, runs `INSERT INTO <target_table> SELECT * FROM <intermediate_table_1> UNION ALL SELECT * FROM <intermediate_table_2> UNION ALL ...` query. If the target table doesn't exist, it is created automatically.
  * Transactional: Yes. This mode successfully writes all rows, or fails with writing zero rows.
  * Resumable: Yes.
* **insert_direct**:
  * Behavior: This mode inserts rows to the target table directly. If the target table doesn't exist, it is created automatically.
  * Transactional: No. If fails, the target table could have some rows inserted.
  * Resumable: No.
* **truncate_insert**:
  * Behavior: Same with `insert` mode excepting that it truncates the target table right before the last `INSERT ...` query.
  * Transactional: Yes.
  * Resumable: Yes.
* **replace**:
  * Behavior: This mode writes rows to an intermediate table first. If all those tasks run correctly, drops the target table and alters the name of the intermediate table into the target table name.
  * Transactional: Yes.
  * Resumable: No.

## Example

```yaml
out:
  type: clickhouse
  host: localhost
  database: test
  table: test_table
  mode: insert
```



## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
