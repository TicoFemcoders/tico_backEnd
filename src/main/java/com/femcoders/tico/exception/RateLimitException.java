package com.femcoders.tico.exception;

public class RateLimitException extends RuntimeException {

  public RateLimitException(String message) {
    super(message);
  }
}
