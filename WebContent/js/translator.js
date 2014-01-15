(function() {
    var c = {
        getSelection: function() {
            var e = this.jquery ? this[0] : this;
            return(('selectionStart'in e && function() {
                var l = e.selectionEnd - e.selectionStart;
                return{
                    start: e.selectionStart,
                    end: e.selectionEnd,
                    length: l,
                    text: e.value.substr(e.selectionStart, l)
                };
            }) || (document.selection && function() {
                e.focus();
                var r = document.selection.createRange();
                if (r === null) {
                    return{
                        start: 0,
                        end: e.value.length,
                        length: 0
                    };
                }
                var a = e.createTextRange();
                var b = a.duplicate();
                a.moveToBookmark(r.getBookmark());
                b.setEndPoint('EndToStart', a);
                return{
                    start: b.text.length,
                    end: b.text.length + r.text.length,
                    length: r.text.length,
                    text: r.text
                };
            }) || function() {
                return{
                    start: 0,
                    end: e.value.length,
                    length: 0
                };
            })();
        },
        replaceSelection: function() {
            var e = this.jquery ? this[0] : this;
            var a = arguments[0] || '';
            return(('selectionStart'in e && function() {
                e.value = e.value.substr(0, e.selectionStart) + a + e.value.substr(e.selectionEnd, e.value.length);
                return this;
            }) || (document.selection && function() {
                e.focus();
                document.selection.createRange().text = a;
                return this;
            }) || function() {
                e.value += a;
                return this;
            })()
        }
    };

    jQuery.each(c, function(i) {
        jQuery.fn[i] = this
    })
})();

function Translator() {
    var currentMousePos = {x: -1, y: -1};
    var orphanScript = '/servlet/orphans';
    var autotranslateScript = '/servlet/autotranslate';
    var dataUrl = '/servlet/data';
    var translatorUrl = '/servlet/translator';
    var sConfirmSave = _jsb('Do you want to save your translations?');
    var sSave = _jsb('Save');
    var sClose = _jsb('Close');
    var sParameter = _jsb('Replace selection with a parameter');
    //var sUpLevel = _jsb("Translate the containing element");
    var sNextSibling = _jsb("Translate the next element");
    var sPrevSibling = _jsb("Translate the previous element");
    var sTextView = _jsb("Text view");
    var sHtmlView = _jsb("Html view");
    var sMoveNext = _jsb("Move after the following element");
    var sMoveBefore = _jsb("Move before the previous element");
    var sSkipTranslated = _jsb("You are viewing only not yet translated items");
    var sIncludeTranslated = _jsb("You are viewing both translated and not translated items");
    var sTranslating = _jsb("You are currently translating: ");
    var sNoTranslations = _jsb("This element has nothing to translate.");
    var sNoTransHint = _jsb("If you were expecting something, perhaps all translations have been done and you are filtering translated items, or maybe you are filtering nested items.");
    var sPopup = _jsb("Show items to translate in a separate window");
    var sNoPopup = _jsb("Show items to translate in the web page window");
    var sAlertPopup = _jsb("Before using this functionality please be sure that your browser doesn't block popups for this site; do you want to continue?");
    var sIgnoreTranslation = _jsb("Ignore this translation");
    var sPageTranslation = _jsb("Page speficic translation");
    var sOrphanWindowTitle = _jsb("Orphan translations");
    var sNoOrphans = _jsb("There are no orphan translations");
    var sAutoSave = _jsb("Autosave enabled");
    var sNoAutoSave = _jsb("Autosave disabled");

    var skipTranslatedCookie = "wlSkipTranslated";
    var trHeightCookie = "wlTranslatorHeight";
    var autosaveCookie = "wlAutosave";

    var popupCookie = "wlPopup";
    var handle = null;
    var btnSave = null;
    var btnSkip = null;
    var btnNext = null;
    var btnAutoSave = null;
    var btnPopup = null;
    var modified = false;
    var sendDataFrame = null;
    var accelerators = [];
    var currentJElement = null;
    var tr = this;
    var translatorWindow = null;
    var translatorFrame = null;
    var jOrphansTable = null;
    var orphansWindow = null;

    var flasher = null;
    var addedBodySpace = 0;
    var skip = getCookie(skipTranslatedCookie) == "true";
    var autosave = getCookie(autosaveCookie) == "true";
    var popup = getCookie(popupCookie) == "true";
    var toTranslate = [];

    tr.addCss("https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/themes/base/jquery-ui.css");
    tr.setThis(this); // per farlo puntare all'oggetto derivato

    var inlines = {
        "a": true,
        "abbr": true,
        "acronym": true,
        "address": false,
        "applet": true,
        "area": false,
        "b": true,
        "base": false,
        "basefont": false,
        "bdo": true,
        "bgsound": false,
        "big": true,
        "blink": true,
        "blockquote": false,
        "body": false,
        "br": true,
        "button": true,
        "caption": false,
        "center": false,
        "cite": true,
        "code": true,
        "col": false,
        "colgroup": false,
        "dd": false,
        "del": true,
        "dfn": true,
        "dir": false,
        "div": false,
        "dl": false,
        "dt": false,
        "em": true,
        "embed": true,
        "face": true,
        "fieldset": false,
        "font": true,
        "form": false,
        "frame": false,
        "frameset": false,
        "h1": false,
        "h2": false,
        "h3": false,
        "h4": false,
        "h5": false,
        "h6": false,
        "head": false,
        "hr": false,
        "html": false,
        "i": true,
        "ia": false,
        "iframe": true,
        "img": true,
        "input": true,
        "ins": true,
        "isindex": false,
        "kbd": true,
        "label": true,
        "legend": false,
        "li": false,
        "link": false,
        "listing": false,
        "map": true,
        "marquee": false,
        "menu": false,
        "meta": false,
        "nobr": true,
        "noembed": false,
        "noframes": false,
        "noscript": false,
        "object": true,
        "ol": false,
        "optgroup": false,
        "option": false,
        "p": false,
        "param": true,
        "plaintext": false,
        "pre": false,
        "q": true,
        "rb": true,
        "rbc": true,
        "rp": true,
        "rt": true,
        "rtc": true,
        "ruby": true,
        "s": true,
        "samp": true,
        "script": false,
        "select": true,
        "small": true,
        "spacer": true,
        "span": true,
        "strike": true,
        "strong": true,
        "style": false,
        "sub": true,
        "sup": true,
        "symbol": true,
        "table": false,
        "tbody": false,
        "td": false,
        "textarea": true,
        "tfoot": false,
        "th": false,
        "thead": false,
        "title": false,
        "tr": false,
        "tt": true,
        "u": true,
        "ul": false,
        "var": true,
        "wbr": true,
        "xml": false,
        "xmp": false,
        "#text": true};
    this.isTUElement = function(el)
    {
        return !inlines[el.nodeName.toLowerCase()];
    };
    this.closestTUElement = function(el)
    {
        if (tr.isTUElement(el))
            return el;
        return tr.closestTUElement(el.parentNode);
    };
    function FlashAnimator(e)
    {
        var flashingPeriod = 400;
        var el = e;
        var stopped = false;
        this.stop = function() {
            stopped = true;
        }
        function pulse()
        {
            //la aggiungo e la rimuovo
            el.switchClass("dummy", "jsb_translateCurrent", flashingPeriod, null, function() {
                el.switchClass("jsb_translateCurrent", "dummy", flashingPeriod)
            });
            //se non mi devo fermare, richiamo il metodo ricorsivamente
            if (!stopped)
                setTimeout(pulse, flashingPeriod * 2);
        }

        pulse();
    }
    function Accelerator(f, c, a, s, k)
    {
        var ctrl = c;
        var alt = a;
        var shift = s;
        var keyCode = k;
        var fn = f;
        this.execute = function(e) {
            if (e.altKey == alt && e.ctrlKey == ctrl && e.shiftKey == shift && keyCode == e.keyCode) {
                fn();
                return true;
            }
            return false;
        }
        this.getDescription = function() {
            var s = "(";
            if (ctrl)
                s += "CTRL+";
            if (alt)
                s += "ALT+";
            if (shift)
                s += "SHIFT+"
            s += getKeyDescription();
            s += ")";
            return s;
        }
        function getKeyDescription()
        {
            switch (keyCode)
            {
                case 27:
                    return 'ESC';
                case 37:
                    return 'LEFT';
                case 38:
                    return 'UP';
                case 39:
                    return 'RIGHT';
                case 40:
                    return 'DOWN';
            }
            return String.fromCharCode((96 <= keyCode && keyCode <= 105) ? keyCode - 48 : keyCode);
        }
    }

    this.translatorTableOpened = function() {
        return handle;
    };
    function isAutoSave() {
        return autosave;
    }
    function getTranslatorWindow()
    {
        if (translatorWindow && !translatorWindow.closed)
            return translatorWindow;
        if (translatorFrame)
            return translatorFrame.contentWindow;
        return null;
    }
    function getTranslatorDocument() {
        var wnd = getTranslatorWindow();
        return wnd ? wnd.document : null;
    }
    function specificPageTranslation() {
        ignoreTargetText(this.rowObjs.ignoreInput);
        var input = this;
        jQuery("input.specificInput", handle).each(function() {
            if (this == input)
                return;
            if (this.rowObjs.baseText.value == input.rowObjs.baseText.value)
            {
                this.checked = input.checked;
            }
        });
    }
    function changeTargetText() {
        var baseText = this.rowObjs.baseText;
        var targetText = this;
        targetText.value = adjustTranslation(baseText.value, targetText.value);

        addTranslation(baseText.value, targetText.value, this.rowObjs.specificInput.checked, isAutoSave());
        jQuery("textarea.baseInput", handle).each(function() {
            if (this == baseText)
                return;
            if (this.value == baseText.value)
                this.rowObjs.targetText.value = targetText.value;
        });
        tr.translateTree(document.documentElement);

        if (!isAutoSave())
            setModified(true);
    }
    function ignoreTargetText(input) {
        var b = tr.prepareString(input.rowObjs.baseText.value);
        if (input.checked) {
            addIgnore(b, isAutoSave());
        }
        else {
            addTranslation(b, input.rowObjs.targetText.value, input.rowObjs.specificInput.checked, isAutoSave());
        }
        adjustControlState(input);
        function adjustControlState(el) {
            if (el.checked) {
                jQuery(el.rowObjs.targetText).attr("readonly", "readonly").attr("tabindex", "-1");
                jQuery(el).parent().addClass("ignoreIsActive");
            }
            else {
                jQuery(el.rowObjs.targetText).removeAttr("readonly").removeAttr("tabindex");
                jQuery(el).parent().removeClass("ignoreIsActive");
            }
        }
        jQuery("input.ignoreInput", handle).each(function() {
            if (this == input)
                return;
            if (this.rowObjs.baseText.value == input.rowObjs.baseText.value)
            {
                this.checked = input.checked;
                adjustControlState(this);
            }
        });
        tr.translateTree(document.documentElement);
        if (!isAutoSave())
            setModified(true);
    }
    function setBtnSaveImage()
    {
        if (btnSave) {
            if (modified)
                btnSave.attr('src', tr.getJsbDomain() + '/img/savered.png');
            else
                btnSave.attr('src', tr.getJsbDomain() + '/img/savegreen.png');
        }
    }
    function setModified(bSet) {
        modified = bSet != false;
        setBtnSaveImage();
    }


    function createTranslatorTable() {
        function getLanguage(locale) {
            return locale.split('-')[0];
        }
        if (handle)
        {
            if (!handle.is(':visible')) {
                handle.show();
            }
            return handle;
        }
        var trnH = parseInt(getCookie(trHeightCookie));
        if (!trnH || isNaN(trnH))
            trnH = 200;
        if (popup)
        {
            translatorWindow = window.open("", "", "location=no, menubar=no");
            var h = getTranslatorDocument().getElementsByTagName("head")[0];
            jQuery("<title class='inlineTranslatorTitle'/>", getTranslatorDocument()).appendTo(h);

            jQuery(translatorWindow).unload(function() {
                if (flasher)
                    flasher.stop();
                if (handle && handle[0].ownerDocument == translatorWindow.document)
                    handle = null;
                translatorWindow = null;
            });
        }
        else
        {
            var f = jQuery("<div id='jsbabelTranslatorFrame' style='z-index:10000;position: fixed;width: 100%;bottom: 0px;background-color: orange;'><iframe style='height: 100%;width: 100%;'/></div>")
                    .appendTo(jQuery(document.body));
            f
                    .height(trnH)
                    .resizable({
                        handles: "n",
                        start: function() {
                            jQuery(translatorFrame).hide();
                        },
                        resize: function() {
                            var $this = jQuery(this);
                            $this.css({
                                top: "auto",
                                width: "100%"
                            });
                        },
                        stop: function() {
                            jQuery(translatorFrame).show();
                            setCookie(trHeightCookie, jQuery(this).height(), 365);
                        }
                    });
            translatorFrame = jQuery("iframe", f)[0];
            var doc = getTranslatorDocument();
            doc.open(); // altrimenti in IE il body è null
            doc.close();
        }

        tr.addCss("/css/translator.css", getTranslatorDocument());
        tr.addCss("/css/babel.css", getTranslatorDocument());
        tr.addCss("https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/themes/base/jquery-ui.css", getTranslatorDocument());

        handle = jQuery(
                '<div class="jsb_notranslate inlineTranslator">' +
                '<table border="0" cellpadding="0" cellspacing="0">' +
                '<thead><tr><td colspan=3 class="inlineTranslatorTitle"/></tr><tr><td class="buttons" colspan=3/></tr></thead>' +
                '</table>' +
                '<div class="translatorcontentcontainer">' +
                '<div class="notranslations"><h2/><p/></div>' +
                '<table border="0" cellpadding="0" cellspacing="0" class="translatorcontent"><tbody/></table></div>' +
                '</div>', getTranslatorDocument())
                .appendTo(getTranslatorDocument().body);
        var cnt = jQuery(".translatorcontentcontainer", handle);
        jQuery(getTranslatorWindow()).resize(function() {
            cnt.height(jQuery(this).height() - 80);
        });

        handle
                .attr('lang', getLanguage(tr.getBaseLocale()))
                .defaultView = doTextView;

        jQuery('.notranslations h2', handle).text(sNoTranslations);
        jQuery('.notranslations p', handle).text(sNoTransHint);
        var tdButtons = jQuery('.buttons', handle);
        handle.keydown(function(e) {
            for (var i = 0; i < accelerators.length; i++) {
                var accelerator = accelerators[i];
                if (accelerator.execute(e))
                {
                    e.preventDefault();
                    e.stopPropagation();
                    return;
                }
            }
        });
        function addToolButton(id, image, title, f, ctrl, alt, shift, key)
        {
            var img = jQuery('<img id="' + id + '"/>', getTranslatorDocument()).appendTo(tdButtons);
            if (image)
                img.attr('src', tr.getJsbDomain() + '/img/' + image + '.png');

            img.click(f);
            if (key)
            {
                var acc = new Accelerator(f, ctrl, alt, shift, key);
                accelerators.push(acc);
                img.accelerator = acc;
                title += (" " + acc.getDescription());
            }
            img.attr('title', title);
            return img;
        }
        function addTab(id, title, f, left)
        {
            var anchor = jQuery('<a id="' + id + '"/>', getTranslatorDocument()).appendTo(tdButtons);
            anchor.attr('title', title);
            anchor.text(title);
            anchor.click(f);
            anchor.css("left", left + 'px');
        }
        addTab("textView", sTextView, doTextView, 0);
        addTab("htmlView", sHtmlView, doHtmlView, 90);

        addToolButton("jsbclose", "close", sClose, closeTranslatorTable, false, false, false, 27);
        addToolButton("jsbparam", "parameter", sParameter, addParameter, true, false, false, 80);
        //addToolButton("jsbupLevel", "up-arrow", sUpLevel, upLevel, true, false, false, 38);
        addToolButton("jsbprevSibling", "prev", sPrevSibling, prevSibling, true, false, false, 37);
        btnNext = addToolButton("jsbnextSibling", "next", sNextSibling, nextSibling, true, false, false, 39);
        btnSkip = addToolButton("jsbskip", "", "", skipTranslated, true, false, false, 75);
        setSkipButtonProperties();
        btnPopup = addToolButton("jsbpopup", "", "", togglePopupWindow, true, false, false, 85);
        setPopupButtonProperties();
        if (!tr.isDemoMode())
        {
            addToolButton("jsbOrphans", "orphans", sOrphanWindowTitle, showOrphans, true, false, false, 79)
            btnAutoSave = addToolButton("jsbautosave", "autosave", sAutoSave, toggleAutosave, true, false, false, 65);
            btnSave = addToolButton("jsbsave", "savegreen", sSave, tr.globalSave, true, false, false, 83);
            setAutosaveButtonProperties();
            setBtnSaveImage();
        }

        var jBody = jQuery(document.body);
        if (popup)
        {
            if (addedBodySpace)
                jBody.height(jBody.height() - addedBodySpace);
        }
        else
        {
            jBody.height(jBody.height() + trnH);
            addedBodySpace = trnH;
        }
        return handle;
    }

    function doHtmlView()
    {
        jQuery('.textRow', handle).hide();
        jQuery('#textView', handle).removeClass('selected');
        jQuery('.htmlRow', handle).show();
        jQuery('#htmlView', handle).addClass('selected');
        handle.defaultView = doHtmlView;
        // do il fuoco al primo textarea
        jQuery('textarea:first', handle).focus();
    }
    function doTextView()
    {
        jQuery('.htmlRow', handle).hide();
        jQuery('#htmlView', handle).removeClass('selected');
        jQuery('.textRow', handle).show();
        jQuery('#textView', handle).addClass('selected');
        handle.defaultView = doTextView;
        jQuery('textarea.targetInput:visible', handle).first().focus();
    }

    function movePrev()
    {
        var jRow = jQuery(this).closest("tr.textRow");
        var elementToMove = jRow[0].associatedEl;
        if (!elementToMove)
            return;
        var id = getElementSafeId(elementToMove);//calcolarlo prima dello spostamento
        var jPrevRow = jRow.prev();
        if (jPrevRow.size() == 0)
            return;
        var prevEl = jPrevRow[0].associatedEl;
        if (!prevEl)
            return;

        while (true)
        {
            var jTmp = jPrevRow.prev();
            if (jTmp.size() == 0 || !jTmp[0] || !jTmp[0].associatedEl || jTmp[0].associatedEl != prevEl)
                break;
            jPrevRow = jTmp;
        }
        jRow.insertBefore(jPrevRow);

        var offset = -1;
        var n = elementToMove;
        while ((n = n.previousSibling) != prevEl)
        {

            if (n == null)
                return;
            offset--;
        }
        if (offset != 0) {
            offset = tr.offsetElement(elementToMove, offset);
            addMove(id, offset.toString(), isAutoSave());
            if (!isAutoSave())
                setModified(true);
        }
    }
    function moveNext()
    {
        var jRow = jQuery(this).closest("tr.textRow");
        var elementToMove = jRow[0].associatedEl;
        if (!elementToMove)
            return;
        var id = getElementSafeId(elementToMove);//calcolarlo prima dello spostamento
        var jNextRow = jRow.next();
        if (jNextRow.size() == 0)
            return;
        var nextEl = jNextRow[0].associatedEl;
        if (!nextEl)
            return;
        while (true)
        {
            var jTmp = jNextRow.next();
            if (jTmp.size() == 0 || !jTmp[0] || !jTmp[0].associatedEl || jTmp[0].associatedEl != nextEl)
                break;
            jNextRow = jTmp;
        }
        jRow.insertAfter(jNextRow);

        var offset = 1;
        var n = elementToMove;
        while ((n = n.nextSibling) != nextEl)
        {
            if (n == null)
                return;
            offset++;
        }

        if (offset != 0) {
            offset = tr.offsetElement(elementToMove, offset);
            addMove(id, offset.toString(), isAutoSave());
            if (!isAutoSave())
                setModified(true);
        }
    }
    function setSkipButtonProperties()
    {
        btnSkip.attr("src", tr.getJsbDomain() + (skip ? "/img/skip.png" : "/img/noskip.png"));
        btnSkip.attr("title", (skip ? sSkipTranslated : sIncludeTranslated) + (" " + btnSkip.accelerator.getDescription()));
    }
    function skipTranslated()
    {
        skip = !skip;
        setCookie(skipTranslatedCookie, skip, 365);
        setSkipButtonProperties();
        if (currentJElement)
            translateElement(currentJElement[0]);
    }
    function setAutosaveButtonProperties()
    {
        btnAutoSave.attr("src", tr.getJsbDomain() + (autosave ? "/img/autosave.png" : "/img/noautosave.png"));
        btnAutoSave.attr("title", (autosave ? sAutoSave : sNoAutoSave) + (" " + btnAutoSave.accelerator.getDescription()));
        if (autosave)
            btnSave.hide();
        else
            btnSave.show();
    }

    function toggleAutosave()
    {
        autosave = !autosave;
        setCookie(autosaveCookie, autosave, 365);
        setAutosaveButtonProperties();
    }

    function setPopupButtonProperties()
    {
        btnPopup.attr("src", tr.getJsbDomain() + (popup ? "/img/nopopup.png" : "/img/popup.png"));
        btnPopup.attr("title", (popup ? sNoPopup : sPopup) + (" " + btnPopup.accelerator.getDescription()));
    }
    function togglePopupWindow()
    {
        if (!popup && !confirm(sAlertPopup))
            return;
        var el = currentJElement ? currentJElement[0] : null;

        closeTranslatorTable();

        popup = !popup;

        setPopupButtonProperties();
        if (el)
            translateElement(el, function()
            {
                setCookie(popupCookie, popup, 365); //solo se tutto va bene, imposto il cookie per rendere persistente lo stato
            });
    }
    function upLevel()
    {
        if (!currentJElement)
            return;

        var el = getFollowingElement(currentJElement[0], function(n) {
            return n ? n.parentNode : null
        }, false)
        if (el)
            translateElement(el);

    }
    function nextSibling()
    {
        if (!currentJElement)
            return;
        var el = getFollowingElement(currentJElement[0], searchNext, true)
        if (el)
            translateElement(el);
    }
    function prevSibling()
    {
        if (!currentJElement)
            return;
        var el = getFollowingElement(currentJElement[0], searchPrev, true)
        if (el)
            translateElement(el);
    }
    function removeFromArray(b, ar)
    {
        if (b.length == 0 || b == " ")
            return false;
        for (var i = 0; i < ar.length; i++) {
            var trn = ar[i];
            if (trn.getBase() == b) {
                ar.splice(i, 1);
                return true;
            }
        }
        return false;
    }
    function addToArray(b, t, specific, ar) {
        if (b.length == 0 || b == " ")
            return null;
        for (var i = 0; i < ar.length; i++) {
            var trn = ar[i];
            if (trn.getBase() == b) {
                trn.setTarget(t);
                trn.setPageSpecific(specific);
                return trn;
            }
        }
        var trn = tr.createTranslation(b, t ? t : "", specific);
        ar.push(trn);
        return trn;
    }
    function addMove(b, t, autosave) {
        var mov = addToArray(b, t, true, tr.getMoves());
        if (autosave)
        {
            tr.specificSave([], [mov], [], true);
        }
        return mov;
    }
    function adjustTranslation(b, t)
    {
        var beginningSpaces = /^(\s*)/gm;
        var endingSpaces = /(\s*)$/gm;
        var adjust = false;
        var prefix = "";
        var matches = b.match(beginningSpaces);
        if (matches && matches.length > 0) {
            prefix = matches[0];
            var targetMatches = t.match(beginningSpaces);
            if (!targetMatches) {
                adjust = true;
            }
            else if (targetMatches.length == 0) {
                adjust = true;
            }
            else if (targetMatches[0] != prefix)
            {
                adjust = true;
            }
        }

        matches = b.match(endingSpaces);
        var suffix = "";
        if (matches && matches.length > 0) {
            suffix = matches[0];
            var targetMatches = t.match(endingSpaces);
            if (!targetMatches) {
                adjust = true;
            }
            if (targetMatches.length == 0) {
                adjust = true;
            }
            else if (targetMatches[0] != suffix)
            {
                adjust = true;
            }
        }

        return adjust
                ? prefix + jQuery.trim(t) + suffix
                : t;
    }

    function addTranslation(b, t, specific, autosave) {
        if (!t)
            return null;
        b = tr.prepareString(b);
        var exist = removeFromArray(b, tr.getIgnores());
        var trn = addToArray(b, t, specific, tr.getTranslations());
        if (autosave)
        {
            if (exist)
                tr.globalSave();
            else
                tr.specificSave([trn], [], [], true);
        }
        return trn;
    }
    function addIgnore(b, autosave) {
        b = tr.prepareString(b);
        var exist = removeFromArray(b, tr.getTranslations());
        var ign = addToArray(b, "true", false, tr.getIgnores());
        if (autosave)
        {
            if (exist)
                tr.globalSave();
            else
                tr.specificSave([], [], [ign], true);
        }
        return ign;
    }

    function addElementToTranslate(val, b, innerEl, propName) {
        if (!b)
            b = val;
        b = tr.prepareString(b);
        if (b.length === 0 || b === " ")
            return;
        var trn = tr.getTranslation(b);
        var ignore = tr.getIgnore(b);
        if (skip)
        {
            if (trn && trn.getTarget())//filtraggio sulla traduzione
                return;
            if (ignore)
                return;//filtraggio perché non va tradotto
        }

        if (toTranslate.rootElement !== tr.closestTUElement(innerEl))
            return;

        var t = trn ? trn.getTarget() : "";
        var spec = trn ? trn.isPageSpecific() : false;
        if (trn)
            b = trn.getBase();
        toTranslate.push({
            "b": b,
            "t": t,
            "ignore": ignore,
            "specific": spec,
            "el": innerEl,
            "prop": propName
        });

    }

    function canTranslate(el, onlyFirstLevel)
    {
        if (el.nodeName === "#document" || el.nodeName === "#text")
            return false;
        toTranslate.length = 0;
        toTranslate.rootElement = el;
        tr.applyTextFunction(el, true, true, addElementToTranslate);
        return toTranslate.length > 0;
    }
    function searchNext(n)
    {
        var next = n.firstChild ? n.firstChild : n.nextSibling;
        if (next == null)
        {
            while ((n = n.parentNode) != null)
            {
                if (n.nextSibling != null)
                    return n.nextSibling;
            }

        }
        return next;
    }

    function searchPrev(n)
    {
        var prev = n.previousSibling;
        return (prev == null)
                ? n.parentNode
                : lastLeaf(prev);

        function lastLeaf(n)
        {
            return (n.lastChild)
                    ? lastLeaf(n.lastChild)
                    : n;
        }
    }
    function getFollowingElement(el, f, onlyFirstLevel)
    {
        var next = f(el);
        if (!next)
            return null;
        return (canTranslate(next, onlyFirstLevel))
                ? next
                : getFollowingElement(next, f, onlyFirstLevel)
    }
    function showOrphans()
    {
        if (orphansWindow && !orphansWindow.closed)
        {
            orphansWindow = window.open("", "orphans", "location=no, menubar=no");
        }
        else
        {
            orphansWindow = window.open("", "orphans", "location=no, menubar=no");
            orphansWindow.document.title = sOrphanWindowTitle;
            tr.addCss("/css/common.css", orphansWindow.document);
            tr.addCss("/css/translator.css", orphansWindow.document);
            tr.addCss("/css/babel.css", orphansWindow.document);
            tr.addCss("https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/themes/base/jquery-ui.css", orphansWindow.document);
            jQuery(orphansWindow).unload(function() {
                jOrphansTable = null;
                orphansWindow = null;
            });
            jOrphansTable = jQuery(
                    '<div class="jsb_notranslate inlineTranslator">' +
                    '<h1></h1>' +
                    '<div class="translatorcontentcontainer">' +
                    '<div class="notranslations"><h2/><p/></div>' +
                    '<table border="0" cellpadding="0" cellspacing="0" class="translatorcontent"><tbody/></table></div>' +
                    '</div>', orphansWindow.document)
                    .appendTo(orphansWindow.document.body);
            jQuery('h1', jOrphansTable).html(sOrphanWindowTitle);
        }

        tr.addScript(orphanScript + '?src='
                + encodeURIComponent(tr.getPageUrl()) + '&loc='
                + encodeURIComponent(tr.getTargetLocale()));
    }
    this.setOrphanData = function(strings)
    {
        if (!jOrphansTable)
            return;

        var localTranslations = [];
        var localMoves = [];
        var localIgnores = [];
        tr.parseTranslations(strings, localTranslations, localMoves, localIgnores);
        var table = jQuery('.translatorcontent>tbody', jOrphansTable);
        table.empty();

        if (localIgnores.length == 0 && localTranslations.length == 0)
        {
            jQuery('.notranslations h2', jOrphansTable).text(sNoOrphans);
        }
        else
        {
            for (var i = 0; i < localIgnores.length; i++)
            {
                addOrphanRow(localIgnores[i], false);
            }
            for (var i = 0; i < localTranslations.length; i++)
            {
                addOrphanRow(localTranslations[i], false);
            }
        }
        function addOrphanRow(trn, ignore)
        {
            var html = "<tr class='textRow'>" +
                    "<td class='copyColumn'></td>" +
                    "<td class='baseColumn'><textarea class='baseInput' readonly='readonly'/></td>" +
                    "<td class='targetColumn'><textarea class='targetInput' readonly='readonly'/></td>" +
                    "<td class='ignoreColumn'><input type='checkbox' tabindex='-1' class='ignoreInput'/ disabled='true'></td>" +
                    "<td class='specificColumn'><input type='checkbox' tabindex='-1' class='specificInput' disabled='true'/></td>" +
                    "</tr>";

            var row = jQuery(html, jOrphansTable).appendTo(table);
            jQuery('.baseInput', row).text(trn.getBase());
            jQuery('.targetInput', row).text(trn.getTarget());
            jQuery('.ignoreInput', row).attr('checked', ignore);
            jQuery('.targetInput', row).attr('checked', trn.isPageSpecific());
        }
    }
    function readParameter(s)
    {
        var matches = s.match(/%\d+%/gm);
        if (matches && matches.length == 1 && matches[0] == s)
            return parseInt(s.substring(1, s.length - 1));
        return null;
    }
    function addParameter() {
        var els = jQuery('textarea.baseInput', handle);
        els.each(function() {
            var el = jQuery(this);
            var sel = el.getSelection();
            if (sel.length == 0)
                return;
            var i = readParameter(sel.text);
            if (i)
            {
                var base = this.rowObjs.targetEl.oldNodeValue ? this.rowObjs.targetEl.oldNodeValue : this.rowObjs.targetEl.nodeValue;
                var pattern = tr.createBasePattern(el.val());
                var matches = base.match(pattern);

                el.replaceSelection(matches[i], true);
            }
            else
            {
                el.replaceSelection('%' + 0 + '%', true);
            }

            var b = el.val();
            var paramIndex = 0;
            b = b.replace(/(%\d+%)/gm, function(m) {
                return '%' + ++paramIndex + '%';
            });
            el.val(b);
            var tgtEl = jQuery(this.rowObjs.targetText);
            var t = tgtEl.val();
            t = t.replace(sel.text, '%' + 0 + '%');
            paramIndex = 0;
            t = t.replace(/(%\d+%)/gm, function(m) {
                return '%' + ++paramIndex + '%';
            });
            tgtEl.val(t);

            tr.applyTextFunction(this.rowObjs.targetEl, true, false, function(val, oldVal) {
                var trn = oldVal ? tr.getTranslation(oldVal) : null;
                if (trn) {
                    trn.setBase(b);
                    trn.setTarget(t);

                    if (isAutoSave())
                        tr.specificSave([trn], [], [], true);
                }
            });
            tr.translateTree(document.documentElement);
            if (!isAutoSave())
                setModified(true);
        });
    }

    function isValid(element) {
        if (element == null)
            return true;
        if (tr.skipTranslate(element))
            return false;
        return isValid(element.parentNode);
    }
    function getElementSafeId(el) {
        var s = "";
        if (el && el.nodeName != '#document') {
            if (el.id)
                return el.nodeName + "#" + el.id;
            s = getElementSafeId(el.parentNode);
            if (el.nodeName == "#text")
            {
                s += "-" + tr.prepareString(el.nodeValue);
            }
            else
            {
                if (s.length > 0)
                    s += '>';
                s += el.nodeName;
                if (el.nodeName != 'HTML' && el.nodeName != 'HEAD' && el.nodeName != 'BODY')
                    s += ":eq(" + jQuery(el).prevAll(el.nodeName).not('.jsb_notranslate').length + ")";
            }
        }
        return s;
    }
    function getFullPath(el) {
        var s = "";
        if (el && el.nodeName != '#document') {
            s = getFullPath(el.parentNode);
            if (s.length > 0)
                s += '>';
            s += el.nodeName;
        }
        return s;
    }
    function fitToContent(text, maxHeight) {
        text.style.height = "0px";
        var adjustedHeight = text.clientHeight;
        if (!maxHeight || maxHeight > adjustedHeight) {
            adjustedHeight = Math.max(text.scrollHeight + 20, adjustedHeight);
            if (maxHeight)
                adjustedHeight = Math.min(maxHeight, adjustedHeight);
            if (adjustedHeight > text.clientHeight) {
                text.style.height = adjustedHeight + "px";
                return adjustedHeight;
            }
        }
        return text.clientHeight;
    }

    var currentHooked = null;
    var jPop = jQuery("<div class='jsb_notranslate' style='cursor:pointer;position:absolute;display:inline-block;'><img></img></div>", getTranslatorDocument())
            .appendTo(document.body)
            .click(function() {
                translateElement(currentHooked);
            });
    jQuery("img", jPop)
            .attr("src", tr.getJsbDomain() + '/img/dotranslate.png');

    var popupTimeoutId = 0;
    this.onHover = function(evt)
    {
        evt.preventDefault();
        evt.stopPropagation();
        var overEl = tr.closestTUElement(evt.target);
        if (tr.isBaseLocale() || !isValid(overEl) || currentHooked === overEl)
            return;
        clearTimeout(popupTimeoutId);
        popupTimeoutId = setTimeout(popupHook, 1000);//un secondo
        function popupHook()
        {
            currentHooked = overEl;
            jPop.offset({
                top: currentMousePos.y - 20, //offset.top, 
                left: currentMousePos.x - 20//offset.left
                
            });
            jQuery("img", jPop)
                    .attr("title", getFullPath(currentHooked) + "\n" + _jsb("Click here to translate this element"));
            tr.moveToTop(jPop);

        }
    };

    var fixedElements = ["#text", "HTML", "BODY", "HEAD", "SCRIPT", "FROM", "FRAME", "IFRAME"];
    function canMoveElement(el)
    {
        return el ? jQuery.inArray(el.nodeName, fixedElements) == -1 : false;
    }
    function isInViewPort(jEl)
    {
        var win = jQuery(window);

        var viewport = {
            top: win.scrollTop(),
            left: win.scrollLeft()
        };
        viewport.right = viewport.left + win.width();
        viewport.bottom = viewport.top + win.height();

        var bounds = jEl.offset();
        bounds.right = bounds.left + jEl.outerWidth();
        bounds.bottom = bounds.top + jEl.outerHeight();

        return (!(viewport.right < bounds.left || viewport.left > bounds.right || viewport.bottom < bounds.top || viewport.top > bounds.bottom));

    }
    function translateElement(el, callback)
    {
        try
        {
            if (!el || !isValid(el))
                return;
            if (el.scrollIntoView && !isInViewPort(jQuery(el)))
                el.scrollIntoView();
            createTranslatorTable();
            forceChange();//scateno il change sul campo che stavo editando per recepirne i cambiamenti
            if (flasher)
                flasher.stop();
            currentJElement = jQuery(el);
            flasher = new FlashAnimator(currentJElement);
            var rootText = getFullPath(el);
            var titleEl = jQuery('.inlineTranslatorTitle', getTranslatorDocument());
            titleEl.text(sTranslating + rootText);
            var table = jQuery('.translatorcontent>tbody', handle);
            table.empty();

            function addRow(innerEl, base, target, propName, ignore, specific)
            {
                var html = "<tr class='textRow'>" +
                        "<td class='jsb_notranslate titleColumn'><span class='rowtitle'/></td>" +
                        "<td class='jsb_notranslate arrowColumn'></td>" +
                        "<td class='baseColumn'><textarea tabindex=-1 class='baseInput' readonly='readonly'/></td>" +
                        "<td class='targetColumn'><textarea class='targetInput'/></td>" +
                        "<td class ='ignoreColumn'><input type='checkbox' tabindex='-1' class='ignoreInput'/></td>" +
                        "<td class ='specificColumn'><input type='checkbox' tabindex='-1' class='specificInput'/></td>" +
                        "</tr>";
                var objs = {};
                objs.targetEl = innerEl;

                var parent = innerEl;
                while (parent && parent.parentNode != el)
                    parent = parent.parentNode;
                var row = jQuery(html, getTranslatorDocument())
                        .appendTo(table).each(function() {
                    this.associatedEl = parent;
                });
                if (canMoveElement(parent))
                {
                    var jArrowCol = jQuery(".arrowColumn", row);

                    jQuery("<img class='moveprev'/>", getTranslatorDocument())
                            .appendTo(jArrowCol)
                            .attr("src", tr.getJsbDomain() + '/img/dropup.png')
                            .click(movePrev)
                            .attr("title", sMoveBefore)
                            .each(function() {
                                this.rowObjs = objs;
                            });
                    jQuery("<img class='movenext'/>", getTranslatorDocument())
                            .appendTo(jArrowCol)
                            .attr("src", tr.getJsbDomain() + '/img/dropdown.png')
                            .attr("title", sMoveNext)
                            .click(moveNext)
                            .each(function() {
                                this.rowObjs = objs;
                            });
                }
                var jBase = jQuery('textarea.baseInput', row)
                        .text(base)
                        .each(function() {
                            this.rowObjs = objs;
                        })
                        .change(changeTargetText);
                objs.baseText = jBase[0];

                var jSpec = jQuery('input.specificInput', row)
                        .each(function() {
                            this.rowObjs = objs;
                        })
                        .click(specificPageTranslation);

                if (specific)
                {
                    jSpec.attr("checked", "checked");
                }
                jSpec.parent().attr("title", sPageTranslation);
                objs.specificInput = jSpec[0];

                var jTarget = jQuery('textarea.targetInput', row)
                        .text(target)
                        .each(function() {
                            this.rowObjs = objs;
                        })
                        .keydown(function(e) {
                            if (e.ctrlKey == true && 73 == e.keyCode) {
                                jQuery(this.rowObjs.ignoreInput).click();
                                e.preventDefault();
                                e.stopPropagation();
                            }
                        })
                        .change(changeTargetText);
                objs.targetText = jTarget[0];

                var jIgn = jQuery('input.ignoreInput', row)
                        .click(function() {
                            ignoreTargetText(this);
                        })
                        .each(function() {
                            this.rowObjs = objs;
                        });
                jIgn.parent().attr("title", sIgnoreTranslation)
                objs.ignoreInput = jIgn[0];

                if (ignore)
                {
                    jIgn.attr("checked", "checked");
                    jIgn.parent().addClass("ignoreIsActive");
                    jTarget.attr("readonly", "readonly");
                }

                var col = jQuery('.rowtitle', row);
                var lbl = getFullPath(innerEl).substr(rootText.length);
                var linkEl = null;
                if (propName) {
                    lbl += '@' + propName;
                    linkEl = innerEl;//se è un attributo, il link navigherà sul nodo di appartenenza
                }
                else {
                    linkEl = innerEl.parentNode;//se è un nodo di testo, il link navigherà sul parent del nodo di appartenenza
                }
                col.text(lbl);
                if (linkEl != el) {
                    col.attr("href", "javascript::void(0);");
                    col.attr("title", "Go to " + getFullPath(linkEl));
                    col[0].linkEl = linkEl;
                    col.click(function() {
                        translateElement(this.linkEl);
                    });
                }

            }

            toTranslate.length = 0;
            toTranslate.rootElement = el;
            tr.applyTextFunction(el, true, true, addElementToTranslate);

            if (toTranslate.length === 0)
            {
                jQuery('.notranslations', handle).show();
            }
            else
            {
                jQuery('.notranslations', handle).hide();
                for (var i = 0; i < toTranslate.length; i++)
                {
                    var item = toTranslate[i];
                    addRow(item.el, item.b, item.t, item.prop, item.ignore, item.specific);
                }
            }

            var row = jQuery("<tr class='htmlRow'><td colspan=5><textarea tabindex=-1 class='htmlInput' readonly='readonly'/></td></tr>", getTranslatorDocument())
                    .appendTo(table);
            var jInput = jQuery('textarea.htmlInput', row);
            jInput.text(el.outerHTML);
            jInput.each(function() {
                fitToContent(this);
            });

            var baseTexts = jQuery('textarea.baseInput', handle);
            var targetTexts = jQuery('textarea.targetInput', handle);
            baseTexts.each(function(n) {
                var l = fitToContent(this);
                var tgt = targetTexts[n];
                tgt.style.height = l + "px";
            });

            row = jQuery("<tr class='textRow'><td class='jsb_notranslate'></td><td></td><td><a href='#' title='Translate next'><img/></a></td></tr>", getTranslatorDocument())
                    .appendTo(table);
            jQuery('td', row).last().css("text-align", "right");
            jQuery('a', row).focus(nextSibling);
            jQuery('img', row)
                    .attr("title", btnNext.attr("title"))
                    .attr("src", btnNext.attr("src"))
                    .width(16)
                    .height(16);
            handle.defaultView();
        }
        catch (e)
        {
            alert(e);
        }
        finally
        {
            if (callback)
                callback();
        }
    }

    this.saveModified = function() {
        if (modified && confirm(sConfirmSave)) {
            tr.globalSave();
            return true;
        }

        return !modified;
    };


    this.globalSave = function() {
        if (tr.isBaseLocale())
            return;
        var localTranslations = [];
        var localIgnores = [];
        function findUsedTranslations(val, b) {
            if (!b)
                b = val;
            b = tr.prepareString(b);
            if (b.length == 0 || b == " ")
                return;
            var ign = tr.getIgnore(b);
            if (ign)
                localIgnores.push(ign);
            var trn = tr.getTranslation(b);
            if (trn && trn.getTarget())
                localTranslations.push(trn);
        }
        //calcolo le traduzioni da tenere perché presenti nella pagina
        tr.applyTextFunction(document.documentElement, true, true, findUsedTranslations);
        tr.specificSave(localTranslations, tr.getMoves(), localIgnores, false)
    }
    this.specificSave = function(translations, moves, ignores, appendToExisting) {
        var data = {};
        data.src = tr.getPageUrl();
        data.targetLocale = tr.getTargetLocale();
        data.baseLocale = tr.getBaseLocale();
        data.appendToExisting = appendToExisting;
        var index = 0;
        for (var i = 0; i < translations.length; i++) {
            var trn = translations[i];
            data["b" + index] = trn.getBase();
            data["t" + index] = trn.getTarget();
            data["p" + index] = trn.isPageSpecific();
            index++;
        }
        index = 0;
        for (var i = 0; i < moves.length; i++) {
            var trn = moves[i];
            data["bm" + index] = trn.getBase();
            data["tm" + index] = trn.getTarget();
            data["pm" + index] = trn.isPageSpecific();
            index++;
        }
        index = 0;
        for (var i = 0; i < ignores.length; i++) {
            var trn = ignores[i];
            if (!trn.used)
                continue;

            data["bi" + index] = trn.getBase();
            data["ti" + index] = trn.getTarget();
            data["pm" + index] = trn.isPageSpecific();
            index++;
        }
        sendData(tr.getJsbDomain() + translatorUrl, data);

        setModified(false);

    };
    function forceChange() {
        jQuery('textarea:first', handle).focus(); // forza l'onchange
    }
    function closeTranslatorTable() {
        if (flasher)
            flasher.stop();
        if (popup)
        {
            if (translatorWindow)
            {
                forceChange();
                translatorWindow.close();
                translatorWindow = null;
            }
        }
        else
        {
            if (handle) {
                forceChange();
                jQuery("#jsbabelTranslatorFrame").remove();
            }
        }
        handle = null;
    }
    this.autoTranslate = function() {
        if (tr.isBaseLocale())
            return;
        var missingTranslations = [];

        tr.applyTextFunction(document.documentElement, true, true, function(val, oldVal) {
            // nell'oldVal ho la stringa originale, se ho già tradotto, oppure
            // nulla,
            // e allora la stringa originale è in val
            // la traduzione, se c'è, è in val
            var b = oldVal ? oldVal : val;

            b = tr.prepareString(b);
            if (b.length == 0 || b == " ")
                return null;
            if (skipAutomaticTranslation(b))
                return null;
            for (var i = 0; i < missingTranslations.length; i++) {
                var trn = missingTranslations[i];
                if (trn.getBase() == b) {
                    continue;
                }
            }
            missingTranslations.push(tr.createTranslation(b, "", false));
        });

        function getSerializedTranslations() {
            var s = new String();
            for (var i = 0; i < missingTranslations.length; i++) {
                var t = missingTranslations[i];
                if (t.getTarget())
                    continue;
                s += t.toString(tr.TypeTextChar);
            }

            return s;
        }

        sendData(tr.getJsbDomain() + autotranslateScript,
                {
                    "targetLocale": tr.getTargetLocale(),
                    "baseLocale": tr.getBaseLocale(),
                    "src": tr.getPageUrl(),
                    "strings": getSerializedTranslations()
                },
        function() {
            tr.addScript(autotranslateScript + '?src='
                    + encodeURIComponent(tr.getPageUrl()) + '&loc='
                    + encodeURIComponent(tr.getTargetLocale()));
        });

    };
    function skipAutomaticTranslation(b)
    {
        // sia che sia nell'array delle traduzione, sia in quello da ignorare, non devo
        //applicare la traduzione automatica
        var existing = tr.getTranslation(b);
        if (existing && existing.getTarget())
            return true;
        existing = tr.getIgnore(b);
        if (existing)
            return true;
        return false;
    }
    this.addAutomaticTranslations = function(strings)
    {
        var localTranslations = [];
        var localMoves = [];
        var localIgnores = [];
        tr.parseTranslations(strings, localTranslations, localMoves, localIgnores);

        for (var i = 0; i < localTranslations.length; i++)
        {
            var trn = localTranslations[i];
            if (skipAutomaticTranslation(trn.getBase()))
                continue;
            addTranslation(trn.getBase(), trn.getTarget(), false)
            //setModified(true); già salvata dal server
        }
        for (var j = 0; j < localIgnores.length; j++)
        {
            var ign = localIgnores[j];
            if (skipAutomaticTranslation(ign.getBase()))
                continue;
            addIgnore(ign.getBase())
            //setModified(true); già salvata dal server

        }
        tr.translateTree(document.documentElement);
        openTranslatorAtFirst();
    }
    function openTranslatorAtFirst()
    {
        var el = getFollowingElement(document, searchNext, true)
        if (el)
            translateElement(el);
    }
    this.sendNewToolbarPosition = function() {
        var pos = tr.getPos();
        sendData(tr.getJsbDomain() + dataUrl, {
            "left": pos.left,
            "top": pos.top,
            "src": tr.getPageUrl()
        });
    };
    function sendData(url, obj, callback) {
        if (sendDataFrame != null)
            sendDataFrame.remove();
        sendDataFrame = jQuery("<iframe style='display:none;'/>").appendTo(jQuery(document.body));
        var doc = (sendDataFrame[0].contentWindow || sendDataFrame[0].contentDocument);
        if (doc.document)
            doc = doc.document;
        doc.open(); // altrimenti in IE il body è null
        doc.close();
        var f = jQuery('<form method="POST" enctype="application/x-www-form-urlencoded;charset=UTF-8"/>', doc).appendTo(jQuery(doc.body));
        f.attr('action', url);

        for (var prop in obj) {
            var i = jQuery('<input type="hidden" name="' + prop + '"/>');
            i.val(obj[prop]);
            f.append(i);
        }
        if (callback)
            sendDataFrame.load(callback);
        f.submit();
    }

    jQuery('*:not(".jsb_notranslate")').hover(tr.onHover);

    $(document).mousemove(function(event) {
        currentMousePos.x = event.pageX;
        currentMousePos.y = event.pageY;
    });

    jQuery(window).unload(function() {
        if (translatorWindow && !translatorWindow.closed)
            translatorWindow.close();
        tr.saveModified();
    });

    jQuery(tr.getTranslatorToolbar())
            .draggable(
                    {
                        drag: tr.adjustFlagZIndex,
                        stop: function() {
                            tr.calculateOffset();
                            tr.adjustFlagZIndex();
                            tr.sendNewToolbarPosition();
                        }
                    })
            .addClass('translateMode');
    if (tr.isBaseLocale())
        return;
    tr.translating = true;

    if (tr.isDemoMode())
        openTranslatorAtFirst();//apro subito il traduttore, senza traduzione automatica
    else
        tr.autoTranslate(); //la risposta chiamerà poi la openTranslatorAtFirst


}
jQuery(function() {
    try {
        Translator.prototype = window.__babel;
        window.__babel = new Translator();
        jQuery("form").submit(function(e) {
            if (window.__babel.translatorTableOpened())
                e.preventDefault();
        });

    }
    catch (e) {
        alert(e);
    }
});
