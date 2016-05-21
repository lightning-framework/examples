<!DOCTYPE HTML>
<html>
  <head>
    <title>FileShare!</title>
  </head>
  <body>
    <h2>FileShare</h2>
    <p>Upload a file to share:</p>
    <form action="/upload" method="POST" enctype="multipart/form-data">
      <p><input type="file" name="file" /></p>
      <p><input type="submit" value="Upload!" /></p>
    </form>
  </body>
</html>
