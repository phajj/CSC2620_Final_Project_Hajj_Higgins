package server.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Sends an audio file to the client over the socket.
 * Protocol: writes "FILE_SIZE:<n>\n" then n raw bytes.
 */
public class FileSender extends Thread {
  private final Socket socket;
  private final String fileName;

  public FileSender(Socket socket, String fileName) {
    this.socket = socket;
    this.fileName = fileName;
  }

  public void sendFile(String fileName) {
    File file = new File(fileName);
    if (!file.exists() || !file.isFile()) {
      try {
        OutputStream out = socket.getOutputStream();
        out.write(("ERROR: file not found\n").getBytes("UTF-8"));
        out.flush();
      } catch (IOException ignored) {
      }
      return;
    }

    try {
      OutputStream out = socket.getOutputStream();

      // Send the size header so the client knows how many bytes to read
      String header = "FILE_SIZE:" + file.length() + "\n";
      out.write(header.getBytes("UTF-8"));

      // Stream the raw audio bytes
      try (FileInputStream fis = new FileInputStream(file)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
      out.flush();
    } catch (IOException e) {
      // Client disconnected or socket closed during transfer
    }
  }

  @Override
  public void run() {
    sendFile(fileName);
  }
}
