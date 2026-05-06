package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.SyntaxTree;
import com.fiskmods.quantify.parser.tree.FunctionBody;

record FunctionBodyParser(Scope scope) implements SyntaxParser<FunctionBody> {
    @Override
    public FunctionBody accept(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.OPEN_BRACES);
        context.push(scope);

        final SyntaxTree syntaxTree = parser.parse(true);

        context.pop();
        parser.next(TokenClass.CLOSE_BRACES);
        return new FunctionBody(syntaxTree);
    }
}
