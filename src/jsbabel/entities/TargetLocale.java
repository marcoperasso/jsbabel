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
@Table(name = "TARGETLOCALES")
public class TargetLocale implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3114242706969832216L;
    @Id
    @Column(name = "LOCALE")
    private String locale;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITEID")
    private Site site;

    public TargetLocale() {
    }

    public TargetLocale(Site site, String locale) {
        this.site = site;
        this.locale = locale;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != TargetLocale.class) {
            return false;
        }
        TargetLocale tl = (TargetLocale) obj;
        return locale.equals(tl.locale) && site.equals(tl.site);
    }

    @Override
    public int hashCode() {
        return locale.hashCode() + site.hashCode();
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}