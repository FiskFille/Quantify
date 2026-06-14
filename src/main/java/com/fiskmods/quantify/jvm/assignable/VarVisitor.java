package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.tree.Expression;

public interface VarVisitor {
    void visitGet();

    void visitSet(Expression value, boolean keepResult);

    void visitSet(Runnable value, boolean keepResult);

    void visitInit();

    void visitModify(Expression value, Operator op);

    void visitLerp(Expression value, VarAddress progress, boolean rotational);

    void visitLerpToZero(Expression value, VarAddress progress);
}
