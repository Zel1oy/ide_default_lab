package com.lab.model;

import com.lab.model.visitor.BlockVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StartBlock extends Block {
    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}