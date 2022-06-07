package com.example.servermaintenance.course.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class SchemaDto {
    @NotEmpty(message = "Course has to have parts")
    private List<SchemaPartDto> parts = new ArrayList<>();

    private Set<SchemaPart> removedEntities = new HashSet<>();

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
