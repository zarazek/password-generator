package org.srwk.passwordgenerator.server;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Value
public class ErrorMessage {
  private final LocalDateTime timestamp;
  @JsonIgnore
  private final HttpStatus httpStatus;
  private final String message;

  public ErrorMessage(final HttpStatus httpStatus, final String message) {
    this.timestamp = LocalDateTime.now();
    this.httpStatus = httpStatus;
    this.message = message;
  }

  public ErrorMessage(final HttpStatus httpStatus, final Throwable ex) {
    this(httpStatus, ex.getLocalizedMessage());
  }

  @JsonProperty
  public int getStatus() {
    return httpStatus.value();
  }

  @JsonProperty
  public String getError() {
    return httpStatus.getReasonPhrase();
  }
}
