package com.jdte.common.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Plans an independent expanded inventory without overwriting occupied destination slots. */
public final class SlotExpansion {
    private SlotExpansion() {}

    public static <T> List<T> plan(List<T> source, List<T> destination,
                                    Predicate<T> empty, UnaryOperator<T> copy) {
        if (source.size() > destination.size()) throw new IllegalArgumentException("Destination is too small");
        if (destination.stream().anyMatch(stack -> !empty.test(stack)))
            throw new IllegalArgumentException("Inactive destination contains items; refusing to overwrite them");
        var result = new ArrayList<T>(destination.size());
        for (int i = 0; i < destination.size(); i++)
            result.add(copy.apply(i < source.size() ? source.get(i) : destination.get(i)));
        return List.copyOf(result);
    }
}
