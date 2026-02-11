package com.lab.test;

import com.lab.model.*;
import lombok.Data;

import java.util.*;
import java.util.function.Supplier;

public class TestEngine {

    private final Map<Integer, List<Block>> allThreadsBlocks;
    private final Map<Integer, Block> globalBlockMap = new HashMap<>();

    public TestEngine(Map<Integer, List<Block>> allThreadsBlocks) {
        this.allThreadsBlocks = allThreadsBlocks;
        for (List<Block> list : allThreadsBlocks.values()) {
            for (Block b : list) globalBlockMap.put(b.getId(), b);
        }
    }

    // Додано аргумент isCancelled
    public TestResult runExploration(String input, String expected, int maxK, Supplier<Boolean> isCancelled) {

        // Знаходимо старти
        Map<Integer, Integer> startPointers = new HashMap<>();
        for (Integer threadId : allThreadsBlocks.keySet()) {
            allThreadsBlocks.get(threadId).stream()
                    .filter(b -> b instanceof StartBlock)
                    .findFirst()
                    .ifPresent(start -> startPointers.put(threadId, start.getId()));
        }

        Queue<ExecutionState> queue = new LinkedList<>();
        List<ExecutionState> finishedStates = new ArrayList<>();

        queue.add(new ExecutionState(startPointers, input));

        int totalPathsVisited = 0;

        while (!queue.isEmpty()) {
            // 1. ПЕРЕВІРКА НА СКАСУВАННЯ
            if (isCancelled.get()) {
                break; // Виходимо з циклу, повертаємо те, що встигли знайти
            }

            ExecutionState state = queue.poll();

            if (state.getStepsTaken() > maxK) continue;

            if (state.isAllFinished()) {
                boolean ok = state.getOutputString().trim().equals(expected.trim());
                state.setPassed(ok);
                finishedStates.add(state);
                continue;
            }

            totalPathsVisited++;

            // Interleaving Logic (перебір варіантів)
            for (Integer threadId : state.getThreadPointers().keySet()) {
                int currentBlockId = state.getThreadPointers().get(threadId);
                if (currentBlockId == -1) continue;

                Block block = globalBlockMap.get(currentBlockId);
                if (block == null) continue;

                ExecutionState nextState = new ExecutionState(state);
                nextState.setStepsTaken(state.getStepsTaken() + 1);

                stepThread(threadId, block, nextState);
                queue.add(nextState);
            }
        }

        return new TestResult(finishedStates, totalPathsVisited, maxK);
    }

    private void stepThread(int threadId, Block block, ExecutionState state) {
        Integer nextId = null;
        if (block instanceof StartBlock) nextId = block.getNextBlockId();
        else if (block instanceof StopBlock) nextId = -1;
        else if (block instanceof AssignmentBlock ab) {
            int val = ab.isConstantAssign() ? ab.getConstantValue() : state.getVariables()[ab.getSourceVarIndex()];
            state.getVariables()[ab.getTargetVarIndex()] = val;
            nextId = ab.getNextBlockId();
        }
        else if (block instanceof IOBlock io) {
            if (io.getIoType() == IOBlock.IOType.PRINT) {
                state.getOutputLog().add(String.valueOf(state.getVariables()[io.getVarIndex()]));
            } else {
                if (!state.getInputQueue().isEmpty()) state.getVariables()[io.getVarIndex()] = state.getInputQueue().poll();
            }
            nextId = io.getNextBlockId();
        }
        else if (block instanceof ConditionBlock cb) {
            int l = state.getVariables()[cb.getLeftVarIndex()];
            int r = cb.getRightConstant();
            boolean res = (cb.getOperator() == ConditionBlock.Operator.EQUALS) ? (l == r) : (l < r);
            nextId = res ? cb.getNextBlockId() : cb.getFalseNextBlockId();
        }
        if (nextId == null) nextId = -1;
        state.getThreadPointers().put(threadId, nextId);
    }

    // Результат + Статистика
    @Data
    public static class TestResult {
        private final List<ExecutionState> finishedPaths;
        private final int totalStatesVisited;
        private final int maxK;

        public String getCoverageInfo() {
            if (finishedPaths.isEmpty()) return "No finished paths found yet.";

            long passed = finishedPaths.stream().filter(ExecutionState::isPassed).count();
            long total = finishedPaths.size();
            double percent = (double) passed / total * 100.0;

            return String.format(
                    "Total Finished Scenarios (Length <= %d): %d\n" +
                            "Passed: %d\n" +
                            "Failed: %d\n" +
                            "Success Rate: %.2f%%",
                    maxK, total, passed, (total - passed), percent);
        }
    }
}