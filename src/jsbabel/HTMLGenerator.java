/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import static jsbabel.PageParser.isAttributeToTranslate;
import jsbabel.entities.StringType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 *
 * @author Marco
 */
public class HTMLGenerator extends PageParser {

    private Translations translations;
    private boolean addTranslationInfo = true;
    private boolean replaceTranslations = true;
    private ArrayList<TextNode> nodesToReplace = new ArrayList<TextNode>();

    public HTMLGenerator(ProcedureController controller, Translations translations, boolean addTranslationInfo, boolean replaceTranslations) {
        super(controller);
        this.translations = translations;
        this.addTranslationInfo = addTranslationInfo;
        this.replaceTranslations = replaceTranslations;
    }

    public Document parse(String url) throws IOException, ParserConfigurationException {
        translations.resetUsedTranslations();
        Document doc = connect(url);
        if (addTranslationInfo) {
            Elements head = doc.getElementsByTag("html");
            head.attr("xmlns:jsb", "http://www.jsbabel.com/");
        }
        doc.traverse(this);

        for (TextNode text : nodesToReplace) {
            Element el = doc.createElement("span");
            el.attr("jsb:text", text.text());
            el.text(getTargetString(text.text()));
            text.replaceWith(el);
        }

        Element hiddenDiv = null;
        //ciclo su tutte le traduzioni, quelle che non sono state usate le metto in TU a parte
        if (addTranslationInfo) {

            for (Translation t : translations.getTranslations()) {
                if (t.isUsed() || t.getType() == StringType.Ignore) {
                    continue;
                }
                if (hiddenDiv == null)
                {
                    hiddenDiv = doc.body().appendElement("div"); 
                    hiddenDiv.attr("style", "display:none");
                    hiddenDiv.attr("name", "JSBABEL_additional_text");
                }
                Element div = hiddenDiv.appendElement("span");
                div.attr("jsb:text", t.getBaseString());
                div.text(replaceTranslations ? t.getTargetString() : t.getBaseString());
            }
        }
        return doc;
    }

    private String getTargetString(String text) {
        return replaceTranslations ? translations.translate(text, true) : text;
    }

    @Override
    protected void processTextNode(TextNode text
    ) {
        final Node parent = text.parent();
        if (skipContent(parent)) {
            return;
        }
        if (text.text() != null) {
            String prepared = Helper.prepareBaseString(text.text());
            if (Helper.isBaseStringToIgnore(prepared)) {
                return;
            }
            if (addTranslationInfo) {
                nodesToReplace.add(text);
            } else {
                text.text(getTargetString(text.text()));
            }
        }
    }

    @Override
    protected void processElement(Element currentNode
    ) {
        for (Attribute attr : currentNode.attributes()) {
            if (isAttributeToTranslate(currentNode, attr)) {

                String prepared = Helper.prepareBaseString(attr.getValue());
                if (Helper.isBaseStringToIgnore(prepared)) {
                    continue;
                }

                if (addTranslationInfo) {
                    currentNode.attr("jsb:" + attr.getKey(), attr.getValue());
                }

                attr.setValue(getTargetString(attr.getValue()));
            }
        }
    }

}
