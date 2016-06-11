package com.davidxia.qcondemo;

import com.spotify.helios.testing.HeliosDeploymentResource;
import com.spotify.helios.testing.HeliosSoloDeployment;
import com.spotify.helios.testing.TemporaryJob;
import com.spotify.helios.testing.TemporaryJobs;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.net.HostAndPort;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.davidxia.qcondemo.Service.KEYSPACE;
import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * This integration tests the service running by itself (not in a container)
 * and an instance of Cassandra running inside a container.
 */
public class CassandraContainerIT {

  private static final int CASSANDRA_CQL_PORT = 9042;

  private static final HeliosDeploymentResource soloResource =
      new HeliosDeploymentResource(HeliosSoloDeployment.fromEnv().build());

  private static final TemporaryJobs temporaryJobs = TemporaryJobs.builder()
      .client(soloResource.client())
      .build();

  @ClassRule
  public static RuleChain chain = RuleChain
      .outerRule(soloResource)
      .around(temporaryJobs);

  private static final OkHttpClient client = new OkHttpClient();

  private TemporaryJob cassandraJob;
  private TemporaryJob serviceJob;
  private String endpoint;

  @Before
  public void before() throws Exception {
    cassandraJob = temporaryJobs.job()
        .image("cassandra:latest")
        .port("cassandra", CASSANDRA_CQL_PORT, CASSANDRA_CQL_PORT)
        .deploy();

    final HostAndPort cassHostPort = cassandraJob.address("cassandra");
    final Cluster cassCluster = Cluster.builder()
        .addContactPoint(cassHostPort.getHostText())
        .withPort(cassHostPort.getPort())
        .build();

    try (final Session session = cassCluster.connect()) {
      createSchema(session);
    }

    serviceJob = temporaryJobs.jobWithConfig(".helios/helios_job_config.json")
        .env("CASSANDRA_HOST", firstNonNull(System.getenv("CASSANDRA_HOST"), "127.0.0.1"))
        .imageFromBuild()
        .deploy();

    final HostAndPort httpPort = serviceJob.address("http");
    endpoint = format("http://%s:%d", httpPort.getHostText(), httpPort.getPort());
  }

  private void createSchema(final Session session) {
      session.execute("CREATE KEYSPACE " + KEYSPACE + " WITH replication "
                      + "= {'class':'SimpleStrategy', 'replication_factor':1};");
      session.execute(format(
          "CREATE TABLE %s.users (" +
          "username text PRIMARY KEY" +
          ");", KEYSPACE));
  }

  @Test
  public void testAddUser() throws Exception {
    final Request request = new Request.Builder()
        .url(endpoint + "/users/david")
        .build();
    final Response response = client.newCall(request).execute();
    assertThat(response.code(), equalTo(404));

    final Request request2 = new Request.Builder()
        .post(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "david"))
        .url(endpoint + "/users")
        .build();
    final Response response2 = client.newCall(request2).execute();
    assertThat(response2.code(), equalTo(200));

    final Request request3 = new Request.Builder()
        .url(endpoint + "/users/david")
        .build();
    final Response response3 = client.newCall(request3).execute();
    assertThat(response3.code(), equalTo(200));
    assertThat(response3.body().string(), equalTo("Found david in the datastore!"));
  }
}
