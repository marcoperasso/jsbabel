package jsbabel.entities;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;
import jsbabel.Const;
import jsbabel.Helper;
import jsbabel.IIDConsumer;
import jsbabel.Messages;
import jsbabel.HiddenField;
import jsbabel.RequiredField;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "USERS")
public class User implements IIDConsumer, Serializable {

    @Id
    @GenericGenerator(name = "seq_id", strategy = "jsbabel.IDGenerator")
    @GeneratedValue(generator = "seq_id")
    @Column(name = "ID")
    @HiddenField
    private Integer id;
    @Column(name = "MAIL")
    @RequiredField
    private String mail;
    @Column(name = "NAME")
    @RequiredField
    private String name;
    @Column(name = "GENDER")
    @RequiredField
    private Gender gender;
    @Column(name = "TAXCODE")
    private String taxCode;
    @Column(name = "BIRTHDATE")
    @Temporal(TemporalType.DATE)
    private Date birthDate;
    @Column(name = "SURNAME")
    @RequiredField
    private String surname;
    @Column(name = "COMPANY")
    private String company;
    @Column(name = "PASSWORD")
    @HiddenField
    private String password;
    @Column(name = "ADDRESS")
    @RequiredField
    private String address;
    @Column(name = "CITY")
    private String city;
    @Column(name = "COUNTRY")
    @RequiredField
    private String country;
    @Column(name = "ZIP")
    @RequiredField
    private String zip;
    @Column(name = "TYPE")
    @RequiredField
    @HiddenField
    private UserType type;
    @Column(name = "ACTIVE")
    @HiddenField
    private boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER, orphanRemoval = true)
    @HiddenField
    private Set<Site> sites = new HashSet<Site>();

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 59 * hash + (this.mail != null ? this.mail.hashCode() : 0);
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
        final User other = (User) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if ((this.mail == null) ? (other.mail != null) : !this.mail.equals(other.mail)) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the type
     */
    public UserType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(UserType type) {
        this.type = type;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the sites
     */
    public Set<Site> getSites() {
        return sites;
    }

    /**
     * @param sites the sites to set
     */
    public void setSites(Set<Site> sites) {
        this.sites = sites;
    }

    public void sendActivationMail(ValidationKey key, HttpServletRequest request) throws MalformedURLException {
        URL u = new URL(request.getRequestURL().toString());

        URL newUrl = new URL(
                u.getProtocol(), 
                u.getHost(), 
                u.getPort(), 
                "/completeregistration.jsp?" + Const.ActivationParam + "=" + key.getValidationKey()+ "&" + Const.RedirectUrlParam + "=/registrationcompleted.jsp");
        String body = String.format(Messages.MailRegistrationBody, newUrl);
        Helper.sendMail(mail, Messages.MailRegistrationSubject, body);
    }

    public void sendResetPwdMail(ValidationKey key, HttpServletRequest request) throws MalformedURLException {
        URL u = new URL(request.getRequestURL().toString());

        URL newUrl = new URL(
                u.getProtocol(),
                u.getHost(), 
                u.getPort(), 
                "/completeregistration.jsp?" + Const.ActivationParam + "=" + key.getValidationKey() + "&" + Const.RedirectUrlParam + "=/passwordchanged.jsp");
        String body = String.format(Messages.MailResetPwdBody, newUrl);
        Helper.sendMail(mail, Messages.MailResetPwdSubject, body);
    }

    public Site getSite(int siteId) {
        for (Site s : sites) {
            if (s.getId() == siteId) {
                return s;
            }
        }
        return null;
    }

    /**
     * @return the gender
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * @return the taxCode
     */
    public String getTaxCode() {
        return taxCode;
    }

    /**
     * @param taxCode the taxCode to set
     */
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    /**
     * @return the birthDate
     */
    public Date getBirthDate() {
        return birthDate;
    }

    /**
     * @param birthDate the birthDate to set
     */
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public long getContextId() {
        return 0;
    }
}
