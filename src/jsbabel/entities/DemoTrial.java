package jsbabel.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DEMOTRIALS")
public class DemoTrial implements Serializable {

    
    public enum TrialType {
        TRANSLATE, XLIFF
    }
    @Column(name = "PAGE")
    @Id
    private String page;
    @Id
    private TrialType trialType;
    @Column(name = "TRIALS")
    private int trials = 0;
    @Column(name = "TRIALTYPE")
    
    /**
     * @return the page
     */
    public String getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * @return the trials
     */
    public int getTrials() {
        return trials;
    }

    /**
     * @param trials the trials to set
     */
    public void setTrials(int trials) {
        this.trials = trials;
    }

    public void increaseTrials() {
        this.trials++;
    }
    
    /**
     * @return the trialType
     */
    public TrialType getTrialType() {
        return trialType;
    }

    /**
     * @param trialType the trialType to set
     */
    public void setTrialType(TrialType trialType) {
        this.trialType = trialType;
    }

}