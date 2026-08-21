package io.github.mtykk.luckygames;

import java.util.List;

public class WeightedRandomPicker {
    static <O extends WeightedObject> O randomPick(List<O> candidates) throws IllegalArgumentException{
        if (candidates.isEmpty()) throw new IllegalArgumentException();
        int[] weights = new int[candidates.size()];

        for(int i = 0; i < candidates.size(); i++){
            weights[i] = candidates.get(i).getWeight();
        }
        return randomPick(candidates, weights);
    }

    static <O> O randomPick(List<O> candidates, int[] weights) throws IllegalArgumentException{
        if(candidates.size() != weights.length) throw new IllegalArgumentException();

        int weightsSum = 0;
        for (int i: weights){
            weightsSum += i;
        }
        if (weightsSum <= 0) throw new IllegalArgumentException();

        int targetWeight = PluginRandom.randomGenerator.nextInt(weightsSum);
        int accumulatedWeights = 0;
        for (int i = 0; i < weights.length; i++){
            accumulatedWeights += weights[i];
            if(accumulatedWeights > targetWeight){
                return candidates.get(i);
            }
        }
        return candidates.getLast();
    }
}
