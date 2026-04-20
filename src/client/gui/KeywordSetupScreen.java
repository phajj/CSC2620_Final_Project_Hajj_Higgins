package client.gui;

import client.network.ServerConnection;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class KeywordSetupScreen {

	private JFrame frame;
	private JTextField keywordField;
	private JLabel messageLabel;
	private String keyword = "";
	private ServerConnection serverConn;
	private String username;

	private static final Pattern KEYWORD_PATTERN = Pattern.compile("^[A-Za-z0-9 ]{1,32}$");

	public KeywordSetupScreen() {
		this(new ServerConnection("localhost", 5555), null);
	}

	public KeywordSetupScreen(ServerConnection conn) {
		this(conn, null);
	}

	public KeywordSetupScreen(ServerConnection conn, String username) {
		this.serverConn = conn;
		this.username = username;
		initUI();
	}

	private void initUI() {
		frame = new JFrame("Keyword Setup");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(400, 160);
		frame.setLocationRelativeTo(null);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 8, 6, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;
		if (username != null) {
			gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
			JLabel userLabel = new JLabel("User: " + username);
			panel.add(userLabel, gbc);
			row++;
		}

		gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
		panel.add(new JLabel("Keyword:"), gbc);
		gbc.gridx = 1;
		keywordField = new JTextField(20);
		panel.add(keywordField, gbc);
		row++;

		gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
		messageLabel = new JLabel(" ");
		messageLabel.setForeground(Color.RED);
		panel.add(messageLabel, gbc);
		row++;

		JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveBtn = new JButton("Save");
		JButton backBtn = new JButton("Back");
		buttonRow.add(backBtn);
		buttonRow.add(saveBtn);

		gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
		panel.add(buttonRow, gbc);

		saveBtn.addActionListener(e -> onSave());
		backBtn.addActionListener(e -> {
			frame.dispose();
			new LoginScreen().display();
		});

		if (username != null) {
			JLabel userLabel = new JLabel("User: " + username);
			gbc.gridx = 0; gbc.gridy = -1; // add above (GridBag will normalize)
			panel.add(userLabel, gbc);
		}

		frame.getContentPane().add(panel);
	}

	public void display() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	private void onSave() {
		final String kw = keywordField.getText().trim();
		if (!KEYWORD_PATTERN.matcher(kw).matches()) {
			messageLabel.setText("Keyword must be 1-32 chars (letters, digits, spaces)");
			return;
		}

		messageLabel.setText("Saving...");

		new Thread(() -> {
			boolean connected = (serverConn != null) && serverConn.connect();
			if (connected) {
				String resp = serverConn.sendCommand("SET_KEYWORD " + kw);
				if (resp != null && (resp.equalsIgnoreCase("OK") || resp.toUpperCase().startsWith("OK") || resp.toUpperCase().startsWith("SUCCESS"))) {
					this.keyword = kw;
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Keyword saved on server.", "Saved", JOptionPane.INFORMATION_MESSAGE));
				} else {
					final String err = (resp == null) ? "No response from server" : resp;
					SwingUtilities.invokeLater(() -> messageLabel.setText("Save failed: " + err));
				}
			} else {
				this.keyword = kw;
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Saved locally (server unavailable).", "Saved", JOptionPane.INFORMATION_MESSAGE));
			}

			SwingUtilities.invokeLater(() -> messageLabel.setText(" "));
		}).start();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
		SwingUtilities.invokeLater(() -> keywordField.setText(keyword));
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new KeywordSetupScreen().display());
	}
}
