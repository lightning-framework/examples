package demos.fileshare.controllers;

import java.sql.Blob;
import java.sql.ResultSet;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableMap;

import lightning.ann.*;
import lightning.db.NamedPreparedStatement;
import lightning.enums.HTTPHeader;
import lightning.enums.HTTPStatus;
import lightning.util.Mimes;
import lightning.util.NumberFormat;
import lightning.util.Time;
import static lightning.server.Context.*;
import static lightning.enums.HTTPMethod.*;

@Controller
public class FileShareController {
  @Route(path="/", methods={GET})
  @Template("upload.ftl")
  public Object handleIndex() throws Exception {
    return ImmutableMap.of();
  }
  
  @Route(path="/upload", methods={POST}) 
  @Multipart
  public void handleUpload() throws Exception {
    badRequestIf(request().getPart("file").getSize() == 0);
    
    try (NamedPreparedStatement query = db().prepare("INSERT INTO files (name, uploaded, downloads, content, last_downloaded) "
        + "VALUES (:name, :uploaded, :downloads, :content, :last_downloaded);")) {
      query.setString("name", request().getPart("file").getSubmittedFileName());
      query.setLong("uploaded", Time.now());
      query.setLong("downloads", 0);
      query.setLong("last_downloaded", 0);
      query.setBinaryStream("content", 
          request().getPart("file").getInputStream(),
          request().getPart("file").getSize());
      
      long fileId = query.executeInsertAndClose();
      redirect("/f/" + fileId);
    }
  }
  
  @Route(path="/f/:file", methods={GET})
  @Template("download.ftl")
  public Object handleFileView(@RParam("file") int fileId) throws Exception {
    try (NamedPreparedStatement query = db().prepare("SELECT name, LENGTH(content) AS size, downloads, id FROM files WHERE id = :id;")) {
      query.setLong("id", fileId);
      try (ResultSet file = query.executeQuery()) {
        notFoundIf(!file.next());
        return ImmutableMap.of(
          "file_name", file.getString("name"),
          "file_size", NumberFormat.formatFileSize(file.getLong("size")),
          "download_count", file.getLong("downloads"),
          "download_url", url().to("/f/" + file.getInt("id") + "/download")
        );
      }
    }
  }
  
  @Route(path="/f/:file/download", methods={GET})
  public void handleFileDownload(@RParam("file") int fileId) throws Exception {
    try (NamedPreparedStatement query = db().prepare("SELECT name, content, uploaded FROM files WHERE id = :id;")) {
      query.setLong("id", fileId);
      try (ResultSet file = query.executeQuery()) {
        notFoundIf(!file.next());
        
        db().prepare("UPDATE files SET last_downloaded = :now, downloads = downloads + 1 WHERE id = :id;",
            ImmutableMap.of("now", Time.now(), "id", fileId)).executeUpdateAndClose();
        
        String fileName = file.getString("name");
        Blob content = file.getBlob("content");
        
        response().status(HTTPStatus.OK);
        response().header(HTTPHeader.CACHE_CONTROL, "public, max-age=3600, must-revalidate");
        response().header(HTTPHeader.CONTENT_TYPE, Mimes.forPath(fileName));
        response().header(HTTPHeader.CONTENT_LENGTH, content.length());
        response().header(HTTPHeader.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response().header(HTTPHeader.ETAG, fileId);
        response().header(HTTPHeader.LAST_MODIFIED, Time.formatForHttp(file.getLong("uploaded")));
        IOUtils.copy(content.getBinaryStream(), response().outputStream());
      }
    }
  }
}
