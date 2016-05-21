package demos.tinyurl.controllers;

import static lightning.enums.HTTPMethod.*;
import static lightning.server.Context.*;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lightning.ann.*;
import lightning.db.NamedPreparedStatement;
import lightning.util.Time;

import com.google.common.collect.ImmutableMap;

@Controller
public class TinyUrlController {  
  @Route(path="/", methods={GET})
  public void home() throws Exception {
    redirectIfNotLoggedIn(url().to("/login"));
    redirect(url().to("/my"));
  }
  
  @Route(path="/my", methods={GET})
  @RequireAuth
  @Template("home.ftl")
  public Object handleListUrls() throws Exception {
    List<Map<String, Object>> urls = new ArrayList<>();
    
    try (NamedPreparedStatement query = db().prepare("SELECT * FROM tinyurls WHERE user_id = :user_id;")) {
      query.setLong("user_id", user().getId());
      try (ResultSet result = query.executeQuery()) {
        while (result.next()) {
          urls.add(new ImmutableMap.Builder<String, Object>()
              .put("code", result.getString("code"))
              .put("url", result.getString("url"))
              .put("last_updated", Time.format(result.getLong("last_updated")))
              .put("last_clicked", Time.format(result.getLong("last_clicked")))
              .put("click_count", result.getLong("click_count"))
              .put("share_url", url().to("/u/" + result.getString("code")))
              .build());
        }
      }
    }
    
    return ImmutableMap.of("urls", urls, "user", user().getUserName());
  }
  
  @Route(path="/delete", methods={POST})
  @RequireAuth
  public void handleDeleteUrl(@QParam("code") String code) throws Exception {
    validate("code").isNotEmpty().isShorterThan(50);
    badRequestIf(!passesValidation());
    
    try (NamedPreparedStatement query = db().prepare("DELETE FROM tinyurls WHERE code = :code AND user_id = :user_id;")) {
      query.setString("code", code);
      query.setLong("user_id", user().getId());
      accessViolationIf(query.executeUpdate() == 0);      
    }
    
    redirect(url().to("/my"));
  }
  
  @Route(path="/add", methods={POST})
  @RequireAuth
  public void handleAddUrl(@QParam("code") String code, @QParam("url") String url) throws Exception {
    validate("code").isNotEmpty().isShorterThan(50).isAlphaNumeric();
    validate("url").isNotEmpty().isURL().isShorterThan(1000);
    badRequestIf(!passesValidation(), validator().getErrorsAsString());
    
    db().transaction(() -> {
      try (NamedPreparedStatement query = db().prepare("SELECT * FROM tinyurls WHERE code = :code;")) {
        query.setString("code", code);
        try (ResultSet result = query.executeQuery()) {
          badRequestIf(result.next(), "Provided code is already in use.");
        }
      }
      
      try (NamedPreparedStatement query = db().prepare("INSERT INTO tinyurls (url, code, last_updated, last_clicked, user_id) VALUES (:url, :code, :last_updated, :last_clicked, :user_id);")) {
        query.setString("url", url);
        query.setString("code", code);
        query.setLong("last_updated", Time.now());
        query.setLong("last_clicked", Time.now());
        query.setLong("user_id", user().getId());
        query.executeUpdate();
      }
    });
    
    redirect(url().to("/my"));
  }
  
  @Route(path="/modify", methods={POST})
  @RequireAuth
  public void handleModifyUrl(@QParam("code") String code, @QParam("url") String url) throws Exception {
    validate("code").isNotEmpty().isShorterThan(50);
    validate("url").isNotEmpty().isURL().isShorterThan(1000);
    badRequestIf(!passesValidation());
    
    try (NamedPreparedStatement query = db().prepare("UPDATE tinyurls SET url = :url, last_updated = :last_updated, last_clicked = :last_clicked WHERE code = :code AND user_id = :user_id;")) {
      query.setString("url", url);
      query.setString("code", code);
      query.setLong("last_updated", Time.now());
      query.setLong("last_clicked", Time.now());
      query.setLong("user_id", user().getId());
      badRequestIf(query.executeUpdate() == 0, "Attempted to update a non-existent or not owned code.");      
    }
    
    redirect(url().to("/my"));
  }
  
  @Route(path="/u/:code", methods={GET})
  public void handleViewUrl(@RParam("code") String code) throws Exception {
    try (NamedPreparedStatement query = db().prepare("SELECT * FROM tinyurls WHERE code = :code;")) {
      query.setString("code", code);
      try (ResultSet result = query.executeQuery()) {
        notFoundIf(!result.next());
        
        // Update the click count.
        db().prepare("UPDATE tinyurls SET last_clicked = :last_clicked, click_count = click_count + 1 WHERE code = :code;",
            ImmutableMap.of("last_clicked", Time.now(), "code", code)).executeUpdateAndClose();
        
        // Redirect the user to the URL.
        redirect(result.getString("url"));
      }
    }
  }
}
