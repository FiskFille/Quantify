package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenStream;
import com.fiskmods.quantify.parser.element.SyntaxSelector;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.TreeGenerator;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class QtfParser extends TreeGenerator implements TokenStream {
    private final Deque<Token.Range> treeStack = new ArrayDeque<>();

    private final TokenStream tokens;

    public QtfParser(final TokenStream tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse(final boolean isEnclosed) throws QtfParseException {
        final List<Statement> statements = new ArrayList<>(64);

        while (hasNext()) {
            if (isEnclosed && peek().type() == TokenClass.CLOSE_BRACES) {
                break;
            }
            if (peek().type() == TokenClass.TERMINATOR) {
                clearPeekedToken();
                continue;
            }

            final SyntaxParser<? extends Statement> syntax = SyntaxSelector.selectSyntax(peek());
            final Statement statement = syntax.accept(this);
            statements.add(statement);
        }
        return statements;
    }

    @Override
    public void startTree() {
        if (tokens.hasNext()) {
            treeStack.push(tokens.peek().range());
        }
    }

    @Override
    protected Token.Range finishTree() {
        final Token.Range start = treeStack.pop();
        final Token last = tokens.last();
        return last != null ? start.union(last.range()) : start;
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

    public <T> List<T> nextSequence(final SyntaxParser<T> syntaxParser, final TokenClass delimiter)
            throws QtfParseException {
        final List<T> list = new ArrayList<>();
        while (true) {
            list.add(syntaxParser.accept(this));

            if (isNext(delimiter)) {
                clearPeekedToken();
                continue;
            }
            break;
        }
        return list;
    }
}
