package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.*;

import java.util.Optional;

class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
    static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public InterpolateStatement accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.startTree();
        final Expression finalProgress;
        VarAddress substitution = null;

        final Token token = parser.next(TokenClass.INTERPOLATE);
        final Expression progress = ExpressionParser.INSTANCE.accept(parser, context);
        parser.skip(TokenClass.TERMINATOR);

        if (progress instanceof NumLiteral || progress instanceof VarAddress) {
            finalProgress = progress;
        } else {
            try {
                final Optional<VarAddress> var = context.scope().members.find(Keywords.INTERPOLATE, MemberType.VARIABLE);

                // Store progress value in a variable if it's not a constant
                if (var.isPresent()) {
                    substitution = var.get().cast(Keywords.INTERPOLATE, VarType.NUM);
                } else {
                    substitution = context.scope().addLocalVariable(Keywords.INTERPOLATE);
                }

                final Identifier identifier = Identifier.of(Keywords.INTERPOLATE, token.range());
                finalProgress = parser.newVariableRef(identifier, substitution, false);
            } catch (final QtfException e) {
                throw new QtfParseException(e, token.range());
            }
        }

        final BlockStatement body = BlockParser.parseBlock(parser, context, t -> {
            final Scope scope = t.copy();
            scope.setLerpProgress(finalProgress);
            return scope;
        });
        return parser.newInterpolateStatement(progress, substitution, body);
    }
}
