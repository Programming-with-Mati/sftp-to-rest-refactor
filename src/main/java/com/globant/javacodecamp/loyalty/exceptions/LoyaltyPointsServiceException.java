package com.globant.javacodecamp.loyalty.exceptions;

public class LoyaltyPointsServiceException extends RuntimeException {
  public LoyaltyPointsServiceException(Throwable cause) {
    super("Unable to process loyalty points", cause);
  }
}
