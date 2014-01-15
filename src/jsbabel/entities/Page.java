package jsbabel.entities;

import jsbabel.IIDConsumer;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Translations;
import jsbabel.entities.IBaseString.IBaseStringFinder;

@Entity
@Table(name = "PAGES")
public class Page implements IIDConsumer, Serializable, Iterable<IBaseString> {

    @Id
    @GenericGenerator(name = "seq_id", strategy = "jsbabel.IDGenerator")
    @GeneratedValue(generator = "seq_id")
    private Long id;
    @Column
    String page;
    @Column
    long siteId;
    @Column
    private boolean ignored;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @JoinTable(name = "PAGESTRINGS", joinColumns = {
        @JoinColumn(name = "PAGEID")}, inverseJoinColumns = {
        @JoinColumn(name = "STRINGID")})
    private List<BaseString> baseStrings = new ArrayList<BaseString>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @JoinTable(name = "PAGELONGSTRINGS", joinColumns = {
        @JoinColumn(name = "PAGEID")}, inverseJoinColumns = {
        @JoinColumn(name = "STRINGID")})
    private List<BaseLongString> baseLongStrings = new ArrayList<BaseLongString>();
    @Transient
    private List<BaseLongString> oldBaseLongStrings;
    @Transient
    private List<BaseString> oldBaseStrings;

    @Override
    public String toString() {
        return "Page{" + "page=" + page + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final Page other = (Page) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public void sortStrings() {
        Comparator<IBaseString> cmp = new Comparator<IBaseString>() {
            @Override
            public int compare(IBaseString o1, IBaseString o2) {
                return o1.getText().trim().compareToIgnoreCase(o2.getText().trim());
            }
        };
        Collections.sort(baseStrings, cmp);
        Collections.sort(baseLongStrings, cmp);
    }

    public int size() {
        return baseStrings.size() + baseLongStrings.size();
    }

    public Statistics getStatistics(String targetLocale) {
        Statistics s = new Statistics();

        s.strings = 0;
        s.words = 0;
        s.translatedWords = 0;
        for (IBaseString bs : this) {
            if (bs.getType() != StringType.Ignore) {
                s.strings++;
                int words = Helper.countWords(bs.getText());
                s.words += words;
                ITargetString ts = bs.getTargetString(targetLocale);
                if (ts != null && !Helper.isNullOrEmpty(ts.getText())) {
                    s.translatedWords += words;
                }
            }
        }
        return s;
    }

    @Override
    public long getContextId() {
        return getSiteId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public List<BaseString> getBaseStrings() {
        return baseStrings;
    }

    public void beginUpdate() {
        if (oldBaseLongStrings == null) {
            this.oldBaseLongStrings = baseLongStrings;
            baseLongStrings = new ArrayList<BaseLongString>();
        }
        if (this.oldBaseStrings == null) {
            this.oldBaseStrings = baseStrings;
            baseStrings = new ArrayList<BaseString>();
        }
    }

    public void endUpdate() {
        this.oldBaseLongStrings = null;
        this.oldBaseStrings = null;
    }

    public IBaseString addBaseString(String text, StringType type, boolean pageSpecific, IBaseStringFinder finder) {
        IBaseString bs = null;
        try {
            if (text.length() > DataHelper.MAX_STRING_LENGTH) {
                for (BaseLongString s : baseLongStrings) // prima cerco se c'è in quello corrente
                {
                    if (isNotPageSpecificCompliant(pageSpecific, s)) {
                        continue;
                    }
                    if (s.getText().equals(text) && (type == null || s.getType() == type)) {
                        return s;
                    }
                }
                //chiamo size per evitare un null pointer (buco di inizializzazione di Hibernate)
                if (oldBaseLongStrings != null && oldBaseLongStrings.size() > 0) {
                    for (BaseLongString s : oldBaseLongStrings) // poi cerco nel vecchio, e se c'� lo travaso nel corrente
                    {
                        if (isNotPageSpecificCompliant(pageSpecific, s)) {
                            continue;
                        }
                        if (s.getText().equals(text) && (type == null || s.getType() == type)) {
                            baseLongStrings.add(s);
                            return s;
                        }
                    }
                }
                //poi la cerco nel database
                if (!pageSpecific) {
                    bs = finder.find(text, type);
                }
                if (bs != null) {
                    baseLongStrings.add((BaseLongString) bs);
                    return bs;
                }
                bs = new BaseLongString();// infine lo aggiungo
                baseLongStrings.add((BaseLongString) bs);
            } else {
                for (BaseString s : baseStrings) {
                    if (isNotPageSpecificCompliant(pageSpecific, s)) {
                        continue;
                    }
                    if (s.getText().equals(text) && (type == null || s.getType() == type)) {
                        return s;
                    }
                }
                //chiamo size per evitare un null pointer (buco di inizializzazione di Hibernate)
                if (oldBaseStrings != null && oldBaseStrings.size() > 0) {
                    for (BaseString s : oldBaseStrings) {
                        if (isNotPageSpecificCompliant(pageSpecific, s)) {
                            continue;
                        }
                        if (s.getText().equals(text) && (type == null || s.getType() == type)) {
                            baseStrings.add(s);
                            return s;
                        }
                    }
                }
                if (!pageSpecific) {
                    bs = finder.find(text, type);
                }
                if (bs != null) {
                    baseStrings.add((BaseString) bs);
                    return bs;
                }
                bs = new BaseString();
                baseStrings.add((BaseString) bs);
            }
        } catch (Exception ex) {
            return null;
        }
        bs.setSiteId(siteId);
        bs.setText(text);
        bs.setType(type == null ? StringType.Text : type);
        if (pageSpecific) {
            bs.setPage(this);
        }

        return bs;
    }

    public List<BaseLongString> getBaseLongStrings() {
        return baseLongStrings;
    }

    public Translations getTranslations(String targetLocale, boolean includeNotTranslated) {
        Translations t = new Translations();
        for (IBaseString bs : baseStrings) {
            if (bs.getType() == StringType.Ignore) {
                t.add(bs.getText(), "true", bs.isPageSpecific(), StringType.Ignore);
            } else {
                ITargetString ts = bs.getTargetString(targetLocale);
                String tgt = ts == null ? "" : ts.getText();
                if (includeNotTranslated || !tgt.isEmpty()) {
                    t.add(bs.getText(), tgt, bs.isPageSpecific(), bs.getType());
                }
            }
        }
        for (IBaseString bs : baseLongStrings) {
            if (bs.getType() == StringType.Ignore) {
                t.add(bs.getText(), "true", bs.isPageSpecific(), StringType.Ignore);
            } else {
                ITargetString ts = bs.getTargetString(targetLocale);
                String tgt = ts == null ? "" : ts.getText();
                if (includeNotTranslated || !tgt.isEmpty()) {
                    t.add(bs.getText(), tgt, bs.isPageSpecific(), bs.getType());
                }
            }
        }
        return t;
    }

    private boolean isNotPageSpecificCompliant(boolean pageSpecific, IBaseString s) {
        if (pageSpecific) {
            //devo aggiungere una traduzione specifica per la pagina:
            //quella che ho va bene solo se gli id corrispondono
            if (s.getPage() == null || s.getPage().getId() != this.id) {
                return true;
            }
        } else {
            //devo aggiungere una traduzione generica di pagina
            //se quella che ho è specifica, non va bene
            if (s.getPage() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<IBaseString> iterator() {
        final Iterator bsi = baseStrings.iterator();
        final Iterator blsi = baseLongStrings.iterator();
        return new Iterator<IBaseString>() {
            @Override
            public boolean hasNext() {
                return bsi.hasNext() || blsi.hasNext();
            }

            @Override
            public IBaseString next() {
                if (bsi.hasNext()) {
                    return (IBaseString) bsi.next();
                }
                if (blsi.hasNext()) {
                    return (IBaseString) blsi.next();
                }
                return null;
            }

            @Override
            public void remove() {
                bsi.remove();
                blsi.remove();
            }
        };
    }

    /**
     * @return the ignored
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * @param ignored the ignored to set
     */
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public void clearStrings() {
        baseLongStrings.clear();
        baseStrings.clear();
    }

    public class Statistics {

        DecimalFormat twoDecFormatter = new DecimalFormat("0.00");
        DecimalFormat thFormatter = new DecimalFormat("###,###");
        private int strings = 0;
        private int words = 0;
        private int translatedWords = 0;

        public boolean complete() {
            return translatedWords == words;
        }

        public String getStringCount() {
            return thFormatter.format(strings);
        }

        public String getWordCount() {
            return thFormatter.format(words);
        }

        public String getTranslatedWordCount() {
            return thFormatter.format(translatedWords);
        }

        public String percentage() {
            double ratio = words > 0 ? (double) translatedWords / (double) words : 0;
            return twoDecFormatter.format(ratio);
        }
    }
}
