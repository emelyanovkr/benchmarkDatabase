package emv.benchmark.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@Warmup(iterations = 2)
public class PostgresBenchmark {

  private static Connection connection;

  private static String EXECUTORS_TABLE;
  private static String FLOWS_TABLE;

  @Setup
  public static void prepareConnection() throws SQLException {
    Dotenv dotenv = Dotenv.load();
    String DATABASE_URL = dotenv.get("DATABASE_URL");
    String POSTGRES_USER = dotenv.get("POSTGRES_USER");
    String POSTGRES_PASSWORD = dotenv.get("POSTGRES_PASSWORD");

    EXECUTORS_TABLE = dotenv.get("EXECUTORS_TABLE");
    FLOWS_TABLE = dotenv.get("FLOWS_TABLE");

    connection = DriverManager.getConnection(DATABASE_URL, POSTGRES_USER, POSTGRES_PASSWORD);
  }

  @TearDown
  public static void closeConnection() throws SQLException {
    connection.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void checkConnection() throws SQLException {
    try (PreparedStatement preparedStatement =
        connection.prepareStatement("SELECT *" + " FROM " + FLOWS_TABLE)) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        System.out.println(resultSet.getString(2));
        System.out.println(resultSet.getInt(3));
        System.out.println(resultSet.getInt(4));
      }
    }
  }
}
