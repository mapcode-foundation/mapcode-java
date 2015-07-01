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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class EncoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(EncoderTest.class);

    @Test
    public void encodeMostResults() {
        LOG.info("encodeMostResults");

        final double lat = 26.904899;
        final double lon = 95.138515;

        final List<Mapcode> results = MapcodeCodec.encode(lat, lon);
        assertEquals(21, results.size());
        int i = 0;
        assertEquals("MMR 9Z3R.YKP", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-NL WKS.H6", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-NL X57.Q6V", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-NL W7Y.9WBB", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-NL YT0.W584", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-NL XBN8.W4TD", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AR 51.XYW3", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AR W7Y.9WBB", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AR YT0.W584", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AR XBN8.W4TD", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AS SC86.C8", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AS W7Y.9WBB", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AS YT0.W584", results.get(i++).getCodeWithTerritory());
        assertEquals("IN-AS XBN8.W4TD", results.get(i++).getCodeWithTerritory());
        assertEquals("IND W7Y.9WBB", results.get(i++).getCodeWithTerritory());
        assertEquals("IND YT0.W584", results.get(i++).getCodeWithTerritory());
        assertEquals("IND XBN8.W4TD", results.get(i++).getCodeWithTerritory());
        assertEquals("CN-XZ SZW8.2TR", results.get(i++).getCodeWithTerritory());
        assertEquals("CN-XZ KQLF.C2K7", results.get(i++).getCodeWithTerritory());
        assertEquals("CHN KQLF.C2K7", results.get(i++).getCodeWithTerritory());
        assertEquals("AAA PRP60.0RVD", results.get(i++).getCodeWithTerritory());
        assertEquals(i, results.size());
    }

    @Test
    public void encodeWithTerritory() {
        LOG.info("encodeWithTerritory");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final List<Mapcode> results = MapcodeCodec.encode(lat, lon, Territory.NLD);
        assertEquals(4, results.size());
        assertEquals("NLD 49.4V", results.get(0).getCodeWithTerritory());
        assertEquals("NLD G9.VWG", results.get(1).getCodeWithTerritory());
        assertEquals("NLD DL6.H9L", results.get(2).getCodeWithTerritory());
        assertEquals("NLD P25Z.N3Z", results.get(3).getCodeWithTerritory());
    }

    @Test
    public void encodeWithoutTerritory1() {
        LOG.info("encodeWithoutTerritory1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final List<Mapcode> results = MapcodeCodec.encode(lat, lon);
        assertEquals(5, results.size());
        assertEquals("NLD 49.4V", results.get(0).getCodeWithTerritory());
        assertEquals("NLD G9.VWG", results.get(1).getCodeWithTerritory());
        assertEquals("NLD DL6.H9L", results.get(2).getCodeWithTerritory());
        assertEquals("NLD P25Z.N3Z", results.get(3).getCodeWithTerritory());
        assertEquals("AAA VHXGB.1J9J", results.get(4).getCodeWithTerritory());
    }

    @Test
    public void encodeWithoutTerritory2() {
        LOG.info("encodeWithoutTerritory2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final List<Mapcode> results = MapcodeCodec.encode(lat, lon);
        assertEquals(10, results.size());
        assertEquals("IN-RJ XX.XX", results.get(0).getCodeWithTerritory());
        assertEquals("IN-RJ 6X.NHG", results.get(1).getCodeWithTerritory());
        assertEquals("IN-RJ KH9.FGV", results.get(2).getCodeWithTerritory());
        assertEquals("IN-RJ H8M.6FTF", results.get(3).getCodeWithTerritory());
        assertEquals("IN-RJ 8BZ9.D61B", results.get(4).getCodeWithTerritory());
        assertEquals("IN-MP H8M.6FTF", results.get(5).getCodeWithTerritory());
        assertEquals("IN-MP 8BZ9.D61B", results.get(6).getCodeWithTerritory());
        assertEquals("IND H8M.6FTF", results.get(7).getCodeWithTerritory());
        assertEquals("IND 8BZ9.D61B", results.get(8).getCodeWithTerritory());
        assertEquals("AAA PQ0PF.5M1H", results.get(9).getCodeWithTerritory());
    }

    @Test
    public void encodeToInternational1() {
        LOG.info("encodeToInternational1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final Mapcode result = MapcodeCodec.encodeToInternational(lat, lon);
        assertEquals("VHXGB.1J9J", result.getCode());
    }

    @Test
    public void encodeToInternational2() {
        LOG.info("encodeToInternational2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final Mapcode result = MapcodeCodec.encodeToInternational(lat, lon);
        assertEquals("PQ0PF.5M1H", result.getCode());
    }

    @Test
    public void encodeToShortest1() throws Exception {
        LOG.info("encodeToShortest1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final Mapcode result = MapcodeCodec.encodeToShortest(lat, lon, Territory.NLD);
        assertEquals("NLD 49.4V", result.getCodeWithTerritory());
    }

    @Test
    public void encodeToShortest2() throws Exception {
        LOG.info("encodeToShortest2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final Mapcode result = MapcodeCodec.encodeToShortest(lat, lon, Territory.IN_RJ);
        assertEquals("IN-RJ XX.XX", result.getCodeWithTerritory());
    }

    @Test
    public void encodeToShortest3() throws Exception {
        LOG.info("encodeToShortest3");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final Mapcode result = MapcodeCodec.encodeToShortest(lat, lon, Territory.NLD);
        assertEquals("NLD 49.4V", result.getCodeWithTerritory());
    }

    @Test(expected = UnknownMapcodeException.class)
    public void encodeToShortest4() throws Exception {
        LOG.info("encodeToShortest4");

        final double lat = 26.87016;
        final double lon = 75.847;

        MapcodeCodec.encodeToShortest(lat, lon, Territory.NLD);
    }

    @Test
    public void legalArguments() {
        LOG.info("legalArguments");
        MapcodeCodec.encode(-90, 0);
        MapcodeCodec.encode(90, 0);
        MapcodeCodec.encode(0, -180);
        MapcodeCodec.encode(0, 180);
        assertEquals(MapcodeCodec.encode(-91, 0), MapcodeCodec.encode(-90, 0));
        assertEquals(MapcodeCodec.encode(91, 0), MapcodeCodec.encode(90, 0));
        assertEquals(MapcodeCodec.encode(0, -181), MapcodeCodec.encode(0, 179));
        assertEquals(MapcodeCodec.encode(0, 181), MapcodeCodec.encode(0, -179));
    }
}
