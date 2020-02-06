Embulk::JavaPlugin.register_output(
  :clickhouse, "org.embulk.output.ClickhouseOutputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
