package com.globant.javacodecamp.loyalty.repositories;

import com.globant.javacodecamp.loyalty.model.Reward;

import java.sql.*;

public class RewardRepository extends AbstractRepository<Reward> {

  public RewardRepository(String connectionString) {
    super(connectionString, "reward", "date_created,customer_id,rate,date_valid,redeemed");
  }

  @Override
  protected void setStatementParams(PreparedStatement statement, Reward entity) throws SQLException {
    statement.setDate(1, Date.valueOf(entity.dateCreated()));
    statement.setLong(2, entity.customerId());
    statement.setInt(3, entity.rate());
    statement.setDate(4, Date.valueOf(entity.dateValid()));
    statement.setBoolean(5, entity.redeemed());
  }

  @Override
  protected Reward mapResultSetToEntity(ResultSet resultSet) throws SQLException {
    return new Reward(
            resultSet.getLong("id"),
            resultSet.getDate("date_created").toLocalDate(),
            resultSet.getLong("customer_id"),
            resultSet.getInt("rate"),
            resultSet.getDate("date_valid").toLocalDate(),
            resultSet.getBoolean("redeemed")
    );
  }
}
