package com.lab.model;

import com.lab.model.visitor.BlockVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentBlock extends Block {
    private int targetVarIndex;      // V зліва (наприклад, індекс 0 для V0)
    
    // Якщо true, то V = Constant. Якщо false, то V = V_source
    private boolean isConstantAssign; 
    
    private Integer sourceVarIndex;  // V справа (використовується, якщо !isConstantAssign)
    private Integer constantValue;   // C (використовується, якщо isConstantAssign)

    @Override
    public void accept(BlockVisitor visitor) {
        visitor.visit(this);
    }
}