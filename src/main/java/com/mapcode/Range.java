/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public class Range<T extends Comparable<T>> {
    private final T min;
    private final T max;

    public Range(T min, T max) {
        this.min = min;
        this.max = max;
    }
    
    public T getMin() {
        return min;
    }
    
    public T getMax() {
        return max;
    }
    
    public boolean contains(T value) {
        if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0)
            return true;
        return false;
    }
    
    public boolean intersects(Range<T> range) {
        if (range.contains(min) || range.contains(max) || contains(range.max) || contains(range.min))
            return true;
        return false;
    }

}
