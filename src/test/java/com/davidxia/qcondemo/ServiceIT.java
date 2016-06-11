package com.davidxia.qcondemo;

import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * This integration test checks that requests to the service's endpoints return the correct
 * responses. Start the service before running this test.
 */
public class ServiceIT {

  private final OkHttpClient client = new OkHttpClient();

  @Test
  public void testService() throws Exception {
    final Request request = new Request.Builder()
        .url("http://localhost:4567/hello")
        .build();

    final Response response = client.newCall(request).execute();
    assertThat(response.body().string(), equalTo("Hello, World"));

    final Request request2 = new Request.Builder()
        .url("http://localhost:4567/David")
        .build();

    final Response response2 = client.newCall(request2).execute();
    assertThat(response2.body().string(), equalTo("Hello, David"));
  }
}
