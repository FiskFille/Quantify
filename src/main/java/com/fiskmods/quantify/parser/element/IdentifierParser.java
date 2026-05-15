package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.NumLiteral;

import java.util.Optional;

class IdentifierParser {
    static final SyntaxParser<?> LINE_START = from(IdentifierParser::lineStart);
    static final SyntaxParser<Expression> ANY_VALUE = from(IdentifierParser::anyValue);

    @FunctionalInterface
    interface ParserSupplier<T> {
        SyntaxParser<T> apply(String name, Token.Range range, Namespace namespace);

        default NameParser<T> asNameParser(final QtfParser parser, final SyntaxContext context) {
            return (name, range, namespace) -> apply(name, range, namespace).accept(parser, context);
        }
    }

    @FunctionalInterface
    interface NameParser<T> {
        T parse(String name, Token.Range range, Namespace namespace) throws QtfParseException;
    }

    static <T> SyntaxParser<T> from(final ParserSupplier<T> nextParser) {
        return (parser, context) -> parseIdentifier(parser, context, nextParser.asNameParser(parser, context));
    }

    static <T> T parseIdentifier(final QtfParser parser, final SyntaxContext context, final NameParser<T> nextParser) throws QtfParseException {
        final Token identifier = parser.next(TokenClass.IDENTIFIER);
        final String name = identifier.getString();

        if (!parser.isNext(TokenClass.DOT)) {
            return nextParser.parse(name, identifier.range(), context.namespace());
        }

        parser.clearPeekedToken();
        final Token child = parser.next(TokenClass.IDENTIFIER);

        if (Keywords.THIS.equals(name)) {
            final String childName = child.getString();
            final Token.Range range = identifier.range().union(child.range());

            return nextParser.parse(childName, range, context.getDefaultNamespace());
        }

        final Optional<MemberMap.Member<?>> parent = context.findMember(name);
        if (parent.isPresent()) {
            final MemberType<?> parentType = parent.get().type();

            if (parentType == MemberType.LIBRARY) {
                final Namespace namespace = ((QtfLibrary) parent.get().value()).namespace();
                final String childName = child.getString();
                final Token.Range range = identifier.range().union(child.range());

                return nextParser.parse(childName, range, namespace);
            }

            if (parentType == MemberType.VARIABLE && ((VarAddress) parent.get().value()).is(VarType.STRUCT)) {
                final Struct struct = (Struct) parent.get().value();
                return parseStruct(parser, struct, child, identifier.range(), nextParser);
            }

            throw QtfParseException.error("expected '%s' to be a %s, was %s".formatted(name, MemberType.LIBRARY.name(), parentType.name()), identifier.range());
        }
        throw QtfParseException.error("undefined library '%s'".formatted(name), identifier.range());
    }

    private static SyntaxParser<?> lineStart(final String name, final Token.Range range, final Namespace namespace) {
        return FunctionRefParser.tryParse(name, range, namespace, false)
                .or((parser, context) -> AssignmentParser.parseAssignment(parser, context, name, range, namespace));
    }

    @SuppressWarnings("unchecked")
    static SyntaxParser<Expression> anyValue(final String name, final Token.Range range, final Namespace namespace) {
        return (SyntaxParser<Expression>) FunctionRefParser.tryParse(name, range, namespace, true)
                .or((parser, context) -> {
                    try {
                        if (namespace.hasConstant(name)) {
                            return new NumLiteral(namespace.getConstant(name));
                        }
                        return namespace.computeVariable(VarType.NUM, name, 0);
                    } catch (final QtfException e) {
                        throw new QtfParseException(e, range);
                    }
                });
    }

    private static <T> T parseStruct(final QtfParser parser, final Struct struct, Token child, Token.Range range, final NameParser<T> nextParser) throws QtfParseException {
        final StringBuilder name = new StringBuilder(child.getString());
        final Token.Range firstRange = child.range();

        while (parser.isNext(TokenClass.DOT)) {
            parser.clearPeekedToken();
            child = parser.next(TokenClass.IDENTIFIER);
            final String childName = child.getString();

            try {
                struct.expand(name.toString());
            } catch (final QtfException e) {
                throw new QtfParseException(e, firstRange);
            }

            name.append('.').append(childName);
        }

        range = range.union(child.range());
        return nextParser.parse(name.toString(), range, struct);
    }
}
