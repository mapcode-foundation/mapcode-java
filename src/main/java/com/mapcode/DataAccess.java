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

    private static final int nrTerritories;
    private static final int nrTerritoryRecords;
    private static final int[] DATA_START;
    private static final int[] FILE_DATA;

    private static final String FILE_NAME = "/com/mapcode/mminfo.dat";

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

                    // Copy stream into data
                    final byte[] bytes = outputStream.toByteArray();
                    assert total == bytes.length;
                    
                    // read array sizes
                    assert total > 4;
                    nrTerritoryRecords = (bytes[0] & 255) + ((bytes[1] & 255) << 8);
                    nrTerritories = (bytes[2] & 255) + ((bytes[3] & 255) << 8);
                    LOG.info("nrTerritories={} nrTerritoryRecords={}",nrTerritories,nrTerritoryRecords);
                    assert (2 + 2 + ((nrTerritories + 1) * 2) + (nrTerritoryRecords * 20) == total);

                    // read DATA+START array
                    DATA_START = new int[nrTerritories + 1];
                    int i = 4;
                    for (int k=0; k <= nrTerritories; k++) {
                        DATA_START[k] = (bytes[i] & 255) + ((bytes[i + 1] & 255) << 8);
                        i += 2;
                    }
                    
                    // read territory rectangle data (mminfo)
                    FILE_DATA = new int[nrTerritoryRecords * 5];
                    for (int k=0; k < nrTerritoryRecords * 5; k++) {
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

    static int minx(final int i) { 
        return FILE_DATA[i * 5];
    }
    
    static int miny(final int i) {
        return FILE_DATA[(i * 5) + 1];
    }
    
    static int maxx(final int i) {
        return FILE_DATA[(i * 5) + 2];
    }

    static int maxy(final int i) {
        return FILE_DATA[(i * 5) + 3];
    }

    static int dataFlags(final int i) {
        return FILE_DATA[(i * 5) + 4] & 65535;
    }

    static int smartDiv(final int i) {
        return FILE_DATA[(i * 5) + 4] >> 16;
    }

    // / low-level routines for data access
    static int dataFirstRecord(final int ccode) {
        return DATA_START[ccode];
    }

    static int dataLastRecord(final int ccode) {
        return DATA_START[ccode + 1] - 1;
    }
}
