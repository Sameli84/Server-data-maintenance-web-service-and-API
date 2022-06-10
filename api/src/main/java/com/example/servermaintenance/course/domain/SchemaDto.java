package com.example.servermaintenance.course.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.*;

@Data
public class SchemaDto {
    @NotEmpty(message = "Course has to have parts")
    private List<SchemaPartDto> parts = new ArrayList<>();

    private LinkedHashSet<SchemaPart> removedEntities = new LinkedHashSet<>();

    private int selectedIndex = 0;

    public void addPart(SchemaPartDto part) {
        this.parts.add(part);
    }

    public int size() {
        return this.parts.size();
    }

    public void markForRemoval(SchemaPart entity) {
        this.removedEntities.add(entity);
    }
}
