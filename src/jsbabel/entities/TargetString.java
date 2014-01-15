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
@Table(name = "TARGETSTRINGS")
public class TargetString implements Serializable, ITargetString {

    /**
     *
     */
    private static final long serialVersionUID = -5355909737373574152L;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BASEID")
    private BaseString baseString;
    @Id
    String locale;
    @Column
    String text;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.baseString != null ? this.baseString.hashCode() : 0);
        hash = 89 * hash + (this.locale != null ? this.locale.hashCode() : 0);
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
        final TargetString other = (TargetString) obj;
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
    /* (non-Javadoc)
     * @see jsbabel.entities.ITargetString#getBaseString()
     */

    public BaseString getBaseString() {
        return baseString;
    }
    /* (non-Javadoc)
     * @see jsbabel.entities.ITargetString#setBaseString(jsbabel.entities.IBaseString)
     */

    public void setBaseString(BaseString baseString) {
        this.baseString = baseString;
    }
}
