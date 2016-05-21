package demo.controllers;

import static com.rethinkdb.RethinkDB.r;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lightning.ann.WebSocketFactory;
import lightning.json.JsonFactory;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import demo.RethinkDBProvider;
import demo.messages.ChatMessage;

/**
 * A web socket is a singleton class which handles incoming requests statelessly through
 * event handlers.
 */
@WebSocket
public class ChatClientWebSocket {
  private final static Logger logger = LoggerFactory.getLogger(ChatClientWebSocket.class);
  private static ChatClientWebSocket socket;
  
  // Installs a web socket factory on the path "/socket".
  // This factory method is injectable - see the documentation for @WebSocketFactory.
  // Notice that we inject the RethinkDB provider we created in the launcher.
  @WebSocketFactory(path = "/socket")
  public static ChatClientWebSocket produce(RethinkDBProvider provider) {
    // Return the same instance every time (singleton web socket) rather than allocating
    // a new object for each connection.
    if (socket == null) {
      socket = new ChatClientWebSocket(provider);
    }
    
    return socket;
  }
  
  // ----------------------------------------------------------
  
  private final RethinkDBProvider dbp;
  private final Thread changeListener;
  private final ConcurrentHashSet<Session> sessions;

  public ChatClientWebSocket(RethinkDBProvider dbp) {
    logger.info("Websocket Created.");
    this.dbp = dbp;
    this.sessions = new ConcurrentHashSet<>();
    ReentrantLock lock = new ReentrantLock();
    Condition cond = lock.newCondition();
    
    // Create a thread that will subscribe to the RethinkDB change feed
    // for the messages table and, whenever a new row is inserted, will
    // push the changes to clients by writing to each client's socket.
    this.changeListener = new Thread() {
      @Override
      public void run() {
        boolean didInit = false;
        
        // A loop to keep attempting to reconnect when connection is lost.
        while (true) {
          logger.debug("Listening for data changes...");
          try (Connection conn = dbp.getConnection()) {
            Cursor<HashMap<String, ?>> cursor = null;
            try {
              cursor = r.table("messages").changes().run(conn);
              
              // Signal that we have subscribed to the change feed.
              if (!didInit) {
                didInit = true;
                lock.lock();
                cond.signal();
                lock.unlock();
              }
              
              // Access the cursor like an infinite iterator.
              // Blocks the thread until new information becomes available.
              // Pretty cool!
              // TODO: Update when RethinkDB adds async connections to the Java driver.
              // TODO: Messages can still be lost in between when a rethink connection goes down and a rethink
              // connection comes back up since, currently, rethink change feeds to not backfill those missed
              // messages on reconnect. See https://github.com/rethinkdb/rethinkdb/issues/3471
              for (HashMap<String, ?> item : cursor) {
                logger.debug("Channel: delivering message notification of {} to {}", item, sessions.size());
                // Every time a new message comes in, broadcast to all connected sockets.
                for (Session session : sessions) {
                  try {
                    // TODO: Probably should use async IO here.
                    session.getRemote().sendString(JsonFactory.newJsonParser().toJson(ImmutableList.of(item.get("new_val"))));
                  } catch (Exception e) {
                    logger.warn("Failed to write message.", e);
                  }
                }
              }
            } catch (Exception e) {
              logger.warn("Connection lost." , e);
              continue;
            } finally {
              if (cursor != null) {
                cursor.close();
              }
            }
          }
          
          // Sleep 500ms between reconnection attempts.
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {}
        }
      }
    };
    this.changeListener.start();
    
    try { // Make sure the event listener thread is ready before servicing any clients.
      lock.lock();
      cond.await();
      lock.unlock();
    } catch (InterruptedException e) {}
  }
  
  @OnWebSocketConnect
  public void connected(final Session session) throws IOException {
    sessions.add(session);
    logger.info("Connected: {} ({} connections total)", session.getRemoteAddress().toString(), sessions.size());
    publish("$SYSTEM", session.getRemoteAddress().toString() + " has joined the chat.");
  }
  
  @OnWebSocketMessage
  public void message(final Session session, String message) throws IOException {
    logger.info("Received: {} -> {}", session.getRemoteAddress().toString(), message);
    publish(session.getRemoteAddress().toString(), message);
  }

  @OnWebSocketClose
  public void closed(final Session session, final int statusCode, final String reason) {
    sessions.remove(session);
    logger.info("Disconnected: {} ({} - {}) ({} connections total)", session.getRemoteAddress().toString(), statusCode, reason, sessions.size());
    session.close();
    publish("$SYSTEM", session.getRemoteAddress().toString() + " has left the chat.");
  }
  
  @OnWebSocketError
  public void error(final Session session, Throwable error) {
    sessions.remove(session);
    logger.info("Error: {} ({}) ({} connections total)", session.getRemoteAddress().toString(), error, sessions.size());
    session.close();
    publish("$SYSTEM", session.getRemoteAddress().toString() + " has left the chat.");
  }
  
  /**
   * Publishes a message to all people in the chat room.
   * @param name The name of the sender
   * @param message The content of the message
   */
  private void publish(String name, String message) {
    try {
      try (Connection conn = dbp.getConnection()) {
        // Notice: Because we are using RethinkDB change notifications, it's sufficient
        // to insert the row into the database here.
        ChatMessage m = new ChatMessage();
        m.name = name;
        m.message = message;
        m.insert(conn);
      }
    } catch (Exception e) {
      logger.warn("Caught exception in publish:", e);
    }
  }
}
