package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

record IfStatement(Value condition, JvmFunction body, @Nullable JvmFunction elseBody) implements JvmFunction {
    static final SyntaxParser<IfStatement> PARSER = new IfStatementParser();

    @Override
    public void apply(final MethodVisitor mv) {
        if (condition instanceof NumLiteral(final double value)) {
            if (value > 0) {
                body.apply(mv);
            }
            return;
        }

        final Label end = new Label();
        condition.apply(mv);
        mv.visitInsn(D2I);

        if (elseBody == null) {
            mv.visitJumpInsn(IFLE, end);
            body.apply(mv);
        } else {
            final Label els = new Label();
            mv.visitJumpInsn(IFLE, els);
            body.apply(mv);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(els);
            elseBody.apply(mv);
        }
        mv.visitLabel(end);
    }

    private static class IfStatementParser implements SyntaxParser<IfStatement> {
        @Override
        public IfStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
            parser.next(TokenClass.IF);
            parser.next(TokenClass.OPEN_PARENTHESIS);
            final Value condition = ExpressionParser.INSTANCE.accept(parser, context);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            final StatementBody body = StatementBody.PARSER.accept(parser, context);
            JvmFunction elseBody = null;
            parser.skip(TokenClass.TERMINATOR);

            if (parser.isNext(TokenClass.ELSE)) {
                parser.clearPeekedToken();
                if (parser.isNext(TokenClass.IF)) {
                    elseBody = this.accept(parser, context);
                } else {
                    elseBody = StatementBody.PARSER.accept(parser, context);
                }
            }
            return new IfStatement(condition, body, elseBody);
        }
    }
}
