This project demonstrates some key concepts in my [QCon NYC 2016 talk][1].

This Java + Maven + Spark project shows how containers can be used in test to help:

1. Test more of the stack in an environment resembling production.
2. Easily start real dependencies
3. Ensure tests are reproducible and isolated

The test framework we use is [helios-testing][2] which is Docker + Java + JUnit based, but one could
use another test framework that applies the same concept of starting and stopping
containers programatically.

`ServiceIT` is a conventional integration test that checks the service behaves correctly.
`ContainerIT` is an IT that starts the service *inside* a Docker container and checks it's behaving
correctly from *outside* the container.

  [1]: https://qconnewyork.com/ny2016/presentation/next-level-integration-testing-containers
  [2]: https://github.com/spotify/helios/blob/master/docs/testing_framework.md
