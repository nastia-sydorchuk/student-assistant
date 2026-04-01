package io.reflectoring.demo.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "grouped_resources")
public class ResourceCategory {

    @Id
    private String id;

    private String categoryTitle;
    private String description;

    private List<ResourceItem> items;

    private String categoryId; // e.g., "academic"
    private String language;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoryTitle() { return categoryTitle; }
    public void setCategoryTitle(String categoryTitle) { this.categoryTitle = categoryTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ResourceItem> getItems() { return items; }
    public void setItems(List<ResourceItem> items) { this.items = items; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}