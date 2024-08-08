package de.voelkel.tomcat_test;

import java.util.Arrays;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.JreMemoryLeakPreventionListener;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.catalina.core.ThreadLocalLeakPreventionListener;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsSci;
import org.glassfish.jersey.servlet.ServletContainer;


public class TomcatUtil {
  public Tomcat tomcat = null;

  public Server start(int port,
      int vpnConnPort, int modemToWebapi1Port, int modemToWebapi2Port, int websocketPort, int ajpPort,
      String contextPath, boolean waitForJoin )
      throws LifecycleException {

    tomcat = new Tomcat();

    // Create and configure executors as per server.xml
    configureExecutors(tomcat);

    // Create and configure connectors as per server.xml
    configureConnectors(tomcat, port, vpnConnPort, modemToWebapi1Port, modemToWebapi2Port, websocketPort, ajpPort);

    addListeners(tomcat);

    // Create a context for the web application
    Context ctx = tomcat.addContext(contextPath, null);

    WsSci wsSci = new WsSci();
    ctx.addServletContainerInitializer(wsSci, null);

    // Add and configure the Jersey Servlet
    addJerseyServlet(ctx);

    // Start Tomcat
    tomcat.start();

    if (waitForJoin) {
      tomcat.getServer().await();
    }

    return tomcat.getServer();
  }

  public static final String STANDARD = "standard";
  public static final String VPN_CONN = "VpnConn";
  public static final String MODEM_TO_WEBAPI = "modemToWebapi";
  public static final String WEBSOCKET = "websocket";
  private void configureConnectors(Tomcat tomcat, int port, int vpnConnPort, int modemToWebapi1Port, int modemToWebapi2Port, int websocketPort, int ajpPort) {
    configureExecutors(tomcat);

    Connector webapi = createConnector(port, 25000);
    webapi.setAsyncTimeout(31000);
    webapi.setProperty("soTimeout", "50000");
    webapi.getProtocolHandler().setExecutor(tomcat.getService().getExecutor(STANDARD));
    tomcat.getService().addConnector(webapi);

    Connector vpnConn = createConnector(vpnConnPort, 120000);
    vpnConn.getProtocolHandler().setExecutor(tomcat.getService().getExecutor(VPN_CONN));
    tomcat.getService().addConnector(vpnConn);

    Connector modemToWebapi1 = createConnector(modemToWebapi1Port, 120000);
    modemToWebapi1.getProtocolHandler().setExecutor(tomcat.getService().getExecutor(MODEM_TO_WEBAPI));
    modemToWebapi1.setProperty("compression", "force");
    modemToWebapi1.setProperty("compressionMinSize", "100");
    tomcat.getService().addConnector(modemToWebapi1);

    Connector modemToWebapi2 = createAjpConnector(modemToWebapi2Port, 120000);
    modemToWebapi2.getProtocolHandler().setExecutor(tomcat.getService().getExecutor(MODEM_TO_WEBAPI));
    modemToWebapi2.setProperty("compression", "force");
    modemToWebapi2.setProperty("compressionMinSize", "100");
    tomcat.getService().addConnector(modemToWebapi2);

    Connector websocket = createConnector(websocketPort, 120000, "org.apache.coyote.http11.Http11NioProtocol");
    websocket.getProtocolHandler().setExecutor(tomcat.getService().getExecutor(MODEM_TO_WEBAPI));
    tomcat.getService().addConnector(websocket);

    Connector ajp = createAjpConnector(ajpPort);
    tomcat.getService().addConnector(ajp);
  }
  private void configureExecutors(Tomcat tomcat) {
    StandardThreadExecutor blub = new StandardThreadExecutor();
    blub.setName(STANDARD);
    blub.setNamePrefix("standard-");
    blub.setMaxThreads(1000);
    blub.setMinSpareThreads(25);
    tomcat.getService().addExecutor(blub);

    StandardThreadExecutor vpnConn = new StandardThreadExecutor();
    vpnConn.setName(VPN_CONN);
    vpnConn.setNamePrefix("vpnconn-");
    vpnConn.setMaxThreads(1000);
    vpnConn.setMinSpareThreads(25);
    tomcat.getService().addExecutor(vpnConn);

    StandardThreadExecutor modemToWebapi = new StandardThreadExecutor();
    modemToWebapi.setName(MODEM_TO_WEBAPI);
    modemToWebapi.setNamePrefix("modem-");
    modemToWebapi.setMaxThreads(1000);
    modemToWebapi.setMinSpareThreads(25);
    tomcat.getService().addExecutor(modemToWebapi);

    StandardThreadExecutor websocket = new StandardThreadExecutor();
    websocket.setName(WEBSOCKET);
    websocket.setNamePrefix("websocket-");
    websocket.setMaxThreads(100);
    websocket.setMinSpareThreads(25);
    tomcat.getService().addExecutor(websocket);
  }

  private Connector createConnector(int port,  int timeout) {
    return createConnector(port, "HTTP/1.1", timeout, 8143, 20485760);
  }

  private Connector createConnector(int port,  int timeout, String protocol) {
    return createConnector(port, protocol, timeout, 8143, 20485760);
  }

  private Connector createAjpConnector(int port) {
    return createAjpConnector(port, -1, false, null);
  }
  private Connector createAjpConnector(int port, int timeout) {
    return createAjpConnector(port, timeout, false, null);
  }

  private Connector createAjpConnector(int port, int timeout, boolean requireSecret, String secret) {
    Connector connector = createConnector(port, "AJP/1.3", timeout, 8143, 20485760);
    connector.setProperty("secretRequired", String.valueOf(requireSecret));
    if (requireSecret && secret != null && !secret.isEmpty()) {
      connector.setProperty("secret", secret);
    }
    return connector;
  }

  private Connector createConnector(int port, String protocol, int timeout, int redirectPort,
      int maxPostSize) {
    Connector connector = new Connector(protocol);
    connector.setPort(port);
    if (timeout > 0) {
      connector.setProperty("connectionTimeout", String.valueOf(timeout));
    }
    if (redirectPort > 0) {
      connector.setProperty("redirectPort", String.valueOf(redirectPort));
    }
    if (maxPostSize > 0) {
      connector.setProperty("maxPostSize", String.valueOf(maxPostSize));
    }
    return connector;
  }

  private void addJerseyServlet(Context ctx) {
    Wrapper wrapper = Tomcat.addServlet(ctx, "JerseyServlet", ServletContainer.class.getName());

    wrapper.addInitParameter("jakarta.ws.rs.Application", JerseyServletInit.class.getName());

    // Optional: Set the package where the resources are located
    // wrapper.addInitParameter("jersey.config.server.provider.packages", "de.voelkel.vds.webapi");

    // Optional: Enable Jersey tracing
    wrapper.addInitParameter("jersey.config.server.tracing", "OFF");

    // Map servlet to a URL pattern
    wrapper.addMapping("/*");

    // Load on startup
    wrapper.setLoadOnStartup(1);

    // Enable async support for chunked responses (ChunkedOutput)
    wrapper.setAsyncSupported(true);
  }

  private void addListeners(Tomcat tomcat) {
    tomcat.getServer().addLifecycleListener(new JreMemoryLeakPreventionListener());
    tomcat.getServer().addLifecycleListener(new ThreadLocalLeakPreventionListener());
  }

  public void stop() throws Exception {
    if (tomcat == null) {
      return;
    }
    if (tomcat.getServer() == null) {
      return;
    }
    Server server = tomcat.getServer();
    stop(server);

    Arrays.stream(server.findServices()).forEach(service -> {
      try {
        service.stop();
        Arrays.stream(service.findConnectors()).forEach(connector -> {
          try {
            connector.stop();
          } catch (LifecycleException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (LifecycleException e) {
        throw new RuntimeException(e);
      }
    });
    try {
      server.destroy();
    } catch (LifecycleException e) {
      throw new RuntimeException(e);
    }
  }
  public void stop(Server server) throws Exception {
    server.stop();
    server.await();
  }

}
