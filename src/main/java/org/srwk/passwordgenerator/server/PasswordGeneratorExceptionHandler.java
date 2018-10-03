package org.srwk.passwordgenerator.server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PasswordGeneratorExceptionHandler {
  @ExceptionHandler({ IllegalArgumentException.class })
  public ResponseEntity<Object> handleIllegalArgument(final IllegalArgumentException ex) {
    final ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST, ex);
    return new ResponseEntity<>(message, message.getHttpStatus());
  }
}
