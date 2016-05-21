package demos.fileshare;

import lightning.Lightning;
import lightning.util.Flags;

/**
 * Usage: java FileShareApp --config /path/to/config.json
 */
public class FileShareApp {
  public static void main(String[] args) throws Exception {
    Flags.parse(args);
    Lightning.launch(Flags.getFile("config"));
  }
}
