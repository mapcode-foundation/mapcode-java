/*
 * Copyright (C) 2014-2019, Stichting Mapcode Foundation (http://www.mapcode.com)
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"ProhibitedExceptionDeclared", "OverlyBroadThrowsClause", "MagicNumber"})
public class ReferenceFileTest {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceFileTest.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private static final String RANDOM_REFERENCE_FILE_1 = "/random_e8_1k.txt";
    private static final String RANDOM_REFERENCE_FILE_2 = "/random_e8_10k.txt";
    private static final String RANDOM_REFERENCE_FILE_3 = "/random_e8_100k.txt";
    private static final String GRID_REFERENCE_FILE_1 = "/grid_e8_1k.txt";
    private static final String GRID_REFERENCE_FILE_2 = "/grid_e8_10k.txt";
    private static final String GRID_REFERENCE_FILE_3 = "/grid_e8_100k.txt";
    private static final String BOUNDARIES_REFERENCE_FILE = "/boundaries_e8.txt";

    private static final int PRECISION_MAX = 8;

    private static final int LOG_LINE_EVERY = 10000;
    private static final Pattern PATTERN_SPACE = Pattern.compile(" ");

    @Test
    public void checkRandomReferenceRecords() throws Exception {
        LOG.info("checkRandomReferenceRecords");
        for (int i = 0; i < 8; ++i) {
            checkFile(i, RANDOM_REFERENCE_FILE_1, false);
        }
        checkFile(0, RANDOM_REFERENCE_FILE_2, false);
        checkFile(0, RANDOM_REFERENCE_FILE_3, false);
        checkFile(8, RANDOM_REFERENCE_FILE_2, false);
        checkFile(8, RANDOM_REFERENCE_FILE_3, false);
    }

    @Test
    public void checkGridReferenceRecords() throws Exception {
        LOG.info("checkGridReferenceRecords");
        for (int i = 0; i < 8; ++i) {
            checkFile(i, GRID_REFERENCE_FILE_1, false);
        }
        checkFile(0, GRID_REFERENCE_FILE_2, false);
        checkFile(0, GRID_REFERENCE_FILE_3, false);
        checkFile(8, GRID_REFERENCE_FILE_2, false);
        checkFile(8, GRID_REFERENCE_FILE_3, false);
    }

    @Test
    public void checkBoundariesReferenceRecords() throws Exception {
        LOG.info("checkBoundariesReferenceRecords");
        checkFile(0, BOUNDARIES_REFERENCE_FILE, false);
        checkFile(8, BOUNDARIES_REFERENCE_FILE, true);
    }

    private static void checkFile(
            final int precision,
            @Nonnull final String baseFileName,
            final boolean checkAllDistances) throws Exception {
        assert (0 <= precision) && (precision <= PRECISION_MAX);

        // Reset error count.
        final AtomicLong[] deltaNm = new AtomicLong[PRECISION_MAX];
        for (int p = 0; p < PRECISION_MAX; ++p) {
            deltaNm[p] = new AtomicLong(0);
        }
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicInteger tasks = new AtomicInteger(0);

        final int threads = Math.min(8, Runtime.getRuntime().availableProcessors() * 2);
        LOG.info("checkFile: Starting {} threads...", threads);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Open data file.
        final ChunkedFile chunkedFile = new ChunkedFile(baseFileName);
        //noinspection CatchMayIgnoreException
        try {

            //noinspection InfiniteLoopStatement
            while (true) {

                // Get next record.
                @Nonnull final ReferenceRec reference = getNextReferenceRecord(chunkedFile);

                // Add task. This may throw an exception if the queue is full. Retry in that case.
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        final int count = tasks.getAndIncrement();
                        if (((count % LOG_LINE_EVERY) == 0)) {
                            LOG.info("checkFile: #{}, file={}", count, chunkedFile.fileName);
                        }

                        // Encode lat/lon to series of mapcodes and check the resulting mapcodes.
                        final List<Mapcode> results = MapcodeCodec.encode(
                                reference.point.getLatDeg(), reference.point.getLonDeg());

                        // Check the number of mapcodes.
                        if (results.isEmpty()) {
                            LOG.error("checkFile: encode fails, no results found for reference={}", reference);
                            errors.incrementAndGet();
                        }

                        // Check if last mapcode is the international code.
                        final Mapcode resultInternational = MapcodeCodec.encodeToInternational(
                                reference.point.getLatDeg(), reference.point.getLonDeg());
                        final Mapcode expectedInternational = results.get(results.size() - 1);
                        if (!resultInternational.equals(expectedInternational)) {
                            LOG.error("checkFile: encodeToInternational fails, expected={}, got={}, reference={}",
                                    expectedInternational, resultInternational, reference);
                            errors.incrementAndGet();
                        }

                        // Check the size of the results.
                        if (reference.mapcodes.size() != results.size()) {

                            // Create a list of mapcodes with a specific precision.
                            final ArrayList<MapcodeRec> resultsConverted = new ArrayList<MapcodeRec>(results.size());
                            for (final Mapcode mapcode : results) {
                                resultsConverted.add(
                                        new MapcodeRec(mapcode.getCode(precision), mapcode.getTerritory()));
                            }
                            LOG.error("checkFile: Incorrect number of results:" +
                                            "\n  lat/lon  = {}" +
                                            "\n  expected = #{}: {} results," +
                                            "\n  actual   = #{}: {} results\n",
                                    reference.point,
                                    reference.mapcodes.size(),
                                    GSON.toJson(reference.mapcodes),
                                    results.size(),
                                    GSON.toJson(resultsConverted));
                            errors.incrementAndGet();
                        }

                        // For every mapcode in the result set, check if it is contained in the reference set.
                        for (final Mapcode result : results) {
                            boolean found = false;
                            for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {

                                // Check if the territory corresponds.
                                if (referenceMapcodeRec.territory == result.getTerritory()) {

                                    // Check if the mapcode corresponds; use only the specified precision.
                                    final int indexOfDash = referenceMapcodeRec.mapcode.lastIndexOf('-');
                                    final int endOfMapcode = indexOfDash + ((precision > 0) ? (precision + 1) : 0);
                                    if (referenceMapcodeRec.mapcode.length() < endOfMapcode) {
                                        LOG.error("checkFile: Reference mapcode not generated at highest precision: {}",
                                                referenceMapcodeRec.mapcode);
                                        errors.incrementAndGet();
                                    }
                                    final String referenceMapcode =
                                            referenceMapcodeRec.mapcode.substring(0, endOfMapcode);
                                    final String generatedMapcode = result.getCode(precision);
                                    if (referenceMapcode.equals(generatedMapcode)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {

                                // This does not fail the test, but rather produces an ERROR in the log file.
                                // It indicates a discrepancy in the C and Java implementations.
                                LOG.error(
                                        "checkFile: Created '{}' at {} which is not present in the reference file!\n" +
                                                "reference={}\n" + "created={}",
                                        result.getCode(precision), reference.point, GSON.toJson(reference),
                                        GSON.toJson(result));
                                errors.incrementAndGet();
                            }
                        }

                        // For every Mapcode in the reference set, check if it is contained in the result set.
                        for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                            final int indexOfDash = referenceMapcodeRec.mapcode.lastIndexOf('-');
                            final int endOfMapcode = indexOfDash + ((precision > 0) ? (precision + 1) : 0);
                            assert referenceMapcodeRec.mapcode.length() >= endOfMapcode;
                            final String referenceMapcode = referenceMapcodeRec.mapcode.substring(0, endOfMapcode);

                            boolean found = false;
                            for (final Mapcode result : results) {

                                // Check if the territory corresponds.
                                if (referenceMapcodeRec.territory == result.getTerritory()) {

                                    // Check if the mapcode corresponds; use only the specified precision.
                                    if (referenceMapcode.equals(result.getCode(precision))) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                LOG.error(
                                        "checkFile: Found   '{} {}' at {} in reference file, not produced by new decoder!\n" +
                                                "reference={}",
                                        referenceMapcodeRec.territory, referenceMapcodeRec.mapcode, reference.point,
                                        GSON.toJson(reference));
                                errors.incrementAndGet();
                            }
                        }

                        try {
                            // Check distance of decoded point to reference point for every possible precision.
                            for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                                final int indexOfDash = referenceMapcodeRec.mapcode.lastIndexOf('-');

                                for (int precision = (checkAllDistances ? 0 : PRECISION_MAX); precision < PRECISION_MAX; ++precision) {
                                    final int endOfMapcode = indexOfDash + ((precision > 0) ? (precision + 1) : 0);
                                    assert referenceMapcodeRec.mapcode.length() >= endOfMapcode;
                                    final String cutOffReferenceMapcode =
                                            referenceMapcodeRec.mapcode.substring(0, endOfMapcode);

                                    // Decode cut-off mapcode to lat/lon.
                                    final Point result =
                                            MapcodeCodec.decode(cutOffReferenceMapcode, referenceMapcodeRec.territory);

                                    // Get distance to reference point.
                                    final double distanceM = Point.distanceInMeters(reference.point, result);

                                    // Keep distance. This is a multi-threaded get/set; requires synchronized.
                                    synchronized (deltaNm) {
                                        //noinspection NumericCastThatLosesPrecision
                                        deltaNm[precision].set(Math.max(deltaNm[precision].get(),
                                                (long) (distanceM * 1.0e6)));
                                    }

                                    // Check if the distance is no greater than the safe maximum specified.
                                    final double maxDeltaM = Mapcode.getSafeMaxOffsetInMeters(precision);
                                    if (distanceM > maxDeltaM) {
                                        LOG.error(
                                                "checkFile: Precision {}: mapcode {} {} was generated for point {}, but decodes to point {} " +
                                                        "which is {} meters from the original point (max is {} meters).",
                                                precision,
                                                referenceMapcodeRec.territory, referenceMapcodeRec.mapcode, reference.point,
                                                result, distanceM, maxDeltaM);
                                        errors.incrementAndGet();
                                    }
                                }
                            }
                        } catch (final UnknownMapcodeException e) {
                            LOG.error("Mapcode was generated for point {}, but cannot be decoded, msg={}",
                                    reference.point, e.getMessage());
                            errors.incrementAndGet();
                        }
                    }
                });
            }
        } catch (final EOFException e) {
            // OK.
        } finally {
            chunkedFile.close();
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        assertEquals(0, errors.get());
        assertEquals("Found errors", 0, errors.get());
        LOG.info("checkFile: Total tasks executed: {}", tasks);
        LOG.info("checkFile: Maximum deltas for this testset:");
        for (int p = (checkAllDistances ? 0 : PRECISION_MAX); p < PRECISION_MAX; ++p) {
            final double maxDeltaFoundMeters = ((double) deltaNm[p].get()) / 1.0e6;
            final double safeMaxOffsetInMeters = Mapcode.getSafeMaxOffsetInMeters(p);
            assertTrue("Max delta is expected to be > 0", maxDeltaFoundMeters > 0.0);
            LOG.info("checkFile: Precision {}: max found is {} meters (absolute max is {} meters, delta is {} meters}",
                    p, maxDeltaFoundMeters, safeMaxOffsetInMeters, safeMaxOffsetInMeters - maxDeltaFoundMeters);
        }
    }

    private static class MapcodeRec {
        @Nonnull
        private final String mapcode;
        @Nonnull
        private final Territory territory;

        MapcodeRec(@Nonnull final String mapcode, @Nonnull final Territory territory) {
            this.mapcode = mapcode;
            this.territory = territory;
        }
    }

    private static class ReferenceRec {
        @Nonnull
        private final Point point;
        @Nonnull
        private final ArrayList<MapcodeRec> mapcodes;

        ReferenceRec(@Nonnull final Point point, @Nonnull final ArrayList<MapcodeRec> mapcodes) {
            this.point = point;
            this.mapcodes = mapcodes;
        }
    }

    @Nonnull
    private static ReferenceRec getNextReferenceRecord(@Nonnull final ChunkedFile chunkedFile)
            throws IOException, UnknownTerritoryException {

        // Read first line of data file: <nr> <lat> <lon> <x> <y> <z>
        final String firstLine = chunkedFile.readNonEmptyLine();
        final String[] args = PATTERN_SPACE.split(firstLine);
        assertTrue("Expecting 3 or 6 elements, not " + args.length + " in line: " + firstLine,
                (args.length == 3) || (args.length == 6));

        final int count = Integer.parseInt(args[0]);
        assertTrue("Expecting between 1 and 22 mapcodes", (1 <= count) && (count <= 22));

        // Read lat/lon.
        final double latDeg = Double.parseDouble(args[1]);
        final double lonDeg = Double.parseDouble(args[2]);
        final Point point = Point.fromDeg(latDeg, lonDeg);
        assertTrue("Latitude must be in [-90, 90]", (-90 <= point.getLatDeg()) && (point.getLatDeg() <= 90));
        assertTrue("Longitude must be in [-180, 180]", (-180 <= point.getLonDeg()) && (point.getLonDeg() <= 180));

        // Read mapcodes: <territory> <mapcode>
        final ArrayList<MapcodeRec> mapcodeRecs = new ArrayList<MapcodeRec>();
        for (int i = 0; i < count; ++i) {
            final String line = chunkedFile.readNonEmptyLine();
            Assert.assertFalse("Line should not be empty", line.isEmpty());

            final String[] mapcodeLine = PATTERN_SPACE.split(line);
            assertTrue("Expecting 1 or 2 elements, territory and mapcode, got: " + mapcodeLine.length + ", " + line,
                    mapcodeLine.length <= 2);

            @Nonnull final Territory territory;
            @Nonnull final String mapcode;
            if (mapcodeLine.length == 1) {
                territory = Territory.AAA;
                mapcode = mapcodeLine[0];
            } else {
                territory = Territory.fromString(mapcodeLine[0]);
                mapcode = mapcodeLine[1];
            }
            final MapcodeRec mapcodeRec = new MapcodeRec(mapcode, territory);
            mapcodeRecs.add(mapcodeRec);
        }
        assertEquals("Wrong number of mapcodes found", count, mapcodeRecs.size());
        final ReferenceRec referenceRec = new ReferenceRec(point, mapcodeRecs);
        return referenceRec;
    }

    /**
     * Utility class to read chunked files. Chunked files have extension appended to them
     * like '.a', '.b', etc. This class provides reading lines from such files and moving
     * to next chunks when needed.
     */
    private static final class ChunkedFile {
        final private String baseFileName;
        private String fileName;
        private char fileExt;
        private InputStream inputStream;
        private BufferedReader bufferedReader;

        private ChunkedFile(final String baseFileName) throws IOException {
            super();
            this.baseFileName = baseFileName;
            this.fileExt = 'a';
            this.fileName = baseFileName + '.' + fileExt;
            this.inputStream = getClass().getResourceAsStream(fileName);
            if (inputStream != null) {
                LOG.info("ChunkedFile: Reading {}...", fileName);
                this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            } else {
                throw new IOException("getResourceAsStream() returned null");
            }
        }

        @Nonnull
        private String readNonEmptyLine() throws IOException {
            String line = null;
            do {
                boolean tryNextChunk;
                try {
                    line = bufferedReader.readLine();
                    tryNextChunk = !bufferedReader.ready() || (line == null);
                } catch (final EOFException ignored) {
                    tryNextChunk = true;
                }
                if (line == null) {
                    line = "";
                }

                if (tryNextChunk) {

                    // Move to next file.
                    nextChunk();
                }
            }
            while (line.isEmpty());
            return line;
        }

        private void nextChunk() throws EOFException {
            close();
            ++fileExt;
            fileName = baseFileName + '.' + fileExt;
            inputStream = getClass().getResourceAsStream(fileName);
            if (inputStream != null) {
                LOG.info("nextChunk: Reading {}...", fileName);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                LOG.debug("nextChunk: End of chunked file found (chunk {} not found)", fileName);
                throw new EOFException("getResourceAsStream() returned null");
            }
        }

        private void close() {
            if (inputStream != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException ignored) {
                    LOG.error("close: Cannot close BufferedReader: {}", fileName);
                }
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    LOG.error("close: Cannot close InputStream: {}", fileName);
                }
            }
        }
    }
}
