package demo;

import java.util.concurrent.TimeUnit;

import lightning.Lightning;
import lightning.config.Config;
import lightning.inject.InjectorModule;

import com.google.common.collect.ImmutableList;

/**
 * Launches the application.
 */
public class ChatAppLauncher {
  public static void main(String[] args) throws Exception {
    // Build the config needed for lightning.
    Config config = new Config();
    config.enableDebugMode = true;
    config.autoReloadPrefixes = ImmutableList.of("demo.controllers");
    config.scanPrefixes = ImmutableList.of("demo.controllers");
    config.server.hmacKey = "ABCDEFG";
    config.server.templateFilesPath = "demo/templates";
    config.server.staticFilesPath = "demo/assets";
    config.server.websocketTimeoutMs = (int) TimeUnit.SECONDS.toMillis(600);

    // Build a RethinkDB connection pool.
    RethinkDBProvider pool = new RethinkDBProvider("localhost", 28015, "chatapp", null, null);

    // Configure dependency injection to make the RethinkDB connection pool injectable.
    InjectorModule injector = new InjectorModule();
    injector.bindClassToInstance(RethinkDBProvider.class, pool);

    // Launch the server.
    Lightning.launch(config, injector);
  }
}
