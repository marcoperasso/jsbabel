function setCookie(n, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value)
            + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = n + "=" + c_value;
}
function getCookie(n) {
    var x, y, cookies = document.cookie.split(";");
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var idx = cookie.indexOf("=");
        x = cookie.substr(0, idx);
        y = cookie.substr(idx + 1);
        x = x.replace(/^\s+|\s+$/g, "");
        if (x == n) {
            return unescape(y);
        }
    }
    return "";
}
function Babel() {
    var jsbHost = "http://www.jsbabel.com";
    var dropUpImg = '/img/dropup.png';
    var dropDownImg = '/img/dropdown.png';
    var stringScript = '/servlet/string';
    var translatorScript = '/js/translator.js';
    var translations = [];
    var missingTranslations = [];
    var moves = [];
    var ignores = [];
    var jsbDomain = (location.host.indexOf('localhost') >= 0 || location.host.indexOf('127.0.0.1') >= 0)
            ? "http://" + location.host : jsbHost;
    var trnCnt = null;

    var tr = this;
    var paramRegExp = /(\%\d+\%)/gm;
    var trimRegExp = /[\r\n\s]+/gm;
    var baseRegExp = /[-[\]{}()*+?.,\\^$|#]|(\%\d+\%)/gm;
    var offset = 0;
    var anchor = 'C';
    var persistencyManager = typeof openDatabase === "undefined" ? null : new PersistenceManager();
    var trnDataVersion = 0;
    var trnStringsVersion = 0;
    var jqueryscript = "https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js";
    var jqueryuiscript = "https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/jquery-ui.min.js";
    var demoMode = false;
    var pageDomain = null;
    var pageUrl = null;
    this.TypeTextChar = '*';
    this.TypeMoveChar = '?';
    this.TypeImageChar = '!';
    this.TypeIgnoreChar = ':';
    this.translating = false;

    function elementFromPoint(x, y)
    {
        if (!document.elementFromPoint)
            return null;
        var doc = jQuery(document);
        x -= doc.scrollLeft();
        y -= doc.scrollTop();

        return document.elementFromPoint(x, y);
    }


    var targetLocale = getCookie("wltargetLocale");
    var baseLocale = getCookie("wlbaseLocale");
    this.isDemoMode = function() {
        return demoMode;
    };
    this.createBasePattern = function(s)
    {
        return s.replace(baseRegExp, function(m) {
            return m.length == 1 ? '\\' + m : "(.*)";
        });
    };
    function Translation(aBase, aTarget, isPageSpecific) {
        var base = aBase;
        var target = aTarget;
        var pageSpecific = isPageSpecific;
        var pattern = null;

        this.getBase = function() {
            return base;
        };
        this.setBase = function(aBase) {
            base = aBase;
            pattern = null;
        };
        this.isPageSpecific = function()
        {
            return pageSpecific;
        };
        this.setPageSpecific = function(isPageSpecific)
        {
            pageSpecific = isPageSpecific;
        };
        this.getBasePattern = function() {
            if (!pattern)
                pattern = tr.createBasePattern(base);
            return pattern;
        };
        this.getTarget = function() {
            return target;
        };
        this.setTarget = function(aTarget) {
            target = aTarget;
        };
        this.toString = function(separator) {
            var b = encodeURIComponent(base);
            var t = encodeURIComponent(target);
            return b.length + separator + b + t.length + separator + t + (pageSpecific ? '1' : '0');
        };

    }
    this.setTranslationData = function(data, dataVersion, strings, stringsVersion) {
        var persist = (trnDataVersion != dataVersion) || (trnStringsVersion != stringsVersion);
        trnDataVersion = dataVersion;
        trnStringsVersion = stringsVersion;
        if (data)
            tr.applyTranslationData(data);
        if (strings)
        {
            tr.parseTranslations(strings, translations, moves, ignores);
            tr.translateTree(document.documentElement);
        }

        if (persist && persistencyManager)
            persistencyManager.persistTranslationData(
                    location.pathname, tr.getTargetLocale(), data, trnDataVersion, strings, trnStringsVersion);

    };

    this.applyTranslationData = function(trnData) {
        tr.setBaseLocale(trnData.bl);
        tr.clearFlags();
        trnData.ld.sort(function(a, b) {
            if (a.l == targetLocale)
                return -1;
            if (b.l == targetLocale)
                return 1;
            return 0;
        });//il target locale in testa
        for (var i = 0; i < trnData.ld.length; i++) {
            var flag = trnData.ld[i];
            tr.addFlag(flag.l, flag.u, flag.t);
        }
        trnCnt.style.display = "block";
        tr.setPos(trnData.a, trnData.x, trnData.y);
    };
    this.setThis = function(th) {
        tr = th;
    };

    this.getTranslatorToolbar = function() {
        return trnCnt;
    };
    this.getJsbDomain = function() {
        return jsbDomain;
    };

    this.clearFlags = function()
    {
        jQuery(trnCnt).empty();
    };
    this.addFlag = function(loc, imgUrl, title) {
        if (!loc)
            return;
        if (targetLocale == "")
            targetLocale = loc;
        var img = document.createElement("img");
        img.src = jsbDomain + imgUrl;
        img.title = decodeURIComponent(title) + _jsb(" - Powered by JSBABEL");
        img.className = "jsb_imageButton jsb_notranslate";
        img.onclick = function() {
            tr.setTargetLocale(loc);
            location.reload();
        };

        trnCnt.appendChild(img);
    };

    this.addAndWait = function(condFunc, script, endFunc, targetDoc) {
        try {
            if (!condFunc())
                tr.addScript(script, targetDoc);

            function execEndFunc() {
                if (condFunc())
                    endFunc();
                else
                    setTimeout(execEndFunc, 1);
            }
            execEndFunc();
        } catch (e) {
            alert(e);
        }
    };
    this.addTranslatorScripts = function(demo) {
        demoMode = demo;
        tr.addAndWait(
                function() {
                    return typeof (jQuery) !== 'undefined';
                },
                jqueryscript,
                function() {
                    tr.addAndWait(
                            function() {
                                return typeof (jQuery.ui) !== 'undefined'
                                        && typeof (jQuery.ui.draggable) != 'undefined';
                            },
                            jqueryuiscript,
                            function() {
                                tr.addScript(translatorScript);
                            });
                });
    };
    this.addScript = function(src, targetDoc) {
        if (!targetDoc)
            targetDoc = document;
        var h = targetDoc.getElementsByTagName("head")[0];
        var scriptNode = targetDoc.createElement('script');
        scriptNode.type = 'text/javascript';
        scriptNode.src = src.indexOf("http") == 0 ? src : jsbDomain + src;
        h.appendChild(scriptNode);
    };
    this.addCss = function(src, targetDoc) {
        if (!targetDoc)
            targetDoc = document;
        var h = targetDoc.getElementsByTagName("head")[0];
        var cssNode = targetDoc.createElement('link');
        cssNode.type = "text/css";
        cssNode.rel = "stylesheet";
        cssNode.href = src.indexOf("http") == 0 ? src : jsbDomain + src;

        h.appendChild(cssNode);
    };
    this.setPos = function(aAnchor, left, top) {
        anchor = aAnchor;
        if ((anchor != 'L' || left > 0) && top > 0) {
            offset = left;
            trnCnt.style.top = top + "px";
        }
        onResize();
        this.addDropdown();
    };
    this.getPos = function() {
        return {
            "left": offset,
            "top": parseInt(trnCnt.style.top)
        };
    };

    this.addDropdown = function() {
        var imgH = 20;
        var h = imgH;
        var max = imgH;
        var isOpen = false;
        function increase() {
            if (h < max) {
                h += 10;
                trnCnt.style.height = h + "px";
                setTimeout(increase, 10);
            } else {
                isOpen = true;
                img.attr("src", jsbDomain + dropUpImg);
            }
        }
        function decrease() {
            if (h > imgH) {
                h -= 10;
                trnCnt.style.height = h + "px";
                setTimeout(decrease, 10);
            } else {
                isOpen = false;
                img.attr("src", jsbDomain + dropDownImg);
            }
        }
        function open() {
            var img1 = trnCnt.childNodes[trnCnt.childNodes.length - 1];
            max = img1.offsetTop + imgH;
            setTimeout(increase, 2);
        }
        function close() {
            setTimeout(decrease, 2);
        }
        var img = jQuery("<img class='jsb_dropdown jsb_notranslate jsb_clickable'/>")
                .attr("src", jsbDomain + dropDownImg)
                .click(function() {
                    if (isOpen)
                        close();
                    else
                        open();
                })
                .insertBefore(trnCnt.childNodes[0]);
        jQuery(trnCnt).mouseleave(close);
    };
    this.getBaseLocale = function() {
        return baseLocale;
    };
    this.setBaseLocale = function(l) {
        baseLocale = l;
        setCookie("wlbaseLocale", baseLocale, 365);
    };
    this.setTargetLocale = function(l) {
        targetLocale = l;
        setCookie("wltargetLocale", l, 365);
    };
    this.getTargetLocale = function() {
        return targetLocale;
    };
    this.isBaseLocale = function() {
        return baseLocale == targetLocale;
    };
    this.getTranslations = function() {
        return translations;
    };
    this.getMoves = function() {
        return moves;
    };
    this.getIgnores = function() {
        return ignores;
    };
    this.parseTranslations = function(s, localTranslations, localMoves, localIgnores) {
        localTranslations.length = 0;
        localMoves.length = 0;
        localIgnores.length = 0;
        var currentIndex = 0;
        while (currentIndex < s.length) {
            var typeChar = null;
            var b = parseString(s);
            var t = parseString(s);
            var specific = s.charAt(currentIndex++) == '1';
            if (typeChar == tr.TypeMoveChar)
                localMoves.push(tr.createTranslation(decodeURIComponent(b), decodeURIComponent(t), specific));
            else if (typeChar == tr.TypeTextChar)
                localTranslations.push(tr.createTranslation(decodeURIComponent(b), decodeURIComponent(t), specific));
            else if (typeChar == tr.TypeIgnoreChar)
                localIgnores.push(tr.createTranslation(decodeURIComponent(b), decodeURIComponent(t), specific));
        }

        function parseString(s) {
            var ch;
            var sb = "";
            while (true) {
                if (currentIndex >= s.length)
                    throw "Invalid translation string:\r\n" + s;
                ch = s.charAt(currentIndex++);//il separatore è * per le traduzioni, ? per gli spostamenti di dom
                if (ch == tr.TypeTextChar || ch == tr.TypeMoveChar || ch == tr.TypeIgnoreChar)
                {
                    typeChar = ch;
                    break;
                }
                sb += ch;
            }
            var l = parseInt(sb);
            return s.substring(currentIndex, currentIndex = currentIndex + l);
        }
    };
    this.getIgnore = function(s) {
        var toTranslate = tr.prepareString(s);
        if (toTranslate.length == 0 || toTranslate == " ")
            return null;
        for (var i = 0; i < ignores.length; i++) {
            var trn = ignores[i];
            if (trn.getBase() == toTranslate) {
                return trn;
            }
        }
        return null;
    };
    this.getTranslation = function(s) {
        var toTranslate = tr.prepareString(s);
        if (toTranslate.length == 0 || toTranslate == " ")
            return null;
        for (var i = 0; i < translations.length; i++) {
            var t = translations[i];
            var matches = toTranslate.match(t.getBasePattern());
            if (matches != null && matches.length > 0
                    && matches[0] == toTranslate) {
                t.matches = matches;
                return t;
            }
        }

        missingTranslations.push(toTranslate);
        return null;

    };

    this.translate = function(s) {
        var trn = tr.getTranslation(s);
        if (!trn)
            return null;
        return trn.getTarget().replace(paramRegExp, replaceParam);

        function replaceParam(m) {
            try {
                var idx = parseInt(m.substring(1, m.length - 1));
                if (idx >= trn.matches.length || idx <= 0)
                    return m;
                return trn.matches[idx];
            } catch (e) {
                return m;
            }
        }
    };
    window._jsb = function(s) {
        var s1 = tr.translate(s);
        return s1 == null ? s : s1;
    };
    this.skipTranslate = function(el) {
        function isNoTranslate(jEl)
        {
            return jEl.size() != 0 && (jEl.hasClass('jsb_notranslate') || isNoTranslate(jEl.parent()));
        }

        return isNoTranslate(jQuery(el));
    };

    this.applyTextFunction = function(el, recursive, includeScript, f) {
        var added = 0;

        try
        {
            if (tr.skipTranslate(el))
                return;
            var name = el.nodeName;
            if (!name)
                return;
            if (name == "STYLE" || name == "TEXTAREA")
                return;

            var fOut = null;
            if (name == "#text")
            {
                if (fOut = f(el.nodeValue, el.oldNodeValue, el)) {
                    try {
                        if (!el.oldNodeValue)
                            el.oldNodeValue = el.nodeValue;
                    } catch (e) {
                        // bug di IE
                    }
                    el.nodeValue = fOut;
                }

            }
            else if (name == "SCRIPT")
            {
                if (includeScript)
                {
                    var text;
                    var src;
                    if (el.src)
                    {
                        //parso solo gli script che provengono dal dominio del sito
                        if (el.src.indexOf(tr.getPageDomain(), 0) == 0)
                        {
                            src = el.src.substr(tr.getPageDomain().length)
                            jQuery.ajax({
                                url: el.src,
                                success: function(data) {
                                    text = data;
                                },
                                async: false,
                                cache: true,
                                dataType: "text"
                            });
                        }
                    }
                    else
                    {
                        src = location.href.substr(tr.getPageDomain().length)
                        text = el.innerHTML;
                    }
                    if (text)
                    {
                        var reg = /_jsb\s*\(\s*"((.(?!"\s*\)))*.)/gm;
                        var tokens;
                        while ((tokens = reg.exec(text)))
                            f(tokens[1], tokens[1], el, "src:" + src);
                    }
                }
                return;
            }
            else if (name == "INPUT") {
                var type = el.type;
                if (type == "hidden" || type == "text" || type == "password"
                        || type == "file")
                    return;

                if (typeof el.value !== "undefined"
                        && (fOut = f(el.value, el.jsbOldValue, el, "value"))) {
                    if (!el.jsbOldValue)
                        el.jsbOldValue = el.value;
                    el.value = fOut;
                }
            }
            function processAttribute(attrName)
            {
                var oldAttr = "jsbOld" + attrName;
                if ((fOut = f(el[attrName], el[oldAttr], el, attrName))) {
                    if (!el[oldAttr])
                        el[oldAttr] = el[attrName];
                    el[attrName] = fOut;
                }
            }
            function isValidAttribute(attr)
            {
                if ("alt" === attr
                        || "title" === attr
                        || "abbr" === attr
                        || "abbr" === attr
                        || "accesskey" === attr
                        || "label" === attr
                        || "placeholder" === attr
                        || "prompt" === attr
                        || "standby" === attr
                        || "summary" === attr)
                    return true;

                if ("content" === attr) {
                    if ("META" !== el.nodeName) {
                        return false;
                    }

                    var httpEquiv = el.httpEquiv;
                    if (httpEquiv)
                        return "keywords" === httpEquiv.toLowerCase();
                    var aName = el["name"];
                    if (!aName)
                        return false;
                    aName = aName.toLowerCase();
                    if ("generator" === aName || "author" === aName || "progid" === aName || "date" === aName)
                        return false;
                    return true;
                }

                if ("value" === attr) {
                    return "BUTTON" === el.nodeName || "INPUT" === el.nodeName;
                }
                return false;
            }
            if (el.attributes)
            {
                for (var i = 0, attrs = el.attributes, l = attrs.length; i < l; i++) {
                    var attr = attrs.item(i).nodeName;
                    if (isValidAttribute(attr))
                        processAttribute(attr);
                }
            }


            if (recursive)
                for (var i = 0; i < el.childNodes.length; i++)
                    tr.applyTextFunction(el.childNodes[i], recursive, includeScript, f);
        }
        catch (e)
        {

        }

    };
    this.prepareString = function(s) {
        s = s.replace(trimRegExp, " ");
        return s;
    };

    this.createTranslation = function(b, t, specific)
    {
        return new Translation(b, t, specific);
    };
    function onResize() {
        if (trnCnt && jQuery(trnCnt).is(':visible')) {
            if (anchor == 'C') {
                var left = Math.round((getWindowWidth() / 2)) - offset;
                trnCnt.style.left = left + "px";
            } else if (anchor == 'R') {
                var left = getWindowWidth() - offset;
                trnCnt.style.right = left + "px";
            } else {
                trnCnt.style.left = offset + "px";
            }
            tr.adjustFlagZIndex();
        }
    }
    this.moveToTop = function(jElToMove)
    {
        var max = 0;
        function calcMax(x, y)
        {
            function calcMaxForEl(el)
            {
                var n = parseInt(el.style.zIndex);
                if (!isNaN(n))
                    max = Math.max(max, n);
                n = parseInt(jQuery(el).css("z-index"));
                if (!isNaN(n))
                    max = Math.max(max, n);

                if (el.parentNode && el.parentNode.style)
                    calcMaxForEl(el.parentNode);
            }
            var el = elementFromPoint(x, y);
            if (el && el.style)
            {
                calcMaxForEl(el);
            }
        }

        var pos = jElToMove.offset();
        var w = jElToMove.width();
        var h = jElToMove.height();
        jElToMove.hide();//lo nascondo, altrimenti la elementFromPoint lo becca

        //calcMax(pos.left, pos.top);//topleft
        //calcMax(pos.left + w, pos.top);//topright
        //calcMax(pos.left, pos.top + h);//bottomleft
        //calcMax(pos.left + w, pos.top + h);//bottomright

        calcMax(pos.left + w / 2, pos.top + h / 2);//center
        jElToMove[0].style.zIndex = max + 1;
        jElToMove.show();
    };
    this.adjustFlagZIndex = function()
    {
        tr.moveToTop(jQuery(trnCnt));
    };
    function onMessage(e) {
        if (e.originalEvent.data == "reload")
            location.reload();
    }

    this.translateTree = function(root) {
        missingTranslations.length = 0;
        //prima applico le inversioni, altrimenti non funziona il check
        for (var i = 0; i < moves.length; i++) {
            var t = moves[i];
            tr.applyMove(t.getBase(), t.getTarget());
        }
        //poi applico le traduzioni
        tr.applyTextFunction(root, true, false, function(val, oldVal, el) {
            var toTranslate = oldVal ? oldVal : val;
            if (!toTranslate)
                return null;
            return tr.translate(toTranslate);
        });

    };
    this.applyMove = function(key, val)
    {
        if (val == 0)
            return;
        var idx = key.indexOf("-");
        if (idx == -1)
            return;
        var selector = key.substring(0, idx);
        var el = jQuery(selector);
        if (el && el.size() == 1)
        {
            var node = el[0];
            var text = key.substring(idx + 1);
            for (var i = 0; i < node.childNodes.length; i++)
            {
                var childEl = node.childNodes[i];
                if (!childEl.nodeValue)
                    continue;
                var toTranslate = tr.prepareString(childEl.nodeValue);
                if (toTranslate == text)
                {
                    if (!childEl.jsbOffset) //solo se non l'ho già applicato!
                        tr.offsetElement(childEl, val);
                    break;
                }
            }
        }
    };

    this.offsetElement = function(el, offset)
    {
        if (!el)
            return 0;
        if (offset == 0)
        {
            if (!el.jsbOffset)
                el.jsbOffset = 0;
            return el.jsbOffset;
        }
        var n = el;
        var abs = Math.abs(offset);
        for (var i = 0; i < abs; i++)
            if (offset > 0)
                n = n.nextSibling;
            else
                n = n.previousSibling;
        if (offset > 0)
            jQuery(el).insertAfter(n);
        else
            jQuery(el).insertBefore(n);

        if (!el.jsbOffset)
            el.jsbOffset = offset;
        else
            el.jsbOffset += offset;
        return el.jsbOffset;
    };

    this.getPageDomain = function()
    {
        if (!pageDomain)
            calculatePageInfo();
        return pageDomain;
    };
    this.getPageUrl = function() {
        if (!pageUrl)
            calculatePageInfo();
        return pageUrl;
    };
    function calculatePageInfo()
    {
        //la variabile demoMode non è ancora inizializzata
        var idx = location.href.indexOf("htmlInjector?", 0);
        if (idx > -1)//ho trovato la parolina che mi dice che sono in demo
            idx = location.href.indexOf("src=", idx);// 4 caratteri
        if (idx > -1)
        {
            pageUrl = location.href.substring(idx + 4);
            var l = pageUrl.indexOf('/', 7); //salto http://
            if (l == -1)
                l = pageUrl.length;

            pageDomain = pageUrl.substr(0, l);
        }
        else
        {
            pageUrl = location.href;
            pageDomain = "http://" + location.host;
        }
    }


    this.calculateOffset = function() {
        var left = parseInt(trnCnt.style.left);
        if (anchor == 'C')
            offset = Math.round((getWindowWidth() / 2) - left);
        else if (anchor == 'R')
            offset = getWindowWidth() - left;
        else
            offset = left;
    };

    function getWindowWidth() {
        return jQuery(window).width();
    }

    function attachTranslator() {

        jQuery(window).bind("resize", onResize);
        jQuery(window).bind("message", onMessage);

        trnCnt = document.createElement("div");
        trnCnt.style.display = "none";
        document.body.appendChild(trnCnt);
        trnCnt.style.left = "10px";
        trnCnt.style.top = "10px";
        trnCnt.className = "jsb_translator jsb_notranslate";

        if (persistencyManager) {
            persistencyManager.open(function() {
                persistencyManager.loadTranslationData(location.pathname, targetLocale,
                        function(data, dataVersion, strings, stringsVersion) {
                            tr.setTranslationData(data, dataVersion, strings, stringsVersion);
                            tr.addStringScript();
                        });
            });
        } else {
            tr.addStringScript();
        }
        tr.addCss("/css/babel.css");
    }
    this.addStringScript = function() {
        tr.addScript(stringScript + '?src='
                + encodeURIComponent(tr.getPageUrl())
                + '&loc=' + encodeURIComponent(targetLocale)
                + '&vData=' + trnDataVersion
                + '&vStrings=' + trnStringsVersion);
    };
    this.addAndWait(function() {
        return typeof (jQuery) !== 'undefined';
    },
            jqueryscript,
            function() {
                jQuery(attachTranslator);
            });

    function PersistenceManager() {
        var data = "";
        var strings = "";
        var db = null;
        function errorHandler(transaction, error) {
            if (error.code == 1) {
                console.log("JSBABEL ERROR: DB Table already exists")
            } else {
                // Error is a human-readable string.
                console.log('JSBABEL ERROR: ' + error.message + ' (Code ' + error.code + ')');
            }
            return false;

        }
        this.open = function(readyCallback) {
            db = openDatabase('JSBABELDATA', '1.0', 'JSBABEL translations', 2097152);//2 * 1024 * 1024

            db.transaction(function(transaction) {
                transaction
                        .executeSql(
                                "CREATE TABLE IF NOT EXISTS TRANSLATIONS (PAGE TEXT NOT NULL, LOCALE TEXT NOT NULL, VERSION INTEGER NOT NULL, STRINGS TEXT NOT NULL, PRIMARY KEY(PAGE, LOCALE))",
                                [], createSettings, errorHandler);
                function createSettings() {
                    transaction
                            .executeSql(
                                    "CREATE TABLE IF NOT EXISTS SETTINGS (ID INTEGER NOT NULL, VERSION INTEGER NOT NULL, DATA TEXT NOT NULL, PRIMARY KEY(ID))",
                                    [], readyCallback, errorHandler);
                }

            });
        };
        this.loadTranslationData = function(page, locale, callback) {
            // prima seleziono i settings
            db.transaction(function(transaction) {
                transaction.executeSql(
                        'SELECT DATA, VERSION FROM SETTINGS WHERE ID=0', [],
                        onSelectSettings, errorHandler);
            });
            // se trovo i settings, seleziono le traduzioni
            function onSelectSettings(transaction, results) {
                if (results.rows.length == 1) {
                    try
                    {
                        data = JSON.parse(results.rows.item(0).DATA);
                        trnDataVersion = results.rows.item(0).VERSION;
                    }
                    catch (e)
                    {
                        data = "";
                        trnDataVersion = 0;
                    }
                    transaction.executeSql(
                            'SELECT STRINGS, VERSION FROM TRANSLATIONS WHERE PAGE=? AND LOCALE=?',
                            [page, locale], onSelectTranslations,
                            errorHandler);
                } else
                    callback("", 0, "", 0);
            }

            // se trovo le traduzioni, chiamo la callback
            function onSelectTranslations(transaction, results) {
                if (results.rows.length == 1)
                {
                    strings = results.rows.item(0).STRINGS;
                    trnStringsVersion = results.rows.item(0).VERSION;
                }
                callback(data, trnDataVersion, strings, trnStringsVersion);

            }

            function errorHandler(transaction, error) {
                console.log(error);
                callback("", 0, "", 0);
                return false;
            }
        };
        function nullDataHandler() {

        }
        this.persistTranslationData = function(page, locale, newData, newDataVersion, newStrings, newStringsVersion) {
            if (newData)//solo se ho nuovi dati
            {
                if (data) {
                    updateSettings();
                } else {
                    insertSettings();
                }
            }
            // le stringhe le salvo solo per le lingue da tradurre, quelle della
            // lingua base sono vuote
            if (locale == tr.getBaseLocale())
                return;
            if (newStrings)
            {
                if (strings) {
                    updateTranslations();
                } else {
                    insertTranslations();
                }
            }
            function insertSettings() {
                db.transaction(function(transaction) {
                    delete Array.prototype.toJSON;// elimina buco della
                    // stringify con gli array
                    transaction.executeSql(
                            'INSERT INTO SETTINGS (ID, VERSION, DATA) VALUES(0, ?, ?)',
                            [newDataVersion, JSON.stringify(newData)],
                            nullDataHandler, errorHandler);
                });
            }
            function updateSettings() {
                db.transaction(function(transaction) {
                    delete Array.prototype.toJSON;// elimina buco della
                    // stringify con gli array
                    transaction.executeSql(
                            'UPDATE SETTINGS SET VERSION=?, DATA=? WHERE ID=0',
                            [newDataVersion,
                                JSON.stringify(newData)],
                            nullDataHandler, errorHandler);
                });
            }

            function insertTranslations() {
                db
                        .transaction(function(transaction) {
                            transaction
                                    .executeSql(
                                            'INSERT INTO TRANSLATIONS (PAGE, LOCALE, VERSION, STRINGS) VALUES(?, ?, ?, ?)',
                                            [page, locale, newStringsVersion, newStrings],
                                            nullDataHandler, errorHandler);
                        });
            }
            function updateTranslations() {
                db
                        .transaction(function(transaction) {
                            transaction
                                    .executeSql(
                                            'UPDATE TRANSLATIONS SET VERSION=?, STRINGS=? WHERE PAGE=? AND LOCALE=?',
                                            [newStringsVersion, newStrings, page, locale],
                                            nullDataHandler, errorHandler);
                        });
            }
        };
    }

}

__babel = new Babel();
