package com.lab.model.visitor;

import com.lab.model.*;
import java.util.List;
import java.util.Map;

public class JavaCodeGenerator implements BlockVisitor {

    private final StringBuilder sb = new StringBuilder();
    private final Map<Integer, List<Block>> allThreads;

    public JavaCodeGenerator(Map<Integer, List<Block>> allThreads) {
        this.allThreads = allThreads;
    }

    public String generateMultiThreaded() {
        sb.setLength(0);

        sb.append("public class MultiThreadedProgram {\n\n");

        // --- ATOMIC SHARED MEMORY ---
        // AtomicIntegerArray provides thread-safe operations on individual elements
        sb.append("    static final java.util.concurrent.atomic.AtomicIntegerArray V = ")
                .append("new java.util.concurrent.atomic.AtomicIntegerArray(100);\n");

        // --- SHARED SCANNER ---
        sb.append("    static final java.util.Scanner scanner = new java.util.Scanner(System.in);\n\n");

        sb.append("    public static void main(String[] args) {\n");
        sb.append("        System.out.println(\"Program started. Threads: ").append(allThreads.size()).append("\");\n");

        for (Integer threadId : allThreads.keySet()) {
            if (allThreads.get(threadId).isEmpty()) continue;
            sb.append("        new Thread(new Worker").append(threadId).append("(), \"Thread-" + threadId + "\").start();\n");
        }
        sb.append("    }\n\n");

        // Worker generation logic remains largely the same, but the case bodies change via visitor
        for (Integer threadId : allThreads.keySet()) {
            List<Block> blocks = allThreads.get(threadId);
            if (blocks.isEmpty()) continue;

            sb.append("    static class Worker").append(threadId).append(" implements Runnable {\n");
            sb.append("        @Override\n");
            sb.append("        public void run() {\n");
            sb.append("            int currentId = ").append(findStartId(blocks)).append(";\n");
            sb.append("            boolean running = true;\n");
            sb.append("            while (running) {\n");
            sb.append("                switch (currentId) {\n");

            for (Block b : blocks) {
                sb.append("                    case ").append(b.getId()).append(": {\n");
                b.accept(this);
                sb.append("                        break;\n");
                sb.append("                    }\n");
            }

            sb.append("                    default: running = false;\n");
            sb.append("                }\n");
            sb.append("                Thread.yield();\n");
            sb.append("            }\n");
            sb.append("            System.out.println(Thread.currentThread().getName() + \" finished.\");\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private int findStartId(List<Block> blocks) {
        return blocks.stream()
                .filter(b -> b instanceof StartBlock)
                .map(Block::getId)
                .findFirst().orElse(-1);
    }

    // --- UPDATED VISITOR METHODS FOR ATOMICINTEGERARRAY ---

    @Override
    public void visit(AssignmentBlock b) {
        sb.append("                        // Atomic Assign\n");
        if (b.isConstantAssign()) {
            // V.set(index, value)
            sb.append("                        V.set(").append(b.getTargetVarIndex()).append(", ")
                    .append(b.getConstantValue()).append(");\n");
        } else {
            // V.set(target, V.get(source))
            sb.append("                        V.set(").append(b.getTargetVarIndex()).append(", ")
                    .append("V.get(").append(b.getSourceVarIndex()).append("));\n");
        }
        goTo(b.getNextBlockId());
    }

    @Override
    public void visit(ConditionBlock b) {
        String op = (b.getOperator() == ConditionBlock.Operator.EQUALS) ? "==" : "<";
        // V.get(index) instead of V[index]
        sb.append("                        if (V.get(").append(b.getLeftVarIndex()).append(") ")
                .append(op).append(" ").append(b.getRightConstant()).append(") ");
        sb.append("currentId = ").append(b.getNextBlockId()).append("; ");
        sb.append("else currentId = ").append(b.getFalseNextBlockId()).append(";\n");
    }

    @Override
    public void visit(IOBlock b) {
        if (b.getIoType() == IOBlock.IOType.PRINT) {
            sb.append("                        synchronized(scanner) {\n");
            sb.append("                            System.out.println(V.get(").append(b.getVarIndex()).append("));\n");
            sb.append("                        }\n");
        } else {
            sb.append("                        synchronized(scanner) {\n");
            sb.append("                            System.out.print(\"[\" + Thread.currentThread().getName() + \"] Enter V").append(b.getVarIndex()).append(": \");\n");
            sb.append("                            if (scanner.hasNextInt()) {\n");
            // V.set(index, value)
            sb.append("                                V.set(").append(b.getVarIndex()).append(", scanner.nextInt());\n");
            sb.append("                            }\n");
            sb.append("                        }\n");
        }
        goTo(b.getNextBlockId());
    }

    @Override
    public void visit(StartBlock b) {
        goTo(b.getNextBlockId());
    }

    @Override
    public void visit(StopBlock b) {
        sb.append("                        running = false;\n");
    }

    private void goTo(Integer id) {
        if (id != null) sb.append("                        currentId = ").append(id).append(";\n");
        else sb.append("                        running = false;\n");
    }
}