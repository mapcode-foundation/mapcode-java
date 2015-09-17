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
 * <p/>
 * This class contains the module that reads the Mapcode areas into memory and processes them.
 */
class DataAccess {
    private static final Logger LOG = LoggerFactory.getLogger(DataAccess.class);

    private static final int NR_TERRITORIES;
    private static final int NR_TERRITORY_RECORDS;
    private static final int[] DATA_START;
    private static final int[] FILE_DATA;

    private static final String FILE_NAME = "/com/mapcode/mminfo.dat";
    private static final int HEADER_SIZE = 8;

    // Read data only once in static initializer.
    static {
        LOG.info("DataAccess: reading regions from file: {}", FILE_NAME);
        final int bufferSize = 131072;
        final byte[] readBuffer = new byte[bufferSize];
        int total = 0;
        try {
            final InputStream inputStream = DataAccess.class.getResourceAsStream(FILE_NAME);
            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    int nrBytes = inputStream.read(readBuffer);
                    while (nrBytes >= 0) {
                        total += nrBytes;
                        outputStream.write(readBuffer, 0, nrBytes);
                        nrBytes = inputStream.read(readBuffer);
                    }

                    // Copy stream into data.
                    final byte[] bytes = outputStream.toByteArray();
                    assert total == bytes.length;

                    // Read SIGNATURE "MC", VERSION.
                    assert total > 12;
                    assert (char) bytes[0] == 'M';
                    assert (char) bytes[1] == 'C';
                    final int dataVersion = (bytes[2] & 255) + ((bytes[3] & 255) << 8);
                    assert (dataVersion >= 220);

                    // Read header: NR TERRITORIES, NR RECTANGLE RECORD.
                    NR_TERRITORY_RECORDS = (bytes[4] & 255) + ((bytes[5] & 255) << 8);
                    NR_TERRITORIES = (bytes[6] & 255) + ((bytes[7] & 255) << 8);
                    LOG.info("version={} NR_TERRITORIES={} NR_TERRITORY_RECORDS={}", dataVersion, NR_TERRITORIES, NR_TERRITORY_RECORDS);
                    final int expectedsize = HEADER_SIZE + ((NR_TERRITORIES + 1) * 2) + (NR_TERRITORY_RECORDS * 20);
                    assert (expectedsize == total);

                    // Read DATA+START array (2 bytes per territory, plus closing record).
                    DATA_START = new int[NR_TERRITORIES + 1];
                    int i = HEADER_SIZE;
                    for (int k = 0; k <= NR_TERRITORIES; k++) {
                        DATA_START[k] = (bytes[i] & 255) + ((bytes[i + 1] & 255) << 8);
                        i += 2;
                    }

                    // Read territory rectangle data (5 longs per record).
                    FILE_DATA = new int[NR_TERRITORY_RECORDS * 5];
                    for (int k = 0; k < (NR_TERRITORY_RECORDS * 5); k++) {
                        FILE_DATA[k] = ((bytes[i] & 255)) +
                                ((bytes[i + 1] & 255) << 8) +
                                ((bytes[i + 2] & 255) << 16) +
                                ((bytes[i + 3] & 255) << 24);
                        i += 4;
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
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

    static int getMinX(final int territoryRecord) {
        return FILE_DATA[territoryRecord * 5];
    }

    static int getMinY(final int territoryRecord) {
        return FILE_DATA[(territoryRecord * 5) + 1];
    }

    static int getMaxX(final int territoryRecord) {
        return FILE_DATA[(territoryRecord * 5) + 2];
    }

    static int getMaxY(final int territoryRecord) {
        return FILE_DATA[(territoryRecord * 5) + 3];
    }

    static int getDataFlags(final int territoryRecord) {
        return FILE_DATA[(territoryRecord * 5) + 4] & 65535;
    }

    static int getSmartDiv(final int territoryRecord) {
        return FILE_DATA[(territoryRecord * 5) + 4] >> 16;
    }

    // / low-level routines for data access
    static int getDataFirstRecord(final int territoryNumber) {
        return DATA_START[territoryNumber];
    }

    static int getDataLastRecord(final int territoryNumber) {
        return DATA_START[territoryNumber + 1] - 1;
    }
}
