/*
 * Copyright (C) 2014-2017, Stichting Mapcode Foundation (http://www.mapcode.com)
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// ----------------------------------------------------------------------------------------------
// Package private implementation class. For internal use within the Mapcode implementation only.
// ----------------------------------------------------------------------------------------------

/**
 * This class contains the module that reads the Mapcode areas into memory and processes them.
 */
@SuppressWarnings("MagicNumber")
class DataModel {
    private static final Logger LOG = LoggerFactory.getLogger(DataModel.class);

    // TODO: This class needs a thorough description of what the data file format looks like and what all bit fields means exactly.
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

    private static final String DATA_FILE_NAME = "/com/mapcode/mminfo.dat";
    private static final int FILE_BUFFER_SIZE = 50000;

    private static final int DATA_VERSION_MIN = 220;

    private static int readIntLoHi(final int lo, final int hi) {
        return (lo & 0xff) + ((hi & 0xff) << 8);
    }

    private static int readLongLoHi(final int lo, final int mid1, final int mid2, final int hi) {
        return ((lo & 0xff)) + ((mid1 & 0xff) << 8) + ((mid2 & 0xff) << 16) + ((hi & 0xff) << 24);
    }

    // Data.
    private final int nrTerritories;
    private final int nrTerritoryRecords;

    private final int[] index;
    private final int[] data;

    private static volatile DataModel instance = null;
    private static final Object mutex = new Object();

    @SuppressWarnings({"DoubleCheckedLocking", "SynchronizationOnStaticField"})
    public static DataModel getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new DataModel(DATA_FILE_NAME);
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("NestedTryStatement")
    DataModel(@Nonnull final String fileName) throws IncorrectDataModelException {
        // Read data only once in static initializer.
        LOG.info("DataModel: reading regions from file: {}", fileName);
        final byte[] readBuffer = new byte[FILE_BUFFER_SIZE];
        int total = 0;
        try {
            final InputStream inputStream = DataModel.class.getResourceAsStream(fileName);
            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {

                    // Read the input stream, copy to memory buffer.
                    int nrBytes = inputStream.read(readBuffer);
                    while (nrBytes > 0) {
                        total += nrBytes;
                        outputStream.write(readBuffer, 0, nrBytes);
                        nrBytes = inputStream.read(readBuffer);
                    }

                    // Copy stream into data.
                    final byte[] bytes = outputStream.toByteArray();
                    assert total == bytes.length;
                    if (total < 12) {
                        LOG.error("DataModel: expected more than {} bytes", total);
                        throw new IncorrectDataModelException("Data file corrupt: " + fileName);
                    }

                    // Read "MC", VERSION.
                    assert total > 8;  // "MC" (2) + VERSION (2) + NR TERRITORIES (2) + NR TERRITORY RECORDS (2).
                    if ((bytes[HEADER_ID_1] != 'M') || (bytes[HEADER_ID_2] != 'C')) {
                        throw new IncorrectDataModelException("Data file does not start with correct header: " + fileName);
                    }
                    final int dataVersion = readIntLoHi(bytes[HEADER_VERSION_LO], bytes[HEADER_VERSION_HI]);

                    if (dataVersion < DATA_VERSION_MIN) {
                        throw new IncorrectDataModelException("Data file version " + dataVersion + " too low: " + fileName);
                    }

                    // Read header: NR TERRITORIES, NR RECTANGLE RECORDS.
                    nrTerritoryRecords = readIntLoHi(bytes[HEADER_NR_TERRITORIES_RECS_LO], bytes[HEADER_NR_TERRITORIES_RECS_HI]);
                    nrTerritories = readIntLoHi(bytes[HEADER_NR_TERRITORIES_LO], bytes[HEADER_NR_TERRITORIES_HI]);

                    // Check if the number of territories matches the enumeration in Territory.
                    if (nrTerritories != Territory.values().length) {
                        LOG.error("DataModel: expected {} territories, got {}", Territory.values().length, nrTerritories);
                        throw new IncorrectDataModelException("Data file corrupt: " + fileName);
                    }

                    // Check if the expected file size matched what we found.
                    final int expectedSize = HEADER_SIZE +
                            ((nrTerritories + 1) * BYTES_PER_INT) +
                            (nrTerritoryRecords * (DATA_FIELDS_PER_REC * BYTES_PER_LONG));

                    if (expectedSize != total) {
                        LOG.error("DataModel: expected {} bytes, got {}", expectedSize, total);
                        throw new IncorrectDataModelException("Data file corrupt: " + fileName);
                    }
                    LOG.debug("DataModel: version={} territories={} territory records={}", dataVersion, nrTerritories, nrTerritoryRecords);

                    // Read DATA+START array (2 bytes per territory, plus closing record).
                    index = new int[nrTerritories + 1];
                    int i = HEADER_SIZE;
                    for (int k = 0; k <= nrTerritories; k++) {
                        index[k] = readIntLoHi(bytes[i], bytes[i + 1]);
                        i += 2;
                    }

                    // Read territory rectangle data (DATA_FIELDS_PER_REC longs per record).
                    data = new int[nrTerritoryRecords * DATA_FIELDS_PER_REC];
                    for (int k = 0; k < (nrTerritoryRecords * DATA_FIELDS_PER_REC); k++) {
                        data[k] = readLongLoHi(bytes[i], bytes[i + 1], bytes[i + 2], bytes[i + 3]);
                        i += 4;
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (final IOException e) {
            throw new IncorrectDataModelException("Cannot initialize static data structure from: " +
                    fileName + ", exception=" + e);
        }
        LOG.info("DataModel: regions initialized, read {} bytes", total);
    }

    /**
     * Get number of territories.
     *
     * @return Number of territories.
     */
    int getNrTerritories() {
        return nrTerritories;
    }

    /**
     * Get number of territory records (rectangles per territory).
     *
     * @return Number of rectangles per territory.
     */
    // TODO: Explain what territory records contain exactly.
    int getNrTerritoryRecords() {
        return nrTerritoryRecords;
    }

    @SuppressWarnings("PointlessArithmeticExpression")
        // TODO: Explain what this does exactly, why not return a Point or Rectangle?
    int getLonMicroDegMin(final int territoryRecord) {
        return data[((territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LON_MICRO_DEG_MIN)];
    }

    int getLatMicroDegMin(final int territoryRecord) {
        return data[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LAT_MICRO_DEG_MIN];
    }

    int getLonMicroDegMax(final int territoryRecord) {
        return data[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LON_MICRO_DEG_MAX];
    }

    int getLatMicroDegMax(final int territoryRecord) {
        return data[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_LAT_MICRO_DEG_MAX];
    }

    int getDataFlags(final int territoryRecord) {
        return data[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_DATA_FLAGS] & MASK_DATA_DATA_FLAGS;
    }

    // TODO: Explain what a "div" and "smart div" is and how you use, and why you need to use it.
    int getSmartDiv(final int territoryRecord) {
        return data[(territoryRecord * DATA_FIELDS_PER_REC) + POS_DATA_DATA_FLAGS] >> SHIFT_POS_DATA_SMART_DIV;
    }

    // TODO: Explain what these methods do exactly.
    // Low-level routines for data access.
    @SuppressWarnings("PointlessArithmeticExpression")
    int getDataFirstRecord(final int territoryNumber) {
        assert (0 <= territoryNumber) && (territoryNumber <= Territory.AAA.getNumber());
        return index[territoryNumber + POS_INDEX_FIRST_RECORD];
    }

    int getDataLastRecord(final int territoryNumber) {
        assert (0 <= territoryNumber) && (territoryNumber <= Territory.AAA.getNumber());
        return index[territoryNumber + POS_INDEX_LAST_RECORD] - 1;
    }
}
