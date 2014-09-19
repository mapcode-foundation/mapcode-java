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

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class DecoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(DecoderTest.class);

    @Test
    public void decodeTomTomOffice1() throws Exception {
        LOG.info("decodeTomTomOffice1");
        final Point point = MapcodeCodec.decode("49.4V", Territory.NLD);
        assertEquals("decodeTomTomOffice latitude", 52376514, point.getLatMicroDeg());
        assertEquals("decodeTomTomOffice longitude", 4908542, point.getLonMicroDeg());
    }

    @Test
    public void decodeTomTomOffice2() throws Exception {
        LOG.info("decodeTomTomOffice2");
        final Point point = MapcodeCodec.decode("NLD 49.4V");
        assertEquals("decodeTomTomOffice latitude", 52376514, point.getLatMicroDeg());
        assertEquals("decodeTomTomOffice longitude", 4908542, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionTomTomOffice1() throws Exception {
        LOG.info("highPrecisionTomTomOffice1");
        final Point point = MapcodeCodec.decode("49.4V-K2", Territory.NLD);
        assertEquals("decodeTomTomOffice hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEquals("decodeTomTomOffice hi-precision longitude", 4908540, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionTomTomOffice2() throws Exception {
        LOG.info("highPrecisionTomTomOffice2");
        final Point point = MapcodeCodec.decode("NLD 49.4V-K2");
        assertEquals("decodeTomTomOffice hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEquals("decodeTomTomOffice hi-precision longitude", 4908540, point.getLonMicroDeg());
    }

	@Test
	public void unicodeMapcodeAthensAcropolis1() throws Exception {
		LOG.info("unicodeMapcodeAthensAcropolis1");
		final Point point = MapcodeCodec.decode("ΗΠ.Θ2", Territory.GRC);
		assertEquals("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
		assertEquals("decodeUnicode longitude", 23726247,
				point.getLonMicroDeg());
	}

	@Test
	public void unicodeMapcodeAthensAcropolis2() throws Exception {
		LOG.info("unicodeMapcodeAthensAcropolis2");
		final Point point = MapcodeCodec.decode("GRC ΗΠ.Θ2");
		assertEquals("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
		assertEquals("decodeUnicode longitude", 23726247,
				point.getLonMicroDeg());
	}

	@Test
	public void unicodeMapcodeTokyoTower1() throws Exception {
		LOG.info("unicodeMapcodeTokyoTower1");
		final Point point = MapcodeCodec.decode("\u30c1\u30ca.8\u30c1", Territory.JPN);
		assertEquals("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
		assertEquals("decodeUnicode longitude", 139745394,
				point.getLonMicroDeg());
	}

	@Test
	public void unicodeMapcodeTokyoTower2() throws Exception {
		LOG.info("unicodeMapcodeTokyoTower2");
		final Point point = MapcodeCodec.decode("JPN \u30c1\u30ca.8\u30c1");
		assertEquals("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
		assertEquals("decodeUnicode longitude", 139745394,
				point.getLonMicroDeg());
	}

	@Test
	public void mapCodeWithZeroGroitzsch() throws Exception {
		LOG.info("mapCodeWithZeroGroitzsch");
		final Point point = MapcodeCodec.decode("HMVM.3Q0", Territory.DEU);
		assertEquals("decodeUnicode latitude", 51154852, point.getLatMicroDeg());
		assertEquals("decodeUnicode longitude", 12278574,
				point.getLonMicroDeg());
	}

	@Test(expected = UnknownMapcodeException.class)
    public void invalidTerritory() throws Exception {
        LOG.info("invalidTerritory");
        MapcodeCodec.decode("NLD 49.4V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidNoDot() throws Exception {
        LOG.info("invalidNoDot");
        MapcodeCodec.decode("494V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidDotLocation1() throws Exception {
        LOG.info("invalidDotLocation1");
        MapcodeCodec.decode("4.94V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidDotLocation2() throws Exception {
        LOG.info("invalidDotLocation2");
        MapcodeCodec.decode("494.V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidDotLocation3() throws Exception {
        LOG.info("invalidDotLocation3");
        MapcodeCodec.decode("494V49.4V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidDotLocation4() throws Exception {
        LOG.info("invalidDotLocation4");
        MapcodeCodec.decode("494.V494V", Territory.NLD);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument1() throws Exception {
        LOG.info("illegalArgument1");
        MapcodeCodec.decode(null, Territory.NLD);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument2() throws Exception {
        LOG.info("illegalArgument2");
        MapcodeCodec.decode("494.V494V", null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument3() throws Exception {
        LOG.info("illegalArgument3");
        MapcodeCodec.decode(null);
    }
}
