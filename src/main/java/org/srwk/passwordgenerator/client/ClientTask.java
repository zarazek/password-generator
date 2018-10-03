package org.srwk.passwordgenerator.client;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.atomic.AtomicLong;

public class ClientTask implements Runnable {
  private final CloseableHttpClient client;
  private final HttpGet request;
  private final AtomicLong outstandingRequests;

  private final LetterHistogram histogram = new LetterHistogram();
  private final ClientStatistics statistics = new ClientStatistics();


  public ClientTask(final CloseableHttpClient client, HttpGet request, final AtomicLong numOfOutstandingRequests) {
    this.client = client;
    this.request = request;
    this.outstandingRequests = numOfOutstandingRequests;
  }

  @Override
  public void run() {
    while (outstandingRequests.getAndDecrement() > 0) {
      try {
        final HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          EntityUtils.consumeQuietly(response.getEntity());
          statistics.registerError();
          continue;
        }
        histogram.addString(EntityUtils.toString(response.getEntity()));
        statistics.registerSuccess();
      } catch (final Exception e) {
        statistics.registerError();
      }
    }
  }

  public LetterHistogram getHistogram() {
    return histogram;
  }

  public ClientStatistics getStatistics() {
    return statistics;
  }

}
