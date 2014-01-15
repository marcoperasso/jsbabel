/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.entities;

/**
 *
 * @author Marco
 */
public enum Gender {

    Unspecified(0),
    Female(1),
    Male(2);
    private final int value;

    private Gender(int value) {
        this.value = value;
    }
}
