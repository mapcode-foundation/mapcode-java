/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public class MapCodeCommon {
    public final static int[] nc = { 1, 31, 961, 29791, 923521, 28629151, 887503681 };

    public final static int[] xside = { 0, 5, 31, 168, 961, 168 * 31, 29791, 165869, 923521, 5141947 };

    public final static int[] yside = { 0, 6, 31, 176, 961, 176 * 31, 29791, 165869, 923521, 5141947 };

    private static int[] xdivider19 = { 360, 360, 360, 360, 360, 360, 361, 361, 361, 361, 362, 362, 362, 363, 363, 363,
        364, 364, 365, 366, 366, 367, 367, 368, 369, 370, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380,
        382, 383, 384, 386, 387, 388, 390, 391, 393, 394, 396, 398, 399, 401, 403, 405, 407, 409, 411, 413, 415,
        417, 420, 422, 424, 427, 429, 432, 435, 437, 440, 443, 446, 449, 452, 455, 459, 462, 465, 469, 473, 476,
        480, 484, 488, 492, 496, 501, 505, 510, 515, 520, 525, 530, 535, 540, 546, 552, 558, 564, 570, 577, 583,
        590, 598, 605, 612, 620, 628, 637, 645, 654, 664, 673, 683, 693, 704, 715, 726, 738, 751, 763, 777, 791,
        805, 820, 836, 852, 869, 887, 906, 925, 946, 968, 990, 1014, 1039, 1066, 1094, 1123, 1154, 1187, 1223,
        1260, 1300, 1343, 1389, 1438, 1490, 1547, 1609, 1676, 1749, 1828, 1916, 2012, 2118, 2237, 2370, 2521, 2691,
        2887, 3114, 3380, 3696, 4077, 4547, 5139, 5910, 6952, 8443, 10747, 14784, 23681, 59485 };

    public static int x_divider(int miny, int maxy)
    // returns divider for longitude (multiplied by 4), for a given latitude
    {
        if (miny >= 0) // maxy>miny>0
            return xdivider19[miny >> 19];
        if (maxy >= 0) // maxy>0>miny
            return xdivider19[0];
        return xdivider19[(-maxy) >> 19]; // 0>maxy>miny
    }
    
    public static int count_city_coordinates_for_country(int samecodex, int index, int firstcode) {
        int i = get_first_nameless_record(samecodex, index, firstcode);
        int e = index;
        while (MapcoderData.codex(e) == samecodex) {
            e++;
        }
        e--;
        return e - i + 1;
    }

    public static int get_first_nameless_record(int samecodex, int index, int firstcode) {
        int i = index;
        while (i >= firstcode && MapcoderData.isNameless(i) && MapcoderData.codex(i) == samecodex) {
            i--;
        }
        i++;
        return i;
    }

}
