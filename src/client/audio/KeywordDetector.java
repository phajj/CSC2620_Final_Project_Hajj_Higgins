package client.audio;

import shared.UserCredentials;

/**
 * This class is resposible for detecting the keyword that the user imported (stored in src/shared/UserCredentials) within the text imported from MicrophoneListener.
 * @author Peter Hajj
 */
public class KeywordDetector {

    public interface RecordingListener {
        void onRecordingTriggered();
    }

    private final String keyword;
    private RecordingListener recordingListener;
    private volatile boolean recordingMode;

    public KeywordDetector(UserCredentials credentials) {
        this.keyword = credentials.getKeyword().toLowerCase();
    }

    public KeywordDetector(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    public void setRecordingListener(RecordingListener listener) {
        this.recordingListener = listener;
    }

    /**
     * Splits the transcript into words and checks each against the activation keyword.
     * Enters recording mode and starts the listener on the first match.
     *
     * @return true if the keyword was found
     */
    public boolean detect(String transcript) {
        if (transcript == null || transcript.isBlank()) 
            return false;
        for (String word : transcript.toLowerCase().split("\\s+")) {
            if (word.equals(keyword)) {
                recordingMode = true;
                if (recordingListener != null) {
                    recordingListener.onRecordingTriggered();
                }
                return true;
            }
        }
        return false;
    }

    public boolean isRecordingMode() {
        return recordingMode;
    }

    public void resetRecordingMode() {
        recordingMode = false;
    }
}
