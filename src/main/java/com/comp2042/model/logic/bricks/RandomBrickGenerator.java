package com.comp2042.model.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBrickGenerator implements BrickGenerator {

    private final List<Brick> brickList;

    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public RandomBrickGenerator() {
        brickList = new ArrayList<>();
        brickList.add(new IBrick());
        brickList.add(new JBrick());
        brickList.add(new LBrick());
        brickList.add(new OBrick());
        brickList.add(new SBrick());
        brickList.add(new TBrick());
        brickList.add(new ZBrick());
        // Initialize with at least 4 bricks (1 current + 3 preview)
        for (int i = 0; i < 4; i++) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
    }

    @Override
    public Brick getBrick() {
        // Ensure we always have at least 4 bricks in the buffer
        while (nextBricks.size() < 4) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
        return nextBricks.poll();
    }

    @Override
    public Brick getNextBrick() {
        return nextBricks.peek();
    }

    @Override
    public List<Brick> getNextBricks(int count) {
        // Ensure we have enough bricks in the buffer
        while (nextBricks.size() < count) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
        
        // Return the next count bricks without removing them (peek)
        List<Brick> result = new ArrayList<>();
        int index = 0;
        for (Brick brick : nextBricks) {
            if (index >= count) break;
            result.add(brick);
            index++;
        }
        return result;
    }
}
