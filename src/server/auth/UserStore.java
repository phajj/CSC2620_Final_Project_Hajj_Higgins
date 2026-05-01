package server.auth;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class UserStore {

    private final Map<String, String> passwordMap = new HashMap<>(); // username -> hashed password
    private final Map<String, String> keywordMap = new HashMap<>();  // username -> keyword

    private final File storageFile;

    public UserStore() {
        this(new File("userstore.txt"));
    }

    public UserStore(File storageFile) {
        this.storageFile = storageFile;
        loadFromFile();
        if (passwordMap.isEmpty()) {
            // seed a test user with hashed password
            String hashed = PasswordUtil.hashPassword("alice123");
            passwordMap.put("alice", hashed);
            keywordMap.put("alice", "hello");
            persist();
        }
    }

    private synchronized void loadFromFile() {
        if (!storageFile.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // format: username\thashedPassword\tkeyword
                String[] parts = line.split("\t", 3);
                String user = parts[0];
                String hashed = parts.length > 1 ? parts[1] : "";
                String kw = parts.length > 2 ? unescape(parts[2]) : "";
                passwordMap.put(user, hashed);
                keywordMap.put(user, kw);
            }
        } catch (IOException e) {
            // ignore and start fresh
        }
    }

    private synchronized void persist() {
        try {
            Path tmp = Files.createTempFile("userstore", ".tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
                for (String user : passwordMap.keySet()) {
                    String hashed = passwordMap.get(user);
                    String kw = keywordMap.getOrDefault(user, "");
                    w.write(user + "\t" + hashed + "\t" + escape(kw));
                    w.write("\n");
                }
            }
            Files.move(tmp, storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("UserStore: persisted " + passwordMap.size() + " users to " + storageFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("UserStore: failed to persist userstore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
    }

    /**
     * Authenticate an existing user. Returns true only if the username exists and the password matches.
     */
    public synchronized boolean authenticateExistingUser(String username, String password) {
        if (!passwordMap.containsKey(username)) return false;
        String stored = passwordMap.get(username);
        return PasswordUtil.verifyPassword(password, stored);
    }

    /**
     * Create a new user with the given password. Returns true when the user was created, false if the user already exists.
     */
    public synchronized boolean createUser(String username, String password) {
        if (passwordMap.containsKey(username)) return false;
        String hashed = PasswordUtil.hashPassword(password);
        passwordMap.put(username, hashed);
        keywordMap.put(username, "");
        persist();
        return true;
    }

    /**
     * Register a new user only if the username does not already exist.
     * Returns true when a new user was created, false if the user already exists.
     */
    public synchronized boolean register(String username, String password) {
        return createUser(username, password);
    }

    public synchronized boolean setKeyword(String username, String keyword) {
        if (!passwordMap.containsKey(username)) return false;
        keywordMap.put(username, keyword);
        persist();
        return true;
    }

    public synchronized String getKeyword(String username) {
        return keywordMap.get(username);
    }

    public synchronized boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!passwordMap.containsKey(username)) return false;
        String stored = passwordMap.get(username);
        if (!PasswordUtil.verifyPassword(oldPassword, stored)) return false;
        String newHashed = PasswordUtil.hashPassword(newPassword);
        passwordMap.put(username, newHashed);
        persist();
        return true;
    }

    public synchronized boolean deleteUser(String username) {
        if (!passwordMap.containsKey(username)) return false;
        passwordMap.remove(username);
        keywordMap.remove(username);
        persist();
        return true;
    }
}

