package de.voelkel.tomcat_test;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyServletInit extends ResourceConfig {

  public JerseyServletInit() {
    super();
    register(SomeController.class);
  }
}
