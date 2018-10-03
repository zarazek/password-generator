package org.srwk.passwordgenerator.server;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordGeneratorController {
  private final static int MAX_PASSWORD_LENGTH = 100;

  @GetMapping("/newPassword")
  public String newPassword(@RequestParam int length) {
    if (length < 1) {
      throw new IllegalArgumentException(String.format("Invalid password length: %d", length));
    }
    if (length > MAX_PASSWORD_LENGTH) {
      throw new IllegalArgumentException(
          String.format("Requested password too long: requeted %d, but max is %d", length, MAX_PASSWORD_LENGTH)
      );
    }
    return RandomStringUtils.randomAlphabetic(length);
  }
}
