package server.library;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores a master list of songs that can be played
 *
 * Implemented using singleton patter so there will only be one master list of
 * songs.
 *
 * @author Jackson Higgins
 */
public class SongLibrary {
  private static SongLibrary instance;
  private static ArrayList<Song> songs;

  private SongLibrary() {
    this.songs = new ArrayList<>();
  }

  /**
   * @return song library instance
   */
  public static SongLibrary getInstance() {
    if (instance == null) {
      instance = new SongLibrary();
    }
    return instance;
  }

  /**
   * Add a song to the list of available songs
   */
  public void addSong(Song song) {
    songs.add(song);
  }

  /**
   * Lookup a song based on title
   *
   * @param title the name of the song being looked up
   *
   * @return song object that matches the title
   * @throws Exception if the title is not found
   */
  public Song findByTitle(String title) throws Exception {
    for (Song song : songs) {
      if (song.getName().equals(title)) {
        return song;
      }
    }
    throw new Exception("Unknown Song");
  }

  /**
   * @return the list of all songs
   */
  public List<Song> getAllSongs() {
    return songs;
  }
}
