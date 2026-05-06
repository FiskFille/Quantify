package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.SyntaxTree;
import com.fiskmods.quantify.parser.tree.StatementBody;

import java.util.function.UnaryOperator;

record StatementBodyParser(UnaryOperator<Scope> scope) implements SyntaxParser<StatementBody> {
    static final StatementBodyParser PARSER = new StatementBodyParser(Scope::copy);

    static StatementBodyParser parser(final Namespace namespace) {
        return new StatementBodyParser(t -> t.copy(namespace));
    }

    @Override
    public StatementBody accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.OPEN_BRACES);
        context.push(scope);

        parser.clearPeekedToken();
        final SyntaxTree syntaxTree = parser.parse(true);

        context.pop();
        parser.next(TokenClass.CLOSE_BRACES);
        return new StatementBody(syntaxTree);
    }
}
