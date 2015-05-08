/*
 * Copyright (C) 2014-2015 Stichting Mapcode Foundation (http://www.mapcode.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 * <p/>
 * This class contains a class that defines an area for local mapcodes.
 */
class SubArea {
    private static final Logger LOG = LoggerFactory.getLogger(SubArea.class);

    private static final ArrayList<SubArea> subAreas = new ArrayList<>();
    private static final TreeMap<Integer, ArrayList<SubArea>> lonMap = new TreeMap<>();
    private static final TreeMap<Integer, ArrayList<SubArea>> latMap = new TreeMap<>();

    private static final Range<Integer> latBoundingRange =
            new Range<>(Point.LAT_MICRODEG_MIN, Point.LAT_MICRODEG_MAX);
    private static final Range<Integer> lonBoundingRange =
            new Range<>(Point.LON_MICRODEG_MIN, Point.LON_MICRODEG_MAX);

    static {
        for (final Territory territory : Territory.values()) {
            final int territoryCode = territory.getTerritoryCode();
            final int first = DataAccess.dataFirstRecord(territoryCode);
            final int last = DataAccess.dataLastRecord(territoryCode);

            // Add a number sub areas.
            for (int i = subAreas.size(); i <= last; i++) {
                subAreas.add(null);
            }
            for (int i = last; i >= first; i--) {
                final SubArea newSubArea = new SubArea(i, territory, subAreas.get(last));
                subAreas.set(i, newSubArea);

                if ((newSubArea.boundedLatRange == null) || (newSubArea.boundedLonRange == null)) {
                    continue;
                }

                for (final Range<Integer> longitudeRange : newSubArea.boundedLonRange) {
                    if (!lonMap.containsKey(longitudeRange.getMin())) {
                        lonMap.put(longitudeRange.getMin(), new ArrayList<SubArea>());
                    }
                    if (!lonMap.containsKey(longitudeRange.getMax())) {
                        lonMap.put(longitudeRange.getMax(), new ArrayList<SubArea>());
                    }
                }

                for (final Range<Integer> latitudeRange : newSubArea.boundedLatRange) {
                    if (!latMap.containsKey(latitudeRange.getMin())) {
                        latMap.put(latitudeRange.getMin(), new ArrayList<SubArea>());
                    }
                    if (!latMap.containsKey(latitudeRange.getMax())) {
                        latMap.put(latitudeRange.getMax(), new ArrayList<SubArea>());
                    }
                }
            }
        }
        for (final SubArea subArea : subAreas) {
            if ((subArea.boundedLatRange == null) || (subArea.boundedLonRange == null)) {
                continue;
            }
            SortedMap<Integer, ArrayList<SubArea>> subMap;

            for (final Range<Integer> longitudeRange : subArea.boundedLonRange) {
                subMap = lonMap.subMap(longitudeRange.getMin(), longitudeRange.getMax() + 1);
                for (final ArrayList<SubArea> areaList : subMap.values()) {
                    areaList.add(subArea);
                }
            }

            for (final Range<Integer> latitudeRange : subArea.boundedLatRange) {
                subMap = latMap.subMap(latitudeRange.getMin(), latitudeRange.getMax() + 1);
                for (final ArrayList<SubArea> areaList : subMap.values()) {
                    areaList.add(subArea);
                }
            }
        }
        LOG.debug("SubArea (init): lat=[{}, {}], lon=[{}, {}]",
                Point.microDegToDeg(latMap.firstKey()), Point.microDegToDeg(latMap.lastKey()),
                Point.microDegToDeg(lonMap.firstKey()), Point.microDegToDeg(lonMap.lastKey()));
    }

    static SubArea getArea(final int i) {
        return subAreas.get(i);
    }


    @SuppressWarnings("unchecked")
    @Nonnull
    static List<SubArea> getAreasForPoint(@Nonnull final Point point) {
        final ArrayList<ArrayList<SubArea>> areaLists = new ArrayList<>();
        ArrayList<SubArea> list;
        list = latMap.get(point.getLatMicroDeg());

        if (list != null) {
            areaLists.add(list);
        } else {
            Entry<Integer, ArrayList<SubArea>> entry = latMap.lowerEntry(point.getLatMicroDeg());
            if (entry == null) {
                return Collections.EMPTY_LIST;
            }
            list = entry.getValue();
            assert list != null;
            areaLists.add(list);
            entry = latMap.higherEntry(point.getLatMicroDeg());
            if (entry == null) {
                return Collections.EMPTY_LIST;
            }
            list = entry.getValue();
            assert list != null;
            areaLists.add(list);
        }

        list = lonMap.get(point.getLonMicroDeg());
        if (list != null) {
            areaLists.add(list);
        } else {
            Entry<Integer, ArrayList<SubArea>> entry = lonMap.lowerEntry(point.getLonMicroDeg());
            if (entry == null) {
                return Collections.EMPTY_LIST;
            }
            list = entry.getValue();
            assert list != null;
            areaLists.add(list);
            entry = lonMap.higherEntry(point.getLonMicroDeg());
            if (entry == null) {
                return Collections.EMPTY_LIST;
            }
            list = entry.getValue();
            assert list != null;
            areaLists.add(list);
        }

        final ArrayList<SubArea> result = new ArrayList<>();
        list = areaLists.get(0);

        mainLoop:
        for (final SubArea subArea : list) {
            for (int i = 1; i < areaLists.size(); i++) {
                if (!areaLists.get(i).contains(subArea)) {
                    continue mainLoop;
                }
            }
            result.add(subArea);
        }

        return result;
    }

    private Range<Integer> latRange;
    private Range<Integer> lonRange;
    private ArrayList<Range<Integer>> boundedLatRange;
    private ArrayList<Range<Integer>> boundedLonRange;
    private final Territory parentTerritory;
    private final Integer subAreaID;

    int getMinX() {
        return lonRange.getMin();
    }

    int getMinY() {
        return latRange.getMin();
    }

    int getMaxX() {
        return lonRange.getMax();
    }

    int getMaxY() {
        return latRange.getMax();
    }

    Territory getParentTerritory() {
        return parentTerritory;
    }

    Integer getSubAreaID() {
        return subAreaID;
    }

    private SubArea(final int i, @Nonnull final Territory territory, @Nullable final SubArea territoryBounds) {
        minMaxSetup(i);
        parentTerritory = territory;
        subAreaID = i;
        boundedLonRange = new ArrayList<>();
        boundedLatRange = new ArrayList<>();

        // Mapcode areas are inclusive for the minimum bounds and exclusive for the maximum bounds
        // Trim max by 1, to address boundary cases.
        final Range<Integer> trimmedLonRange = trimRange(lonRange);
        Range<Integer> trimmedLatRange = latRange;
        // Special handling for latitude +90.0 which should not be trimmed, in order to produce
        // mapcode AAA Z0000.010G for lat: 90.0 lon:180.0.
        if (latRange.getMax() != 90000000) {
            trimmedLatRange = trimRange(latRange);
        }
        final ArrayList<Range<Integer>> normalisedLonRange = normaliseRange(trimmedLonRange, lonBoundingRange);
        final ArrayList<Range<Integer>> normalisedLatRange = normaliseRange(trimmedLatRange, latBoundingRange);
        if (territoryBounds == null) {
            boundedLonRange = normalisedLonRange;
            boundedLatRange = normalisedLatRange;
        } else {
            for (final Range<Integer> normalisedRange : normalisedLonRange) {
                final ArrayList<Range<Integer>> boundedRange =
                        normalisedRange.constrain(territoryBounds.boundedLonRange);
                if (boundedRange != null) {
                    boundedLonRange.addAll(boundedRange);
                }
            }
            for (final Range<Integer> normalisedRange : normalisedLatRange) {
                final ArrayList<Range<Integer>> boundedRange =
                        normalisedRange.constrain(territoryBounds.boundedLatRange);
                if (boundedRange != null) {
                    boundedLatRange.addAll(boundedRange);
                }
            }
        }
    }

    @Nonnull
    private static ArrayList<Range<Integer>> normaliseRange(
            @Nonnull final Range<Integer> range, @Nonnull final Range<Integer> boundingRange) {
        final ArrayList<Range<Integer>> ranges = new ArrayList<>();

        Range<Integer> tempRange = range.constrain(boundingRange);
        if (tempRange != null) {
            ranges.add(tempRange);
        }

        Range<Integer> normalisingRange = range;
        while (normalisingRange.getMin() < boundingRange.getMin()) {
            normalisingRange = new Range<>((normalisingRange.getMin() + boundingRange.getMax())
                    - boundingRange.getMin(), (normalisingRange.getMax() + boundingRange.getMax())
                    - boundingRange.getMin());
            tempRange = normalisingRange.constrain(boundingRange);
            if (tempRange != null) {
                ranges.add(tempRange);
            }
        }

        normalisingRange = range;
        while (normalisingRange.getMax() > boundingRange.getMax()) {
            normalisingRange = new Range<>((normalisingRange.getMin() - boundingRange.getMax())
                    + boundingRange.getMin(), (normalisingRange.getMax() - boundingRange.getMax())
                    + boundingRange.getMin());
            tempRange = normalisingRange.constrain(boundingRange);
            if (tempRange != null) {
                ranges.add(tempRange);
            }
        }

        return ranges;
    }

    private SubArea() {
        parentTerritory = null;
        subAreaID = null;
    }

    boolean containsPoint(@Nonnull final Point point) {
        if (latRange.contains(point.getLatMicroDeg()) && containsLongitude(point.getLonMicroDeg())) {
            return true;
        }
        return false;
    }

    @Nonnull
    SubArea extendBounds(final int xExtension, final int yExtension) {
        final SubArea result = new SubArea();
        result.latRange = new Range<>(this.getMinY() - yExtension, getMaxY() + yExtension);
        result.lonRange = new Range<>(this.getMinX() - xExtension, getMaxX() + xExtension);
        return result;
    }

    boolean containsLongitude(final int argLonMicroDeg) {
        int lonMicroDeg = argLonMicroDeg;
        if (this.lonRange.contains(lonMicroDeg)) {
            return true;
        }
        if (lonMicroDeg < lonRange.getMin()) {
            lonMicroDeg += 360000000;
        } else {
            lonMicroDeg -= 360000000;
        }
        if (this.lonRange.contains(lonMicroDeg)) {
            return true;
        }
        return false;
    }

    private void minMaxSetup(final int arg) {
        int i = arg * 20;
        final int minX = DataAccess.asLong(i);

        i += 4;
        final int minY = DataAccess.asLong(i);

        i += 4;
        final int maxX = DataAccess.asLong(i);

        i += 4;
        final int maxY = DataAccess.asLong(i);

        latRange = new Range<>(minY, maxY);
        lonRange = new Range<>(minX, maxX);
    }

    private static Range<Integer> trimRange(final Range<Integer> range) {
        return new Range<>(range.getMin(), range.getMax() - 1);
    }
}
