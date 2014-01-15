package jsbabel;

public class LocaleData {

    public String l;// locale
    public String u;// url
    public String t;// title;

    public String toJSON() {
        //{"l":"it-IT","u":"/img/flags/it.png","t":"italiano%20(Italia)"}
        StringBuilder sb = new StringBuilder();
        sb.append('{');

        sb.append("'l':'");
        sb.append(l);
        sb.append("',");

        sb.append("'u':'");
        sb.append(u);
        sb.append("',");

        sb.append("'t':'");
        sb.append(t);
        sb.append("'");
        sb.append('}');
        return sb.toString();
    }
}
