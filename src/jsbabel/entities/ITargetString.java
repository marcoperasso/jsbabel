package jsbabel.entities;

public interface ITargetString {

    public abstract String getLocale();

    public abstract void setLocale(String locale);

    public abstract String getText();

    public abstract void setText(String text);
}