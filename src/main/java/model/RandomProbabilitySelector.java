package model;

import javafx.util.Pair;

import java.util.List;
import java.util.Random;

public class RandomProbabilitySelector {

    public static <T> T selectRandomItem(List<Pair<T, Integer>> items) {
        int totalSum = 0;
        for (Pair<T, Integer> pair : items) {
            totalSum += pair.getValue();
        }
        Random random = new Random();
        if (totalSum <= 0) {
            int randomValue = random.nextInt(items.size());
            return items.get(randomValue).getKey();
        }
        int randomValue = random.nextInt(totalSum);

        for (Pair<T, Integer> pair : items) {
            randomValue -= pair.getValue();
            if (randomValue <= 0) {
                return pair.getKey();
            }
        }
        return null;
    }
}
