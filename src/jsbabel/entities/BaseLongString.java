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
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import jsbabel.IDGenerator;
import jsbabel.IIDConsumer;

@Entity
@Table(name = "BASELONGSTRINGS")
public class BaseLongString implements IBaseString, IIDConsumer, Serializable {

    @Id
    @GenericGenerator(name = "seq_id", strategy = "jsbabel.IDGenerator")
    @GeneratedValue(generator = "seq_id")
    private long id;
    @Column
    private long siteId;
    @JoinColumn(name = "PAGEID")
    @ManyToOne
    private Page page;
    @Column
    private String text;
    @Column
    @Enumerated(EnumType.ORDINAL)
    private StringType type;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "baseString", fetch = FetchType.LAZY)
    private Set<TargetLongString> targetStrings = new HashSet<TargetLongString>();
    @Transient
    private Set<TargetLongString> oldTargetStrings;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final BaseLongString other = (BaseLongString) obj;
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

    public Set<TargetLongString> getTargetStrings() {
        return targetStrings;
    }

    public void setTargetStrings(Set<TargetLongString> targetStrings) {
        this.targetStrings = targetStrings;
    }

    @Override
    public TargetLongString addTargetString(String locale, String text) {
        for (TargetLongString s : targetStrings) {
            if (s.getLocale().equals(locale)) {
                s.setText(text);
                return s;
            }
        }
        if (oldTargetStrings != null) {
            for (TargetLongString s : oldTargetStrings) {
                if (s.getLocale().equals(locale)) {
                    s.setText(text);
                    targetStrings.add(s);
                    return s;
                }
            }
        }
        TargetLongString ts = new TargetLongString();
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
            this.targetStrings = new HashSet<TargetLongString>();
        }

    }

    @Override
    public StringType getType() {
        return type;
    }

    @Override
    public void setType(StringType type) {
        this.type = type;
    }

    @Override
    public ITargetString getTargetString(String targetLocale) {
        for (TargetLongString ts : targetStrings) {
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
