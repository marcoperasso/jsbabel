/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Marco
 */
public class Crypter {

    private static final String[] scripts = {
        "this.crypt = function(s){\n"
        + "    var ret = '';\n"
        + "    for (var i = 0; i < s.length; i++){\n"
        + "        var code = s.charCodeAt(i) + 1;\n"
        + "        ret += String.fromCharCode(code);\n"
        + "    }\n"
        + "    return ret;"
        + "}",//************************************************************
        "this.crypt = function(s){\n"
        + "    var ret = '';\n"
        + "    for (var i = 0; i < s.length; i++){\n"
        + "        var code = s.charCodeAt(i) -1;\n"
        + "        ret += String.fromCharCode(code);\n"
        + "    }\n"
        + "    return ret;\n"
        + "}",//**************************************************************
        "this.crypt = function(s){\n"
        + "    var ret = '';\n"
        + "    for (var i = 0; i < s.length; i=i+2){\n"
        + "        ret += s.charAt(i);\n"
        + "    }\n"
        + "    for (var i = 1; i < s.length; i=i+2){\n"
        + "        ret += s.charAt(i);\n"
        + "    }\n"
        + "    return ret;\n"
        + "}",//**************************************************************
        "this.crypt = function(s){\n"
        + "    var ret = '';\n"
        + "    for (var i = 1; i < s.length; i=i+2){\n"
        + "        ret += s.charAt(i);\n"
        + "    }\n"
        + "    for (var i = 0; i < s.length; i=i+2){\n"
        + "        ret += s.charAt(i);\n"
        + "    }\n"
        + "    return ret;\n"
        + "}"
    };

    public static String getScript(HttpSession session) {
        Random rnd = new Random(new Date().getTime());
        int i = rnd.nextInt(scripts.length);
        session.setAttribute(Const.CryptIndex, i);
        return scripts[i];
    }

    public static String decrypt(String s, HttpSession session) throws Exception {
        Integer i = (Integer) session.getAttribute(Const.CryptIndex);
        if (i == null) {
            throw new Exception(Messages.CannotDecrypt);

        }
        StringBuilder ret = new StringBuilder();
        char[] chars = s.toCharArray();
        switch (i) {
            case 0: {
                for (int ch : chars) {
                    ch--;
                    ret.append((char) ch);
                }
                break;
            }
            case 1: {
                for (int ch : chars) {
                    ch++;
                    ret.append((char) ch);
                }
                break;
            }
            case 2: {
                int mid = (int) Math.ceil((double) chars.length / 2.f);
                for (int j = 0; j < mid; j++) {
                    ret.append(chars[j]);
                    int j2 = j + mid;
                    if (j2 < chars.length) {
                        ret.append(chars[j2]);
                    }
                }
                break;
            }
            case 3: {
                int mid = (int) Math.floor((double) chars.length / 2.f);
                for (int j = mid; j < chars.length; j++) {
                    ret.append(chars[j]);
                    int j2 = j - mid;
                    if (j2 < mid) {
                        ret.append(chars[j2]);
                    }
                }
                break;
            }
            default:
                throw new Exception(Messages.CannotDecrypt);
        }
        return ret.toString();
    }
}
