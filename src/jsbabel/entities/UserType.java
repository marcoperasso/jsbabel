/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.entities;

/**
 *
 * @author perasso
 */
public enum UserType {
    Admin(0),
    SiteOwner(1),
    SiteTranslator(2);
    private final int value;

    private UserType(int value) {
        this.value = value;
    }
    
}
