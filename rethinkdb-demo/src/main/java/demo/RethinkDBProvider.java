package demo;

import static com.rethinkdb.RethinkDB.r;
import com.rethinkdb.net.Connection;

/**
 * A connection pooling library for RethinkDB.
 * TODO: Add an option to use SSL.
 * TODO: Use true connection pooling (once driver supports it).
 */
public class RethinkDBProvider {
  private final String host;
  private final int port;
  private final String db;
  private final String user;
  private final String password;
  
  public RethinkDBProvider(String host, int port, String db, String user, String password) {
    this.host = host;
    this.port = port;
    this.db = db;
    this.user = user;
    this.password = password;
  }
  
  /**
   * @return A new RethinkDB connection for use in the calling thread. Caller should close the
   *         connection when finished.
   */
  public Connection getConnection() {
    Connection.Builder c = r.connection()
        .hostname(host)
        .port(port)
        .db(db);
    
    if (user != null && password != null) {
      c.user(user, password);
    }
    
    return c.connect();
  }
}
