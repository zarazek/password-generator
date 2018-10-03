package org.srwk.passwordgenerator.client;

import lombok.Getter;

@Getter
public class ClientStatistics {
  private long successes = 0;
  private long errors = 0;

  void registerSuccess() {
    ++successes;
  }

  void registerError() {
    ++errors;
  }

  long getTotal() {
    return successes + errors;
  }

  public static ClientStatistics combine(final ClientStatistics... statistics) {
    final ClientStatistics result = new ClientStatistics();
    for (final ClientStatistics single : statistics) {
      result.successes += single.successes;
      result.errors += single.errors;
    }
    return result;
  }
}
