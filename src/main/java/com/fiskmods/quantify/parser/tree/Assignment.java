package com.fiskmods.quantify.parser.tree;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.Operator;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

public interface Assignment extends JvmFunction {
    record AbsoluteAssignment(Assignable target, @Nullable Value value, @Nullable Operator op) implements Assignment {
        @Override
        public void apply(final MethodVisitor mv) {
            if (value == null) {
                target.init(mv);
            } else if (op != null) {
                target.modify(mv, value, op);
            } else {
                target.set(mv, value);
            }
        }
    }

    record LerpAssignment(Assignable target, Value value, Value progress, boolean rotational) implements Assignment {
        @Override
        public void apply(final MethodVisitor mv) {
            target.lerp(mv, value, progress, rotational);
        }
    }
}
