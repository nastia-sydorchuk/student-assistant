package io.reflectoring.demo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Setter
@Getter
@Document(collection = "grouped_resources")
public class ResourceCategory {

    @Id
    private String id;

    private String categoryTitle;
    private String description;

    private List<ResourceItem> items;

}