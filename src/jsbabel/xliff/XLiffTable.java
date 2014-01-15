/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.xliff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author perasso
 */
public class XLiffTable {

    class XLiffComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
    XLiffElement latestElement = null;
    final XLiffComparator comparator = new XLiffComparator();
    List<XLiffElement> customElements = new ArrayList<XLiffElement>();

    private XLiffElement findCustomElement(String name) {
        int idx = Collections.binarySearch(customElements, name, comparator);
        if (idx >= 0) {
            return customElements.get(idx);
        }
        XLiffElement el = new XLiffElement(name, false, true, false, false, "x-html-" + name);
        customElements.add(el);
        return el;
    }

    public XLiffElement findElement(String name) {
        if (latestElement != null && (latestElement.name == null ? name == null : latestElement.name.equals(name))) {
            return latestElement;
        }
        int idx = Arrays.binarySearch(elements, name, comparator);
        latestElement = (idx < 0) ? findCustomElement(name) : elements[idx];
        return latestElement;
    }
    final static XLiffElement[] elements = new XLiffElement[]{
        //               HTML   NAME             INLINE  EMPTY   MIXED   WRAPPER XLIFFNAME     
        new XLiffElement("a", true, false, false, true, "x-html-a"),
        new XLiffElement("abbr", true, false, true, false, "x-html-abbr"),
        new XLiffElement("acronym", true, false, true, false, "x-html-acronym"),
        new XLiffElement("address", false, false, true, false, "x-html-address"),
        new XLiffElement("applet", true, false, true, true, "x-html-applet"),
        new XLiffElement("area", false, true, false, false, "x-html-area"),
        new XLiffElement("b", true, false, true, false, "bold"),
        new XLiffElement("base", false, true, false, false, "x-html-base"),
        new XLiffElement("basefont", false, true, false, false, "x-html-basefont"),
        new XLiffElement("bdo", true, false, true, false, "x-html-bdo"),
        new XLiffElement("bgsound", false, true, false, false, "x-html-bgsound"),
        new XLiffElement("big", true, false, true, false, "x-html-big"),
        new XLiffElement("blink", true, false, true, false, "x-html-blink"),
        new XLiffElement("blockquote", false, false, false, true, "x-html-blockquote"),
        new XLiffElement("body", false, false, false, true, "x-html-body"),
        new XLiffElement("br", true, true, false, false, "lb"),
        new XLiffElement("button", true, false, true, false, "x-html-button"),
        new XLiffElement("caption", false, false, true, false, "caption"),
        new XLiffElement("center", false, false, false, false, "x-html-center"),
        new XLiffElement("cite", true, false, true, false, "x-html-cite"),
        new XLiffElement("code", true, false, true, false, "x-html-code"),
        new XLiffElement("col", false, true, false, false, "x-html-col"),
        new XLiffElement("colgroup", false, false, false, true, "x-html-colgroup"),
        new XLiffElement("dd", false, false, true, false, "x-html-dd"),
        new XLiffElement("del", true, false, true, false, "x-html-del"),
        new XLiffElement("dfn", true, false, true, false, "x-html-dfn"),
        new XLiffElement("dir", false, false, false, true, "x-html-dir"),
        new XLiffElement("div", false, false, true, false, "x-html-div"),
        new XLiffElement("dl", false, false, false, true, "x-html-dl"),
        new XLiffElement("dt", false, false, true, false, "x-html-dt"),
        new XLiffElement("em", true, false, true, false, "x-html-em"),
        new XLiffElement("embed", true, false, false, false, "x-html-embed"),
        new XLiffElement("face", true, false, true, false, "x-html-face"),
        new XLiffElement("fieldset", false, false, true, true, "groupbox"),
        new XLiffElement("font", true, false, true, false, "x-html-font"),
        new XLiffElement("form", false, false, false, true, "dialog"),
        new XLiffElement("frame", false, true, false, false, "frame"),
        new XLiffElement("frameset", false, false, false, false, "x-html-frameset"),
        new XLiffElement("h1", false, false, true, false, "x-html-h1"),
        new XLiffElement("h2", false, false, true, false, "x-html-h2"),
        new XLiffElement("h3", false, false, true, false, "x-html-h3"),
        new XLiffElement("h4", false, false, true, false, "x-html-h4"),
        new XLiffElement("h5", false, false, true, false, "x-html-h5"),
        new XLiffElement("h6", false, false, true, false, "x-html-h6"),
        new XLiffElement("head", false, false, false, true, "header"),
        new XLiffElement("hr", false, true, false, false, "x-html-hr"),
        new XLiffElement("html", false, false, false, true, "x-html-html"),
        new XLiffElement("i", true, false, true, false, "italic"),
        new XLiffElement("ia", false, false, false, false, "x-html-ia"),
        new XLiffElement("iframe", true, false, true, false, "x-html-iframe"),
        new XLiffElement("img", true, true, false, false, "image"),
        new XLiffElement("input", true, true, false, false, "x-html-input"),
        new XLiffElement("ins", true, false, true, false, "x-html-ins"),
        new XLiffElement("isindex", false, true, false, false, "x-html-isindex"),
        new XLiffElement("kbd", true, false, true, false, "x-html-kbd"),
        new XLiffElement("label", true, false, true, false, "x-html-label"),
        new XLiffElement("legend", false, false, true, false, "x-html-legend"),
        new XLiffElement("li", false, false, true, false, "listitem"),
        new XLiffElement("link", false, true, false, false, "x-html-link"),
        new XLiffElement("listing", false, false, true, false, "x-html-listing"),
        new XLiffElement("map", true, false, false, false, "x-html-map"),
        new XLiffElement("marquee", false, false, true, false, "x-html-marquee"),
        new XLiffElement("menu", false, false, false, true, "menu"),
        new XLiffElement("meta", false, true, false, false, "x-html-meta"),
        new XLiffElement("nobr", true, true, false, false, "x-html-nobr"),
        new XLiffElement("noembed", false, false, false, true, "x-html-noembed"),
        new XLiffElement("noframes", false, false, false, true, "x-html-noframes"),
        new XLiffElement("noscript", false, false, false, true, "x-html-noscript"),
        new XLiffElement("object", true, false, true, true, "x-html-object"),
        new XLiffElement("ol", false, false, false, true, "x-html-ol"),
        new XLiffElement("optgroup", false, false, false, true, "x-html-optgroup"),
        new XLiffElement("option", false, false, false, false, "x-html-option"),
        new XLiffElement("p", false, false, true, false, "x-html-p"),
        new XLiffElement("param", true, true, false, false, "x-html-param"),
        new XLiffElement("plaintext", false, false, false, false, "x-html-plaintext"),
        new XLiffElement("pre", false, false, true, false, "x-html-pre"),
        new XLiffElement("q", true, false, true, false, "x-html-q"),
        new XLiffElement("rb", true, false, true, false, "x-html-rb"),
        new XLiffElement("rbc", true, false, true, false, "x-html-rbc"),
        new XLiffElement("rp", true, false, true, false, "x-html-rp"),
        new XLiffElement("rt", true, false, true, false, "x-html-rt"),
        new XLiffElement("rtc", true, false, true, false, "x-html-rtc"),
        new XLiffElement("ruby", true, false, true, false, "x-html-ruby"),
        new XLiffElement("s", true, false, true, false, "x-html-s"),
        new XLiffElement("samp", true, false, true, false, "x-html-samp"),
        new XLiffElement("script", false, false, false, false, "x-html-script"),
        new XLiffElement("select", true, false, false, true, "x-html-select"),
        new XLiffElement("small", true, false, true, false, "x-html-small"),
        new XLiffElement("spacer", true, true, false, false, "x-html-spacer"),
        new XLiffElement("span", true, false, true, false, "x-html-span"),
        new XLiffElement("strike", true, false, true, false, "x-html-strike"),
        new XLiffElement("strong", true, false, true, false, "x-html-strong"),
        new XLiffElement("style", false, false, false, false, "x-html-style"),
        new XLiffElement("sub", true, false, true, false, "x-html-sub"),
        new XLiffElement("sup", true, false, true, false, "x-html-sup"),
        new XLiffElement("symbol", true, false, true, false, "x-html-symbol"),
        new XLiffElement("table", false, false, false, true, "table"),
        new XLiffElement("tbody", false, false, false, true, "x-html-tbody"),
        new XLiffElement("td", false, false, true, false, "cell"),
        new XLiffElement("textarea", true, false, false, false, "x-html-textarea"),
        new XLiffElement("tfoot", false, false, false, true, "footer"),
        new XLiffElement("th", false, false, true, false, "x-html-th"),
        new XLiffElement("thead", false, false, false, true, "x-html-thead"),
        new XLiffElement("title", false, false, false, false, "x-html-title"),
        new XLiffElement("tr", false, false, false, true, "row"),
        new XLiffElement("tt", true, false, true, false, "x-html-tt"),
        new XLiffElement("u", true, false, true, false, "underlined"),
        new XLiffElement("ul", false, false, false, true, "x-html-ul"),
        new XLiffElement("var", true, false, true, false, "x-html-var"),
        new XLiffElement("wbr", true, true, false, false, "x-html-wbr"),
        new XLiffElement("xml", false, false, true, true, "x-html-xml"),
        new XLiffElement("xmp", false, false, false, false, "x-html-xmp")
    };
}
