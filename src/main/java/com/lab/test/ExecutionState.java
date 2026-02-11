package com.lab.test;

import lombok.Data;
import java.util.*;

@Data
public class ExecutionState {
    // Map<ThreadID, CurrentBlockID>. Якщо потік закінчив, ID = -1
    private Map<Integer, Integer> threadPointers;

    private int[] variables;
    private Queue<Integer> inputQueue;
    private List<String> outputLog;
    private int stepsTaken;

    private boolean passed; // Для фінального звіту

    public ExecutionState(Map<Integer, Integer> initialPointers, String rawInput) {
        this.threadPointers = new HashMap<>(initialPointers);
        this.variables = new int[100];
        this.inputQueue = parseInput(rawInput);
        this.outputLog = new ArrayList<>();
        this.stepsTaken = 0;
    }

    // Copy Constructor
    public ExecutionState(ExecutionState other) {
        this.threadPointers = new HashMap<>(other.threadPointers);
        this.variables = Arrays.copyOf(other.variables, 100);
        this.inputQueue = new LinkedList<>(other.inputQueue);
        this.outputLog = new ArrayList<>(other.outputLog);
        this.stepsTaken = other.stepsTaken;
    }

    public boolean isAllFinished() {
        // Всі потоки мають -1 (або null)
        return threadPointers.values().stream().allMatch(id -> id == -1);
    }

    private Queue<Integer> parseInput(String r) {
        Queue<Integer> q = new LinkedList<>();
        if(r==null || r.isEmpty()) return q;
        for(String s : r.split("\\s+")) try { q.add(Integer.parseInt(s)); } catch(Exception ignored){}
        return q;
    }

    public String getOutputString() { return String.join(" ", outputLog); }
}