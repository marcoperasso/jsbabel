/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author perasso
 */
@Entity
@Table(name = "stringstatistics")
public class Stringstatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "STRINGLEN")
    private Integer stringlen=0;
    @Column(name = "STRINGCOUNT")
    private Integer stringcount=0;

    public Stringstatistics() {
    }

    public Stringstatistics(Integer stringlen) {
        this.stringlen = stringlen;
    }

    public Integer getStringlen() {
        return stringlen;
    }

    public void setStringlen(Integer stringlen) {
        this.stringlen = stringlen;
    }

    public Integer getStringcount() {
        return stringcount;
    }

    public void setStringcount(Integer stringcount) {
        this.stringcount = stringcount;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (stringlen != null ? stringlen.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Stringstatistics)) {
            return false;
        }
        Stringstatistics other = (Stringstatistics) object;
        if ((this.stringlen == null && other.stringlen != null) || (this.stringlen != null && !this.stringlen.equals(other.stringlen))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jsbabel.entities.Stringstatistics[ stringlen=" + stringlen + " ]";
    }
    
}
