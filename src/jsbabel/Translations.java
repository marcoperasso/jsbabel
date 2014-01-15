package jsbabel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jsbabel.entities.StringType;

public class Translations implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8286789850612860777L;
    private int currentIndex;
    /**
     *
     */
    private List<Translation> translations;

    public Translations() {
        this.translations = new ArrayList<Translation>();
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public void parse(String s) {
        while (currentIndex < s.length()) {
            ParsedString b = parseString(s);
            ParsedString t = parseString(s);
            boolean pageSpecific = s.charAt(currentIndex++) == '1';
            getTranslations().add(new Translation(Helper.decodeURIComponent(b.Text), Helper.decodeURIComponent(t.Text), pageSpecific, b.Type, this));
        }
    }

    public void addMissingTranslations(Translations source) {
        List<Translation> missing = new ArrayList<Translation>();
        for (Translation t : source.getTranslations()) {
            boolean found = false;
            for (Translation t1 : translations) {
                if (t1.getBaseString().equals(t.getBaseString()) && t1.getType().equals(t.getType())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(t);
            }
        }
        translations.addAll(missing);
    }

    boolean contains(String baseText) {
        for (Translation t : translations) {
            if (baseText.equals(t.getBaseString())) {
                return true;
            }
        }
        return false;
    }

    void adjustTranslationSpaces() {
        for (Translation t : translations) {
            t.adjustTranslationSpaces();
        }
    }

    public boolean isBaseStringToIgnore(String text) {
       String prepared = Helper.prepareBaseString(text);
        if (Helper.isBaseStringToIgnore(prepared)) {
            return true;
        }
        for (Translation t : translations) {
            String s = t.applyTranslation(prepared);
            if (s != null) {
                return t.getType() == StringType.Ignore;
            }
        }
        return false;
    }

    class ParsedString {

        public String Text;
        public StringType Type;
    }

    private ParsedString parseString(String s) {
        ParsedString ps = new ParsedString();
        char ch;
        StringBuilder sb = new StringBuilder();
        while (true) {

            ch = s.charAt(currentIndex++);
            StringType t = Translation.fromSeparator(ch);
            if (t != null) {
                ps.Type = t;
                break;
            }
            sb.append(ch);
        }
        int len = Integer.parseInt(sb.toString());
        ps.Text = s.substring(currentIndex, currentIndex = currentIndex + len);
        return ps;
    }

    @Override
    public String toString() {
        return serialize();
    }

    public void resetUsedTranslations() {
        for (Translation t : translations) {
            t.setUsed(false);
        }
    }

    public String translate(String text, boolean useBaseIfNotFound) {
        String prepared = Helper.prepareBaseString(text);
        if (Helper.isBaseStringToIgnore(prepared)) {
            return useBaseIfNotFound ? text : "";
        }
        for (Translation t : translations) {
            if (t.getType() == StringType.Ignore) {
                continue;
            }
            String s = t.applyTranslation(prepared);
            if (s != null) {
                return s;
            }
        }
        return useBaseIfNotFound ? text : "";
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        if (getTranslations() != null) {
            for (Translation t : getTranslations()) {
                String bs = t.getBaseString();
                String ts = t.getTargetString();
                bs = Helper.encodeURIComponent(bs);
                ts = Helper.encodeURIComponent(ts);

                sb.append(bs.length());
                sb.append(t.getSeparator());
                sb.append(bs);
                sb.append(ts.length());
                sb.append(t.getSeparator());
                sb.append(ts);
                sb.append(t.isPageSpecific() ? '1' : '0');
            }
        }
        return sb.toString();
    }

    public void add(String base, String target, boolean pageSpecific, StringType type) {
        getTranslations().add(new Translation(base, target, pageSpecific, type, this));

    }
}
