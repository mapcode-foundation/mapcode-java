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
    private static final String FILE_NAME = "/com/mapcode/mminfo.dat";

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
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (final IOException ignored) {
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
        return asUnsignedByte((i * 20) + 16) + (asUnsignedByte((i * 20) + 17) * 256);
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

    private final static int[] DATA_START = {
            0, 3, 6, 9, 13, 16, 18, 19, 30, 31,
            33, 35, 37, 42, 44, 47, 51, 54, 56, 58,
            60, 62, 64, 68, 71, 79, 81, 116, 121, 131,
            133, 135, 138, 143, 155, 159, 165, 167, 169, 173,
            176, 183, 185, 187, 189, 193, 195, 202, 208, 212,
            216, 219, 221, 223, 250, 252, 259, 261, 266, 284,
            288, 292, 296, 298, 303, 311, 315, 325, 327, 332,
            336, 342, 356, 360, 365, 369, 385, 391, 396, 404,
            412, 428, 432, 436, 443, 454, 491, 495, 521, 527,
            545, 557, 593, 659, 694, 733, 745, 754, 782, 798,
            832, 848, 882, 918, 932, 956, 967, 1022, 1080, 1094,
            1155, 1179, 1208, 1276, 1353, 1405, 1484, 1569, 1621, 1696,
            1772, 1833, 1875, 1900, 1948, 1991, 2036, 2097, 2183, 2270,
            2319, 2382, 2467, 2558, 2643, 2702, 2815, 2912, 3004, 3088,
            3186, 3248, 3350, 3416, 3511, 3602, 3688, 3776, 3866, 3973,
            4119, 4221, 4325, 4356, 4393, 4468, 4504, 4538, 4574, 4626,
            4690, 4741, 4756, 4791, 4931, 5017, 5067, 5143, 5168, 5236,
            5293, 5328, 5408, 5459, 5530, 5623, 5658, 5736, 5795, 5861,
            5918, 5971, 6028, 6065, 6156, 6285, 6335, 6365, 6417, 6513,
            6546, 6627, 6675, 6755, 6798, 6892, 6926, 6971, 7078, 7083,
            7155, 7199, 7283, 7323, 7347, 7404, 7490, 7564, 7603, 7690,
            7766, 7874, 7952, 8026, 8096, 8153, 8235, 8317, 8376, 8403,
            8441, 8495, 8571, 8681, 8761, 8825, 8882, 8936, 9014, 9085,
            9171, 9221, 9282, 9426, 9430, 9435, 9440, 9445, 9451, 9456,
            9464, 9472, 9483, 9491, 9506, 9515, 9526, 9559, 9568, 9576,
            9591, 9601, 9611, 9619, 9627, 9643, 9651, 9661, 9672, 9682,
            9692, 9701, 9710, 9721, 9762, 9803, 9821, 9918, 10069, 10160,
            10242, 10437, 10440, 10443, 10446, 10456, 10461, 10464, 10476, 10489,
            10500, 10516, 10525, 10531, 10565, 10593, 10612, 10680, 10772, 10787,
            10791, 10827, 10914, 10925, 10943, 10960, 11024, 11056, 11097, 11117,
            11222, 11302, 11366, 11420, 11479, 11535, 11595, 11601, 11643, 11647,
            11650, 11660, 11675, 11686, 11724, 11787, 11833, 11838, 11845, 11850,
            11869, 11879, 11886, 11894, 11908, 11924, 11931, 11939, 11947, 11995,
            12004, 12015, 12065, 12076, 12086, 12105, 12121, 12134, 12146, 12165,
            12199, 12208, 12220, 12232, 12236, 12242, 12248, 12260, 12271, 12280,
            12286, 12300, 12318, 12329, 12371, 12422, 12472, 12528, 12541, 12554,
            12617, 12677, 12738, 12803, 12874, 12939, 13008, 13083, 13150, 13229,
            13303, 13317, 13332, 13377, 13390, 13418, 13432, 13445, 13461, 13477,
            13489, 13531, 13543, 13554, 13589, 13604, 13616, 13633, 13641, 13679,
            13692, 13707, 13827, 13873, 13917, 13957, 13981, 14022, 14061, 14081,
            14097, 14115, 14127, 14141, 14149, 14160, 14177, 14190, 14245, 14246,
            14247, 14248, 14287, 14293, 14298, 14303, 14308, 14314, 14321, 14327,
            14336, 14342, 14350, 14356, 14363, 14372, 14380, 14389, 14401, 14410,
            14418, 14429, 14440, 14449, 14459, 14466, 14516, 14550, 14559, 14571,
            14582, 14621, 14632, 14646, 14663, 14675, 14688, 14702, 14717, 14730,
            14751, 14759, 14778, 14787, 14799, 14810, 14824, 14839, 14852, 14866,
            14879, 14893, 14908, 14926, 14946, 14958, 14978, 14991, 15006, 15020,
            15038, 15054, 15068, 15081, 15098, 15118, 15135, 15153, 15168, 15182,
            15198, 15209, 15225, 15238, 15253, 15268, 15314, 15328, 15337, 15347,
            15360, 15374, 15385, 15398, 15414, 15429, 15430, 15457, 15463, 15471,
            15477, 15493, 15530, 15547, 15571, 15594, 15611, 15631, 15657, 15683,
            15702, 15719, 15772, 15813, 15835, 15862, 15882, 15903, 15921, 15951,
            15977, 15997, 16018, 16036, 16054, 16073, 16089, 16105, 16133, 16159,
            16166, 16168, 16169, 16171, 16172, 16174, 16176, 16178, 16180, 16182,
            16183, 16184, 16216};

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
