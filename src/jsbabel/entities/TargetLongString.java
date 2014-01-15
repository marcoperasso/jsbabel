package jsbabel.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TARGETLONGSTRINGS")
public class TargetLongString implements Serializable, ITargetString {

    /**
     *
     */
    private static final long serialVersionUID = -5355909737373574152L;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BASEID")
    private BaseLongString baseString;
    @Id
    String locale;
    @Column
    String text;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.baseString != null ? this.baseString.hashCode() : 0);
        hash = 11 * hash + (this.locale != null ? this.locale.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TargetLongString other = (TargetLongString) obj;
        if (this.baseString != other.baseString && (this.baseString == null || !this.baseString.equals(other.baseString))) {
            return false;
        }
        if ((this.locale == null) ? (other.locale != null) : !this.locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

    

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BaseLongString getBaseString() {
        return baseString;
    }

    public void setBaseString(BaseLongString baseString) {
        this.baseString = baseString;
    }
}
