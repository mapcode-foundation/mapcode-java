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
    private static final Logger LOG = LoggerFactory.getLogger(DataAccess.class);

    private static final int[] FILE_DATA;
    private static final String FILE_NAME = "/com/mapcode/mminfo.dat";

    // Read data only once in static initializer.
    static {
        LOG.info("DataAccess: reading regions from file: {}", FILE_NAME);
        final int bufferSize = 100000;
        final byte[] readBuffer = new byte[bufferSize];
        int total = 0;
        try (final InputStream inputStream = DataAccess.class.getResourceAsStream(FILE_NAME)) {
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                int nrBytes = inputStream.read(readBuffer);
                while (nrBytes >= 0) {
                    total += nrBytes;
                    outputStream.write(readBuffer, 0, nrBytes);
                    nrBytes = inputStream.read(readBuffer);
                }

                // Copy stream as unsigned bytes (ints).
                final byte[] bytes = outputStream.toByteArray();
                assert total == bytes.length;
                FILE_DATA = new int[total];
                for (int i = 0; i < total; ++i) {
                    FILE_DATA[i] = (bytes[i] < 0) ? (bytes[i] + 256) : bytes[i];

                }
            }
        } catch (final IOException e) {
            throw new ExceptionInInitializerError("Cannot initialize static data structure from: " +
                    FILE_NAME + ", exception=" + e);
        }
        LOG.info("DataAccess: regions initialized, read {} bytes", total);
    }

    private DataAccess() {
        // Empty.
    }

    static int dataFlags(final int i) {
        return FILE_DATA[(i * 20) + 16] +
                (FILE_DATA[(i * 20) + 17] * 256);
    }

    static int asLong(final int i) {
        return FILE_DATA[i] +
                (FILE_DATA[i + 1] << 8) +
                (FILE_DATA[i + 2] << 16) +
                (FILE_DATA[i + 3] << 24);
    }

    static int smartDiv(final int i) {
        return FILE_DATA[(i * 20) + 18] +
                (FILE_DATA[(i * 20) + 19] * 256);
    }

    private final static int[] DATA_START = {
            0, 3, 6, 10, 14, 17, 19, 20, 31, 32,
            34, 36, 38, 43, 45, 48, 52, 59, 63, 65,
            67, 71, 73, 81, 87, 95, 97, 132, 139, 149,
            151, 153, 156, 161, 173, 181, 188, 190, 192, 197,
            200, 207, 212, 214, 216, 220, 222, 229, 235, 239,
            243, 246, 250, 252, 281, 283, 290, 292, 297, 317,
            325, 329, 333, 335, 340, 348, 353, 364, 368, 373,
            377, 386, 400, 404, 409, 413, 429, 435, 440, 448,
            456, 472, 476, 480, 487, 498, 535, 539, 565, 571,
            589, 601, 637, 703, 738, 777, 789, 798, 826, 842,
            876, 892, 926, 962, 991, 1015, 1026, 1081, 1139, 1153,
            1215, 1239, 1268, 1336, 1414, 1467, 1546, 1631, 1683, 1758,
            1834, 1895, 1937, 1962, 2010, 2053, 2098, 2159, 2245, 2332,
            2383, 2446, 2531, 2622, 2707, 2766, 2881, 2984, 3077, 3161,
            3259, 3321, 3425, 3491, 3586, 3682, 3768, 3856, 3946, 4053,
            4199, 4301, 4405, 4436, 4473, 4550, 4586, 4620, 4656, 4708,
            4772, 4823, 4838, 4873, 5020, 5106, 5156, 5232, 5257, 5325,
            5382, 5417, 5499, 5550, 5623, 5716, 5751, 5829, 5888, 5954,
            6011, 6064, 6121, 6158, 6249, 6380, 6430, 6460, 6512, 6608,
            6641, 6722, 6770, 6850, 6893, 6987, 7021, 7066, 7173, 7177,
            7249, 7293, 7379, 7419, 7446, 7503, 7589, 7663, 7702, 7789,
            7865, 7973, 8051, 8125, 8195, 8252, 8334, 8416, 8475, 8502,
            8540, 8594, 8670, 8783, 8863, 8927, 8984, 9038, 9118, 9189,
            9275, 9325, 9389, 9533, 9537, 9542, 9547, 9552, 9558, 9563,
            9571, 9579, 9590, 9598, 9613, 9622, 9633, 9666, 9675, 9683,
            9698, 9708, 9718, 9726, 9734, 9750, 9758, 9768, 9779, 9789,
            9799, 9808, 9817, 9828, 9869, 9910, 9928, 10025, 10176, 10267,
            10349, 10544, 10547, 10550, 10553, 10563, 10568, 10571, 10583, 10596,
            10607, 10623, 10632, 10638, 10672, 10700, 10719, 10787, 10879, 10894,
            10898, 10934, 11021, 11032, 11050, 11067, 11131, 11163, 11204, 11224,
            11329, 11409, 11473, 11527, 11586, 11642, 11702, 11709, 11751, 11755,
            11758, 11768, 11783, 11794, 11832, 11895, 11941, 11946, 11953, 11958,
            11977, 11987, 11994, 12002, 12016, 12032, 12039, 12047, 12056, 12104,
            12113, 12124, 12174, 12185, 12195, 12214, 12230, 12243, 12255, 12274,
            12308, 12317, 12329, 12341, 12345, 12351, 12357, 12369, 12380, 12389,
            12395, 12409, 12429, 12440, 12482, 12533, 12583, 12639, 12652, 12665,
            12728, 12788, 12849, 12914, 12985, 13050, 13119, 13194, 13262, 13341,
            13418, 13432, 13447, 13492, 13505, 13533, 13547, 13560, 13576, 13592,
            13604, 13646, 13658, 13669, 13704, 13719, 13731, 13748, 13756, 13794,
            13807, 13822, 13942, 13988, 14032, 14072, 14096, 14137, 14176, 14196,
            14212, 14230, 14242, 14256, 14264, 14275, 14292, 14305, 14360, 14361,
            14362, 14363, 14403, 14409, 14414, 14419, 14424, 14430, 14437, 14443,
            14452, 14458, 14466, 14472, 14479, 14488, 14496, 14505, 14517, 14526,
            14534, 14545, 14556, 14565, 14575, 14582, 14632, 14666, 14675, 14687,
            14698, 14737, 14748, 14762, 14779, 14791, 14804, 14818, 14833, 14846,
            14867, 14875, 14894, 14903, 14915, 14926, 14940, 14955, 14968, 14982,
            14995, 15009, 15024, 15042, 15062, 15074, 15094, 15107, 15122, 15136,
            15154, 15170, 15184, 15197, 15214, 15234, 15251, 15269, 15284, 15298,
            15314, 15325, 15341, 15354, 15369, 15384, 15430, 15444, 15453, 15463,
            15476, 15490, 15501, 15514, 15530, 15545, 15546, 15573, 15579, 15587,
            15593, 15609, 15646, 15663, 15687, 15710, 15727, 15747, 15773, 15799,
            15818, 15835, 15888, 15929, 15951, 15978, 15998, 16019, 16037, 16068,
            16094, 16114, 16135, 16153, 16171, 16190, 16206, 16222, 16276, 16302,
            16309, 16311, 16312, 16344
    };

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
