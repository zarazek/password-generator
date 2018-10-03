package org.srwk.passwordgenerator.client;

import lombok.Builder;
import lombok.Value;

import java.net.URI;

@Value @Builder
public class ClientOptions {
  private final URI uri;
  @Builder.Default
  private final int numOfRequests = 1000;
  @Builder.Default
  private final int numOfConnections = 5;
  @Builder.Default
  private int numOfThreads = 20;

}
