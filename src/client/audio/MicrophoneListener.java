package client.audio;

// Speech-to-text model,Vosk
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * This class handles microphone input using the Vosk library.
 * @author Peter Hajj
 */
public class MicrophoneListener {

    public interface CommandListener {
        void onCommandCaptured(String command);
    }

    private static final float SAMPLE_RATE = 16000f;
    private static final int BUFFER_SIZE = 4096;

    private final String modelPath;
    private final KeywordDetector keywordDetector;
    private CommandListener commandListener;
    private volatile boolean running;
    private volatile boolean recordingMode;
    private Thread listenerThread;
    private final StringBuilder commandBuffer = new StringBuilder();

    public MicrophoneListener(String modelPath, KeywordDetector keywordDetector) {
        this.modelPath = modelPath;
        this.keywordDetector = keywordDetector;
        if (keywordDetector != null) {
            keywordDetector.setRecordingListener(() -> {
                recordingMode = true;
                commandBuffer.setLength(0);
                System.out.println("\n[Keyword detected] Recording command...");
            });
        }
    }

    public MicrophoneListener(String modelPath) {
        this(modelPath, null);
    }

    public MicrophoneListener() {
        this("model", null);
    }

    public void setCommandListener(CommandListener listener) {
        this.commandListener = listener;
    }

    /** 
     * Starts the microphone listener on a background daemon thread. 
     */
    public void startListening() {
        if (running) return;
        running = true;
        listenerThread = new Thread(this::listen, "MicrophoneListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /** 
     * Signals the listener thread to stop and interrupts it if it is blocked on I/O. 
     */
    public void stopListening() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    /**
     * Captures audio from the default microphone and feeds it to the Vosk recognizer.
     * Runs until running is false or the thread is interrupted.
     */
    private void listen() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Microphone not supported on this system.");
            return;
        }

        try (Model model = new Model(modelPath);
             Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
             TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {

            microphone.open(format);
            microphone.start();
            System.out.println("Listening... (speak into the microphone)");

            byte[] buffer = new byte[BUFFER_SIZE];
            while (running && !Thread.currentThread().isInterrupted()) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String transcript = extractText(recognizer.getResult());
                        if (!transcript.isEmpty()) {
                            handleTranscript(transcript);
                        }
                    } else if (!recordingMode) {
                        System.out.print("\r[Listening]  " + padRight(extractText(recognizer.getPartialResult()), 60));
                    }
                }
            }

            String finalResult = extractText(recognizer.getFinalResult());
            if (!finalResult.isEmpty()) {
                handleTranscript(finalResult);
            }
            System.out.println("\nStopped listening.");

        } catch (LineUnavailableException e) {
            System.err.println("Microphone unavailable: " + e.getMessage());
        } catch (IOException e) {
            // Prompt to Vosk download page
            System.err.println("Error loading Vosk model from '" + modelPath + "': " + e.getMessage());
            System.err.println("Download a model from https://alphacephei.com/vosk/models and unzip it to the 'model/' directory.");
        }
    }

    /**
     * Routes a completed transcript: checks for the keyword when idle, or captures
     * it as a command when already in recording mode.
     */
    private void handleTranscript(String transcript) {
        if (recordingMode) {
            if (commandBuffer.length() > 0) commandBuffer.append(' ');
            commandBuffer.append(transcript);
            System.out.println("\n[Command]    " + commandBuffer);
            if (commandListener != null) commandListener.onCommandCaptured(commandBuffer.toString());
        } else {
            System.out.println("\n[Transcript] " + transcript);
            if (keywordDetector != null) keywordDetector.detect(transcript);
        }
    }

    /**
     * Extracts the transcript string from Vosk's JSON output.
     *
     * @param json the raw JSON string returned by the Vosk recognizer
     * @return the transcript value, or an empty string if not found
     */
    private String extractText(String json) {
        for (String key : new String[]{"\"text\"", "\"partial\""}) {
            int keyIdx = json.indexOf(key);
            if (keyIdx >= 0) {
                int colon = json.indexOf(':', keyIdx + key.length());
                int start = json.indexOf('"', colon + 1) + 1;
                int end = json.indexOf('"', start);
                if (start > 0 && end > start) {
                    return json.substring(start, end);
                }
            }
        }
        return "";
    }

    /** 
     * Left-justifies to keep the console line stable.
     */
    private String padRight(String s, int width) {
        return String.format("%-" + width + "s", s);
    }
}