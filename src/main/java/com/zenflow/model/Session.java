package com.zenflow.model;

public class Session {
    private Integer id;
    private long startTs;
    private Long endTs;
    private String type;
    private int completed;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public long getStartTs() { return startTs; }
    public void setStartTs(long startTs) { this.startTs = startTs; }
    public Long getEndTs() { return endTs; }
    public void setEndTs(Long endTs) { this.endTs = endTs; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }
}
