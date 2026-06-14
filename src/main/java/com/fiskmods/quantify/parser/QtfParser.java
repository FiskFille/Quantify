package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.lexer.token.TokenStream;
import com.fiskmods.quantify.parser.tree.Statement;
import com.fiskmods.quantify.parser.tree.TreeGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class QtfParser extends TreeGenerator {
    private final Deque<Token.Range> treeStack = new ArrayDeque<>();

    private final TokenStream tokens;
    private final StatementParser stmt;

    public QtfParser(final TokenStream tokens) {
        this.tokens = tokens;

        final ExpressionParser expr = new ExpressionParser(this, tokens);
        stmt = new StatementParser(this, tokens, expr);
    }

    public List<Statement> parse(final boolean isEnclosed) throws QtfParseException {
        final List<Statement> statements = new ArrayList<>(64);

        while (tokens.hasNext() && !(isEnclosed && tokens.isNext(TokenClass.CLOSE_BRACES))) {
            if (!tokens.consume(TokenClass.TERMINATOR)) {
                statements.add(stmt.parseStatement());
            }
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
}
