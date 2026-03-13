package io.reflectoring.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceItem {

    private String title;
    private String icon;
    private String description;
    private String actionText;
    private String url;

    // Optional fields that only some items use
    private String label;
    private String badgeClass;
    private String location;
    private String contact;

}