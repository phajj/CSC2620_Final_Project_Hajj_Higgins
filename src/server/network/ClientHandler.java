package server.network;

import server.auth.UserStore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final UserStore store;

    private BufferedReader in;
    private BufferedWriter out;
    private String currentUser = null;

    public ClientHandler(Socket socket, UserStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String resp = handleCommand(line);
                if (resp == null) resp = "ERROR: internal";
                out.write(resp);
                out.write("\n");
                out.flush();
            }
        } catch (IOException e) {
            // client disconnected or IO error
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String handleCommand(String command) {
        if (command == null) return "ERROR: empty";
        String trimmed = command.trim();
        if (trimmed.isEmpty()) return "ERROR: empty";

        int firstSpace = trimmed.indexOf(' ');
        String cmd = (firstSpace == -1) ? trimmed.toUpperCase() : trimmed.substring(0, firstSpace).toUpperCase();
        String args = (firstSpace == -1) ? "" : trimmed.substring(firstSpace + 1);

        switch (cmd) {
            case "LOGIN":
                if (args.isEmpty()) return "ERROR: LOGIN requires username and password";
                int sp = args.indexOf(' ');
                if (sp == -1) return "ERROR: LOGIN requires username and password";
                String user = args.substring(0, sp);
                String pass = args.substring(sp + 1);
                boolean ok = store.authenticateOrCreate(user, pass);
                if (ok) {
                    currentUser = user;
                    return "OK";
                } else {
                    return "ERROR: invalid credentials";
                }
            case "SET_KEYWORD":
                if (args.isEmpty()) return "ERROR: SET_KEYWORD requires keyword";
                if (currentUser == null) return "ERROR: not authenticated";
                String kw = args;
                boolean saved = store.setKeyword(currentUser, kw);
                return saved ? "OK" : "ERROR: could not save keyword";
            case "GET_KEYWORD":
                if (currentUser == null) return "ERROR: not authenticated";
                String k = store.getKeyword(currentUser);
                return k == null ? "NONE" : (k.isEmpty() ? "NONE" : ("KEYWORD " + k));
            case "PING":
                return "PONG";
            case "CHANGE_PASSWORD":
                if (currentUser == null) return "ERROR: not authenticated";
                if (args.isEmpty()) return "ERROR: CHANGE_PASSWORD requires old and new password";
                int sp2 = args.indexOf(' ');
                if (sp2 == -1) return "ERROR: CHANGE_PASSWORD requires old and new password";
                String oldp = args.substring(0, sp2);
                String newp = args.substring(sp2 + 1);
                boolean changed = store.changePassword(currentUser, oldp, newp);
                return changed ? "OK" : "ERROR: password not changed";
            case "DELETE_ACCOUNT":
                if (currentUser == null) return "ERROR: not authenticated";
                boolean removed = store.deleteUser(currentUser);
                if (removed) {
                    currentUser = null;
                    return "OK";
                } else {
                    return "ERROR: could not delete account";
                }
            case "LOGOUT":
                currentUser = null;
                return "OK";
            default:
                return "ERROR: unknown command";
        }
    }
}
