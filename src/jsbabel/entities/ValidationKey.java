package jsbabel.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import jsbabel.RequiredField;

@Entity
@Table(name = "VALIDATIONKEYS")
public class ValidationKey implements Serializable {

    @Id
    @RequiredField
    @Column(name = "USERID")
    private int userId;
    @Column(name = "VALIDATIONKEY")
    @RequiredField
    private String validationKey;
    @Column(name = "DATECREATED")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateCreated;
    public ValidationKey() {
    }

    public ValidationKey(int userId) {
        this.userId = userId;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the validationKey
     */
    public String getValidationKey() {
        return validationKey;
    }

    /**
     * @param validationKey the validationKey to set
     */
    public void setValidationKey(String validationKey) {
        this.validationKey = validationKey;
    }

    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    

}
