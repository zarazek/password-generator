package org.srwk.passwordgenerator.client;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

class ClientCallback implements FutureCallback<HttpResponse> {
  private final ExecutorService executor;
  private final ThreadLocal<ClientStatistics> localStatistics;
  private final ThreadLocal<LetterHistogram> localHistogram;
  private final CountDownLatch latch;

  public ClientCallback(ExecutorService executor, ThreadLocal<ClientStatistics> localStatistics, ThreadLocal<LetterHistogram> localHistogram, CountDownLatch latch) {
    this.executor = executor;
    this.localStatistics = localStatistics;
    this.localHistogram = localHistogram;
    this.latch = latch;
  }

  @Override
  public void completed(final HttpResponse httpResponse) {
    executor.execute(() -> {
      try {
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          EntityUtils.consumeQuietly(httpResponse.getEntity());
          localStatistics.get().registerError();
          return;
        }
        localHistogram.get().addString(EntityUtils.toString(httpResponse.getEntity()));
        localStatistics.get().registerSuccess();
      } catch (final Exception e) {
        localStatistics.get().registerError();
      }
    });
    latch.countDown();
  }

  @Override
  public void failed(final Exception e) {
    executor.execute(() -> localStatistics.get().registerError());
    latch.countDown();
  }

  @Override
  public void cancelled() {
    executor.execute(() -> localStatistics.get().registerError());
    latch.countDown();
  }
}
