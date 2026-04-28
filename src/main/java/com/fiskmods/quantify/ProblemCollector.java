package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfCompilerException;

import java.util.ArrayList;
import java.util.List;

public class ProblemCollector implements ProblemReporter {
    private final List<Problem> problems = new ArrayList<>();

    @Override
    public void report(final Problem problem) {
        problems.add(problem);
    }

    public void flush() throws QtfCompilerException {
        if (!problems.isEmpty()) {
            final int n = problems.size();
            printAll();
            clear();
            throw new QtfCompilerException(n == 1 ? n + " error" : n + " errors");
        }
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void printAll() {
        problems.forEach(Problem::print);
    }

    public void clear() {
        problems.clear();
    }
}
