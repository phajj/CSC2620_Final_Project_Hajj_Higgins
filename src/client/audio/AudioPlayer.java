package client.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;

public class AudioPlayer {

    private Clip clip;

    public AudioPlayer() {
    }

    public void play(byte[] audioData) {
        stop(); // stop any current playback before starting a new one
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new ByteArrayInputStream(audioData));
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            // Unsupported format or audio system unavailable
        }
    }

    public void stop() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
            clip = null;
        }
    }
}
