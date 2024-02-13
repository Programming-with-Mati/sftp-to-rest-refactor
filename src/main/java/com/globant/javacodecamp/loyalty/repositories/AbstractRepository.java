package com.globant.javacodecamp.loyalty.repositories;

import com.globant.javacodecamp.loyalty.model.CustomerPoints;
import com.globant.javacodecamp.loyalty.model.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractRepository<T extends Entity<T>> {

  private final String connectionString;
  private final String table;
  private final String columns;
  private final String insertParamList;


  public AbstractRepository(String connectionString, String table, String columns) {
    this.connectionString = connectionString;
    this.table = table;
    this.columns = columns;
    this.insertParamList = Arrays.stream(columns.split(","))
            .map(s -> "?")
            .collect(Collectors.joining(","));
  }

  public T save(T entity) {
    String sql = """
            INSERT INTO %s (%s)
            VALUES (%s)""".formatted(table, columns, insertParamList);
    try (Connection connection = createConnection();
         PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      setStatementParams(statement, entity);
      statement.execute();
      var resultSet = statement.getGeneratedKeys();
      resultSet.next();
      return entity.setId(resultSet.getLong(1));
    } catch (SQLException e) {
      throw new RuntimeException("Error reading database", e);
    }
  }

  protected abstract void setStatementParams(PreparedStatement statement, T entity) throws SQLException;

  public List<T> findAll() {
    String sql = "SELECT * FROM %s;".formatted(table);
    List<T> result = new ArrayList<>();
    try (Connection connection = createConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(sql)) {
      while (resultSet.next()) {
        T value = mapResultSetToEntity(resultSet);
        result.add(value);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error reading database", e);
    }

    return result;
  }

  public void update(T entity) {
    String sql = """
                      UPDATE %s
                      SET %s
                      WHERE id =?;""".formatted(table, getUpdateSection(columns));
    try (Connection connection = createConnection();
       PreparedStatement statement = connection.prepareStatement(sql)) {
       setStatementParams(statement, entity);
       statement.setLong(columns.split(",").length + 1, entity.id());
       statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error reading database", e);
    }
  }

  private String getUpdateSection(String columns) {
    return Arrays.stream(columns.split(","))
            .map("%s = ?"::formatted)
            .collect(Collectors.joining(","));
  }

  public List<T> findAll(Predicate<T> condition) {
    return findAll().stream()
            .filter(condition)
            .toList();
  }

  protected abstract T mapResultSetToEntity(ResultSet resultSet) throws SQLException;

  protected Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return DriverManager
            .getConnection(connectionString, "root", "test");
  }


}
