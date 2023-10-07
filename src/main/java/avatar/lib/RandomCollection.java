/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.lib;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import lombok.Getter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class RandomCollection<E> {

    @Getter
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) {
            return this;
        }
        total += weight;
        map.put(total, result);
        return this;
    }

    public boolean isEmpty() {
        return map.size() == 0;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}

