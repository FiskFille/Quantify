package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

record InterpolateStatement(Value progress, @Nullable VarAddress substitution, JvmFunction body) implements JvmFunction {
    static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public void apply(final MethodVisitor mv) {
        if (progress instanceof NumLiteral(final double value) && value == 0) {
            return;
        }
        if (substitution != null) {
            substitution.set(mv, progress);
        }
        body.apply(mv);
    }

    private static class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
        @Override
        public InterpolateStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
            final Value progress;
            final VarAddress substitution;

            final Token token = parser.next(TokenClass.INTERPOLATE);
            parser.next(TokenClass.OPEN_PARENTHESIS);
            progress = parser.next(ExpressionParser.INSTANCE);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            if (progress instanceof NumLiteral || progress instanceof VarAddress) {
                substitution = null;
            } else {
                try {
                    // Store progress value in a variable if it's not a constant
                    if (context.hasMember(Keywords.INTERPOLATE, MemberType.VARIABLE)) {
                        substitution = context.getMember(Keywords.INTERPOLATE, MemberType.VARIABLE);
                    } else {
                        substitution = context.addLocalVariable(Keywords.INTERPOLATE);
                    }
                } catch (final QtfException e) {
                    throw new QtfParseException(e, token.range());
                }
            }

            final StatementBody body = parser.next(new StatementBody.StatementBodyParser(t -> {
                final Scope scope = t.copy();
                scope.setLerpProgress(substitution != null ? substitution : progress);
                return scope;
            }));
            return new InterpolateStatement(progress, substitution, body);
        }
    }
}
