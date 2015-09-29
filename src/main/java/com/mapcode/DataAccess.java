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

    private static final int[] INDEX;
    private static final int[] DATA;

    private static final int HEADER_ID_1 = 0;
    private static final int HEADER_ID_2 = 1;
    private static final int HEADER_VERSION_LO = 2;
    private static final int HEADER_VERSION_HI = 3;
    private static final int HEADER_NR_TERRITORIES_RECS_LO = 4;
    private static final int HEADER_NR_TERRITORIES_RECS_HI = 5;
    private static final int HEADER_NR_TERRITORIES_LO = 6;
    private static final int HEADER_NR_TERRITORIES_HI = 7;
    private static final int HEADER_SIZE = HEADER_NR_TERRITORIES_HI + 1;

    private static final int BYTES_PER_INT = 2;
    private static final int BYTES_PER_LONG = 4;

    private static final int POS_DATA_LON_MICRO_DEG_MIN = 0;
    private static final int POS_DATA_LAT_MICRO_DEG_MIN = 1;
    private static final int POS_DATA_LON_MICRO_DEG_MAX = 2;
    private static final int POS_DATA_LAT_MICRO_DEG_MAX = 3;
    private static final int POS_DATA_DATA_FLAGS = 4;
    private static final int DATA_FIELDS_PER_REC = 5;

    private static final int MASK_DATA_DATA_FLAGS = 0xffff;
    private static final int SHIFT_POS_DATA_SMART_DIV = 16;

    private static final int POS_INDEX_FIRST_RECORD = 0;
    private static final int POS_INDEX_LAST_RECORD = 1;

    private static final String FILE_NAME = "/com/mapcode/mminfo.dat";
    private static final int FILE_BUFFER_SIZE = 50000;

    // Read data only once in static initializer.
    static {
        LOG.info("DataAccess: reading regions from file: {}", FILE_NAME);
        final byte[] readBuffer = new byte[FILE_BUFFER_SIZE];
        int total = 0;
        try {
            final InputStream inputStream = DataAccess.class.getResourceAsStream(FILE_NAME);
            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    int nrBytes = inputStream.read(readBuffer);
                    while (nrBytes > 0) {
                        total += nrBytes;
                        outputStream.write(readBuffer, 0, nrBytes);
                        nrBytes = inputStream.read(readBuffer);
                    }

                    // Copy stream into data.
                    final byte[] bytes = outputStream.toByteArray();
                    assert total == bytes.length;

                    // Read SIGNATURE "MC", VERSION.
                    assert total > 12;
                    assert (char) bytes[HEADER_ID_1] == 'M';
                    assert (char) bytes[HEADER_ID_2] == 'C';
                    final int dataVersion = readIntLoHi(bytes[HEADER_VERSION_LO], bytes[HEADER_VERSION_HI]);
                    assert (dataVersion >= 220);

                    // Read header: NR TERRITORIES, NR RECTANGLE RECORD.
                    NR_TERRITORY_RECORDS = readIntLoHi(bytes[HEADER_NR_TERRITORIES_RECS_LO], bytes[HEADER_NR_TERRITORIES_RECS_HI]);
                    NR_TERRITORIES = readIntLoHi(bytes[HEADER_NR_TERRITORIES_LO], bytes[HEADER_NR_TERRITORIES_HI]);
                    final int expectedSize = HEADER_SIZE +
                            ((NR_TERRITORIES + 1) * BYTES_PER_INT) +
                            (NR_TERRITORY_RECORDS * (DATA_FIELDS_PER_REC * BYTES_PER_LONG));

                    if (expectedSize != total) {
                        LOG.error("DataAccess: expected {} territories, got {}", expectedSize, total);
                        throw new IllegalStateException("Data file corrupt: " + FILE_NAME);
                    }
                    LOG.debug("DataAccess: version={} territories={} territory records={}", dataVersion, NR_TERRITORIES, NR_TERRITORY_RECORDS);

                    // Read DATA+START array (2 bytes per territory, plus closing record).
                    INDEX = new int[NR_TERRITORIES + 1];
                    int i = HEADER_SIZE;
                    for (int k = 0; k <= NR_TERRITORIES; k++) {
                        INDEX[k] = readIntLoHi(bytes[i], bytes[i + 1]);
                        i += 2;
                    }

                    // Read territory rectangle data (DATA_FIELDS_PER_REC longs per record).
                    DATA = new int[NR_TERRITORY_RECORDS * DATA_FIELDS_PER_REC];
                    for (int k = 0; k < (NR_TERRITORY_RECORDS * DATA_FIELDS_PER_REC); k++) {
                        DATA[k] = readLongLoHi(bytes[i], bytes[i + 1], bytes[i + 2], bytes[i + 3]);
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

    private static int readIntLoHi(final int lo, final int hi) {
        return (lo & 0xff) + ((hi & 0xff) << 8);
    }

    private static int readLongLoHi(final int lo, final int mid1, final int mid2, final int hi) {
        return ((lo & 0xff)) + ((mid1 & 0xff) << 8) + ((mid2 & 0xff) << 16) + ((hi & 0xff) << 24);
    }

    private DataAccess() {
        // Empty.
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    static int getLonMicroDegMin(final int territoryRecord) {
        return DATA[((territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LON_MICRO_DEG_MIN)];
    }

    static int getLatMicroDegMin(final int territoryRecord) {
        return DATA[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LAT_MICRO_DEG_MIN];
    }

    static int getLonMicroDegMax(final int territoryRecord) {
        return DATA[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LON_MICRO_DEG_MAX];
    }

    static int getLatMicroDegMax(final int territoryRecord) {
        return DATA[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LAT_MICRO_DEG_MAX];
    }

    static int getDataFlags(final int territoryRecord) {
        return DATA[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_DATA_FLAGS] & MASK_DATA_DATA_FLAGS;
    }

    static int getSmartDiv(final int territoryRecord) {
        return DATA[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_DATA_FLAGS] >> SHIFT_POS_DATA_SMART_DIV;
    }

    // Low-level routines for data access.
    @SuppressWarnings("PointlessArithmeticExpression")
    static int getDataFirstRecord(final int territoryNumber) {
        return INDEX[territoryNumber + POS_INDEX_FIRST_RECORD];
    }

    static int getDataLastRecord(final int territoryNumber) {
        return INDEX[territoryNumber + POS_INDEX_LAST_RECORD] - 1;
    }
}

