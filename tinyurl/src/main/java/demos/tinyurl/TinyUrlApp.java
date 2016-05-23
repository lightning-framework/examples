package demos.tinyurl;

import lightning.Lightning;
import lightning.inject.InjectorModule;
import lightning.util.Flags;

/**
 * Usage: java TinyUrlApp --config /path/to/config.json
 */
public class TinyUrlApp {
  public static void main(String[] args) throws Exception {
    Flags.parse(args);

    InjectorModule injector = new InjectorModule();
    injector.bindAnnotationToInstance(CASHost.class, "netid.rice.edu");
    injector.bindAnnotationToInstance(CASPath.class, "/cas");
    injector.bindAnnotationToInstance(CASDomain.class, "@rice.edu");
    Lightning.launch(Flags.getFile("config"), injector);
  }
}
