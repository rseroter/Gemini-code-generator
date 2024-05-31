package com.seroter.demo;

import java.util.List;

public class PromptContext {

    private String folder;
    private List<PromptData> prompts;

    public String getFolder() {
        return folder;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public List<PromptData> getPrompts() {
        return prompts;
    }
    public void setPrompts(List<PromptData> prompts) {
        this.prompts = prompts;
    }
    
}