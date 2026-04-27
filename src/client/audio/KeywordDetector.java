package client.audio;

import client.network.ServerConnection;

public class KeywordDetector {
    private volatile String keyword = null;
    private final ServerConnection serverConn;

    public KeywordDetector() {
        this(null);
    }

    public KeywordDetector(ServerConnection conn) {
        this.serverConn = conn;
    }

    public boolean containsKeyword(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String kw = keyword;
        if ((kw == null || kw.isEmpty()) && serverConn != null) {
            String resp = serverConn.sendCommand("GET_KEYWORD");
            if (resp != null && resp.startsWith("KEYWORD ")) {
                kw = resp.substring("KEYWORD ".length());
                setKeyword(kw);
            }
        }
        if (kw == null || kw.trim().isEmpty()) return false;
        return input.toLowerCase().contains(kw.toLowerCase());
    }

    public void setKeyword(String kw) {
        this.keyword = (kw == null) ? "" : kw;
    }

    public String getKeyword() {
        return keyword;
    }
}
