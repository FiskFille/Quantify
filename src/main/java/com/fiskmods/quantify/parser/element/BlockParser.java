package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.BlockTree;

import java.util.function.UnaryOperator;

class BlockParser {
    private static BlockTree parseBlock(final QtfParser parser) throws QtfParseException {
        parser.next(TokenClass.OPEN_BRACES);
        final var statements = parser.parse(true);
        parser.next(TokenClass.CLOSE_BRACES);
        return new BlockTree(statements);
    }

    static BlockTree parseBlock(final QtfParser parser, final SyntaxContext context, final Scope scope) throws QtfParseException {
        context.push(scope);
        final BlockTree block = parseBlock(parser);
        context.pop();
        return block;
    }

    static BlockTree parseBlock(final QtfParser parser, final SyntaxContext context, final UnaryOperator<Scope> scope) throws QtfParseException {
        context.push(scope);
        final BlockTree block = parseBlock(parser);
        context.pop();
        return block;
    }

    static BlockTree parseBlock(final QtfParser parser, final SyntaxContext context) throws QtfParseException {
        return parseBlock(parser, context, Scope::copy);
    }
}
