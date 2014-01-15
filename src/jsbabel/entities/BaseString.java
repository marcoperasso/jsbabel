package jsbabel.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import jsbabel.IDGenerator;
import jsbabel.IIDConsumer;

@Entity
@Table(name = "BASESTRINGS")
public class BaseString implements IBaseString, IIDConsumer, Serializable {

    @Id
    @GenericGenerator(name = "seq_id", strategy = "jsbabel.IDGenerator")
    @GeneratedValue(generator = "seq_id")
    private long id;
    @Column
    private long siteId;
    @JoinColumn(name = "PAGEID")
    @ManyToOne()
    private Page page;
    @Column
    private String text;
    @Column
    @Enumerated(EnumType.ORDINAL)
    private StringType type;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "baseString", fetch = FetchType.LAZY)
    private Set<TargetString> targetStrings = new HashSet<TargetString>();
    @Transient
    private Set<TargetString> oldTargetStrings;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final BaseString other = (BaseString) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getSiteId() {
        return siteId;
    }

    @Override
    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    @Override
    public StringType getType() {
        return type;
    }

    @Override
    public void setType(StringType type) {
        this.type = type;
    }

    public Set<TargetString> getTargetStrings() {
        return targetStrings;
    }

    public void setTargetStrings(Set<TargetString> targetStrings) {
        this.targetStrings = targetStrings;
    }

    @Override
    public ITargetString addTargetString(String locale, String text) {
        for (TargetString s : targetStrings) {
            if (s.getLocale().equals(locale)) {
                s.setText(text);
                return s;
            }
        }
        if (oldTargetStrings != null) {
            for (TargetString s : oldTargetStrings) {
                if (s.getLocale().equals(locale)) {
                    s.setText(text);
                    targetStrings.add(s);
                    return s;
                }
            }
        }
        TargetString ts = new TargetString();
        ts.setLocale(locale);
        ts.setText(text);
        ts.setBaseString(this);
        this.targetStrings.add(ts);
        return ts;

    }

    @Override
    public void beginUpdate() {
        if (this.oldTargetStrings == null) {
            this.oldTargetStrings = targetStrings;
            this.targetStrings = new HashSet<TargetString>();
        }

    }

    @Override
    public ITargetString getTargetString(String targetLocale) {
        for (TargetString ts : targetStrings) {
            if (ts.getLocale().equals(targetLocale)) {
                return ts;
            }
        }
        return null;
    }

    /**
     * @return the pageId
     */
    @Override
    public Page getPage() {
        return page;
    }

    /**
     * @param pageId the pageId to set
     */
    @Override
    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public boolean isPageSpecific() {
        return page!=null;
    }
    @Override
    public long getContextId() {
        return getSiteId();
    }

    @Override
    public boolean hasTranslations() {
        return targetStrings.size() > 0;
    }
}
