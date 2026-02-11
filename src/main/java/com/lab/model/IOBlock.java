package com.lab.model;

import com.lab.model.visitor.BlockVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IOBlock extends Block {
    public enum IOType { INPUT, PRINT }

    private IOType ioType;
    private int varIndex; // Змінна V

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}