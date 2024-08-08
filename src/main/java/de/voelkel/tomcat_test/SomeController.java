package de.voelkel.tomcat_test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/some")
public class SomeController {

  @GET
  public String get() {
    return "Hello World";
  }
}
