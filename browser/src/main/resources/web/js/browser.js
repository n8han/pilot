$(function() {
  $("li.project a").click(function() {
    var fork = window.open("", "_new");
    $.getJSON(this.href + "?callback=?", function(res) {
      if (res != "fail")
         fork.location = res;
      else {
        fork.close();
        alert("Pilot failed to take off.");
      }
    });
    return false;
  });
});