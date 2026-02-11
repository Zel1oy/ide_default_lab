package com.lab.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lab.model.visitor.BlockVisitor;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StartBlock.class, name = "START"),
    @JsonSubTypes.Type(value = StopBlock.class, name = "STOP"),
    @JsonSubTypes.Type(value = AssignmentBlock.class, name = "ASSIGN"),
    @JsonSubTypes.Type(value = ConditionBlock.class, name = "CONDITION"),
    @JsonSubTypes.Type(value = IOBlock.class, name = "IO")
})
public abstract class Block implements Serializable {
    private int id;                 // Унікальний ID блоку
    private Integer nextBlockId;    // ID наступного блоку (може бути null, якщо це кінець)
    private double x;
    private double y;

    public abstract void accept(BlockVisitor visitor);

    @JsonIgnore
    public String getType() {
        return this.getClass().getAnnotation(JsonSubTypes.Type.class) != null ?
                "BLOCK" : this.getClass().getSimpleName().replace("Block", "").toUpperCase();
    }
}