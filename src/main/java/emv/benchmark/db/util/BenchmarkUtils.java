package emv.benchmark.db.util;

import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class BenchmarkUtils {

  private BenchmarkUtils() {}

  private static final HikariDataSource dataSource = new HikariDataSource();

  static {
    Dotenv dotenv = Dotenv.load();
    String DATABASE_URL = dotenv.get("DATABASE_URL");
    String POSTGRES_USER = dotenv.get("POSTGRES_USER");
    String POSTGRES_PASSWORD = dotenv.get("POSTGRES_PASSWORD");

    int MINIMUM_IDLE_THREADS = Integer.parseInt(dotenv.get("MINIMUM_IDLE_THREADS"));
    int MAXIMUM_POOL_SIZE = Integer.parseInt(dotenv.get("MAXIMUM_POOL_SIZE"));

    dataSource.setJdbcUrl(DATABASE_URL);
    dataSource.setUsername(POSTGRES_USER);
    dataSource.setPassword(POSTGRES_PASSWORD);
    dataSource.setAutoCommit(false);
    dataSource.setMinimumIdle(MINIMUM_IDLE_THREADS);
    dataSource.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
  }

  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public static void FillDatabaseWithRandomData() throws SQLException {
    Connection connection = getConnection();
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
