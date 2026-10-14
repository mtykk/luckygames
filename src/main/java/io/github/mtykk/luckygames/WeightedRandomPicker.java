package io.github.mtykk.luckygames;

import java.util.List;
import java.util.Objects;

public class WeightedRandomPicker {
    static <O extends WeightedObject> O randomPick(List<O> candidates) throws IllegalArgumentException {
        Objects.requireNonNull(candidates, "candidates");
        int[] weights = new int[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            O candidate = Objects.requireNonNull(candidates.get(i), "candidate");
            weights[i] = candidate.getWeight();
        }
        return randomPick(candidates, weights);
    }

    static <O> O randomPick(List<O> candidates, int[] weights) throws IllegalArgumentException {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(weights, "weights");
        if (candidates.size() != weights.length) {
            throw new IllegalArgumentException("Candidate and weight counts must match");
        }

        long totalWeight = 0;
        for (int weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("Weights cannot be negative");
            }
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            throw new IllegalArgumentException("At least one weight must be positive");
        }

        long targetWeight = PluginRandom.randomGenerator.nextLong(totalWeight);
        long accumulatedWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            accumulatedWeight += weights[i];
            if (accumulatedWeight > targetWeight) {
                return candidates.get(i);
            }
        }

        // The target is strictly less than totalWeight, so this is unreachable.
        throw new IllegalStateException("Failed to select an item by weight");
    }
}
