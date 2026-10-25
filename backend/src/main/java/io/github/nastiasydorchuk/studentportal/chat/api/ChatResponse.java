package io.github.nastiasydorchuk.studentportal.chat.api;

import java.util.List;

public class ChatResponse {

    private String answer;
    private List<String> sources;

    public ChatResponse() {}

    public ChatResponse(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}