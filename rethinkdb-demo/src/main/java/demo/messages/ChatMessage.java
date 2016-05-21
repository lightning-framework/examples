package demo.messages;

import lightning.util.Time;

import com.rethinkdb.net.Connection;

import static com.rethinkdb.RethinkDB.r;

/**
 * A model representing a chat message.
 */
public class ChatMessage {
  public String id = null;
  public long time = Time.now();
  public String name;
  public String message;
  
  public void insert(Connection conn) {
    // TODO: Should write in the message ID.
    r.table("messages").insert(
        r.hashMap("name", name)
        .with("message", message)
        .with("time", time)).run(conn);
  }
}
