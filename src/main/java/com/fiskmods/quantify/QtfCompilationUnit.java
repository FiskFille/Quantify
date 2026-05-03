package com.fiskmods.quantify;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.QtfListener;
import com.fiskmods.quantify.member.Variable;
import com.fiskmods.quantify.util.IndexMap;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public record QtfCompilationUnit(
        Class<?> compiledType,
        IndexMap<String> inputs,
        IndexMap<String> outputs,
        Map<String, FunctionAddress> functions
) {
    public QtfScript createScript(final @Nullable QtfListener listener) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        final Object memory = new double[outputs.size()];
        final Constructor<?> c = compiledType.getConstructors()[0];
        final JvmRunnable runnable = (JvmRunnable) c.newInstance(memory);

        if (listener != null) {
            listener.listen(Variable.resolve(outputs, runnable), outputs.keys()::stream);
        }
        return runnable;
    }

    public QtfLibrary createLibrary(final String name) {
        return QtfLibrary.create(name, b -> {
            functions.forEach(b::addFunction);
            return b;
        });
    }
}
