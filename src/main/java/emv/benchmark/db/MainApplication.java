package emv.benchmark.db;

import emv.benchmark.db.util.BenchmarkUtils;
import java.sql.SQLException;
import org.openjdk.jmh.runner.RunnerException;

public class MainApplication
{

  public static void main(String[] args) throws RunnerException {
    BenchmarkRunner runner = new BenchmarkRunner();
    runner.runBenchmark();
  }

  public static void filDatabaseWithData() {
    try {
      BenchmarkUtils.FillDatabaseWithRandomData();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
