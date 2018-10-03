package org.srwk.passwordgenerator.client;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
  private final ClientOptions options;

  public Client(final ClientOptions options) {
    this.options = options;
  }

  public Pair<ClientStatistics, LetterHistogram> run() {
    final CloseableHttpClient client = createHttpClient();
    final ClientTask[] tasks = createTasks(client);
    executeTasks(tasks);
    return extractResults(tasks);
  }

  private CloseableHttpClient createHttpClient() {
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(options.getNumOfConnections());
    connectionManager.setDefaultMaxPerRoute(options.getNumOfConnections());
    return HttpClients.custom().setConnectionManager(connectionManager).build();
  }

  // To have n busy connections we have to have n tasks running in n threads.
  // This is bad, but it is constraint of blockin I/O model.
  private ClientTask[] createTasks(CloseableHttpClient client) {
    final HttpGet request = new HttpGet(options.getUri());
    final AtomicLong outstandingRequests = new AtomicLong(options.getNumOfRequests());
    final ClientTask[] tasks = new ClientTask[options.getNumOfConnections()];
    Arrays.setAll(tasks, i -> new ClientTask(client, request, outstandingRequests));
    return tasks;
  }

  private void executeTasks(ClientTask[] tasks) {
    final ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
    for (final ClientTask task : tasks) {
      executor.execute(task);
    }

    executor.shutdown();
    try {
      // ridicusly long timeout - if you want to terminate program, just press Ctrl+C
      executor.awaitTermination(1000L, TimeUnit.DAYS);
    } catch (final InterruptedException e) { }
  }

  private Pair<ClientStatistics, LetterHistogram> extractResults(ClientTask[] tasks) {
    final ClientStatistics statistics = ClientStatistics.combine(
      Arrays.stream(tasks)
        .map(ClientTask::getStatistics)
        .toArray(size -> new ClientStatistics[size])
    );
    final LetterHistogram histogram = LetterHistogram.combine(
        Arrays.stream(tasks)
          .map(ClientTask::getHistogram)
          .toArray(size -> new LetterHistogram[size])
    );

    return Pair.of(statistics, histogram);
  }
}
