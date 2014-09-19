/*
 * Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class contains the module that reads the Mapcode areas into memory and processes them.
 */
class DataAccess {

    private static final byte[] FILE_DATA;
    private static final String FILE_NAME = "/mminfo.dat";

    // Read data only once in static initializer.
    static {
        boolean initialized = false;
        final InputStream inputStream = DataAccess.class.getResourceAsStream(FILE_NAME);
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (int readBytes = inputStream.read(); readBytes >= 0; readBytes = inputStream.read()) {
                outputStream.write(readBytes);
            }

            FILE_DATA = outputStream.toByteArray();
            inputStream.close();
            outputStream.close();
            initialized = true;
        }
        catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (final IOException ignored) {
                // Ignore.
            }
        }
        if (!initialized) {
            throw new IllegalArgumentException("Cannot initialize static data structure from: " + FILE_NAME);
        }
    }

    private DataAccess() {
        // Empty.
    }

    private static int asUnsignedByte(final int i) {
        int u = FILE_DATA[i];
        if (u < 0) {
            u += 256;
        }
        return u;
    }

    static int dataFlags(final int i) {
        return asUnsignedByte((i * 20) + 16) + asUnsignedByte((i * 20) + 17) * 256;
    }

    static int asLong(final int i) {
        return asUnsignedByte(i) +
            (asUnsignedByte(i + 1) << 8) +
            (asUnsignedByte(i + 2) << 16) +
            (asUnsignedByte(i + 3) << 24);
    }

    static int smartDiv(final int i) {
        return asUnsignedByte((i * 20) + 18) + (asUnsignedByte((i * 20) + 19) * 256);
    }

    private static final int[] DATA_START = {
        0, 3, 6, 9, 13, 16, 18, 19, 30, 31, 33, 35, 37, 42, 44, 47, 51, 54, 56, 58, 60, 62, 64, 68, 71, 79, 81, 116,
        121, 131, 133, 135, 138, 143, 155, 159, 165, 167, 169, 173, 176, 183, 185, 187, 189, 193, 195, 202, 208, 212,
        216, 219, 221, 223, 250, 252, 259, 261, 266, 284, 288, 292, 296, 298, 303, 311, 315, 325, 327, 332, 336,
        342, 356, 360, 365, 369, 385, 391, 396, 404, 412, 428, 432, 436, 443, 454, 491, 495, 521, 527, 545, 557,
        593, 659, 694, 733, 745, 754, 782, 798, 832, 848, 882, 918, 932, 956, 967, 1022, 1080, 1094, 1155, 1179,
        1208, 1276, 1353, 1405, 1484, 1569, 1621, 1696, 1772, 1833, 1875, 1900, 1948, 1991, 2036, 2097, 2183, 2270,
        2319, 2382, 2467, 2558, 2643, 2702, 2815, 2912, 3004, 3088, 3186, 3248, 3350, 3416, 3511, 3602, 3688, 3776,
        3866, 3973, 4119, 4221, 4325, 4356, 4393, 4468, 4504, 4538, 4574, 4626, 4690, 4741, 4756, 4791, 4931, 5017,
        5067, 5143, 5168, 5236, 5293, 5328, 5408, 5459, 5530, 5623, 5658, 5736, 5795, 5861, 5918, 5971, 6028, 6065,
        6156, 6285, 6335, 6365, 6417, 6513, 6546, 6627, 6675, 6755, 6798, 6892, 6926, 6971, 7078, 7083, 7155, 7199,
        7283, 7323, 7347, 7404, 7490, 7564, 7603, 7690, 7766, 7874, 7952, 8026, 8096, 8153, 8235, 8317, 8376, 8403,
        8441, 8495, 8571, 8681, 8761, 8825, 8882, 8936, 9014, 9085, 9171, 9221, 9282, 9426, 9430, 9435, 9440, 9445,
        9451, 9456, 9464, 9472, 9483, 9491, 9506, 9515, 9526, 9559, 9568, 9576, 9591, 9601, 9611, 9619, 9627, 9643,
        9651, 9661, 9672, 9682, 9692, 9701, 9710, 9721, 9762, 9803, 9821, 9918, 10069, 10160, 10242, 10437, 10440,
        10443, 10446, 10456, 10461, 10464, 10476, 10489, 10500, 10516, 10525, 10531, 10565, 10593, 10612, 10680,
        10772, 10787, 10791, 10827, 10914, 10925, 10943, 10960, 11024, 11056, 11097, 11117, 11222, 11302, 11366,
        11425, 11481, 11541, 11547, 11589, 11593, 11596, 11606, 11621, 11632, 11670, 11733, 11779, 11784, 11791,
        11796, 11815, 11825, 11832, 11840, 11854, 11870, 11877, 11885, 11893, 11941, 11950, 11961, 12011, 12022,
        12032, 12051, 12067, 12080, 12092, 12111, 12145, 12154, 12166, 12178, 12182, 12188, 12194, 12206, 12217,
        12226, 12232, 12246, 12264, 12275, 12317, 12368, 12418, 12474, 12487, 12500, 12563, 12623, 12684, 12749,
        12820, 12885, 12954, 13029, 13096, 13175, 13249, 13263, 13278, 13323, 13336, 13364, 13378, 13391, 13407,
        13423, 13435, 13477, 13489, 13500, 13535, 13550, 13562, 13579, 13587, 13625, 13638, 13653, 13773, 13819,
        13863, 13903, 13927, 13968, 14007, 14027, 14043, 14061, 14073, 14087, 14095, 14106, 14123, 14136, 14191,
        14192, 14193, 14194, 14233, 14239, 14244, 14249, 14254, 14260, 14267, 14273, 14282, 14288, 14296, 14302,
        14309, 14318, 14326, 14335, 14347, 14356, 14364, 14375, 14386, 14395, 14405, 14412, 14462, 14496, 14505,
        14517, 14528, 14567, 14578, 14592, 14609, 14621, 14634, 14648, 14663, 14676, 14697, 14705, 14724, 14733,
        14745, 14756, 14770, 14785, 14798, 14812, 14825, 14839, 14854, 14872, 14892, 14904, 14924, 14937, 14952,
        14966, 14984, 15000, 15014, 15027, 15044, 15064, 15081, 15099, 15114, 15128, 15144, 15155, 15171, 15184,
        15199, 15214, 15260, 15274, 15283, 15293, 15306, 15320, 15331, 15344, 15360, 15375, 15376, 15403, 15409,
        15417, 15423, 15439, 15476, 15493, 15517, 15540, 15557, 15577, 15603, 15629, 15648, 15665, 15718, 15759,
        15781, 15808, 15828, 15849, 15867, 15897, 15923, 15943, 15964, 15982, 16000, 16019, 16035, 16051, 16079,
        16105, 16112, 16114, 16115, 16117, 16118, 16120, 16122, 16124, 16126, 16128, 16129, 16130, 16162};

    // / low-level routines for data access
    static int dataFirstRecord(final int ccode) {
        return DATA_START[ccode];
    }

    static int dataLastRecord(final int ccode) {
        return DATA_START[ccode + 1] - 1;
    }

    static int numberOfSubAreas() {
        return DATA_START[DATA_START.length - 1];
    }
}
