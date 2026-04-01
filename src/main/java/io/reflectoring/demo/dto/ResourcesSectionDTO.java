package io.reflectoring.demo.dto;

import java.util.List;

public class ResourcesSectionDTO {
    private String title;
    private List<String> items;
    private String source;

    public ResourcesSectionDTO(String title, List<String> items, String source) {
        this.title = title;
        this.items = items;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }
    public List<String> getItems() {
        return items;
    }
    public String getSource() {
        return source;
    }
}
