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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckArgsTest {
    private static final Logger LOG = LoggerFactory.getLogger(CheckArgsTest.class);

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCheckNonnullOK() {
        LOG.info("testCheckNonnullOK");
        CheckArgs.checkNonnull("test", this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNonnullError() {
        LOG.info("testCheckNonnullError");
        CheckArgs.checkNonnull("test", null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCheckDefinedOK() {
        LOG.info("testCheckDefinedOK");
        CheckArgs.checkDefined("test", Point.fromDeg(0.0, 0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckDefinedError() {
        LOG.info("testCheckDefinedError");
        CheckArgs.checkDefined("test", Point.undefined());
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testMapcodeCodeOK() {
        LOG.info("testCheckMapcodeCodeOK");
        CheckArgs.checkMapcodeCode("test", "XX.XX");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapcodeCodeError() {
        LOG.info("testCheckMapcodeCodeError");
        CheckArgs.checkMapcodeCode("test", "XX.XX-");
    }
}
