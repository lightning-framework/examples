<#escape x as x?html>
  <p>Welcome, ${user}! <a href="/logout">Log Out</a><p>

  <p>You have the following URLs:</p>

  <#list urls as item>
    <hr />
    <form action="/modify" method="POST">
      <input type="hidden" name="code" value="${item.code}" />
      Code: ${item.code} <br />
      URL: <input type="text" name="url" value="${item.url}" /><input type="submit" value="Save Changes" /><br />
      Last Saved: ${item.last_updated}<br />
      Last Clicked: ${item.last_clicked}<br />
      Clicks: ${item.click_count}<br />
      Share: <input type="text" name="_" value="${item.share_url}" disabled="disabled" /> <a href="${item.share_url}">[View]</a><br />
    </form>

    <form action="/delete" method="POST">
      <input type="hidden" name="code" value="${item.code}" />
      <input type="submit" value="Delete URL" /><br />
    </form>
  </#list>

  <hr />

  <p>Add URL:</p>
  <form action="/add" method="POST">
    Short Code:
    <input type="text" name="code" value="" />
    URL:
    <input type="text" name="url" value="" />
    <input type="submit" value="Create Short URL" />
  </form>
</#escape>
