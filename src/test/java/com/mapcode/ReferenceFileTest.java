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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"ProhibitedExceptionDeclared", "OverlyBroadThrowsClause"})
public class ReferenceFileTest {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceFileTest.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private static final String RANDOM_REFERENCE_FILE_1 = "/random_1k.txt";
    private static final String RANDOM_REFERENCE_FILE_2 = "/random_10k.txt";
    private static final String RANDOM_REFERENCE_FILE_3 = "/random_100k.txt";

    private static final String RANDOM_REFERENCE_FILE_1_HP = "/random_hp_1k.txt";
    private static final String RANDOM_REFERENCE_FILE_2_HP = "/random_hp_10k.txt";
    private static final String RANDOM_REFERENCE_FILE_3_HP = "/random_hp_100k.txt";

    private static final String GRID_REFERENCE_FILE_1 = "/grid_1k.txt";
    private static final String GRID_REFERENCE_FILE_2 = "/grid_10k.txt";
    private static final String GRID_REFERENCE_FILE_3 = "/grid_100k.txt";

    private static final String GRID_REFERENCE_FILE_1_HP = "/grid_hp_1k.txt";
    private static final String GRID_REFERENCE_FILE_2_HP = "/grid_hp_10k.txt";
    private static final String GRID_REFERENCE_FILE_3_HP = "/grid_hp_100k.txt";

    private static final String BOUNDARIES_REFERENCE_FILE = "/boundaries.txt";
    private static final String BOUNDARIES_REFERENCE_FILE_HP = "/boundaries_hp.txt";

    private static final int LOG_LINE_EVERY = 25000;

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkRandomReferenceRecords() throws Exception {
        LOG.info("checkRandomReferenceRecords");
        checkFile(RANDOM_REFERENCE_FILE_1);
        checkFile(RANDOM_REFERENCE_FILE_2);
        checkFile(RANDOM_REFERENCE_FILE_3);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkGridReferenceRecords() throws Exception {
        LOG.info("checkGridReferenceRecords");
        checkFile(GRID_REFERENCE_FILE_1);
        checkFile(GRID_REFERENCE_FILE_2);
        checkFile(GRID_REFERENCE_FILE_3);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkBoundariesReferenceRecords() throws Exception {
        LOG.info("checkBoundariesReferenceRecords");
        checkFile(BOUNDARIES_REFERENCE_FILE);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkRandomReferenceRecordsPrecision2() throws Exception {
        LOG.info("checkRandomReferenceRecordsPrecision2");
        checkFile(RANDOM_REFERENCE_FILE_1_HP);
        checkFile(RANDOM_REFERENCE_FILE_2_HP);
        checkFile(RANDOM_REFERENCE_FILE_3_HP);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkGridReferenceRecordsPrecision2() throws Exception {
        LOG.info("checkGridReferenceRecordsPrecision2");
        checkFile(GRID_REFERENCE_FILE_1_HP);
        checkFile(GRID_REFERENCE_FILE_2_HP);
        checkFile(GRID_REFERENCE_FILE_3_HP);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkBoundariesReferenceRecordsPrecision2() throws Exception {
        LOG.info("checkBoundariesReferenceRecordsPrecision2");
        checkFile(BOUNDARIES_REFERENCE_FILE_HP);
    }

    @SuppressWarnings("BusyWait")
    private static void checkFile(@Nonnull final String baseFileName) throws Exception {

        // Reset error count.
        final AtomicLong deltaNm = new AtomicLong(0);
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicInteger tasks = new AtomicInteger(0);

        final int threads = Runtime.getRuntime().availableProcessors();
        LOG.info("checkFile: Starting {} threads...", threads);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Open data file.
        final ChunkedFile chunkedFile = new ChunkedFile(baseFileName);
        try {

            //noinspection InfiniteLoopStatement
            while (true) {

                // Get next record.
                @Nonnull final ReferenceRec reference = getNextReferenceRecord(chunkedFile);

                final int count = tasks.getAndIncrement();
                if (((count % LOG_LINE_EVERY) == 0)) {
                    LOG.debug("checkFile: #{}, file={}", count, chunkedFile.fileName);
                    LOG.debug("checkFile: lat/lon  = {}", reference.point);
                    LOG.debug("checkFile: expected = #{}: {}", reference.mapcodes.size(), GSON.toJson(reference.mapcodes));
                }

                // Add task. This may throw an exception if the queue is full. Retry in that case.
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        // Encode lat/lon to series of mapcodes and check the resulting mapcodes.
                        final List<Mapcode> results = MapcodeCodec.encode(
                                reference.point.getLatDeg(), reference.point.getLonDeg());

                        // Check the number of mapcodes.
                        if (results.isEmpty()) {
                            LOG.error("checkFile: encode fails, no results found for reference={}", reference);
                            errors.incrementAndGet();
                        }

                        // Check encodeToInternational.
                        final Mapcode resultInternational = MapcodeCodec.encodeToInternational(
                                reference.point.getLatDeg(), reference.point.getLonDeg());
                        final Mapcode expectedInternational = results.get(results.size() - 1);
                        if (!resultInternational.equals(expectedInternational)) {
                            LOG.error("checkFile: encodeToInternational fails, expected={}, got={} for reference",
                                    expectedInternational, resultInternational, reference);
                            errors.incrementAndGet();
                        }

                        // Check the size of the results.
                        if (reference.mapcodes.size() != results.size()) {
                            final ArrayList<MapcodeRec> resultsConverted = new ArrayList<MapcodeRec>(results.size());
                            for (final Mapcode mapcode : results) {
                                resultsConverted.add(new MapcodeRec(mapcode.getCode(2), mapcode.getTerritory()));
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
                        int precision = 0;
                        for (final Mapcode result : results) {
                            boolean found = false;
                            for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                                precision = (referenceMapcodeRec.mapcode.lastIndexOf('-') > 4) ? 2 : 0;

                                if (referenceMapcodeRec.territory.equals(result.getTerritory())) {
                                    if (referenceMapcodeRec.mapcode.equals(result.getCode(precision))) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {

                                // This does not fail the test, but rather produces an ERROR in the log file.
                                // It indicates a discrepancy in the C and Java implementations.
                                LOG.error("checkFile: Created '{}' at {} which is not present in the reference file!\n" +
                                                "ref={}\n" + "new={}",
                                        result.getCode(precision), reference.point, GSON.toJson(reference), GSON.toJson(result));
                                errors.incrementAndGet();
                            }
                        }

                        // For every Mapcode in the reference set, check if it is contained in the result set.
                        for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                            precision = (referenceMapcodeRec.mapcode.lastIndexOf('-') > 4) ? 2 : 0;
                            boolean found = false;
                            for (final Mapcode result : results) {
                                if (referenceMapcodeRec.territory.equals(result.getTerritory())) {
                                    if (referenceMapcodeRec.mapcode.equals(result.getCode(precision))) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                LOG.error("checkFile: Found   '{} {}' at {} in reference file, not produced by new decoder!\n" +
                                                "ref={}",
                                        referenceMapcodeRec.territory, referenceMapcodeRec.mapcode, reference.point,
                                        GSON.toJson(reference));
                                errors.incrementAndGet();
                            }
                        }

                        // Check distance of decoded point to reference point.
                        for (final MapcodeRec mapcodeRec : reference.mapcodes) {
                            //noinspection NestedTryStatement
                            try {
                                final Point result = MapcodeCodec.decode(mapcodeRec.mapcode, mapcodeRec.territory);
                                final long distanceNm = (long) (Point.distanceInMeters(reference.point, result) * 1000000.0);
                                synchronized (deltaNm) {
                                    deltaNm.set(Math.max(deltaNm.get(), distanceNm));
                                }

                                final long maxDeltaNm = (long) (((mapcodeRec.mapcode.lastIndexOf('-') > 4) ?
                                        Mapcode.getSafeMaxOffsetInMeters(2) : Mapcode.getSafeMaxOffsetInMeters(0)) * 1000000.0);
                                if (distanceNm > maxDeltaNm) {
                                    LOG.error("Mapcode {} {} was generated for point {}, but decodes to point {} " +
                                                    "which is {} meters from the original point (max is {} meters).",
                                            mapcodeRec.territory, mapcodeRec.mapcode, reference.point, result,
                                            ((double) distanceNm) / 1000000.0, ((double) maxDeltaNm) / 1000000.0);
                                    errors.incrementAndGet();
                                }
                            } catch (final UnknownMapcodeException unknownMapcodeException) {
                                LOG.error("Mapcode {} {} was generated for point {}, but cannot be decoded.",
                                        mapcodeRec.territory, mapcodeRec.mapcode, reference.point);
                                errors.incrementAndGet();
                            }
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
        LOG.debug("checkFile: Maximum delta for this testset = {}m, executed {} tasks", ((double) deltaNm.get()) / 1000000.0, tasks);
    }

    private static class MapcodeRec {
        @Nonnull
        private final String mapcode;
        @Nonnull
        private final Territory territory;

        public MapcodeRec(@Nonnull final String mapcode, @Nonnull final Territory territory) {
            this.mapcode = mapcode;
            this.territory = territory;
        }
    }

    private static class ReferenceRec {
        @Nonnull
        private final Point point;
        @Nonnull
        private final ArrayList<MapcodeRec> mapcodes;

        public ReferenceRec(@Nonnull final Point point, @Nonnull final ArrayList<MapcodeRec> mapcodes) {
            this.point = point;
            this.mapcodes = mapcodes;
        }
    }

    @Nonnull
    private static ReferenceRec getNextReferenceRecord(@Nonnull final ChunkedFile chunkedFile)
            throws IOException, UnknownTerritoryException {

        // Read first line of data file: <nr> <lat> <lon> <x> <y> <z>
        final String firstLine = chunkedFile.readNonEmptyLine();
        final String[] args = firstLine.split(" ");
        assertTrue("Expecting 3 or 6 elements, not " + args.length + " in line: " + firstLine,
                (args.length == 3) || (args.length == 6));

        final int count = Integer.parseInt(args[0]);
        assertTrue("Expecting between 1 and 21 mapcodes", (1 <= count) && (count <= 21));

        // Read lat/lon.
        final double latDeg = Double.parseDouble(args[1]);
        final double lonDeg = Double.parseDouble(args[2]);
        final Point point = Point.fromMicroDeg(Point.degToMicroDeg(latDeg), Point.degToMicroDeg(lonDeg));
        assertTrue("Latitude must be in [-90, 90]", (-90 <= point.getLatDeg()) && (point.getLatDeg() <= 90));
        assertTrue("Longitude must be in [-180, 180]", (-180 <= point.getLonDeg()) && (point.getLonDeg() <= 180));

        // Read mapcodes: <territory> <mapcode>
        final ArrayList<MapcodeRec> mapcodeRecs = new ArrayList<MapcodeRec>();
        for (int i = 0; i < count; ++i) {
            final String line = chunkedFile.readNonEmptyLine();
            assertTrue("Line should not be empty", !line.isEmpty());

            final String[] mapcodeLine = line.split(" ");
            assertEquals("Expecting 2 elements, territory and mapcode, got: " + mapcodeLine.length, 2, mapcodeLine.length);

            @Nonnull final Territory territory = Territory.fromString(mapcodeLine[0]);
            @Nonnull final String mapcode = mapcodeLine[1];
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
    private static class ChunkedFile {
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
                throw new IOException();
            }
        }

        @SuppressWarnings("OverlyBroadThrowsClause")
        @Nonnull
        private String readNonEmptyLine() throws IOException {
            //noinspection NonConstantStringShouldBeStringBuffer
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
                throw new EOFException();
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
