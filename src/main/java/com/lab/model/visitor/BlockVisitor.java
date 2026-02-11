package com.lab.model.visitor;

import com.lab.model.*;

public interface BlockVisitor {
    void visit(StartBlock block);
    void visit(StopBlock block);
    void visit(AssignmentBlock block);
    void visit(ConditionBlock block);
    void visit(IOBlock block);
}