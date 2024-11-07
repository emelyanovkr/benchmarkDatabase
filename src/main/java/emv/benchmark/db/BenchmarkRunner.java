package emv.benchmark.db;

import emv.benchmark.db.benchmarks.PostgresBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {

  public void runBenchmark() throws RunnerException
  {
    Options options =
        new OptionsBuilder().include(PostgresBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(options).run();
  }
}
