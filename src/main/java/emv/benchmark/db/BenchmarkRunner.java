package emv.benchmark.db;

import emv.benchmark.db.benchmarks.PostgresBenchmark;
import emv.benchmark.db.util.BenchmarkUtils;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.sql.SQLException;

public class BenchmarkRunner {

  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder().include(PostgresBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(options).run();
  }

  public static void filDatabaseWithData() {
    try {
      BenchmarkUtils.FillDatabaseWithRandomData();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
