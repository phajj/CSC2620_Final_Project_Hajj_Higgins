package server.library;

import java.io.File;

/**
 * This class is an object representation of a song
 *
 * @author Jackson Higgins
 */
public class Song {
  private String name;
  private File song;

  public Song(String name, File song) {
    this.name = name;
    this.song = song;
  }

  /**
   * @return name of the song
   */
  public String getName() {
    return name;
  }

  /**
   * @return the song file
   */
  public File getSong() {
    return song;
  }
}
