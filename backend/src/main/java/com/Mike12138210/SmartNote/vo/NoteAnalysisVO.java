package com.Mike12138210.SmartNote.vo;

import java.util.List;

public class NoteAnalysisVO {
    private String summary;
    private List<String> keyPoints;

    public NoteAnalysisVO(String summary,List<String> keyPoints){
        this.summary = summary;
        this.keyPoints = keyPoints;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }
}
