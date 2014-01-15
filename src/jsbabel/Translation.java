package jsbabel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jsbabel.entities.StringType;

public class Translation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3380429953674828550L;
    private static final char IMAGE_CHAR = '!';
    private static final char MOVE_CHAR = '?';
    private static final char IGNORE_CHAR = ':';
    private static final char TEXT_CHAR = '*';
    private static Pattern ParamRegExp = Pattern.compile("(\\%\\d+\\%)");
    private static Pattern BeginningSpaces = Pattern.compile("^(\\s*)");
    private static Pattern EndingSpaces = Pattern.compile("(\\s*)$");
    private String baseString;
    private String targetString;
    private StringType type;
    private boolean pageSpecific;
    private Pattern basePattern = null;
    private boolean used;

    public Translation() {
    }

    public Translation(String b, String t, boolean pageSpecific, StringType type, Translations owner) {
        this.baseString = b;
        this.targetString = t;
        this.pageSpecific = pageSpecific;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Translation{" + "baseString=" + baseString + ", targetString=" + targetString + ", type=" + type + ", pageSpecific=" + pageSpecific + '}';
    }

    public String getBaseString() {
        return baseString;
    }

    public void setBaseString(String baseString) {
        this.baseString = baseString;
    }

    public String getTargetString() {
        return targetString;
    }

    public void setTargetString(String targetString) {
        this.targetString = targetString;
    }

    public StringType getType() {
        return type;
    }

    public void setType(StringType type) {
        this.type = type;
    }

    public char getSeparator() {
        switch (type) {
            case Image:
                return IMAGE_CHAR;
            case Move:
                return MOVE_CHAR;
            case Ignore:
                return IGNORE_CHAR;
            case Text:
            default:
                return TEXT_CHAR;
        }
    }

    public static StringType fromSeparator(char ch) {
        switch (ch) {
            case IMAGE_CHAR:
                return StringType.Image;
            case MOVE_CHAR:
                return StringType.Move;
            case IGNORE_CHAR:
                return StringType.Ignore;
            case TEXT_CHAR:
                return StringType.Text;
            default:
                return null;
        }
    }

    /**
     * @return the pageSpecific
     */
    public boolean isPageSpecific() {
        return pageSpecific;
    }

    /**
     * @param pageSpecific the pageSpecific to set
     */
    public void setPageSpecific(boolean pageSpecific) {
        this.pageSpecific = pageSpecific;
    }

    public String applyTranslation(String baseToTranslate) {
        if (basePattern == null) {
            basePattern = Helper.getBasePattern(baseString);
        }
        Matcher m = basePattern.matcher(baseToTranslate);
        if (!m.matches()) {
            return null;
        }
        
        setUsed(true);

        ArrayList<String> matches = new ArrayList<String>();
        for (int i = 0; i <= m.groupCount(); i++) {
            String s = m.group(i);
            if (s == null ? baseToTranslate == null : s.equals(baseToTranslate)) {
                continue;
            }
            matches.add(s);
        }

        int idx = 0;
        m = ParamRegExp.matcher(targetString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, matches.get(idx));
            idx++;
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void adjustTranslationSpaces() {
        if (Helper.isNullOrEmpty(targetString)) { 
            return;
        }

        int begin = 0, end = targetString.length();
        boolean adjust = false;
        String prefix = "";
        Matcher matcher = BeginningSpaces.matcher(baseString);
        if (matcher.find()) {
            prefix = matcher.group();
            Matcher targetMatcher = BeginningSpaces.matcher(targetString);
            if (!targetMatcher.find()){
                adjust = true;
            }
            else if(!targetMatcher.group().equals(prefix))
            {
                adjust = true;
                begin = targetMatcher.end();
            }
        }

        matcher = EndingSpaces.matcher(baseString);
        String suffix = "";
        if (matcher.find()) {
            suffix = matcher.group();
            Matcher targetMatcher = EndingSpaces.matcher(targetString);
            if (!targetMatcher.find()){
                adjust = true;
            }
            else if (!targetMatcher.group().equals(suffix)) 
            {
                adjust = true;
                end = targetMatcher.start();
            }
        }
        
        if (adjust) {
            StringBuilder newTarget = new StringBuilder();
            newTarget.append(prefix);
            newTarget.append(targetString.substring(begin, end));
            newTarget.append(suffix);
            targetString = newTarget.toString();
        }
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * @return the used
     */
    public boolean isUsed() {
        return used;
    }
}
