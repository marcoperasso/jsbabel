package jsbabel;

import java.util.ArrayList;

public class ClientData {

    public String bl; // base locale
    public char a;// anchor
    public int x;// offset
    public int y;// top
    public ArrayList<LocaleData> ld = new ArrayList<LocaleData>();// locales

    public String toJSON() {
        //{"tm":1355230878710,"bl":"it-IT","a":"C","x":0,"y":0,"ld":[{"l":"it-IT","u":"/img/flags/it.png","t":"italiano%20(Italia)"}]}
        StringBuilder sb = new StringBuilder();
        sb.append('{');

        sb.append("'bl':'");
        sb.append(bl);
        sb.append("',");

        sb.append("'a':'");
        sb.append(a);
        sb.append("',");

        sb.append("'x':");
        sb.append(x);
        sb.append(",");

        sb.append("'y':");
        sb.append(y);
        sb.append(",");

        sb.append("'ld':[");
        boolean comma = false;
        for (LocaleData d : ld) {
            if (comma) {
                sb.append(",");
            }
            sb.append(d.toJSON());
            comma = true;
        }
        sb.append("]");
        sb.append('}');
        return sb.toString();
    }
}
