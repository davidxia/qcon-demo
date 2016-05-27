package com.davidxia.qcondemo;

import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * A simple integration test that tests that the service can start and respond to
 * a request.
 * <p>
 * Extend this integration test with more tests specific to your service! We
 * recommend that you take a look a Cucumber for acceptance test definitions.</p>
 */
public class ServiceIT {

  private final OkHttpClient client = new OkHttpClient();

  @Test
  public void testService() throws Exception {
    final Request request = new Request.Builder()
        .url("http://localhost:4567/hello")
        .build();

    final Response response = client.newCall(request).execute();
    assertThat(response.body().string(), equalTo("Hello World"));
  }
}
