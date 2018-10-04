package org.srwk.passwordgenerator.client;

import lombok.Builder;
import lombok.Value;

import java.net.URI;

@Value @Builder
public class ClientOptions {
  private final URI uri;
  @Builder.Default
  private final int numOfRequests = 100000;
  @Builder.Default
  private final int numOfConnections = 100;
  @Builder.Default
  private int numOfThreads = 4;

}
