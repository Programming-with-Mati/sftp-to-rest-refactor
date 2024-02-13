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
}
