package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Value;

public interface VarVisitor {
    void visitGet();

    void visitSet(Value value);

    void visitInit();

    void visitModify(Value value, Operator op);

    void visitLerp(Value value, Value progress, boolean rotational);

    void visitLerpToZero(Value progress);
}
