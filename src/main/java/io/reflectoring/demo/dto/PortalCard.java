package io.reflectoring.demo.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ui_config")
public class PortalCard {

    @Id
    private String id;
    private String title;
    private String route;
    private String icon;
    private String description;

    private String cardId;
    private String language;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}