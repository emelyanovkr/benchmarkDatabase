package emv.benchmark.db.benchmarks;

import emv.benchmark.db.FlowsState;
import emv.benchmark.db.util.BenchmarkUtils;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class PostgresBenchmark {

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 0, time = 1)
  @Measurement(iterations = 1, time = 1)
  public void checkConnection() throws SQLException {
    try (Connection connection = BenchmarkUtils.getConnection();
        PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT * FROM flows LIMIT 1")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        System.out.println(resultSet.getInt(3) + " | " + resultSet.getInt(4));
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void rowLockSingleTransaction() {
    try (Connection connection = BenchmarkUtils.getConnection()) {
      connection.setAutoCommit(false);
      String acquireLockQuery =
          """
          SELECT flow_id, state, status
                  FROM flows
                  WHERE status < 10000
                  ORDER BY flow_id
                  LIMIT 1
                  FOR UPDATE
                  SKIP LOCKED
          """;
      String updateFlowsQuery =
          """
          UPDATE flows
          SET state = ?, status = ?
          WHERE flow_id = ?
          """;
      try (PreparedStatement acquireLockStatement = connection.prepareStatement(acquireLockQuery);
          PreparedStatement updateFlowsStatement = connection.prepareStatement(updateFlowsQuery)) {
        ResultSet acquiredLockSet = acquireLockStatement.executeQuery();

        if (acquiredLockSet.next()) {
          int lockedFlowId = acquiredLockSet.getInt(1);
          int acquiredStatus = acquiredLockSet.getInt(3);

          String generatedState = FlowsState.getRandomState().toString();
          int newStatus = ++acquiredStatus;

          updateFlowsStatement.setString(1, generatedState);
          updateFlowsStatement.setInt(2, newStatus);
          updateFlowsStatement.setInt(3, lockedFlowId);

          updateFlowsStatement.executeUpdate();

          connection.commit();
        } else {
          connection.rollback();
        }
      } catch (SQLException e) {

        connection.rollback();
      }
    } catch (RuntimeException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void rowLockTwoTransactions() {
    try (Connection connection = BenchmarkUtils.getConnection()) {
      connection.setAutoCommit(false);

      String findLocksQuery =
          """
      SELECT flow_id, state, status
      FROM flows
      WHERE executor_id IS NULL
      AND status < 10000
      ORDER BY flow_id
      LIMIT 1
      FOR UPDATE
      SKIP LOCKED
      """;

      String acquireLockQuery =
          """
      UPDATE flows
      SET executor_id = ?
      WHERE flow_id = ?
      """;

      String updateStateQuery =
          """
      UPDATE flows
      SET executor_id = NULL, state = ?, status = ?
      WHERE flow_id = ?
      """;

      try (PreparedStatement findLocksStatement = connection.prepareStatement(findLocksQuery);
          PreparedStatement acquireLockStatement = connection.prepareStatement(acquireLockQuery);
          PreparedStatement updateStatement = connection.prepareStatement(updateStateQuery)) {

        ResultSet foundLocks = findLocksStatement.executeQuery();

        if (foundLocks.next()) {
          int lockedFlowId = foundLocks.getInt(1);
          acquireLockStatement.setInt(1, new Random().nextInt(5, 1000));
          acquireLockStatement.setInt(2, lockedFlowId);
          connection.commit();

          int lockedStatus = foundLocks.getInt(3);

          updateStatement.setString(1, FlowsState.getRandomState().toString());
          updateStatement.setInt(2, ++lockedStatus);
          updateStatement.setInt(3, lockedFlowId);

          updateStatement.executeUpdate();
          connection.commit();
        } else {
          connection.rollback();
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
