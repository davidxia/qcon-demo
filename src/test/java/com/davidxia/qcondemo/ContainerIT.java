package com.davidxia.qcondemo;

import com.spotify.helios.testing.HeliosDeploymentResource;
import com.spotify.helios.testing.HeliosSoloDeployment;
import com.spotify.helios.testing.TemporaryJob;
import com.spotify.helios.testing.TemporaryJobs;

import com.google.common.net.HostAndPort;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

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
public class ContainerIT {

  private static final HeliosDeploymentResource soloResource =
      new HeliosDeploymentResource(HeliosSoloDeployment.fromEnv().build());

  private static final TemporaryJobs temporaryJobs = TemporaryJobs.builder()
      .client(soloResource.client())
      .build();

  @ClassRule
  public static RuleChain chain = RuleChain
      .outerRule(soloResource)
      .around(temporaryJobs);

  private static TemporaryJob job;
  private static final OkHttpClient client = new OkHttpClient();
  private static String endpoint;

  @BeforeClass
  public static void setUp() throws Exception {
    // deploys a Helios job using the same config as the deployment pipeline, so that the port
    // mapping and other settings are reused here in the tests. The image the job uses is the
    // one that was built in this Maven build.
    job = temporaryJobs.jobWithConfig(".helios/helios_job_config.json")
        .imageFromBuild()
        .deploy();

    final HostAndPort httpPort = job.address("http");
    endpoint = String.format("http://%s:%d", httpPort.getHostText(), httpPort.getPort());
  }

  @Test
  public void testContainerHello() throws Exception {
    final Request request = new Request.Builder()
        .url(endpoint + "/hello")
        .build();

    final Response response = client.newCall(request).execute();
    assertThat(response.body().string(), equalTo("Hello World"));

    final Request request2 = new Request.Builder()
        .url(endpoint + "/david")
        .build();

    final Response response2 = client.newCall(request2).execute();
    assertThat(response2.body().string(), equalTo("Hello, david"));
  }
}
