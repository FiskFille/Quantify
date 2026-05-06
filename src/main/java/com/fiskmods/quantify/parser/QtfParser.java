package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.IteratorTokenStream;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.element.SyntaxSelector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QtfParser extends IteratorTokenStream {
    private final SyntaxContext context;

    public QtfParser(final Iterator<Token> tokens, final SyntaxContext context) {
        super(tokens);
        this.context = context;
    }

    public SyntaxTree parse(final boolean isEnclosed) throws QtfParseException {
        final List<JvmFunction> elements = new ArrayList<>(64);
        final int stack = context.stackDepth();

        while (hasNext()) {
            if (isEnclosed && peek().type() == TokenClass.CLOSE_BRACES) {
                break;
            }
            if (peek().type() == TokenClass.TERMINATOR) {
                clearPeekedToken();
                continue;
            }

            final SyntaxParser<?> syntax = SyntaxSelector.selectSyntax(context, peek());
            final JvmFunction element = syntax.accept(this, context);
            if (element != null) {
                elements.add(element);
            }
        }

        final int currentStack = context.stackDepth();
        if (stack != currentStack) {
            final Token last = last();
            throw new QtfParseException("Unbalanced stack: " + currentStack, "expected" + stack,
                    last != null ? last.range() : new Token.Range(0, 0)
            );
        }

        return SyntaxTree.of(elements);
    }

    public void expectLineBreak() throws QtfParseException {
        if (hasNext()) {
            next(TokenClass.TERMINATOR);
        }
    }

    public <T extends JvmFunction> List<T> nextSequence(final SyntaxParser<T> syntaxParser, final TokenClass delimiter)
            throws QtfParseException {
        final List<T> list = new ArrayList<>();
        while (true) {
            list.add(syntaxParser.accept(this, context));

            if (isNext(delimiter)) {
                clearPeekedToken();
                continue;
            }
            break;
        }
        return list;
    }
}
