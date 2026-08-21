package io.github.mtykk.luckygames;

import java.util.List;

public class RandomPicker {
    public static <O> O randomPick(List<O> candidates) throws IllegalArgumentException{
        if (candidates.isEmpty()) throw new IllegalArgumentException();
        int randomIndex = PluginRandom.randomGenerator.nextInt(candidates.size());

        return candidates.get(randomIndex);
    }
}
