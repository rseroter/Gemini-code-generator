package com.seroter.demo;

public class PromptData {

    private String fileName;
    private String prompt;

    public PromptData() {}

    public PromptData(String fileName, String prompt) {
        this.fileName = fileName;
        this.prompt = prompt;
    }
    public String getFileName() {
        return fileName;
    }
    public String getPrompt() {
        return prompt;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

}
