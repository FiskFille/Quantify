package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenStream;
import com.fiskmods.quantify.parser.element.SyntaxSelector;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QtfParser implements TokenStream {
    private final TokenStream tokens;
    private final SyntaxContext context;

    public QtfParser(final TokenStream tokens, final SyntaxContext context) {
        this.tokens = tokens;
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

    @Override
    public void clearPeekedToken() {
        tokens.clearPeekedToken();
    }

    @Override
    public Token peek() {
        return tokens.peek();
    }

    @Nullable
    @Override
    public Token last() {
        return tokens.last();
    }

    @Override
    public boolean hasNext() {
        return tokens.hasNext();
    }

    @Override
    public boolean hasNext(final Boundary boundary) {
        return tokens.hasNext(boundary);
    }

    @Override
    public Token next() {
        return tokens.next();
    }

    @Override
    public Token next(final TokenClass expectedClass) throws QtfParseException {
        return tokens.next(expectedClass);
    }

    @Override
    public boolean isNext(final TokenClass expectedClass) {
        return tokens.isNext(expectedClass);
    }

    @Override
    public boolean isNext(final TokenClass expectedClass, final Object expectedValue) {
        return tokens.isNext(expectedClass, expectedValue);
    }

    @Override
    public boolean skip(final TokenClass tokenClass) {
        return tokens.skip(tokenClass);
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
