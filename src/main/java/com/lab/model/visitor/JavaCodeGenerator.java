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

        // --- СПІЛЬНА ПАМ'ЯТЬ ---
        // V0-V99 доступні всім потокам
        sb.append("    static volatile int[] V = new int[100];\n");

        // --- СПІЛЬНИЙ СКАНЕР ---
        // Один об'єкт Scanner для вводу, щоб уникнути конфліктів потоків
        sb.append("    static final java.util.Scanner scanner = new java.util.Scanner(System.in);\n\n");

        sb.append("    public static void main(String[] args) {\n");
        sb.append("        System.out.println(\"Program started. Threads: ").append(allThreads.size()).append("\");\n");

        // Запуск потоків
        for (Integer threadId : allThreads.keySet()) {
            if (allThreads.get(threadId).isEmpty()) continue;
            sb.append("        new Thread(new Worker").append(threadId).append("(), \"Thread-" + threadId + "\").start();\n");
        }
        sb.append("    }\n\n");

        // Генерація класів-воркерів (потоків)
        for (Integer threadId : allThreads.keySet()) {
            List<Block> blocks = allThreads.get(threadId);
            if (blocks.isEmpty()) continue;

            sb.append("    static class Worker").append(threadId).append(" implements Runnable {\n");
            sb.append("        @Override\n");
            sb.append("        public void run() {\n");
            // Знаходимо ID стартового блоку
            sb.append("            int currentId = ").append(findStartId(blocks)).append(";\n");
            sb.append("            boolean running = true;\n");

            sb.append("            while (running) {\n");
            sb.append("                switch (currentId) {\n");

            // Генерація case для кожного блоку
            for (Block b : blocks) {
                sb.append("                    case ").append(b.getId()).append(": {\n");
                b.accept(this); // Виклик візитора для генерації тіла блоку
                sb.append("                        break;\n");
                sb.append("                    }\n");
            }

            sb.append("                    default: running = false;\n");
            sb.append("                }\n"); // кінець switch

            // yield допомагає планувальнику частіше перемикати контекст між потоками,
            // що робить "гонки даних" більш помітними (корисно для лаби)
            sb.append("                Thread.yield();\n");

            sb.append("            }\n"); // кінець while
            sb.append("            System.out.println(Thread.currentThread().getName() + \" finished.\");\n");
            sb.append("        }\n"); // кінець run
            sb.append("    }\n"); // кінець class Worker
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

    // --- VISITOR METHODS ---

    @Override
    public void visit(AssignmentBlock b) {
        sb.append("                        // Assign\n");
        if (b.isConstantAssign()) {
            sb.append("                        V[").append(b.getTargetVarIndex()).append("] = ")
                    .append(b.getConstantValue()).append(";\n");
        } else {
            sb.append("                        V[").append(b.getTargetVarIndex()).append("] = ")
                    .append("V[").append(b.getSourceVarIndex()).append("];\n");
        }
        goTo(b.getNextBlockId());
    }

    @Override
    public void visit(ConditionBlock b) {
        String op = (b.getOperator() == ConditionBlock.Operator.EQUALS) ? "==" : "<";
        sb.append("                        if (V[").append(b.getLeftVarIndex()).append("] ")
                .append(op).append(" ").append(b.getRightConstant()).append(") ");
        sb.append("currentId = ").append(b.getNextBlockId()).append("; ");
        sb.append("else currentId = ").append(b.getFalseNextBlockId()).append(";\n");
    }

    @Override
    public void visit(IOBlock b) {
        // ВИПРАВЛЕНО: Додано синхронізацію для коректного вводу/виводу в багатопотоковій середі
        if (b.getIoType() == IOBlock.IOType.PRINT) {
            sb.append("                        synchronized(scanner) {\n");
            sb.append("                            System.out.println(V[").append(b.getVarIndex()).append("]);\n");
            sb.append("                        }\n");
        } else {
            // Реальний INPUT
            sb.append("                        synchronized(scanner) {\n");
            sb.append("                            System.out.print(\"[\" + Thread.currentThread().getName() + \"] Enter V").append(b.getVarIndex()).append(": \");\n");
            sb.append("                            if (scanner.hasNextInt()) {\n");
            sb.append("                                V[").append(b.getVarIndex()).append("] = scanner.nextInt();\n");
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