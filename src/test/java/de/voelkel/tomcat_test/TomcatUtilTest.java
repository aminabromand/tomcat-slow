package de.voelkel.tomcat_test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TomcatUtilTest {

  TomcatUtil tomcatUtil = new TomcatUtil();
  int port;

  @BeforeEach
  public void setUp() throws LifecycleException {
    System.out.println("############# setUp");
    port = PortChecker.getFreePort();
    Server server = tomcatUtil.start(
        port,
        PortChecker.getFreePort(),
        PortChecker.getFreePort(),
        PortChecker.getFreePort(),
        PortChecker.getFreePort(),
        PortChecker.getFreePort(),
        "/mycontext",
        false
    );
  }

  @AfterEach
  public void tearDown() throws Exception {
    tomcatUtil.stop();
    System.out.println("############# tearDown");
  }

  @Test
  public void startOnce() {
    System.out.println("Tomcat started");
    JerseyClient client = JerseyClientBuilder.createClient();
    String response = client.target("http://localhost:" + port + "/mycontext/some").request().get(String.class);
    System.out.println("Response: " + response);
    assertThat(response).isEqualTo("Hello World");
  }

  @ParameterizedTest
  @MethodSource("provideParameters")
  public void start(Integer number) {
    System.out.println("Tomcat started: " + number);
    JerseyClient client = JerseyClientBuilder.createClient();
    String response = client.target("http://localhost:" + port + "/mycontext/some").request().get(String.class);
    System.out.println("Response: " + response);
    assertThat(response).isEqualTo("Hello World");
  }

  private static Stream<Arguments> provideParameters() {
    List<Arguments> arguments = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      arguments.add(Arguments.of(i));
    }

    return arguments.stream();
  }
}
