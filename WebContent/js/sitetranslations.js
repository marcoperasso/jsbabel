var hideignored = 1;
var hidenotignored = 2;
var hidetranslated = 4;
var hidenottranslated = 8;
function hideflag(flag, set) {
    var cook = getCookie("wlHideFlag");
    cook = cook ? parseInt(cook) : 0;
    if (typeof set !== "undefined")
    {

        if (set)
            cook |= flag;
        else
            cook &= ~flag;
        setCookie("wlHideFlag", cook, 365);
        return set;
    }
    return (cook & flag) == flag;

}
function getPageId(el)
{
    var jPage = $(el).closest(".page");
    return jPage.attr("pageid");
}
function getSelectedPages() {
    var pages = [];
    $(".pageselector:checked").each(function() {
        pages.push(getPageId(this));
    });
    return pages;
}
function getSelectedStrings() {
    var strings = [];
    $(".basestring:checked").each(function() {
        var stringId = $(this).attr("stringid");
        strings.push(stringId);
    });
    return strings;
}
function getSelectedLongStrings() {
    var longstrings = [];
    $(".baselongstring:checked").each(function() {
        var stringId = $(this).attr("stringid");
        longstrings.push(stringId);
    });
    return longstrings;
}
function getStringsById(stringId, isLong)
{
    var jInputs;
    if (isLong)
        jInputs = $("input.baselongstring[stringid='" + stringId + "']");
    else
        jInputs = $("tr input.basestring[stringid='" + stringId + "']");
    return jInputs.closest("tr");
}

function applyFilter(context)
{
    var jHide = $();
    var jIgnores = $(".ignore,.ignorepage", context);
    if (hideflag(hideignored))
    {
        jHide = jHide.add(jIgnores);
    }
    else
    {
        jIgnores.show();
    }

    $(".whenignore", context).each(function() {
        var jThis = $(this);
        var jIgn = jThis.closest(".ignorable");
        if (jIgn.hasClass("ignore") || jIgn.hasClass("ignorepage"))
            jThis.show();
        else if (jIgn.hasClass("notignore") || jIgn.hasClass("notignorepage"))
            jThis.hide();
    });
    $(".whennotignore", context).each(function() {
        var jThis = $(this);
        var jIgn = jThis.closest(".ignorable");
        if (jIgn.hasClass("ignore") || jIgn.hasClass("ignorepage"))
            jThis.hide();
        else if (jIgn.hasClass("notignore") || jIgn.hasClass("notignorepage"))
            jThis.show();
    });

    var jNotIgnores = $(".notignore", context);
    if (hideflag(hidenotignored))
    {
        jHide = jHide.add(jNotIgnores);
    }
    else
    {
        jNotIgnores.show();
    }

    var jTranslated = $("td.targetstring:not(:empty)", context).closest("tr");
    if (hideflag(hidetranslated))
    {
        jHide = jHide.add(jTranslated);
    }
    else
    {
        jTranslated.show();
    }
    var jNotTranslated = $("td.targetstring:empty", context).closest("tr");
    if (hideflag(hidenottranslated))
    {
        jHide = jHide.add(jNotTranslated);
    }
    else
    {
        jNotTranslated.show();
    }
    jHide.hide();
}
$(function() {
    var sConfirmLongProcedure = _jsb("This procedure is time consuming, may take minutes; do you want to confirm?");
    var sConfirmStop = _jsb("Are you sure to stop the procedure?");
    var sNoSelectedPages = _jsb("No pages selected!");
    $("#ignore").attr("checked", hideflag(hideignored));
    $("#notignore").attr("checked", hideflag(hidenotignored));
    $("#translated").attr("checked", hideflag(hidetranslated));
    $("#nottranslated").attr("checked", hideflag(hidenottranslated));

    $("#xliffexportform").dialog({
        autoOpen: false,
        width: 400,
        modal: true,
        buttons: {
            Export: function() {
                $(this).dialog("close");
                startXLiffExport(this);
            },
            Cancel: function() {
                $(this).dialog("close");
            }
        },
        close: function() {

        }
    }).keydown(function(e) {
        if (e.keyCode === 13)
        {
            e.preventDefault();
            e.stopPropagation();
            $(this).dialog("close");
            startXLiffExport(this);
        }
    });


    $("#xliffimportform").dialog({
        autoOpen: false,
        width: 500,
        modal: true,
        buttons: {
            Import: function() {
                if (startXLiffImport(this))
                    $(this).dialog("close");
            },
            Cancel: function() {
                $(this).dialog("close");
            }
        },
        close: function() {

        }
    }).keydown(function(e) {
        if (e.keyCode === 13)
        {
            e.preventDefault();
            e.stopPropagation();
            if (startXLiffImport(this))
                $(this).dialog("close");
        }
    });
    $("#htmlexportform").dialog({
        autoOpen: false,
        width: 400,
        modal: true,
        buttons: {
            Export: function() {
                $(this).dialog("close");
                startHTMLExport(this);
            },
            Cancel: function() {
                $(this).dialog("close");
            }
        },
        close: function() {

        }
    }).keydown(function(e) {
        if (e.keyCode === 13)
        {
            e.preventDefault();
            e.stopPropagation();
            $(this).dialog("close");
            startHTMLExport(this);
        }
    });

    var refreshParser = new ProcedureController(".refreshbutton", ".generationoutput", "/servlet/siteParser");
    refreshParser.setFinish(function() {
        location.reload(true);
    });
    refreshParser.setBeforeStarting(function() {
        var pages = getSelectedPages();
        if (pages.length === 0)
        {
            alert(sNoSelectedPages);
            return false;
        }
        refreshParser.setProcedureData(
                {
                    "siteid": getSiteId(),
                    "cmd": "refresh",
                    "src": getHost(),
                    "pages": pages
                });
        return confirm(sConfirmLongProcedure);
    });
    refreshParser.setBeforeStopping(function() {
        return confirm(sConfirmStop);
    });

    var missingParser = new ProcedureController(".generatemissingbutton", ".generationoutput", "/servlet/siteParser");

    missingParser.setFinish(function() {
        location.reload(true);
    });
    missingParser.setBeforeStarting(function() {
        missingParser.setProcedureData(
                {
                    "siteid": getSiteId(),
                    "cmd": "addmissing",
                    "src": getHost()
                });
        return confirm(sConfirmLongProcedure);
    });
    missingParser.setBeforeStopping(function() {
        return confirm(sConfirmStop);
    });


    var xliffExporter = new ProcedureController(".exportxliffbutton", "", "/servlet/siteParser");
    xliffExporter.setDownloadOutput(true);
    xliffExporter.setBeforeStarting(function() {
        var pages = getSelectedPages();
        if (pages.length === 0)
        {
            alert(sNoSelectedPages);
            return false;
        }
        $("#xliffexportform").dialog("open");

        return false;
    });
    xliffExporter.setBeforeStopping(function() {
        return confirm(sConfirmStop);
    });

    var htmlExporter = new ProcedureController(".exporthtmlbutton", "", "/servlet/siteParser");
    htmlExporter.setDownloadOutput(true);
    htmlExporter.setBeforeStarting(function() {
        var pages = getSelectedPages();
        if (pages.length === 0)
        {
            alert(sNoSelectedPages);
            return false;
        }
        $("#htmlexportform").dialog("open");

        return false;
    });
    htmlExporter.setBeforeStopping(function() {
        return confirm(sConfirmStop);
    });
    function startHTMLExport(form)
    {
        var plain = !$("#translationinfo", form).is(':checked');
        var translated = $("#translated", form).is(':checked');
        htmlExporter.setProcedureData(
                {
                    "siteid": getSiteId(),
                    "plain": plain,
                    "translated": translated,
                    "format": "html",
                    "cmd": "export",
                    "src": getHost(),
                    "targetlocale": getTargetLocale(),
                    "pages": getSelectedPages()
                });
        htmlExporter.procedure();
    }
    function startXLiffImport(form)
    {
        var ext = $('#filetoupload').val().split('.').pop().toLowerCase();
        if ($.inArray(ext, ['xlf', 'zip']) == -1) {
            alert(_jsb("Please select a file with *.zip or *.xlf extension."));
            return false;
        }
        jQuery("#xliffimportform", form).submit();
        return true;
    }
    function startXLiffExport(form)
    {
        var plain = !$("#alsohtml", form).is(':checked');
        xliffExporter.setProcedureData(
                {
                    "siteid": getSiteId(),
                    "plain": plain,
                    "format": "xliff",
                    "cmd": "export",
                    "src": getHost(),
                    "targetlocale": getTargetLocale(),
                    "pages": getSelectedPages()
                });
        xliffExporter.procedure();
    }
    $(".importxliffbutton").click(function()
    {
        $("#xliffimportform").dialog("open");
    });
    $(".importhtmlbutton").click(function()
    {
        alert(_jsb("Sorry, this functionality is not yet available!"));
    });
    $(".stringhandle").click(function()
    {
        var jThis = $(this);
        var jPage = jThis.closest(".page");
        if (jPage.hasClass("ignorepage"))
            return;

        var jDiv = $(".stringscontainer", jPage);
        var attr = jDiv.attr("pageindex");
        if (attr)
        {
            var index = parseInt(attr, 10);
            $.ajax({
                url: "pagestrings.jsp?siteid=" + getSiteId() + "&pageindex=" + index + "&targetlocale=" + getTargetLocale()
            }).done(function(data) {
                jDiv.html(data);
                $(".stringselector", jDiv)
                        .change(function() {
                            if (!$(this).is(':checked'))
                            {
                                $(".multistringselector", $(this).closest(".page")).attr("checked", false);
                            }

                        });
                applyFilter(jDiv);

                $(".multistringselector", jDiv)
                        .change(function() {
                            $(".stringselector", $(this).closest(".page")).attr("checked", $(this).is(':checked'));
                        });
                $("td.targetstring", jDiv).mousedown(function(e) {
                    var jTD = $(this);
                    if (jTD.closest("tr").hasClass("ignore"))
                        return;
                    if ($('textarea', jTD).length > 0)
                        return;
                    var old = jTD.html();
                    jTD.html("<textarea class='targettext'></textarea>");
                    $('textarea', jTD).text(old).focus().blur(function() {

                        if (old == this.value)
                        {
                            jTD.html(old);
                            return;
                        }
                        var info = getSingleStringInfo(this)
                        var newValue = this.value;
                        $.getJSON("/servlet/siteParser",
                                {
                                    "cmd": "updatestring",
                                    "stringid": info.stringId,
                                    "islong": info.isLong,
                                    "text": this.value,
                                    "targetlocale": getTargetLocale(),
                                    "siteid": getSiteId()
                                }, function(data) {
                            if (data.success == true)
                            {
                                jTD.html(newValue);
                            }
                            else
                            {
                                jTD.html(old);
                                alert(data.message);
                            }

                        });
                    });
                    e.preventDefault();
                });
                function getSingleStringInfo(commandEl)
                {
                    var jRow = $(commandEl).closest("tr");
                    var jInput = $(".stringselector", jRow);
                    var stringId = jInput.attr("stringid");
                    var isLong = jInput.hasClass("baselongstring");
                    return {
                        "stringId": stringId,
                        isLong: isLong
                    };
                }
                function ignoreString(ignore, contextElement)
                {
                    var info = getSingleStringInfo(contextElement)
                    $.getJSON("/servlet/siteParser",
                            {
                                "cmd": "ignorestrings",
                                "delete": ignore,
                                "strings": info.isLong ? [] : [info.stringId],
                                "longstrings": info.isLong ? [info.stringId] : [],
                                "siteid": getSiteId()
                            }, function(data) {
                        if (data.success == true)
                        {
                            var jStrings = getStringsById(info.stringId, info.isLong);
                            if (ignore)
                                jStrings.removeClass("notignore").addClass("ignore");
                            else
                                jStrings.removeClass("ignore").addClass("notignore");
                            applyFilter(jStrings.parent());
                        }
                        else
                        {
                            alert(data.message);
                        }

                    });
                }
                $(".ignorestring", jDiv).click(function() {
                    ignoreString(true, this);
                });
                $(".includestring", jDiv).click(function() {
                    ignoreString(false, this);
                });
            });
        }
        jDiv.slideToggle(function() {
            jThis.toggleClass("minus");
        });
    }
    );
    $(".pageselector").change(function() {
        if (!$(this).is(':checked'))
        {
            $(".multipageselector", $(this).closest(".page")).attr("checked", false);
        }
    });

    $("body").selectable({
        distance: 30,
        filter: "input[type='checkbox']",
        selected: function(event, ui) {
            if (ui.selected && ui.selected.nodeName == 'INPUT')
                ui.selected.checked = !ui.selected.checked;
        }
    });
    $(".multipageselector").change(function() {
        $(".pageselector").attr("checked", $(this).is(':checked'));
    });
    function ignorePage(ignore, contextElement)
    {
        var jPage = $(contextElement).closest(".page");
        var pages = [getPageId(contextElement)];
        $.getJSON("/servlet/siteParser",
                {
                    "cmd": "ignorestrings",
                    "delete": ignore,
                    "pages": pages,
                    "siteid": getSiteId()
                }, function(data) {
            if (data.success == true)
            {
                if (ignore)
                {
                    jPage.removeClass("notignore").addClass("ignore");
                    $(".stringscontainer", jPage).html("");
                }
                else
                {
                    jPage.removeClass("ignore").addClass("notignore");
                }
                applyFilter(jPage.parent());
            }
            else
            {
                alert(data.message);
            }

        });
    }
    $(".deletepage").click(function() {
        ignorePage(true, this);
    });
    $(".includepage").click(function() {
        ignorePage(false, this);
    });
    applyFilter();

    $("#ignore").click(function() {
        hideflag(hideignored, this.checked);
        applyFilter();
    });
    $("#notignore").click(function() {
        hideflag(hidenotignored, this.checked);
        applyFilter();
    });
    $("#translated").click(function() {
        hideflag(hidetranslated, this.checked);
        applyFilter();
    });
    $("#nottranslated").click(function() {
        hideflag(hidenottranslated, this.checked);
        applyFilter();
    });
    function ignoreStrings(ignore)
    {
        $.getJSON("/servlet/siteParser",
                {
                    "cmd": "ignorestrings",
                    "delete": ignore,
                    "pages": getSelectedPages(),
                    "strings": getSelectedStrings(),
                    "longstrings": getSelectedLongStrings(),
                    "siteid": getSiteId()
                }, function(data) {
            if (data.success == true)
            {
                location.reload();
            }
            else
            {
                alert(data.message);
            }

        });
    }
    function deleteStrings()
    {
        $.getJSON("/servlet/siteParser",
                {
                    "cmd": "deletestrings",
                    "pages": getSelectedPages(),
                    "strings": getSelectedStrings(),
                    "longstrings": getSelectedLongStrings(),
                    "siteid": getSiteId()
                }, function(data) {
            if (data.success == true)
            {
                location.reload();
            }
            else
            {
                alert(data.message);
            }

        });
    }
    $(".deletebutton").click(function() {
        deleteStrings();
    });
    $(".ignorebutton").click(function() {
        ignoreStrings(true);
    });

    $(".includebutton").click(function() {
        ignoreStrings(false);
    });
});