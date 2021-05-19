package com.example.PlacementPortal.Entities;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name="posting_seq", initialValue=1, allocationSize=1)
public class Posting {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posting_seq")
    private long id;

    private String companyName;

    private String postingTitle;

    private String description;

    private String postingType;

    private String eligibleCGPA;

    private String requirements;

    private long companyId;

    private String companyEmail;

    private LocalDate postedOn;

    private String deadlineStr;

    private LocalDate deadline;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPostingTitle() {
        return postingTitle;
    }

    public void setPostingTitle(String postingTitle) {
        this.postingTitle = postingTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostingType() {
        return postingType;
    }

    public void setPostingType(String postingType) {
        this.postingType = postingType;
    }

    public String getEligibleCGPA() {
        return eligibleCGPA;
    }

    public void setEligibleCGPA(String eligibleCGPA) {
        this.eligibleCGPA = eligibleCGPA;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public LocalDate getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(LocalDate postedOn) {
        this.postedOn = postedOn;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getDeadlineStr() {
        return deadlineStr;
    }

    public void setDeadlineStr(String deadlineStr) {
        this.deadlineStr = deadlineStr;
    }
}