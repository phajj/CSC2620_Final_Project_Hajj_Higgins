package client.network;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerConnection {

  private final String host;
  private final int port;

  private Socket socket;
  private DataInputStream in;
  private BufferedWriter out;
  private volatile boolean connected = false;

  public ServerConnection(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public synchronized boolean connect() {
    if (connected && socket != null && socket.isConnected() && !socket.isClosed()) {
      return true;
    }
    try {
      socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), 2000);
      socket.setSoTimeout(5000);
      in = new DataInputStream(socket.getInputStream());
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      connected = true;
      return true;
    } catch (IOException e) {
      disconnect();
      return false;
    }
  }

  public synchronized void disconnect() {
    try {
      if (in != null)
        in.close();
    } catch (IOException ignored) {
    }
    try {
      if (out != null)
        out.close();
    } catch (IOException ignored) {
    }
    try {
      if (socket != null && !socket.isClosed())
        socket.close();
    } catch (IOException ignored) {
    }
    in = null;
    out = null;
    socket = null;
    connected = false;
  }

  public synchronized String sendCommand(String command) {
    if (!connect())
      return null;
    try {
      out.write(command);
      out.write("\n");
      out.flush();
      try {
        return in.readLine();
      } catch (SocketTimeoutException ste) {
        return null;
      }
    } catch (IOException e) {
      disconnect();
      return null;
    }
  }

  /**
   * Reads exact amount of bytes from the socket and returns them.
   */
  public synchronized byte[] receiveAudioData(long size) {
    if (in == null)
      return null;
    try {
      byte[] data = new byte[(int) size];
      in.readFully(data);
      return data;
    } catch (IOException e) {
      disconnect();
      return null;
    }
  }
}
