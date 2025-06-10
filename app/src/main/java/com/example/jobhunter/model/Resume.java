package com.example.jobhunter.model;

import com.example.jobhunter.util.constant.ResumeStateEnum;

public class Resume {
    private long id;
    private String email;
    private String url;
    private ResumeStateEnum status; // Enum dạng String: "PENDING", "APPROVED", "REJECTED", ...
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
    private User user;
    private Job job;

    public Resume(long id, String email, String url, ResumeStateEnum status, String createdAt, String updatedAt, String createdBy, String updatedBy, User user, Job job) {
        this.id = id;
        this.email = email;
        this.url = url;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.user = user;
        this.job = job;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResumeStateEnum getStatus() {
        return status;
    }

    public void setStatus(ResumeStateEnum status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
