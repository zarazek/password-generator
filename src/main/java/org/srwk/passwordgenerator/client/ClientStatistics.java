package org.srwk.passwordgenerator.client;

import lombok.Getter;

@Getter
public class ClientStatistics {
  private long successes = 0;
  private long errors = 0;

  public synchronized void registerSuccess() {
    ++successes;
  }

  public synchronized void registerError() {
    ++errors;
  }

  long getTotal() {
    return successes + errors;
  }

  public static ClientStatistics combine(final Iterable<ClientStatistics> statisticsCollection) {
    final ClientStatistics result = new ClientStatistics();
    for (final ClientStatistics single : statisticsCollection) {
      result.successes += single.successes;
      result.errors += single.errors;
    }
    return result;
  }
}
