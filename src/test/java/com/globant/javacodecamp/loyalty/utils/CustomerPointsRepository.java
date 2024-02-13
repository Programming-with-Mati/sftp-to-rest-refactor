package com.globant.javacodecamp.loyalty.utils;

import com.globant.javacodecamp.loyalty.model.CustomerPoints;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerPointsRepository extends AbstractRepository<CustomerPoints> {
  public CustomerPointsRepository(String connectionString) {
    super(connectionString, "customer_points", "points,date_created,customer_id");
  }

  @Override
  protected void setStatementParams(PreparedStatement statement, CustomerPoints entity) throws SQLException {
    statement.setInt(1, entity.points());
    statement.setDate(2, Date.valueOf(entity.dateCreated()));
    statement.setLong(3, entity.customerId());
  }

  @Override
  protected CustomerPoints mapResultSetToEntity(ResultSet resultSet) throws SQLException {
    return new CustomerPoints(
            resultSet.getLong("id"),
            resultSet.getInt("points"),
            resultSet.getDate("date_created").toLocalDate(),
            resultSet.getLong("customer_id")
    );
  }
}
