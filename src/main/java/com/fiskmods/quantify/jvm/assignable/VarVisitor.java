package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;

public interface VarVisitor {
    void visitGet();

    void visitSet(Expression value);

    void visitInit();

    void visitModify(Expression value, Operator op);

    void visitLerp(Expression value, Expression progress, boolean rotational);

    void visitLerpToZero(Expression progress);
}
