package jsbabel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import jsbabel.entities.BaseLongString;
import jsbabel.entities.BaseString;
import jsbabel.entities.DemoTrial;
import jsbabel.entities.IBaseString;
import jsbabel.entities.IBaseString.IBaseStringFinder;
import jsbabel.entities.ITargetString;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import jsbabel.entities.StringType;
import jsbabel.entities.User;
import jsbabel.entities.ValidationKey;

public class DataHelper {

    public static final int MAX_STRING_LENGTH = 255;
    private boolean disposed = false;
    private Session hibernateSession = null;
    private List<IBaseString> baseStringsWithParams = null;
    Transaction transaction = null;
    private int transactionLevel = 0;
    private boolean transactionAborted = false;

    public void beginTransaction() throws HibernateException, ClassNotFoundException {
        if (transactionLevel == 0) {
            transaction = getHSession().beginTransaction();
        }
        transactionLevel++;
    }

    public void commit() throws Exception {
        transactionLevel--;
        if (transactionLevel < 0) {
            throw new Exception("commit or rollback called more times than beginTransaction");
        }
        if (transactionAborted) {
            return;
        }
        if (transactionLevel == 0) {
            transaction.commit();
        }
    }

    public void rollback() throws Exception {
        transactionLevel--;
        if (transactionLevel < 0) {
            throw new Exception("commit or rollback called more times than beginTransaction");
        }
        if (transactionAborted) {
            return;
        }
        transaction.rollback();
        transactionAborted = true;
    }

    public int Test() throws HibernateException, ClassNotFoundException {
        Criteria c = getHSession().createCriteria(Site.class);
        return c.list().size();
    }

    public Session getHSession() throws HibernateException, ClassNotFoundException {
        if (hibernateSession == null) {
            hibernateSession = MySessionFactory.configureSession();
        }
        return hibernateSession;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            if (hibernateSession != null) {
                hibernateSession.close();
                hibernateSession = null;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    public long getSiteId(HttpSession session, String url) {
        Site site = getSite(session, url);
        if (site == null) {
            return 0;
        }
        Long id = site.getId();
        return id == null ? 0 : id.longValue();

    }

    public void setSite(HttpSession session, Site site, boolean persistent) throws MyException {

        if (site.isModified() && persistent) {

            site.increaseTranslationVersion();

            try {
                beginTransaction();
                getHSession().saveOrUpdate(site);
                commit();
            } catch (Exception e) {
                Helper.log(e, session);

                throw new MyException(e);
            } finally {
            }
        }

        session.setAttribute(Helper.getSiteKey(site.getHost()), site);
        site.setModified(false);
    }

    public Site getSite(HttpSession session, String url) {
        String host = Helper.getHost(url);
        Site site = (Site) session.getAttribute(Helper.getSiteKey(host));
        if (site != null) {
            return site;
        }
        site = getSiteFromDB(host);
        if (site != null) {
            session.setAttribute(Helper.getSiteKey(host), site);
        }
        return site;
    }

    public Site getSiteFromDB(String host) {
        try {
            Criteria c = getHSession().createCriteria(Site.class).add(Restrictions.eq("host", host));
            return (Site) c.uniqueResult();

        } catch (Exception e) {
            Helper.log(e);
        } finally {
        }
        return null;
    }

    public List<Site> getSites() {
        try {
            Criteria c = getHSession().createCriteria(Site.class);
            return c.list();
        } catch (Exception e) {
            Helper.log(e);
        } finally {
        }
        return new ArrayList<Site>();
    }

    public void refreshSite(HttpSession session, String url) throws MyException {
        //aggiorna la versione della traduzione, per invalidare la cache del client
        Site s = getSite(session, url);
        if (s != null) {
            s.setModified(true);
            setSite(session, s, true);
        }
    }

    private String getTranslationsId(SplitUrlParameter param, String locale, boolean ignores) {
        return locale + '-' + param.host + '-' + param.path + '-' + ignores;
    }

    public void setTranslations(HttpSession session, Translations tt, String url, String targetLocale, boolean append) {

        SplitUrlParameter param = new SplitUrlParameter();
        Helper.splitUrl(url, param);
        tt.adjustTranslationSpaces();
        try {
            long siteId = getSiteId(session, url);

            Page p = getPage(param.path, siteId);
            //la beginupdate mi sposta le stringhe esistenti in un array di old
            //se devo andare in append, le old vanno mantenute
            if (!append) {
                p.beginUpdate();
            }
            beginTransaction();
            BaseStringFinder bsf = new BaseStringFinder(siteId, false);
            for (Translation t : tt.getTranslations()) {
                if (Helper.isNullOrEmpty(t.getTargetString()) && t.getType() != StringType.Ignore) {
                    continue;
                }
                IBaseString bs = p.addBaseString(t.getBaseString(), t.getType(), t.isPageSpecific(), bsf);
                bs.beginUpdate();
                //se il tipo è Ignore, non metto niente in target
                if (t.getType() != StringType.Ignore) {
                    bs.addTargetString(targetLocale, t.getTargetString());
                }

            }
            getHSession().saveOrUpdate(p);
            refreshSite(session, url);
            commit();

        } catch (Exception e) {
            Helper.log(e, session);
            try {
                rollback();
            } catch (Exception ie) {
                Helper.log(ie, session);
            }
        } finally {
        }
    }

    public List<Page> getPages(long siteId) throws HibernateException, ClassNotFoundException {
        Criteria c = getHSession().createCriteria(Page.class);
        c.add(Restrictions.eq("siteId", siteId)).addOrder(Order.asc("page"));
        return c.list();

    }

    public Page getPage(HttpSession session, String url) throws ClassNotFoundException {
        SplitUrlParameter param = new SplitUrlParameter();
        Helper.splitUrl(url, param);
        final long siteId = getSiteId(session, url);
        return getPage(param.path, siteId);
    }

    private Page getPage(String path, long siteId) throws ClassNotFoundException {
        Criteria c = getHSession().createCriteria(Page.class).add(Restrictions.eq("siteId", siteId)).add(Restrictions.eq("page", path));

        Page p = (Page) c.uniqueResult();
        if (p == null) {
            p = new Page();
            p.setSiteId(siteId);
            p.setPage(path);
        }
        return p;
    }

    public Translations getTranslations(final HttpSession session, final String url, final String targetLocale, final boolean persistent, final boolean translateMode) {

        final SplitUrlParameter param = new SplitUrlParameter();
        Helper.splitUrl(url, param);

        String translationsId = getTranslationsId(param, targetLocale, translateMode);
        Translations translations = translateMode ? null : (Translations) session.getAttribute(translationsId);
        if (translations != null) {
            return translations;
        }
        final Translations newTranslations = new Translations();

        if (persistent) {

            try {
                getHSession().doWork(new Work() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        PreparedStatement selectStmt = null;

                        try {
                            long siteId = getSiteId(session, url);

                            selectStmt = conn.prepareStatement(
                                    "SELECT DISTINCT "
                                    + "    B.TYPE, B.TEXT, T.TEXT, B.PAGEID IS NOT NULL "
                                    + "FROM "
                                    + "    BASESTRINGS B "
                                    + "        INNER JOIN "
                                    + "    TARGETSTRINGS T ON T.BASEID = B.ID "
                                    + "    INNER JOIN PAGESTRINGS ON B.ID = PAGESTRINGS.STRINGID "
                                    + "    INNER JOIN PAGES ON PAGES.ID = PAGESTRINGS.PAGEID "
                                    + "WHERE PAGES.SITEID = ? AND T.LOCALE = ? AND PAGES.PAGE = ? AND (B.PAGEID =PAGES.ID OR B.PAGEID IS NULL)"
                                    + "UNION SELECT DISTINCT "
                                    + "    0, B.TEXT, T.TEXT, B.PAGEID IS NOT NULL "
                                    + "FROM "
                                    + "    BASELONGSTRINGS B "
                                    + "        INNER JOIN "
                                    + "    TARGETLONGSTRINGS T ON T.BASEID = B.ID "
                                    + "    INNER JOIN PAGELONGSTRINGS ON B.ID = PAGELONGSTRINGS.STRINGID "
                                    + "    INNER JOIN PAGES ON PAGES.ID = PAGELONGSTRINGS.PAGEID "
                                    + "WHERE PAGES.SITEID = ? AND T.LOCALE = ? AND PAGES.PAGE = ? AND (B.PAGEID =PAGES.ID OR B.PAGEID IS NULL)");
                            selectStmt.setLong(1, siteId);
                            selectStmt.setString(2, targetLocale);
                            selectStmt.setString(3, param.path);
                            selectStmt.setLong(4, siteId);
                            selectStmt.setString(5, targetLocale);
                            selectStmt.setString(6, param.path);
                            ResultSet rs = selectStmt.executeQuery();
                            while (rs.next()) {
                                newTranslations.add(rs.getString(2), rs.getString(3), rs.getBoolean(4), StringType.valueOf(rs.getInt(1)));
                            }
                            rs.close();
                            if (translateMode) {
                                selectStmt.close();
                                selectStmt = conn.prepareStatement(
                                        "SELECT DISTINCT "
                                        + "    B.TEXT, B.PAGEID IS NOT NULL "
                                        + "FROM "
                                        + "    BASESTRINGS B "
                                        + "    INNER JOIN PAGESTRINGS ON B.ID = PAGESTRINGS.STRINGID "
                                        + "    INNER JOIN PAGES ON PAGES.ID = PAGESTRINGS.PAGEID "
                                        + "WHERE B.TYPE = 3 AND PAGES.SITEID = ?  AND PAGES.PAGE = ? AND (B.PAGEID=PAGES.ID OR B.PAGEID IS NULL)"
                                        + "UNION SELECT DISTINCT "
                                        + "    B.TEXT, B.PAGEID IS NOT NULL "
                                        + "FROM "
                                        + "    BASELONGSTRINGS B "
                                        + "    INNER JOIN PAGELONGSTRINGS ON B.ID = PAGELONGSTRINGS.STRINGID "
                                        + "    INNER JOIN PAGES ON PAGES.ID = PAGELONGSTRINGS.PAGEID "
                                        + "WHERE B.TYPE = 3 AND PAGES.SITEID = ?  AND PAGES.PAGE = ? AND (B.PAGEID=PAGES.ID OR B.PAGEID IS NULL)");
                                selectStmt.setLong(1, siteId);
                                selectStmt.setString(2, param.path);
                                selectStmt.setLong(3, siteId);
                                selectStmt.setString(4, param.path);
                                rs = selectStmt.executeQuery();
                                while (rs.next()) {
                                    newTranslations.add(rs.getString(1), "true", rs.getBoolean(2), StringType.Ignore);
                                }
                            }

                        } catch (SQLException se) {
                            while (se != null) {
                                Helper.log(se, session);
                                se = se.getNextException();
                            }
                        } catch (Exception e) {
                            Helper.log(e, session);
                        } finally {
                            if (selectStmt != null) {
                                try {
                                    selectStmt.close();
                                } catch (SQLException e) {
                                    Helper.log(e, session);
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Helper.log(e);
            }
        }
        session.setAttribute(translationsId, newTranslations);
        return newTranslations;
    }

    public Translations getOrphanTranslations(HttpSession session, final String url, final String targetLocale) {

        Translations translations = new Translations();
        String where = "{alias}.siteId = ? AND {alias}.ID NOT IN "
                + "(SELECT A.STRINGID FROM PAGESTRINGS AS A WHERE A.PAGEID IN "
                + "(SELECT B.ID FROM PAGES AS B WHERE B.SITEID = ?))  ";
        String whereLong = "{alias}.siteId = ? AND {alias}.ID NOT IN "
                + "(SELECT A.STRINGID FROM PAGELONGSTRINGS AS A WHERE A.PAGEID IN "
                + "(SELECT B.ID FROM PAGES AS B WHERE B.SITEID = ?))  ";
        Long siteId = getSiteId(session, url);
        Object[] parms = {siteId, siteId};
        Type[] types = {LongType.INSTANCE, LongType.INSTANCE};
        try {


            Criteria c = getHSession()
                    .createCriteria(BaseString.class)
                    .add(Restrictions.sqlRestriction(where, parms, types));

            for (Iterator it = c.list().iterator(); it.hasNext();) {
                IBaseString ibs = (IBaseString) it.next();
                addOrpnan(translations, ibs, targetLocale);
            }
            c = getHSession()
                    .createCriteria(BaseLongString.class)
                    .add(Restrictions.sqlRestriction(whereLong, parms, types));
            for (Iterator it = c.list().iterator(); it.hasNext();) {
                IBaseString ibs = (IBaseString) it.next();
                addOrpnan(translations, ibs, targetLocale);
            }

        } catch (Exception e) {
            Helper.log(e);
        }
        return translations;
    }

    private void addOrpnan(Translations translations, IBaseString ibs, String targetLocale) {
        ITargetString targetString = ibs.getTargetString(targetLocale);
        if (targetString != null) {
            translations.add(ibs.getText(), targetString.getText(), ibs.isPageSpecific(), ibs.getType());
        }
    }

    public boolean persistent(HttpServletRequest req, String url) {
        Site site = getSite(req.getSession(), url);
        return site == null ? false : !site.isDemoMode();
    }

    public void update(Site site, List<Page> pages, Long id, boolean isLong, String text, String targetLocale) throws ClassNotFoundException, Exception {
        beginTransaction();
        try {
            for (Page p : pages) {
                for (IBaseString bs : isLong ? p.getBaseLongStrings() : p.getBaseStrings()) {
                    if (id.equals(bs.getId())) {
                        reattach(bs);
                        bs.addTargetString(targetLocale, text);
                        getHSession().saveOrUpdate(bs);
                    }
                }
            }
            getHSession().update(site);
            site.increaseTranslationVersion();
            commit();
        } catch (Exception ex) {
            Helper.log(ex);
            rollback();
            throw ex;
        }
    }

    public void delete(List<Page> pages, String[] pageIds, String[] stringIds, String[] longStringIds) throws Exception {
        beginTransaction();
        try {
            List<IBaseString> baseStrings = new ArrayList<IBaseString>();
            List<IBaseString> baseLongStrings = new ArrayList<IBaseString>();
            List<Page> modifiedPages = new ArrayList<Page>();
            if (pageIds != null) {
                for (String sp : pageIds) {
                    Long id = Long.parseLong(sp);
                    removePage(pages, id, baseStrings, baseLongStrings);
                }
            }

            if (stringIds != null) {
                for (String s : stringIds) {
                    addStringAndPageToList(Long.parseLong(s), pages, modifiedPages, baseStrings, false);
                }
            }
            for (IBaseString bs : baseStrings) {
                addStringAndPageToList(bs.getId(), pages, modifiedPages, baseStrings, false);
            }
            if (longStringIds != null) {
                for (String s : longStringIds) {
                    addStringAndPageToList(Long.parseLong(s), pages, modifiedPages, baseLongStrings, true);
                }
            }
            for (IBaseString bs : baseLongStrings) {
                addStringAndPageToList(bs.getId(), pages, modifiedPages, baseLongStrings, true);
            }
            for (Page p : modifiedPages) {
                getHSession().saveOrUpdate(p);
            }
            for (IBaseString bs : baseStrings) {
                if (!bs.hasTranslations()) {
                    getHSession().delete(bs);
                }
            }
            for (IBaseString bs : baseLongStrings) {
                if (!bs.hasTranslations()) {
                    getHSession().delete(bs);
                }
            }
        } catch (Exception e) {
            rollback();
            throw e;
        }
        commit();
    }

    private void removePage(List<Page> pages, Long id, List<IBaseString> baseStrings, List<IBaseString> baseLongStrings) throws ClassNotFoundException, HibernateException {
        for (Page p : pages) {
            if (p.getId().equals(id)) {
                getHSession().delete(p);

                for (IBaseString bs : p.getBaseLongStrings()) {
                    if (!bs.hasTranslations() && !baseLongStrings.contains(bs)) {
                        baseLongStrings.add(bs);
                    }
                }
                p.getBaseLongStrings().clear();

                for (IBaseString bs : p.getBaseStrings()) {
                    if (!bs.hasTranslations() && !baseStrings.contains(bs)) {
                        baseStrings.add(bs);
                    }
                }
                p.getBaseStrings().clear();


                pages.remove(p);
                return;
            }
        }

    }

    private void addStringAndPageToList(Long stringId, List<Page> pages, List<Page> modifiedPages, List<IBaseString> strings, boolean isLong) throws HibernateException, ClassNotFoundException {

        for (Page p : pages) {
            for (IBaseString bs : isLong ? p.getBaseLongStrings() : p.getBaseStrings()) {
                if (stringId.equals(bs.getId())) {
                    (isLong ? p.getBaseLongStrings() : p.getBaseStrings()).remove(bs);
                    if (!modifiedPages.contains(p)) {
                        modifiedPages.add(p);
                    }
                    if (!strings.contains(bs)) {
                        strings.add(bs);

                    }
                    break;
                }
            }
        }
    }

    public void reattach(Object obj) throws ClassNotFoundException, HibernateException {
        getHSession().update(obj);
    }

    public void ignore(Site site, boolean ignore, List<Page> pages, String[] pageIds, String[] stringIds, String[] longStringIds) throws HibernateException, ClassNotFoundException, Exception {
        beginTransaction();
        List<IBaseString> baseStrings = new ArrayList<IBaseString>();
        List<IBaseString> baseLongStrings = new ArrayList<IBaseString>();
        List<Page> modifiedPages = new ArrayList<Page>();
        try {
            if (pageIds != null) {
                for (String sp : pageIds) {
                    Long id = Long.parseLong(sp);
                    for (Page p : pages) {
                        if (id.equals(p.getId())) {
                            p.setIgnored(ignore);
                            if (ignore) {
                                p.clearStrings();
                            }
                            getHSession().update(p);
                        }
                    }
                }
            }

            if (stringIds != null) {
                for (String s : stringIds) {
                    addStringAndPageToList(Long.parseLong(s), pages, modifiedPages, baseStrings, false);
                }
            }

            if (longStringIds != null) {
                for (String s : longStringIds) {
                    addStringAndPageToList(Long.parseLong(s), pages, modifiedPages, baseLongStrings, true);
                }
            }

            for (IBaseString bs : baseStrings) {
                bs.setType(ignore ? StringType.Ignore : StringType.Text);
                getHSession().update(bs);
            }
            for (IBaseString bs : baseLongStrings) {
                bs.setType(ignore ? StringType.Ignore : StringType.Text);
                getHSession().update(bs);
            }

            getHSession().update(site);
            site.increaseTranslationVersion();


        } catch (Exception e) {
            rollback();
            throw e;
        }
        commit();
    }

    public void addDemoTrial(String src, DemoTrial.TrialType type) throws HibernateException, ClassNotFoundException, Exception {
        beginTransaction();
        Criteria c = getHSession().createCriteria(DemoTrial.class);
                c.add(Restrictions.eq("page", src))
                        .add(Restrictions.eq("trialType", type));
        DemoTrial dt = (DemoTrial) c.uniqueResult();
        if (dt == null) {
            dt = new DemoTrial();
            dt.setTrialType(type);
            dt.setPage(src);
            dt.setTrials(1);
        } else {
            dt.increaseTrials();
        }
        getHSession().saveOrUpdate(dt);
        commit();
    }

    class BaseStringFinder implements IBaseStringFinder {

        long siteId;
        boolean findParameterStrings;

        public BaseStringFinder(long siteId, boolean findParameterStrings) {
            this.siteId = siteId;
            this.findParameterStrings = findParameterStrings;
        }

        @Override
        public IBaseString find(String text, StringType type) {
            try {
                boolean longstring = text.length() > MAX_STRING_LENGTH;
                Criteria c = longstring
                        ? getHSession().createCriteria(BaseLongString.class)
                        : getHSession().createCriteria(BaseString.class);
                c.add(Restrictions.eq("siteId", siteId))
                        .add(Restrictions.isNull("page"))
                        .add(Restrictions.eq("text", text));
                if (type != null) {
                    c.add(Restrictions.eq("type", type));
                }

                IBaseString res = null;
                //potrebbero essercene di più perché la query è case insensitive
                for (Iterator it = c.list().iterator(); it.hasNext();) {
                    IBaseString ibs = (IBaseString) it.next();
                    if (ibs.getText() == null ? text == null : ibs.getText().equals(text)) {
                        res = ibs;
                        break;
                    }
                }
                if (res != null) {
                    return res;
                }
                if (!findParameterStrings || (type != null && type != StringType.Text)) {
                    return null;
                }
                if (baseStringsWithParams == null) {
                    baseStringsWithParams = new ArrayList<IBaseString>();
                    baseStringsWithParams.addAll(getHSession()
                            .createCriteria(BaseString.class)
                            .add(Restrictions.isNull("page"))
                            .add(Restrictions.eq("siteId", siteId))
                            .add(Restrictions.like("text", "%\\%1\\%%"))
                            .list());
                    baseStringsWithParams.addAll(getHSession()
                            .createCriteria(BaseLongString.class)
                            .add(Restrictions.isNull("page"))
                            .add(Restrictions.eq("siteId", siteId))
                            .add(Restrictions.like("text", "%\\%1\\%%"))
                            .list());
                }

                for (IBaseString s : baseStringsWithParams) {
                    if (type != null && s.getType() != type) {
                        continue;
                    }
                    if (Helper.matchBaseString(text, s.getText())) {
                        return s;
                    }
                }
                return null;
            } catch (Exception e) {
                Helper.log("Error finding translation for:\r\n" + text);
                Helper.log(e);
                return null;
            }
        }
    }

    public void translateBlock(Page p, HttpSession session, String url, Translations tt, boolean persist, final long siteId) throws Exception {
        //se lo chiamo, non mi va in append ma in sostituzione (invece voglio che mi lasci le stringhe esistenti,
        //anche se non gliene aggiungo di nuove
        //p.beginUpdate();
        BaseStringFinder bsf = new BaseStringFinder(siteId, true);
        for (Translation t : tt.getTranslations()) {
            p.addBaseString(t.getBaseString(), null, false, bsf);
        }
        if (persist) {
            beginTransaction();
            getHSession().saveOrUpdate(p);
            refreshSite(session, url);
            commit();
        }
    }

    public Page translateBlock(HttpSession session, String url, Translations tt, boolean persist) {

        SplitUrlParameter param = new SplitUrlParameter();
        Helper.splitUrl(url, param);
        final long siteId = getSiteId(session, url);

        try {
            Page p = getPage(param.path, siteId);
            translateBlock(p, session, url, tt, persist, siteId);
            return p;
        } catch (Exception e) {
            Helper.log(e, session);
            try {
                rollback();
            } catch (Exception ex) {
                Helper.log(ex);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void addManySites(HttpServletRequest req, ProcedureController controller) {
        try {
            String longs = "";
            for (int kkkk = 0; kkkk < 200; kkkk++) {
                longs = longs + "*";
            }
            for (int siteIndex = 0; siteIndex < 1000; siteIndex++) {
                Site site = null;
                for (int pageIndex = 0; pageIndex < 50; pageIndex++) {
                    String siteUrl = "http://www.site" + siteIndex + ".it/page" + pageIndex + ".htm";
                    if (site == null) {
                        site = getSite(req.getSession(), siteUrl);
                    }
                    if (site == null) {
                        site = new Site();
                        site.setHost(siteUrl);
                        setSite(req.getSession(), site, true);
                    }
                    if (controller.isAborted()) {
                        return;
                    }
                    controller.log(siteUrl);
                    Translations tt = new Translations();
                    for (int stringIndex = 0; stringIndex < 30; stringIndex++) {
                        String b = "b-site" + siteIndex + "-page" + pageIndex + "-" + stringIndex;
                        if (stringIndex == 0) {
                            b = b + longs;
                        }
                        tt.add(b, "t-site" + siteIndex + "-page" + pageIndex + "-" + stringIndex, false, StringType.Text);
                    }
                    setTranslations(req.getSession(), tt, siteUrl, "en-us", false);
                }
            }



            beginTransaction();
            Page p = new Page();
            p.setPage("root");
            p.setSiteId(13);
            IBaseString bt = p.addBaseString("Ciao", StringType.Text, false, null);
            bt.addTargetString("en", "Hello");
            bt.addTargetString("fr", "Salue");

            getHSession().saveOrUpdate(p);
            commit();

            Criteria c = getHSession().createCriteria(Page.class);
            beginTransaction();
            for (Page pp : (List<Page>) c.list()) {

                bt = p.addBaseString("Ciao", StringType.Text, false, null);
                bt.addTargetString("en", "Hullo");
                getHSession().saveOrUpdate(p);
            }

            commit();
            c = getHSession().createCriteria(Page.class);

            beginTransaction();
            for (Page pp : (List<Page>) c.list()) {

                getHSession().delete(pp);
            }

            commit();
        } catch (Exception ex) {
            Helper.log(ex, req.getSession());
        }
    }

    public ValidationKey saveUserToValidate(User user) throws HibernateException, HibernateException, ClassNotFoundException, Exception {
        beginTransaction();

        getHSession().saveOrUpdate(user);
        Criteria c = getHSession().createCriteria(ValidationKey.class).add(Restrictions.eq("userId", user.getId()));
        ValidationKey key = (ValidationKey) c.uniqueResult();
        if (key
                == null) {
            key = new ValidationKey(user.getId());
            UUID uid = UUID.randomUUID();
            key.setValidationKey(uid.toString());
            key.setDateCreated(new Date());
            getHSession().save(key);
        }

        commit();
        return key;
    }

    public void confirmUser(User user) throws HibernateException, ClassNotFoundException, Exception {
        beginTransaction();
        getHSession().saveOrUpdate(user);
        String hql = "delete from ValidationKey where userId = :id";
        Query query = getHSession().createQuery(hql);
        query.setInteger("id", user.getId());
        query.executeUpdate();
        commit();

    }

    public User getUserByMail(String mail) throws HibernateException, ClassNotFoundException {
        Criteria c = getHSession().createCriteria(User.class).add(Restrictions.eq("mail", mail));
        return (User) c.uniqueResult();
    }

    public User getUserByActivationCode(String code) throws HibernateException, ClassNotFoundException {
        Criteria c = getHSession().createCriteria(ValidationKey.class).add(Restrictions.eq("validationKey", code));
        ValidationKey key = (ValidationKey) c.uniqueResult();
        if (key
                == null) {
            return null;
        }
        c = getHSession().createCriteria(User.class).add(Restrictions.eq("id", key.getUserId()));
        return (User) c.uniqueResult();
    }

    public void saveUser(User user) throws HibernateException, ClassNotFoundException, Exception {
        beginTransaction();
        getHSession().saveOrUpdate(user);
        commit();
        for (Site s : user.getSites()) {
            s.setModified(false);
        }
    }
}
