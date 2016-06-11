package com.davidxia.qcondemo;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.Iterator;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static spark.Spark.get;
import static spark.Spark.post;

public class Service {

  static final String KEYSPACE = "qcondemo";

  public static void main(String[] args) {
    // Connect to the cluster
    final String contactPoint = firstNonNull(System.getenv("CASSANDRA_HOST"), "127.0.0.1");
    final Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();

    get("/hello", (req, res) -> "Hello, World");

    get("/:name", (req, res) -> {
      final String name = req.params(":name");
      return "Hello, " + name;
    });

    post("/users", (req, res) -> {
      final String username = req.body();

      // Insert one record into the users table
      try (final Session session = cluster.connect(KEYSPACE)) {
        session.execute(format(
            "INSERT INTO %s.users (username) VALUES ('%s')", KEYSPACE, username));
        return "";
      }
    });

    get("/users/:username", (req, res) -> {
      // Use select to get the user we just entered
      final String username = req.params(":username");

      try (final Session session = cluster.connect(KEYSPACE)) {
        final ResultSet resultSet = session.execute(format(
            "SELECT * FROM %s.users WHERE username='%s'", KEYSPACE, username));
        final Iterator<Row> iterator = resultSet.iterator();

        if (!iterator.hasNext()) {
          res.status(404);
          return "";
        }

        final Row row = iterator.next();
        return format("Found %s in the datastore!", row.getString("username"));
      }
    });
  }
}
