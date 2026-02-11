package com.lab.model;

import lombok.Data;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class ThreadFlow implements Serializable {
    private int threadId;
    private Map<Integer, Block> blocks = new HashMap<>(); // Всі блоки: ID -> Block
    private Integer startBlockId; // Точка входу

    public void addBlock(Block block) {
        blocks.put(block.getId(), block);
    }
}