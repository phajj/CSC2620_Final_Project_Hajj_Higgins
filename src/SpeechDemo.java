

import client.audio.KeywordDetector;
import client.audio.MicrophoneListener;

/**
 * Quick terminal demo: prints live transcripts and shows the command buffer
 * accumulating after the wake keyword is spoken.
 *
 * Ctrl+C to stop.
 * @author Peter Hajj
 */
public class SpeechDemo {

    public static void main(String[] args) {
        String keyword = args.length > 0 ? args[0] : "jarvis";

        System.out.println("Keyword : \"" + keyword + "\"");
        System.out.println("Model   : model/");
        System.out.println("Press Ctrl+C to stop.\n");

        KeywordDetector detector = new KeywordDetector(keyword);
        MicrophoneListener mic = new MicrophoneListener("model", detector);

        mic.setCommandListener(command ->
            System.out.println("[COMMAND] " + command)
        );

        mic.startListening();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mic.stopListening();
            System.out.println("\nDemo stopped.");
        }));

        // Block main thread until Ctrl+C
        try { Thread.currentThread().join(); }
        catch (InterruptedException ignored) {}
    }
}
