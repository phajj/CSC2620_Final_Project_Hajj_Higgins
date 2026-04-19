package client.network;

public class ServerConnection {

    private String host;
    private int port;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        return false;
    }

    public void disconnect() {
    }

    public String sendCommand(String command) {
        return null;
    }
}
