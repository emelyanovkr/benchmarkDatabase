package emv.benchmark.db.util;

import emv.benchmark.db.FlowsState;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class BenchmarkUtils {

  private BenchmarkUtils() {}

  private static Connection connection;

  public static Connection getConnection() throws SQLException {
    Dotenv dotenv = Dotenv.load();
    String DATABASE_URL = dotenv.get("DATABASE_URL");
    String POSTGRES_USER = dotenv.get("POSTGRES_USER");
    String POSTGRES_PASSWORD = dotenv.get("POSTGRES_PASSWORD");

    return DriverManager.getConnection(DATABASE_URL, POSTGRES_USER, POSTGRES_PASSWORD);
  }

  public static void FillDatabaseWithRandomData() throws SQLException {
    connection = getConnection();
    try (PreparedStatement executorsStatement =
            connection.prepareStatement("INSERT INTO executors(name) VALUES(?)");
        PreparedStatement flowsStatement =
            connection.prepareStatement(
                "INSERT INTO flows(state, executor_id, status) VALUES(?, ?, ?)")) {
      for (int i = 0; i < 1000; i++) {
        executorsStatement.setString(1, "executor_" + i);
        executorsStatement.addBatch();

        flowsStatement.setString(1, FlowsState.getRandomState().toString());
        flowsStatement.setInt(2, new Random().nextInt(5, 1000));
        flowsStatement.setInt(3, new Random().nextInt(10000));

        flowsStatement.addBatch();
      }

      executorsStatement.executeBatch();
      flowsStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
