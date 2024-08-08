package de.voelkel.tomcat_test;

import java.io.IOException;
import java.net.ServerSocket;

public class PortChecker {

  static int startPort = 9090;
  static int maxPort = 50000;
  static int currentPort = startPort;
  static int maxRetries = 3;

  public static int getFreePort() {
    if (maxRetries == 0) {
      throw new RuntimeException("No available port found");
    }
    int availablePort = findAvailablePort(currentPort);
    if (availablePort > maxPort) {
      currentPort = startPort;
      maxRetries = maxRetries - 1;
      return getFreePort();
    }
    currentPort = availablePort + 1;
    return availablePort;
  }

  public static int findAvailablePort(int startPort) {
    int port = startPort;
    while (!isPortAvailable(port)) {
      port++;
    }
    return port;
  }

  public static boolean isPortAvailable(int port) {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      return true;
    } catch (IOException e) {
      // The port is not available
      return false;
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          // Failed to close the socket
          e.printStackTrace();
        }
      }
    }
  }
}
