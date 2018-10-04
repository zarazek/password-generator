package org.srwk.passwordgenerator.client;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
  private final ClientOptions options;

  public Client(final ClientOptions options) {
    this.options = options;
  }

  public Pair<ClientStatistics, LetterHistogram> run() {
    try (final CloseableHttpAsyncClient httpClient = createHttpClient()) {
      final List<ClientStatistics> statisticsList = Collections.synchronizedList(new ArrayList<>());
      final List<LetterHistogram> histogramsList = Collections.synchronizedList(new ArrayList<>());
      final ThreadLocal<ClientStatistics> localStatistics = ThreadLocal.withInitial(() -> {
        final ClientStatistics statistics = new ClientStatistics();
        statisticsList.add(statistics);
        return statistics;
      });
      final ThreadLocal<LetterHistogram> localHistogram = ThreadLocal.withInitial(() -> {
        final LetterHistogram histogram = new LetterHistogram();
        histogramsList.add(histogram);
        return histogram;
      });

      ExecutorService executor = Executors.newFixedThreadPool(options.getNumOfThreads());
      final CountDownLatch latch = new CountDownLatch(options.getNumOfRequests());
      final HttpGet request = new HttpGet(options.getUri());
      final FutureCallback<HttpResponse> callback = new ClientCallback(executor, localStatistics, localHistogram, latch);
      for (long i = 0; i < options.getNumOfRequests(); ++i) {
        httpClient.execute(request, callback);
      }
      latch.await();
      executor.shutdown();
      executor.awaitTermination(1000, TimeUnit.DAYS);

      final ClientStatistics statistics = ClientStatistics.combine(statisticsList);
      final LetterHistogram histogram = LetterHistogram.combine(histogramsList);
      return Pair.of(statistics, histogram);
    } catch (final Exception ex) {
      throw new RuntimeException("Client failure", ex);
    }
  }

  private CloseableHttpAsyncClient createHttpClient() throws IOReactorException {
    final ConnectingIOReactor reactor;
    reactor = new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT);
    final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(reactor);
    connectionManager.setMaxTotal(options.getNumOfConnections());
    connectionManager.setDefaultMaxPerRoute(options.getNumOfConnections());
    final CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
        .setConnectionManager(connectionManager)
        .build();
    httpClient.start();
    return httpClient;
  }

}
