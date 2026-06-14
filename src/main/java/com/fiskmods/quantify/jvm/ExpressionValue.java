package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfErrors;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.tree.AbstractMemberExpression;
import com.fiskmods.quantify.parser.tree.Expression;
import com.fiskmods.quantify.parser.tree.FunctionRef;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Optional;

public interface ExpressionValue {
    Expression expression();

    void validate(@Nullable Type expectedType) throws QtfException;

    default ExpressionValue select(final AbstractMemberExpression expression, final String memberName) throws QtfException {
        return new SelectImpl(expression, evaluateAsNamespace(), memberName, false);
    }

    default ExpressionValue invoke(final FunctionRef ref) throws QtfException {
        return new InvokeImpl(ref, evaluateAs(MemberType.FUNCTION));
    }

    Namespace evaluateAsNamespace() throws QtfException;

    <T> T evaluateAs(MemberType<T> type) throws QtfException;

    static ExpressionValue num(final Expression expression) {
        return new NumImpl(expression);
    }

    static ExpressionValue _this(final AbstractMemberExpression expression, final SyntaxContext context) {
        return new NamespaceImpl(expression, context.getDefaultNamespace());
    }

    static ExpressionValue getMember(final AbstractMemberExpression expression, final SyntaxContext context, final String memberName) {
        final Namespace namespace = context.namespace().element();
        return new SelectImpl(expression, namespace, memberName, false);
    }

    private static void validate(final Type expectedType, final Type actualType, final String memberName) throws QtfException {
        if (expectedType != actualType) {
            throw new QtfException("Expected '%s' to be of type %s, was %s".formatted(memberName, expectedType, actualType));
        }
    }

    private static void validate(final Type expectedType, final Type actualType) throws QtfException {
        if (expectedType != actualType) {
            throw new QtfException("Expected value to be of type %s, was %s".formatted(expectedType, actualType));
        }
    }

    record NumImpl(Expression expression) implements ExpressionValue {
        @Override
        public void validate(final @Nullable Type expectedType) throws QtfException {
            if (expectedType != null) {
                ExpressionValue.validate(expectedType, Type.DOUBLE_TYPE);
            }
        }

        @Override
        public Namespace evaluateAsNamespace() throws QtfException {
            throw QtfErrors.undefined(MemberType.LIBRARY, expression.toString());
        }

        @Override
        public <T> T evaluateAs(final MemberType<T> type) throws QtfException {
            throw QtfErrors.undefined(type, expression.toString());
        }
    }

    record NamespaceImpl(AbstractMemberExpression expression, Namespace namespace) implements ExpressionValue {
        @Override
        public void validate(final @Nullable Type expectedType) {
            expression.member = namespace;
        }

        @Override
        public Namespace evaluateAsNamespace() {
            return namespace;
        }

        @Override
        public <T> T evaluateAs(final MemberType<T> type) throws QtfException {
            throw QtfErrors.undefined(type, Keywords.THIS);
        }
    }

    record SelectImpl(AbstractMemberExpression expression, Namespace namespace, String memberName, boolean isPartOfStruct) implements ExpressionValue {
        @Override
        public void validate(final @Nullable Type expectedType) throws QtfException {
            final var member = namespace.find(memberName);
            if (member.isPresent()) {
                final MemberMap.Member<?> m = member.get();
                final Type actualType = m.value() instanceof final VarAddress v
                        ? v.type().internal()
                        : m.type().internal();

                if (expectedType != null) {
                    ExpressionValue.validate(expectedType, actualType, memberName);
                }
                expression.member = m.value();
                return;
            }

            if (expectedType == VarType.STRUCT.internal()) {
                expression.member = namespace.computeVariable(VarType.STRUCT, memberName);
            } else if (expectedType != null) {
                expression.member = namespace.computeVariable(VarType.NUM, memberName);
            }
        }

        @Override
        public ExpressionValue select(final AbstractMemberExpression expression, final String memberName) throws QtfException {
            return new SelectImpl(expression, evaluateAsNamespace(), memberName, isPartOfStruct || namespace instanceof Struct);
        }

        @Override
        public Namespace evaluateAsNamespace() throws QtfException {
            final Optional<QtfLibrary> library = namespace.find(memberName, MemberType.LIBRARY);
            if (library.isPresent()) {
                return library.get().namespace();
            }
            return namespace.computeVariable(VarType.STRUCT, memberName);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T evaluateAs(final MemberType<T> type) throws QtfException {
            if (type == MemberType.VARIABLE && isPartOfStruct) {
                return (T) namespace.computeVariable(VarType.STRUCT, memberName);
            }
            return namespace.get(memberName, type);
        }
    }

    record InvokeImpl(FunctionRef expression, FunctionAddress func) implements ExpressionValue {
        @Override
        public void validate(final @Nullable Type expectedType) throws QtfException {
            if (expectedType != null) {
                ExpressionValue.validate(expectedType, Type.DOUBLE_TYPE);
            }
            func.validateParameters(expression.args().size());
            expression.address = func;
        }

        @Override
        public Namespace evaluateAsNamespace() throws QtfException {
            throw QtfErrors.undefined(MemberType.LIBRARY, func.getLoggingName());
        }

        @Override
        public <T> T evaluateAs(final MemberType<T> type) throws QtfException {
            throw QtfErrors.undefined(type, func.getLoggingName());
        }
    }
}
