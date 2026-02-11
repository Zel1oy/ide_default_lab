package com.lab.model;

import com.lab.model.visitor.BlockVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionBlock extends Block {
    public enum Operator { EQUALS, LESS_THAN }

    private int leftVarIndex;   // V
    private Operator operator;  // == або <
    private int rightConstant;  // C

    // nextBlockId (з батьківського класу) - це шлях TRUE (Yes)
    // falseNextBlockId - це шлях FALSE (No)
    private Integer falseNextBlockId;

    public ConditionBlock() {
        // Значення за замовчуванням
        this.operator = Operator.LESS_THAN;
        this.leftVarIndex = 0;
        this.rightConstant = 0;
    }

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}