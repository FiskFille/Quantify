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

    public void parse(final SyntaxTree syntaxTree, final boolean isEnclosed) throws QtfParseException {
        while (hasNext()) {
            if (isEnclosed && peek().type() == TokenClass.CLOSE_BRACES) {
                return;
            }
            if (peek().type() == TokenClass.TERMINATOR) {
                clearPeekedToken();
                continue;
            }

            final SyntaxParser<?> syntax = SyntaxSelector.selectSyntax(context, peek());
            final JvmFunction element = next(syntax);
            if (element != null) {
                syntaxTree.elements().add(element);
            }
        }
    }

    public void expectLineBreak() throws QtfParseException {
        if (hasNext()) {
            next(TokenClass.TERMINATOR);
        }
    }

    public <T extends JvmFunction> T next(final SyntaxParser<T> syntaxParser) throws QtfParseException {
        return syntaxParser.accept(this, context);
    }

    public <T extends JvmFunction> List<T> nextSequence(final SyntaxParser<T> syntaxParser, final TokenClass delimiter)
            throws QtfParseException {
        final List<T> list = new ArrayList<>();
        while (true) {
            list.add(next(syntaxParser));

            if (isNext(delimiter)) {
                clearPeekedToken();
                continue;
            }
            break;
        }
        return list;
    }
}
