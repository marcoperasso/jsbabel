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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author Marco
 */
public class HTMLGenerator extends PageParser {

    private Translations translations;
    private boolean addTranslationInfo = true;
    private ArrayList<TextNode> nodesToReplace = new ArrayList<TextNode>();

    public HTMLGenerator(ProcedureController controller) {
        super(controller);
    }

    public void setTranslations(Translations translations) {
        this.translations = translations;
    }

    public Document parse(String url) throws IOException, ParserConfigurationException {
        Document doc = connect(url);
        if (addTranslationInfo) {
            doc.getElementsByTag("html").attr("xmlns:jsb", "http://www.jsbabel.com/");
        }
        doc.traverse(this);

        for (TextNode text : nodesToReplace) {
            Element el = doc.createElement("span");
            el.attr("jsb:text", text.text());
            el.text(translations.translate(text.text(), true));
            text.replaceWith(el);
        }
        return doc;
    }

    @Override
    protected void processTextNode(TextNode text) {
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
                text.text(translations.translate(text.text(), true));
            }
        }
    }

    @Override
    protected void processElement(Element currentNode) {
        for (Attribute attr : currentNode.attributes()) {
            if (isAttributeToTranslate(currentNode, attr)) {

                String prepared = Helper.prepareBaseString(attr.getValue());
                if (Helper.isBaseStringToIgnore(prepared)) {
                    continue;
                }

                if (addTranslationInfo) {
                    currentNode.attr("jsb:" + attr.getKey(), attr.getValue());
                }

                attr.setValue(translations.translate(attr.getValue(), true));
            }
        }
    }

}
