package jsbabel.entities;

import java.io.*;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import jsbabel.IDGenerator;
import jsbabel.IIDConsumer;

@Entity
@Table(name = "SITES")
public class Site implements IIDConsumer, Serializable, Comparable {

    /**
     *
     */
    private static final long serialVersionUID = 2035009080033484570L;
    @GenericGenerator(name = "seq_id", strategy = "jsbabel.IDGenerator")
    @GeneratedValue(generator = "seq_id")
    @Id
    @Column(name = "ID")
    private Long id = -1L;
    @Column(name = "HOST")
    private String host = "";
    @Column(name = "BASELOCALE")
    private String baseLanguage = "";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "site", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TargetLocale> targetLocales = new HashSet<TargetLocale>();
    @Column(name = "OFFSET")
    private int offset = 0;
    @Column(name = "TOP")
    private int top = 0;
    @Column(name = "ANCHOR")
    private char anchor = 'C';
    @Column(name = "TRANSLATIONVERSION")
    private Integer translationVersion = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID")
    private User user;
    @Transient
    private boolean demoMode = false;
    @Transient
    private boolean modified = false;

    public void setTargetLanguages(String[] targetLanguages) {
        if (targetLanguages == null || targetLanguages.length == 0) {
            //se non ho elementi da inserire, esco; ho modificato solo
            //se la collezione vecchia conteneva dati
            modified = targetLocales.size() > 0;
            targetLocales.clear();
            return;
        }
        //prima cerco in quelli che ho e vedo se ne devo togliere
        List<TargetLocale> removed = new ArrayList<TargetLocale>();
        for (TargetLocale tl : targetLocales) {
            boolean found = false;
            for (String loc : targetLanguages) {
                if (loc.equals(tl.getLocale())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removed.add(tl);
                modified = true;
            }
        }
        targetLocales.removeAll(removed);
        //poi aggiungo quelli mancanti
        for (String locale : targetLanguages) {
            //cerco se esiste nella vecchia collezione
            boolean existing = false;
            for (TargetLocale tl : targetLocales) {
                if (tl.getLocale().equals(locale)) {
                    //se esiste non faccio nulla
                    existing = true;
                    break;
                }
            }
            if (!existing) {
                //se non esiste, la aggiungo: questo costituisce modifica!
                targetLocales.add(new TargetLocale(this, locale));
                modified = true;
            }
        }
    }

    public void setBaseLanguage(String baseLanguage) {
        if ((this.baseLanguage == null && baseLanguage != null) || !this.baseLanguage.equals(baseLanguage)) {
            modified = true;
        }
        this.baseLanguage = baseLanguage;
    }

    public String getBaseLanguage() {
        return baseLanguage;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int left) {
        if (this.offset != left) {
            modified = true;
        }
        this.offset = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        if (this.top != top) {
            modified = true;
        }
        this.top = top;
    }

    public char getAnchor() {
        return anchor;
    }

    public void setAnchor(char anchor) {
        if (this.anchor != anchor) {
            modified = true;
        }
        this.anchor = anchor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (!this.host.equalsIgnoreCase(host)) {
            modified = true;
        }
        this.host = host;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean b) {
        modified = b;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTranslationVersion() {
        return translationVersion == null ? 0 : translationVersion;
    }

    public void increaseTranslationVersion() {
        if (translationVersion == null) {
            translationVersion = 0;
        }
        translationVersion++;
        modified = true;
    }

    public boolean containsTargetLanguage(String locale) {
        for (TargetLocale loc : targetLocales) {
            if (loc.getLocale().equals(locale)) {
                return true;
            }
        }
        return false;
    }

    public Set<TargetLocale> getTargetLocales() {
        return targetLocales;
    }

    public void setTargetLocales(Set<TargetLocale> targetLocales) {
        this.targetLocales = targetLocales;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public long getContextId() {
        return 0;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return 1;
        }
        if (!(o instanceof Site)) {
            return 1;
        }
        Site other = (Site) o;
        if (host == null) {
            return -1;
        }
        return getHost().compareTo(other.getHost());
    }
}
