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
        $.ajax(
            type: "POST"
            data:
                action: @value
        )
        if @value == "Exit"
           window.close()
        false
    )
)