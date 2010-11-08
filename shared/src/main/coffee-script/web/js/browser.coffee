$( ->
    $("li.project a").click( ->
        # open now so we're not blocked
        fork = window.open("", "_new")
        $.getJSON("#{@href}?callback=?", (res) ->
            if res isnt "fail"
                fork.location = res
            else
                fork.close()
                alert("Pilot failed to take off.")
        )
        false
    )
    $("input[name=action]").click( ->
        data = action: @value
        $("textarea").each( (i, ta) ->
            data.contents = ta.value
        )
        plane = $("img.plane")
        $.ajax(
            type: "POST"
            data: data
            error: (req, status, exc) ->
                alert(req.responseText)
            complete: (req, status) ->
                plane.hide()
        )
        if @value == "Exit"
            window.close()
        else
            plane.show()
        false
    )
)