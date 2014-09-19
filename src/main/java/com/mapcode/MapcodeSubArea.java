/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapcodeSubArea {

    private static ArrayList<MapcodeSubArea> subareas;
    private static TreeMap<Integer, ArrayList<MapcodeSubArea>> longitudeMap;
    private static TreeMap<Integer, ArrayList<MapcodeSubArea>> latitudeMap;

    static {
        subareas = new ArrayList<MapcodeSubArea>();
        longitudeMap = new TreeMap<Integer, ArrayList<MapcodeSubArea>>();
        latitudeMap = new TreeMap<Integer, ArrayList<MapcodeSubArea>>();
        for (MapcodeTerritory territory : MapcodeTerritory.values()) {
            int territoryCode = territory.getTerritoryCode();
            int first = MapcoderDataAccess.dataFirstRecord(territoryCode);
            int last = MapcoderDataAccess.dataLastRecord(territoryCode);
            for (int i = subareas.size(); i <= last; i++) {
                subareas.add(null);
            }
            for (int i = last; i >= first; i--) {
                MapcodeSubArea newMapcodeSubArea = new MapcodeSubArea(i, territory, subareas.get(last));
                subareas.set(i, newMapcodeSubArea);

                // @@TODO - Normalise longitudes.
                
                if (newMapcodeSubArea.boundedLatitudeRange == null) {
                    continue;
                }

                if (!longitudeMap.containsKey(newMapcodeSubArea.boundedLongitudeRange.getMin())) {
                    longitudeMap.put(newMapcodeSubArea.boundedLongitudeRange.getMin(), new ArrayList<MapcodeSubArea>());
                }
                if (!longitudeMap.containsKey(newMapcodeSubArea.boundedLongitudeRange.getMax())) {
                    longitudeMap.put(newMapcodeSubArea.boundedLongitudeRange.getMax(), new ArrayList<MapcodeSubArea>());
                }
                if (!latitudeMap.containsKey(newMapcodeSubArea.boundedLatitudeRange.getMin())) {
                    latitudeMap.put(newMapcodeSubArea.boundedLatitudeRange.getMin(), new ArrayList<MapcodeSubArea>());
                }
                if (!latitudeMap.containsKey(newMapcodeSubArea.boundedLatitudeRange.getMax())) {
                    latitudeMap.put(newMapcodeSubArea.boundedLatitudeRange.getMax(), new ArrayList<MapcodeSubArea>());
                }
            }
        }
        for (MapcodeSubArea mapcodeSubArea : subareas) {
            if (mapcodeSubArea.boundedLatitudeRange == null) {
                continue;
            }
            SortedMap<Integer, ArrayList<MapcodeSubArea>> subMap;

            subMap = longitudeMap.subMap(mapcodeSubArea.boundedLongitudeRange.getMin(),
                    mapcodeSubArea.boundedLongitudeRange.getMax() + 1);
            for (ArrayList<MapcodeSubArea> areaList : subMap.values()) {
                areaList.add(mapcodeSubArea);
            }

            subMap = latitudeMap.subMap(mapcodeSubArea.boundedLatitudeRange.getMin(),
                    mapcodeSubArea.boundedLatitudeRange.getMax() + 1);
            for (ArrayList<MapcodeSubArea> areaList : subMap.values()) {
                areaList.add(mapcodeSubArea);
            }

        }
        System.out.println("Min Latitiude: " + latitudeMap.firstKey());
        System.out.println("Max Latitiude: " + latitudeMap.lastKey());
        System.out.println("Min Longitiude: " + longitudeMap.firstKey());
        System.out.println("Max Longitiude: " + longitudeMap.lastKey());
    }

    public static MapcodeSubArea getArea(int i) {
        return subareas.get(i);
    }

    public static ArrayList<MapcodeSubArea> getAreasForPoint(Point point) {
        return getAreasForPoint(point, true);
    }

    private static ArrayList<MapcodeSubArea> getAreasForPoint(Point point, Boolean allowRecursion) {
        ArrayList<ArrayList<MapcodeSubArea>> areaLists = new ArrayList<ArrayList<MapcodeSubArea>>();
        ArrayList<MapcodeSubArea> list;
        list = latitudeMap.get(point.getLatitude());

        if (list != null) {
            areaLists.add(list);
        } else {
            Map.Entry<Integer, ArrayList<MapcodeSubArea>> entry = latitudeMap.lowerEntry(point.getLatitude());
            if (entry == null) {
                return null;
            }
            list = entry.getValue();
            if (list == null) {
                throw new IllegalStateException();
            }
            areaLists.add(list);
            entry = latitudeMap.higherEntry(point.getLatitude());
            if (entry == null) {
                return null;
            }
            list = entry.getValue();
            if (list == null)
                throw new IllegalStateException();
            areaLists.add(list);
        }

        list = longitudeMap.get(point.getLongitude());
        if (list != null) {
            areaLists.add(list);
        } else {
            Map.Entry<Integer, ArrayList<MapcodeSubArea>> entry = longitudeMap.lowerEntry(point.getLongitude());
            if (entry == null) {
                return null;
            }
            list = entry.getValue();
            if (list == null) {
                throw new IllegalStateException();
            }
            areaLists.add(list);
            entry = longitudeMap.higherEntry(point.getLongitude());
            if (entry == null) {
                return null;
            }
            list = entry.getValue();
            if (list == null) {
                throw new IllegalStateException();
            }
            areaLists.add(list);
        }

        ArrayList<MapcodeSubArea> result = new ArrayList<MapcodeSubArea>();
        list = areaLists.get(0);
        mainLoop: for (MapcodeSubArea mapcodeSubArea : list) {
            for (int i = 1; i < areaLists.size(); i++) {
                if (!areaLists.get(i).contains(mapcodeSubArea)) {
                    continue mainLoop;
                }
            }
            result.add(mapcodeSubArea);
        }

        if (allowRecursion) {
            // @@TODO Normalise areas.
            Point longitudeOutsideBoundsPoint;
            if (point.getLongitude() < 180000000) {
                longitudeOutsideBoundsPoint = new Point(point.getLongitude() + 360000000, point.getLatitude());
            } else {
                longitudeOutsideBoundsPoint = new Point(point.getLongitude() - 360000000, point.getLatitude());
            }
            ArrayList<MapcodeSubArea> extraAreas = getAreasForPoint(longitudeOutsideBoundsPoint, false);
            if (null != extraAreas) {
                result.addAll(extraAreas);
            }
        }

        return result;
    }

    private Range<Integer> latitudeRange, longitudeRange;
    private Range<Integer> boundedLatitudeRange, boundedLongitudeRange;
    private final MapcodeTerritory parentTerritory;
    private final Integer subAreaID;

    public int getMinX() {
        return longitudeRange.getMin();
    }

    public int getMinY() {
        return latitudeRange.getMin();
    }

    public int getMaxX() {
        return longitudeRange.getMax();
    }

    public int getMaxY() {
        return latitudeRange.getMax();
    }

    public MapcodeTerritory getParentTerritory() {
        return parentTerritory;
    }

    public Integer getSubAreaID() {
        return subAreaID;
    }
    
    private MapcodeSubArea(int i, MapcodeTerritory territory, MapcodeSubArea territoryBounds) {
        minmaxSetup(i);
        parentTerritory = territory;
        subAreaID = i;
        if (territoryBounds == null || (getMinX() >= territoryBounds.getMinX())
                && getMaxX() <= territoryBounds.getMaxX()) {
            boundedLongitudeRange = longitudeRange;
        } else {
            boundedLongitudeRange = new Range<Integer>(Math.max(getMinX(), territoryBounds.getMinX()), Math.min(
                    getMaxX(), territoryBounds.getMaxX()));
        }
        if (territoryBounds == null
                || (getMinY() >= territoryBounds.getMinY() && getMaxY() <= territoryBounds.getMaxY())) {
            boundedLatitudeRange = latitudeRange;
        } else {
            boundedLatitudeRange = new Range<Integer>(Math.max(getMinY(), territoryBounds.getMinY()), Math.min(
                    getMaxY(), territoryBounds.getMaxY()));
        }        
        if (boundedLatitudeRange.getMin() > boundedLatitudeRange.getMax() || boundedLongitudeRange.getMin() > boundedLongitudeRange.getMax())
        {
            boundedLatitudeRange = null;
            boundedLongitudeRange = null;
        }
    }

    private MapcodeSubArea() {
        parentTerritory = null;
        subAreaID = null;
    }

    public boolean containsPoint(Point point) {
        if (latitudeRange.contains(point.getLatitude()) && containsLongitude(point.getLongitude())) {
            return true;
        }
        return false;
    }

    public MapcodeSubArea extendBounds(int xExtension, int yExtension) {
        MapcodeSubArea result = new MapcodeSubArea();
        result.latitudeRange = new Range<Integer>(this.getMinY() - yExtension, getMaxY() + yExtension);
        result.longitudeRange = new Range<Integer>(this.getMinX() - xExtension, getMaxX() + xExtension);
        return result;
    }

    public boolean containsLongitude(int longitude) {
        if (this.longitudeRange.contains(longitude))
            return true;
        if (longitude < 180000000)
            longitude += 360000000;
        else
            longitude -= 360000000;
        if (this.longitudeRange.contains(longitude))
            return true;
        return false;
    }

    private void minmaxSetup(int arg) {
        int i = arg * 20;
        int minX = MapcoderDataAccess._long(i);

        i += 4;
        int minY = MapcoderDataAccess._long(i);

        i += 4;
        int maxX = MapcoderDataAccess._long(i);

        i += 4;
        int maxY = MapcoderDataAccess._long(i);

        latitudeRange = new Range<Integer>(minY, maxY);
        longitudeRange = new Range<Integer>(minX, maxX);
    }
}
