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
    $("input[value=Exit]").click( ->
        $.ajax(
            url: "/"
            type: "POST"
            data:
                action: "Exit"
        )
        window.close()
        false
    )
)