package com.globant.javacodecamp.loyalty.model;

import java.time.LocalDate;

public record Reward(long id,
                     LocalDate dateCreated,
                     long customerId,
                     int rate,
                     LocalDate dateValid,
                     boolean redeemed) implements Entity<Reward> {

  @Override
  public Reward setId(long id) {
    return new Reward(id, dateCreated, customerId, rate, dateValid, redeemed);
  }
}
