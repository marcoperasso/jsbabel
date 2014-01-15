package jsbabel.entities;

public interface IBaseString {

    public abstract long getId();

    public ITargetString getTargetString(String targetLocale);

    public abstract String getText();

    public abstract void setText(String text);

    public abstract StringType getType();

    public abstract void setType(StringType type);

    public abstract long getSiteId();

    public abstract void setSiteId(long siteId);

    public abstract Page getPage();

    public abstract void setPage(Page page);

    public abstract ITargetString addTargetString(String locale, String text);

    public abstract void beginUpdate();

    public boolean isPageSpecific();

    public boolean hasTranslations();

    public interface IBaseStringFinder {

        IBaseString find(String text, StringType type);
    }
}
