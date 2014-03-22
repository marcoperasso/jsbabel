package jsbabel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import nl.captcha.Captcha;
import jsbabel.entities.IBaseString;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import jsbabel.entities.StringType;
import jsbabel.entities.User;
import org.apache.catalina.util.Base64;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;

public final class Helper {

    private static Logger log = Logger.getLogger(Helper.class.getName());
    public static String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*()";
    private static Pattern BaseRegExp = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])|(\\%\\d+\\%)");
    private static Pattern TrimRegExp = Pattern.compile("[\\s]+");
    private static Locale[] locales = null;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    public static void dispose() {
        log = null;
        ALLOWED_CHARS = null;
        BaseRegExp = null;
        TrimRegExp = null;
        locales = null;
        dateFormatter = null;
    }

    public static String getExecutingHost() {
        return "http://www.jsbabel.com";
    }

    public static String encodeURIComponent(String input) {
        if (Helper.isNullOrEmpty(input)) {
            return input;
        }

        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        try {
            for (int i = 0; i < l; i++) {
                String e = input.substring(i, i + 1);
                if (ALLOWED_CHARS.indexOf(e) == -1) {
                    byte[] b = e.getBytes("utf-8");
                    o.append(getHex(b));
                    continue;
                }
                o.append(e);
            }
            return o.toString();
        } catch (UnsupportedEncodingException e) {
            log(e);
        }
        return input;
    }

    private static String getHex(byte buf[]) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (int i = 0; i < buf.length; i++) {
            int n = (int) buf[i] & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }

    public static String decodeURIComponent(String encodedURI) {
        char actualChar;

        StringBuilder buffer = new StringBuilder();

        int bytePattern, sumb = 0;

        for (int i = 0, more = -1; i < encodedURI.length(); i++) {
            actualChar = encodedURI.charAt(i);

            switch (actualChar) {
                case '%': {
                    actualChar = encodedURI.charAt(++i);
                    int hb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    actualChar = encodedURI.charAt(++i);
                    int lb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    bytePattern = (hb << 4) | lb;
                    break;
                }
                case '+': {
                    bytePattern = ' ';
                    break;
                }
                default: {
                    bytePattern = actualChar;
                }
            }

            if ((bytePattern & 0xc0) == 0x80) { // 10xxxxxx
                sumb = (sumb << 6) | (bytePattern & 0x3f);
                if (--more == 0) {
                    buffer.append((char) sumb);
                }
            } else if ((bytePattern & 0x80) == 0x00) { // 0xxxxxxx
                buffer.append((char) bytePattern);
            } else if ((bytePattern & 0xe0) == 0xc0) { // 110xxxxx
                sumb = bytePattern & 0x1f;
                more = 1;
            } else if ((bytePattern & 0xf0) == 0xe0) { // 1110xxxx
                sumb = bytePattern & 0x0f;
                more = 2;
            } else if ((bytePattern & 0xf8) == 0xf0) { // 11110xxx
                sumb = bytePattern & 0x07;
                more = 3;
            } else if ((bytePattern & 0xfc) == 0xf8) { // 111110xx
                sumb = bytePattern & 0x03;
                more = 4;
            } else { // 1111110x
                sumb = bytePattern & 0x01;
                more = 5;
            }
        }
        return buffer.toString();
    }

    public static String getHost(String url) {
        return getHost(url, false);
    }

    public static String getHost(String url, boolean endingSlash) {
        if (Helper.isNullOrEmpty(url)) {
            return "";
        }
        try {
            return getHost(new URL(url), endingSlash);
        } catch (MalformedURLException ex) {
            log(ex);
        }

        return "";
    }

    public static String getBareHost(String host) {
        if (host.toLowerCase().startsWith("http://")) {
            host = host.substring(7);
        }
        return host.replace(':', '.');
    }

    public static String getCleanPath(URL url) {
        String path = url.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    public static void splitUrl(String sUrl, SplitUrlParameter parameterObject) {
        if (sUrl == null || Helper.isNullOrEmpty(sUrl)) {
            return;
        }
        URL url;
        try {
            url = new URL(sUrl);
            parameterObject.host = url.getHost();
            if (parameterObject.host.endsWith("/")) {
                parameterObject.host = parameterObject.host.substring(0, parameterObject.host.length() - 2);
            }
            parameterObject.path = getCleanPath(url);
            parameterObject.args = url.getQuery();
            if (parameterObject.args == null) {
                parameterObject.args = "";
            }
        } catch (MalformedURLException e) {
            log(e);
        }
    }

    public static Locale[] getSortedLocales() {
        if (locales == null) {
            locales = Locale.getAvailableLocales();
            ArrayList<Locale> ll = new ArrayList<Locale>();
            for (Locale l : locales) {
                if (l.getCountry().length() > 0) {
                    ll.add(l);
                }
            }
            locales = new Locale[ll.size()];
            locales = ll.toArray(locales);
            Arrays.sort(locales, new Comparator<Locale>() {
                public int compare(Locale l1, Locale l2) {
                    return (l1.getDisplayName(l1).compareToIgnoreCase(l2.getDisplayName(l2)));
                }
            });
        }
        return locales;
    }

    public static Date parseDate(String sDate) throws ParseException {
        return dateFormatter.parse(sDate);
    }

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static String getLanguageCode(Locale l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getLanguage());
        String v = l.getCountry();
        if (!v.isEmpty()) {
            sb.append('-');
            sb.append(v);
        }
        return sb.toString();
    }

    public static Locale fromLanguageCode(String loc) {
        if (loc == null) {
            return null;
        }
        String[] tok = loc.split("-");
        switch (tok.length) {
            case 1:
                return new Locale(tok[0]);
            case 2:
                return new Locale(tok[0], tok[1]);
        }

        return null;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean matchBaseString(String memoryBase, String dbBase) {
        Pattern p = getBasePattern(dbBase);
        return p.matcher(memoryBase).matches();
    }

    public static Pattern getBasePattern(String dbBase) {
        StringBuffer sb = new StringBuffer();
        Matcher m = BaseRegExp.matcher(dbBase);
        while (m.find()) {
            if (m.group().length() == 1) {
                m.appendReplacement(sb, "\\\\$1");
            } else {
                m.appendReplacement(sb, "(.*)");
            }
        }
        m.appendTail(sb);
        return Pattern.compile(sb.toString());
    }

    public static boolean isBaseStringToIgnore(String prepared) {
        return prepared.equals("") || prepared.equals(" ");
    }

    public static String prepareBaseString(String text) {
        StringBuffer sb = new StringBuffer();
        Matcher m = TrimRegExp.matcher(text);
        while (m.find()) {
            m.appendReplacement(sb, " ");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void log(String message) {
        log.log(Level.SEVERE, message);
    }

    public static void log(Level level, String message) {
        log.log(level, message);
    }

    public static void log(Exception e) {
        log(e, null);
    }

    public static void log(Exception e, HttpSession session) {
        if (log != null)
            log.severe(getStackTrace(e));
        if (session != null) {
            getErrors(session).add(e);
        }
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    public static List<Exception> getErrors(HttpSession session) {
        List<Exception> errors = (List<Exception>) session.getAttribute(Const.ErrorKey);
        if (errors == null) {
            errors = new ArrayList<Exception>();
            session.setAttribute(Const.ErrorKey, errors);
        }
        return errors;
    }

    public static String getSiteKey(String host) {
        return Const.Site + host;
    }

    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(Const.UserKey);
    }

    public static boolean isUserLogged(HttpSession session) {
        User user = getUser(session);
        return user != null && user.isActive();
    }

    public static void sendMail(String to, String subject, String body) {
        String from = "info@jsbabel.com";
        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.jsbabel.com");
        // props.put("mail.smtp.socketFactory.port", "465");
        // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("info@jsbabel.com", "lebabsj");
                    }
                });

        try {
            Multipart mp = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(body, "text/html");
            mp.addBodyPart(htmlPart);

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            InternetAddress a = new InternetAddress(from);
            a.setPersonal("JSBABEL");
            message.setFrom(a);
            // Set To: header field of the header. 
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // Now set the actual message
            message.setText(body);
            // Set Subject: header field
            message.setSubject(subject);
            message.setContent(mp);
            Transport.send(message);
        } catch (Exception ex) {
            log(ex);
        }
    }

    public static void checkRequiredFields(Object o) throws Exception {
        for (Field field : o.getClass().getDeclaredFields()) {
            RequiredField ann = field.getAnnotation(RequiredField.class);
            if (ann
                    != null) {
                field.setAccessible(true);
                Object fi = field.get(o);
                String s = fi == null ? "" : fi.toString();
                if (isNullOrEmpty(s)) {
                    throw new Exception(String.format(Messages.MissingField, field.getName()));
                }
            }
        }
    }

    public static void cleanupThreadLocals(Thread thread, final ClassLoader classLoader) {
        Thread[] threadList;
        if (thread != null) {
            threadList = new Thread[1];
            threadList[0] = thread;
        } else {
            // Every thread
            threadList = new Thread[Thread.activeCount()];
            Thread.enumerate(threadList);
        }

        for (int iThreadList = 0; iThreadList < threadList.length; iThreadList++) {
            Thread t = threadList[iThreadList];

            Field field;
            try {
                Class c;
                if (t instanceof java.lang.Thread) {
                    c = t.getClass();


                    while ((c != null) && (c != java.lang.Thread.class)) {
                        c = c.getSuperclass();
                    }
                    if (c != null) {
                        field = c.getDeclaredField("threadLocals");
                        field.setAccessible(true);

                        Object threadLocals = field.get(t);
                        if (threadLocals != null) {
                            Field entries = threadLocals.getClass().getDeclaredField("table");
                            entries.setAccessible(true);
                            Object entryList[] = (Object[]) entries.get(threadLocals);
                            for (int iEntry = 0; iEntry < entryList.length; iEntry++) {
                                if (entryList[iEntry] != null) {
                                    Field fValue = entryList[iEntry].getClass().getDeclaredField("value");
                                    if (fValue != null) {
                                        fValue.setAccessible(true);
                                        Object value = fValue.get(entryList[iEntry]);
                                        if (value != null) {
                                            boolean flag = true;
                                            if ((classLoader != null) && (value.getClass().getClassLoader() != classLoader)) {
                                                flag = false;
                                            }

                                            if (flag) {
                                                entryList[iEntry] = null;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Helper.log(ex);
            }
        }
    }

    public static void redirectForLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            url += '?' + request.getQueryString();
        }
        url = URLEncoder.encode(url, "UTF-8");
        response.sendRedirect("/needlogin.jsp?url=" + url);
    }

    public static void requireCaptcha(HttpSession session) {
        requireCaptcha(session, true);
    }

    public static void requireCaptcha(HttpSession session, boolean b) {
        session.setAttribute(Const.RequireCaptchaParam, b);
    }

    public static boolean isCaptchaRequired(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(Const.RequireCaptchaParam));
    }

    public static void testCaptcha(HttpServletRequest request, boolean forceTest) throws Exception {
        if (!forceTest && !isCaptchaRequired(request.getSession())) {
            return;
        }
        Captcha captcha = (Captcha) request.getSession().getAttribute(Captcha.NAME);
        // Or, for an AudioCaptcha:
        // AudioCaptcha captcha = (AudioCaptcha) session.getAttribute(Captcha.NAME);
        String answer = request.getParameter("captchaanswer");
        if (captcha == null || captcha.isCorrect(answer)) {
            requireCaptcha(request.getSession(), false);
        } else {
            throw new Exception("Invalid verification code");
        }
    }

    public static String getAutomaticTranslationKey(String url, String targetLocale) {
        return "AT-" + url + "-" + targetLocale;
    }

    public static boolean isTranslateMode(Site site, HttpSession session) {
        if (Helper.isUserLogged(session)) {
            User user = Helper.getUser(session);
            for (Site s : user.getSites()) {
                if (s.getId() == site.getId()) {
                    return true;
                }
            }
        }
        return site.isDemoMode();
    }

    public static boolean isBot(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        return userAgent.contains("googlebot") || userAgent.contains("bingbot");
    }

    public static String getPageCacheKey(Long id) {
        return Const.PagesKey + id.toString();
    }

    public static long countWords(Page p) {
        long words = 0;
        if (!p.isIgnored()) {
            for (IBaseString bs : p) {
                if (bs.getType() == StringType.Ignore) {
                    continue;
                }
                words += countWords(bs.getText());
            }
        }
        return words;
    }

    public static int countWords(String s) {

        int counter = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is letter, word = true.
            if (Character.isLetter(s.charAt(i)) == true && i != endOfLine) {
                word = true;
                // if char isnt letter and there have been letters before (word
                // == true), counter goes up.
            } else if (Character.isLetter(s.charAt(i)) == false && word == true) {
                counter++;
                word = false;
                // last word of String, if it doesnt end with nonLetter it
                // wouldnt count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                counter++;
            }
        }
        return counter;
    }

    public static String getHost(URL uri, boolean endingSlash) {
        StringBuilder sb = new StringBuilder();
        sb.append(uri.getProtocol());
        sb.append("://");
        String host = uri.getHost();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 2);
        }
        sb.append(host);
        Integer port = uri.getPort();
        if (port != 80 && port > 0) {
            sb.append(":");
            sb.append(port.toString());
        }
        if (endingSlash) {
            sb.append('/');
        }
        return sb.toString();
    }

    public static String base64Decode(String s) throws IOException, UnsupportedEncodingException {
        byte[] buff = s.getBytes("utf-8");
        ByteChunk bc = new ByteChunk(buff.length);
        bc.append(buff, 0, buff.length);
        CharChunk out = new CharChunk();
        Base64.decode(bc, out);
        s = out.toString();
        return s;
    }

    public static String base64Encode(String s) throws IOException, UnsupportedEncodingException {
        byte[] buff = s.getBytes("utf-8");
        byte[] encode = Base64.encode(buff);
        return new String(encode, "utf-8");
    }

    static String hexEncode(String string) {
        try {
            return String.format("%040x", new BigInteger(string.getBytes("utf-8")));
        } catch (UnsupportedEncodingException ex) {
           return string;
        }
    }
}
