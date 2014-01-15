/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.xliff;

public class XLiffElement {
    
    public String name = "";
    public boolean inline = false;
    public boolean empty = false;
    public boolean mixed = false;
    public boolean wrapper = false;
    public String resType = "";

    public XLiffElement(String name, boolean inline, boolean empty, boolean mixed, boolean wrapper, String resType) {

        this.name = name;
        this.inline = inline;
        this.empty = empty;
        this.mixed = mixed;
        this.wrapper = wrapper;
        this.resType = resType;
    }

    @Override
    public String toString() {
        return name;
    }

    public XLiffElement(String name) {
        this.name = name;
    }
}
