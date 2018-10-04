package org.srwk.passwordgenerator.client;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientApplication {
  private static final Map<String, BiConsumer<ClientOptions.ClientOptionsBuilder, Integer>> OPTIONS_PARSERS = ImmutableMap.of(
      "-numOfRequests", ClientOptions.ClientOptionsBuilder::numOfRequests,
      "-numOfConnections", ClientOptions.ClientOptionsBuilder::numOfConnections,
      "-numOfThreads", ClientOptions.ClientOptionsBuilder::numOfThreads
  );

  public static void main(String[] args) {
    final ClientOptions options;
    try {
      options = parseArguments(args);
    } catch (final InvalidCommandLineException ex) {
      System.out.println(ex.getLocalizedMessage());
      System.exit(1);
      return;
    }

    System.out.println(String.format(
        "Running %d requests over %d simulataneous connections using %d threads on %s",
        options.getNumOfRequests(),
        options.getNumOfConnections(),
        options.getNumOfThreads(),
        options.getUri().toString()
    ));
    final Client client = new Client(options);
    final Pair<ClientStatistics, LetterHistogram> result = client.run();

    final ClientStatistics statistics = result.getLeft();
    System.out.println(
        String.format(
          "Requests: %d total, %d successes,  %d errors",
          statistics.getTotal(),
          statistics.getSuccesses(),
          statistics.getErrors()
      )
    );

    if (statistics.getSuccesses() == 0) {
      return;
    }

    final LetterHistogram histogram = result.getRight();
    for (final Map.Entry<Character, Pair<Long, Double>> entry : histogram.getOccurrences().entrySet()) {
      System.out.println(
          String.format(
              "\t%c -> %.2f%% (%d)",
              entry.getKey(),
              100.0 * entry.getValue().getRight(),
              entry.getValue().getLeft()
          )
      );
    }
  }

  private static ClientOptions parseArguments(String[] args) throws InvalidCommandLineException {
    ClientOptions.ClientOptionsBuilder builder = ClientOptions.builder();
    if (args.length == 0) {
      throw new InvalidCommandLineException("Program requires at least one argument: request URI");
    }

    // last argument is always an URI
    final URI uri = parseUri(args[args.length - 1]);
    builder.uri(uri);

    // first arguments are for options
    for (int i = 0; i < args.length - 1; i += 2) {
      parseOption(builder, args[i], maybeAt(args, i + 1));
    }
    return builder.build();
  }

  private static void parseOption(final ClientOptions.ClientOptionsBuilder builder,
                                  final String optName,
                                  final String optValueStr) throws InvalidCommandLineException {
    final BiConsumer<ClientOptions.ClientOptionsBuilder, Integer> setter = OPTIONS_PARSERS.get(optName);
    if (setter == null) {
      throw new InvalidCommandLineException(String.format("Unknown option '%s'", optName));
    }
    if (optValueStr == null) {
      throw new InvalidCommandLineException(String.format("Option '%s' requres and argument", optName));
    }
    final int optValue;
    try {
      optValue = Integer.parseInt(optValueStr);
    } catch (final NumberFormatException ex) {
      throw new InvalidCommandLineException(String.format("Option '%s': '%s': invalid number", optName, optValueStr), ex);
    }
    if (optValue < 0) {
      throw new InvalidCommandLineException(String.format("Value for option '%s' should be > 0, but got %d", optName, optValue));
    }

    setter.accept(builder, optValue);
  }

  private static URI parseUri(String uriStr) throws InvalidCommandLineException {
    final URI uri;
    try {
      uri = new URI(uriStr);
    } catch (final URISyntaxException ex) {
      throw new InvalidCommandLineException(String.format("Invalid uri '%s'", uriStr), ex);
    }
    return uri;
  }

  private static <T> T maybeAt(final T[] arr, int idx) {
    if (idx < 0 || idx >= arr.length) {
      return null;
    }
    return arr[idx];
  }
}
