package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.ResourceCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/grouped_resources")
public class ResourceController {

    @Autowired
    private ResourceRepository repository;

    /**
     * Goal: Fetch all categories
     */
    @GetMapping
    public List<ResourceCategory> getAllCategories() {
        return repository.findAll();
    }

    /**
     * Goal: Fetch exactly ONE category bucket by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceCategory> getCategoryById(@PathVariable String id) {
        Optional<ResourceCategory> category = repository.findById(id);

        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}