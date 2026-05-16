package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.BlockStatement;

import java.util.function.UnaryOperator;

class BlockParser {
    private static BlockStatement parseBlock(final QtfParser parser) throws QtfParseException {
        parser.startTree();
        parser.next(TokenClass.OPEN_BRACES);
        final var statements = parser.parse(true);
        parser.next(TokenClass.CLOSE_BRACES);
        return parser.newBlockStatement(statements);
    }

    static BlockStatement parseBlock(final QtfParser parser, final SyntaxContext context, final Scope scope) throws QtfParseException {
        context.push(scope);
        final BlockStatement block = parseBlock(parser);
        context.pop();
        return block;
    }

    static BlockStatement parseBlock(final QtfParser parser, final SyntaxContext context, final UnaryOperator<Scope> scope) throws QtfParseException {
        context.push(scope);
        final BlockStatement block = parseBlock(parser);
        context.pop();
        return block;
    }

    static BlockStatement parseBlock(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        return parseBlock(parser, context, Scope::copy);
    }
}
