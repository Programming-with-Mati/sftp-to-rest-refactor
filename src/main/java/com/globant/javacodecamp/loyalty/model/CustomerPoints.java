package com.globant.javacodecamp.loyalty.model;

import java.time.LocalDate;

public record CustomerPoints(
        long id,
        int points,
        LocalDate dateCreated,
        long customerId
) implements Entity<CustomerPoints> {
  @Override
  public CustomerPoints setId(long id) {
    return new CustomerPoints(id, points, dateCreated, customerId);
  }

  public CustomerPoints add(Integer newPoints) {
    return new CustomerPoints(id, points + newPoints, dateCreated, customerId);
  }

  public CustomerPoints resetPoints() {
    return new CustomerPoints(id, 0, dateCreated, customerId);
  }
}
