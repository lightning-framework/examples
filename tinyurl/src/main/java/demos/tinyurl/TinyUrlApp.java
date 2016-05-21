package demos.tinyurl;

import lightning.Lightning;
import lightning.util.Flags;

/**
 * Usage: java TinyUrlApp --config /path/to/config.json
 */
public class TinyUrlApp {
  public static void main(String[] args) throws Exception {
    Flags.parse(args);
    Lightning.launch(Flags.getFile("config"));
  }
}
