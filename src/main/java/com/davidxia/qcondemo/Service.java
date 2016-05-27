package com.davidxia.qcondemo;

import static spark.Spark.get;

public class Service {
  public static void main(String[] args) {
    get("/hello", (req, res) -> "Hello World");
    get("/:name", (req, res) -> {
      final String name = req.params(":name");
      return "Hello, " + name;
    });
  }
}
