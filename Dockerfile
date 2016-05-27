FROM davidxia/qcon-demo:base
MAINTAINER David Xia <dxia@spotify.com>

ENTRYPOINT ["/bin/bash", "-c", "exec /usr/bin/java $JVM_DEFAULT_ARGS $JVM_ARGS -jar /usr/share/qcon-demo/qcon-demo.jar \"$@\"", "bash"]

COPY target/lib /usr/share/qcon-demo/lib
COPY target/qcon-demo-0.0.1-SNAPSHOT.jar /usr/share/qcon-demo/qcon-demo.jar
