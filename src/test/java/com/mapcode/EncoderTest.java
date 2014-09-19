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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class EncoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(EncoderTest.class);

    @Test
    public void encodeWithTerritory() {
        LOG.info("encodeWithTerritory");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final List<MapcodeInfo> results = Mapcode.encode(lat, lon, Territory.NLD);
        assertEquals(4, results.size());
        assertEquals("NLD 49.4V", results.get(0).asInternationalISO());
        assertEquals("NLD G9.VWG", results.get(1).asInternationalISO());
        assertEquals("NLD DL6.H9L", results.get(2).asInternationalISO());
        assertEquals("NLD P25Z.N3Z", results.get(3).asInternationalISO());
    }

    @Test
    public void encodeWithoutTerritory1() {
        LOG.info("encodeWithoutTerritory1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final List<MapcodeInfo> results = Mapcode.encode(lat, lon);
        assertEquals(5, results.size());
        assertEquals("NLD 49.4V", results.get(0).asInternationalISO());
        assertEquals("NLD G9.VWG", results.get(1).asInternationalISO());
        assertEquals("NLD DL6.H9L", results.get(2).asInternationalISO());
        assertEquals("NLD P25Z.N3Z", results.get(3).asInternationalISO());
        assertEquals("AAA VHXGB.1J9J", results.get(4).asInternationalISO());
    }

    @Test
    public void encodeWithoutTerritory2() {
        LOG.info("encodeWithoutTerritory2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final List<MapcodeInfo> results = Mapcode.encode(lat, lon);
        assertEquals(10, results.size());
        assertEquals("IN-RJ XX.XX", results.get(0).asInternationalISO());
        assertEquals("IN-RJ 6X.NHG", results.get(1).asInternationalISO());
        assertEquals("IN-RJ KH9.FGV", results.get(2).asInternationalISO());
        assertEquals("IN-RJ H8M.6FTF", results.get(3).asInternationalISO());
        assertEquals("IN-RJ 8BZ9.D61B", results.get(4).asInternationalISO());
        assertEquals("IN-MP H8M.6FTF", results.get(5).asInternationalISO());
        assertEquals("IN-MP 8BZ9.D61B", results.get(6).asInternationalISO());
        assertEquals("IND H8M.6FTF", results.get(7).asInternationalISO());
        assertEquals("IND 8BZ9.D61B", results.get(8).asInternationalISO());
        assertEquals("AAA PQ0PF.5M1H", results.get(9).asInternationalISO());
    }

    @Test
    public void encodeToInternational1() {
        LOG.info("encodeToInternational1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final MapcodeInfo result = Mapcode.encodeToInternational(lat, lon);
        assertEquals("VHXGB.1J9J", result.getMapcode());
    }

    @Test
    public void encodeToInternational2() {
        LOG.info("encodeToInternational2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final MapcodeInfo result = Mapcode.encodeToInternational(lat, lon);
        assertEquals("PQ0PF.5M1H", result.getMapcode());
    }

    @Test
    public void encodeToShortest1() {
        LOG.info("encodeToShortest1");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final MapcodeInfo result = Mapcode.encodeToShortest(lat, lon);
        assertEquals("NLD 49.4V", result.asInternationalISO());
    }

    @Test
    public void encodeToShortest2() {
        LOG.info("encodeToShortest2");

        final double lat = 26.87016;
        final double lon = 75.847;

        final MapcodeInfo result = Mapcode.encodeToShortest(lat, lon);
        assertEquals("IN-RJ XX.XX", result.asInternationalISO());
    }

    @Test
    public void encodeToShortest3() throws Exception {
        LOG.info("encodeToShortest3");

        final double lat = 52.376514;
        final double lon = 4.908542;

        final MapcodeInfo result = Mapcode.encodeToShortest(lat, lon, Territory.NLD);
        assertEquals("NLD 49.4V", result.asInternationalISO());
    }

    @Test(expected = UnknownMapcodeException.class)
    public void encodeToShortest4() throws Exception {
        LOG.info("encodeToShortest4");

        final double lat = 26.87016;
        final double lon = 75.847;

        Mapcode.encodeToShortest(lat, lon, Territory.NLD);
    }

    public void legalArgument() {
        LOG.info("legalArgument");
        Mapcode.encode(-90, 0);
        Mapcode.encode(90, 0);
        Mapcode.encode(0, -180);
        Mapcode.encode(0, 180);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument1() {
        LOG.info("illegalArgument1");
        Mapcode.encode(-91, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument2() {
        LOG.info("illegalArgument2");
        Mapcode.encode(91, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument3() {
        LOG.info("illegalArgument3");
        Mapcode.encode(0, -181);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument4() {
        LOG.info("illegalArgument4");
        Mapcode.encode(0, 181);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument5() {
        LOG.info("illegalArgument4");
        Mapcode.encode(0, 0, null);
    }
}
