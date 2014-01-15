function ProcedureController(commandSelector, outputSelector, procedureUrl, data) {
    var sStopping = _jsb("Stopping procedure...");
    var sStop = _jsb("STOP");
    var sStarted = _jsb("Procedure started");
    var sessionId = null;
    var procedureData = data;
    var output = $(outputSelector); 
    var command = $(commandSelector);
    var downloadOutput = false;
    var originalCommandContent = command.html();
    var originalCommandTitle = command.attr("title");
    var finish;
    var beforeStarting;
    var beforeStopping;
    var _this = this;
    this.setBeforeStopping = function(f)
    {
        beforeStopping = f;
    }
    this.setBeforeStarting = function(f)
    {
        beforeStarting = f;
    }
    this.setFinish = function(f)
    {
        finish = f;
    }
    this.setProcedureData = function(data)
    {
        procedureData = data;
    }
    this.setDownloadOutput = function(set)
    {
        downloadOutput = (set == true);
    }
    this.procedure = function()
    {
        output.html(sStarted);
        command.html("<img src='img/stop.png'>").attr("title", sStop);
        sessionId = new Date().getTime();
        procedureData.sessionId = sessionId;
        if (downloadOutput) 
        {
            download(procedureUrl + "?" + $.param(procedureData));
        }
        else
        {
            $.post(procedureUrl, procedureData, onResponse);
        }
        setTimeout(ping, 1000);

        function onResponse(data) {
            output.html(data);
        }
        function onPingResponse(data) {
            if (sessionId)
            {
                output.html(data.message); 
                if (!data.terminated)
                    setTimeout(ping, 1000);
                else
                {
                    sessionId = null;
                    command.html(originalCommandContent).attr("title", originalCommandTitle);
                    if (finish)
                        finish();
                }
            }
        }

        function ping() {
            $.getJSON(
                "servlet/procedureController", 
                {
                    "sessionId" : sessionId
                }, onPingResponse);
           
        }
    }
    command.click(function() { 
        if (sessionId) {
            if (beforeStopping && !beforeStopping())
                return;
            output.html(sStopping);
            $.get("servlet/procedureController", {
                "sessionId" : sessionId,
                "stop" : true
            });
            return;
        }
        if (beforeStarting && !beforeStarting())
            return;
    
        _this.procedure();
    });
}

function download(src)
{
    var hiddenIFrameID = 'hiddenDownloader',
    iframe = document.getElementById(hiddenIFrameID);
    if (iframe === null) {
        iframe = document.createElement('iframe');
        iframe.id = hiddenIFrameID;
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
    }
    iframe.src = src;
}