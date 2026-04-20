package tests;

import client.network.ServerConnection;
import server.auth.UserStore;
import server.network.ClientHandler;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerIntegrationTests {

    public static void main(String[] args) throws Exception {
        int failed = 0;

        File tmp = File.createTempFile("userstore_integ", ".txt");
        tmp.deleteOnExit();

        // Start first server
        UserStore store = new UserStore(tmp);
        ServerSocket ss = new ServerSocket(0);
        ExecutorService exec = Executors.newCachedThreadPool();
        Thread acceptThread = new Thread(() -> {
            try {
                while (!ss.isClosed()) {
                    Socket client = ss.accept();
                    exec.submit(new ClientHandler(client, store));
                }
            } catch (IOException e) {
                // server closed
            }
        });
        acceptThread.start();
        int port = ss.getLocalPort();

        ServerConnection c1 = new ServerConnection("localhost", port);
        if (!c1.connect()) {
            System.err.println("Failed to connect to server");
            System.exit(2);
        }

        String r = c1.sendCommand("PING");
        if (!"PONG".equals(r)) {
            System.err.println("PING failed: " + r);
            failed++;
        }

        r = c1.sendCommand("LOGIN integUser old");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("LOGIN failed: " + r);
            failed++;
        }

        r = c1.sendCommand("SET_KEYWORD my integration keyword");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("SET_KEYWORD failed: " + r);
            failed++;
        }

        r = c1.sendCommand("GET_KEYWORD");
        if (r == null || !r.startsWith("KEYWORD ")) {
            System.err.println("GET_KEYWORD failed: " + r);
            failed++;
        }

        r = c1.sendCommand("CHANGE_PASSWORD old new");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("CHANGE_PASSWORD failed: " + r);
            failed++;
        }

        r = c1.sendCommand("LOGOUT");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("LOGOUT failed: " + r);
            failed++;
        }

        ServerConnection c2 = new ServerConnection("localhost", port);
        c2.connect();
        r = c2.sendCommand("LOGIN integUser old");
        if (r != null && r.toUpperCase().startsWith("OK")) {
            System.err.println("Old password should not authenticate after change");
            failed++;
        }

        ServerConnection c3 = new ServerConnection("localhost", port);
        c3.connect();
        r = c3.sendCommand("LOGIN integUser new");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("Login with new password failed: " + r);
            failed++;
        }

        r = c3.sendCommand("DELETE_ACCOUNT");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("DELETE_ACCOUNT failed: " + r);
            failed++;
        }

        r = c3.sendCommand("GET_KEYWORD");
        if (r == null || !r.toUpperCase().startsWith("ERROR")) {
            System.err.println("Expected error after delete; got: " + r);
            failed++;
        }

        // Shutdown first server
        ss.close();
        acceptThread.join(1000);
        exec.shutdownNow();

        // Start second server reading same userstore file
        UserStore store2 = new UserStore(tmp);
        ServerSocket ss2 = new ServerSocket(0);
        ExecutorService exec2 = Executors.newCachedThreadPool();
        Thread acceptThread2 = new Thread(() -> {
            try {
                while (!ss2.isClosed()) {
                    Socket client = ss2.accept();
                    exec2.submit(new ClientHandler(client, store2));
                }
            } catch (IOException e) {
                // closed
            }
        });
        acceptThread2.start();
        int port2 = ss2.getLocalPort();

        ServerConnection c4 = new ServerConnection("localhost", port2);
        c4.connect();
        r = c4.sendCommand("LOGIN integUser new");
        if (r == null || !r.toUpperCase().startsWith("OK")) {
            System.err.println("Login after restart failed: " + r);
            failed++;
        }

        r = c4.sendCommand("GET_KEYWORD");
        if (r == null || !(r.equals("NONE") || r.toUpperCase().startsWith("NONE"))) {
            System.err.println("Expected NONE for keyword after recreate; got: " + r);
            failed++;
        }

        ss2.close();
        acceptThread2.join(1000);
        exec2.shutdownNow();

        if (failed == 0) {
            System.out.println("ALL INTEGRATION TESTS PASSED");
            System.exit(0);
        } else {
            System.err.println(failed + " TEST(S) FAILED");
            System.exit(2);
        }
    }
}
