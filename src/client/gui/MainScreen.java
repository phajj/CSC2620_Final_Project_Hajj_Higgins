package client.gui;

import client.network.ServerConnection;

import javax.swing.*;
import java.awt.*;

public class MainScreen {

    private JFrame frame;
    private JLabel statusLabel;
    private final ServerConnection serverConn;
    private final String username;
    private String initialMessage;

    public MainScreen(ServerConnection serverConn, String username) {
        this(serverConn, username, null);
    }

    public MainScreen(ServerConnection serverConn, String username, String initialMessage) {
        this.serverConn = serverConn;
        this.username = username;
        this.initialMessage = initialMessage;
        initUI();
    }

    public MainScreen() {
        this(null, null);
    }

    private void initUI() {
        frame = new JFrame("Main Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 240);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(statusLabel, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Logout");
        if (username != null) {
            JLabel userLabel = new JLabel("Logged in as: " + username);
            userLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            top.add(userLabel);
        }
        JButton reconnectBtn = new JButton("Reconnect");
        reconnectBtn.addActionListener(e -> attemptReconnect());
        top.add(reconnectBtn);

        JButton changePwdBtn = new JButton("Change Password");
        changePwdBtn.addActionListener(e -> openChangePasswordDialog());
        top.add(changePwdBtn);

        JButton deleteBtn = new JButton("Delete Account");
        deleteBtn.addActionListener(e -> confirmAndDeleteAccount());
        top.add(deleteBtn);

        top.add(logoutBtn);
        root.add(top, BorderLayout.NORTH);

        logoutBtn.addActionListener(e -> performLogout());

        frame.getContentPane().add(root);
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            if (initialMessage != null && !initialMessage.isEmpty()) {
                JOptionPane.showMessageDialog(frame, initialMessage, "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    private void performLogout() {
        // Run logout/cleanup off the EDT
        new Thread(() -> {
            if (serverConn != null) {
                try {
                    serverConn.sendCommand("LOGOUT");
                } catch (Exception ignored) {
                }
                serverConn.disconnect();
            }

            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                new LoginScreen().display();
            });
        }).start();
    }

    private void attemptReconnect() {
        updateStatus("Reconnecting...");
        new Thread(() -> {
            boolean ok = (serverConn != null) && serverConn.connect();
            if (ok) updateStatus("Connected"); else updateStatus("Disconnected");
        }).start();
    }

    private void openChangePasswordDialog() {
        if (username == null || serverConn == null) {
            JOptionPane.showMessageDialog(frame, "No user/connection available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPasswordField oldField = new JPasswordField();
        JPasswordField newField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        Object[] inputs = {
                "Current password:", oldField,
                "New password:", newField,
                "Confirm new password:", confirmField
        };
        int result = JOptionPane.showConfirmDialog(frame, inputs, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;
        String oldp = new String(oldField.getPassword());
        String newp = new String(newField.getPassword());
        String conf = new String(confirmField.getPassword());
        if (!newp.equals(conf)) {
            JOptionPane.showMessageDialog(frame, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newp.length() < 6) {
            JOptionPane.showMessageDialog(frame, "New password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateStatus("Changing password...");
        new Thread(() -> {
            String cmd = "CHANGE_PASSWORD " + oldp + " " + newp;
            String resp = serverConn.sendCommand(cmd);
            if (resp != null && resp.toUpperCase().startsWith("OK")) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE));
            } else {
                final String err = (resp == null) ? "No response from server" : resp;
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Change failed: " + err, "Error", JOptionPane.ERROR_MESSAGE));
            }
            updateStatus("Ready");
        }).start();
    }

    private void confirmAndDeleteAccount() {
        if (username == null || serverConn == null) {
            JOptionPane.showMessageDialog(frame, "No user/connection available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(frame, "Delete account '" + username + "'? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        updateStatus("Deleting account...");
        new Thread(() -> {
            String resp = serverConn.sendCommand("DELETE_ACCOUNT");
            if (resp != null && resp.toUpperCase().startsWith("OK")) {
                serverConn.disconnect();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Account deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new LoginScreen().display();
                });
            } else {
                final String err = (resp == null) ? "No response from server" : resp;
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Delete failed: " + err, "Error", JOptionPane.ERROR_MESSAGE));
            }
            updateStatus("Ready");
        }).start();
    }

   
}
