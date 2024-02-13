package com.globant.javacodecamp.loyalty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsRequest {

  private long customerId;
  private int points;
}
