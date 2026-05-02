package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.SyntaxTree;
import org.objectweb.asm.MethodVisitor;

record FunctionBody(SyntaxTree tree) implements JvmFunction {
    @Override
    public void apply(final MethodVisitor mv) {
        tree.apply(mv);
    }

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
}
