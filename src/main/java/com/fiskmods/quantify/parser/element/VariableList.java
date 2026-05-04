package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

record VariableList<T extends VarAddress>(T[] addresses) implements Assignable {
    @Override
    public void apply(final MethodVisitor mv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final MethodVisitor mv, final Value value) {
        JvmUtil.set(mv, addresses, value);
    }

    @Override
    public void init(final MethodVisitor mv) {
        for (final VarAddress address : addresses) {
            address.init(mv);
        }
    }

    @Override
    public void modify(final MethodVisitor mv, final Value value, final Operator op) {
        JvmUtil.modify(mv, addresses, value, op);
    }

    @Override
    public void lerp(final MethodVisitor mv, final Value value, final Value progress, final boolean rotational) {
        JvmUtil.lerp(mv, addresses, progress, rotational, value);
    }

    @SuppressWarnings("unchecked")
    static <T extends VarAddress> SyntaxParser<VariableList<T>> parse(final T firstVar, final int modifiers) {
        return (parser, context) -> {
            final List<T> list = new ArrayList<>();
            list.add(firstVar);
            do {
                parser.clearPeekedToken();
                list.add((T) Assignable.nextVariable(parser, firstVar.type(), modifiers));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList<>(list.toArray((T[]) new VarAddress[0]));
        };
    }
}
