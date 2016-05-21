package demo.controllers;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lightning.ann.*;
import static lightning.enums.HTTPMethod.*;
import static lightning.server.Context.*;

/**
 * A simple controller which renders the home.ftl view setting the view variabke "endpoint" to the
 * URL of the websocket.
 */
@Controller
public class ChatAppController {
  @Route(path="/", methods={GET})
  @Template("home.ftl")
  public Map<String, ?> handleIndex() throws Exception {
    return ImmutableMap.of("endpoint", url().to("/socket").replace("http://", "ws://"));
  }
}
