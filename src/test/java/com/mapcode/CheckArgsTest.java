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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class CheckArgsTest {
    private static final Logger LOG = LoggerFactory.getLogger(CheckArgsTest.class);

    public void testCheckNonnullOK() throws Exception {
        LOG.info("testCheckNonnullOK");
        CheckArgs.checkNonnull("test", this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNonnullError() throws Exception {
        LOG.info("testCheckNonnullError");
        CheckArgs.checkNonnull("test", null);
    }

    public void testCheckDefinedOK() throws Exception {
        LOG.info("testCheckDefinedOK");
        CheckArgs.checkDefined("test", Point.fromDeg(0.0, 0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckDefinedError() throws Exception {
        LOG.info("testCheckDefinedError");
        CheckArgs.checkDefined("test", Point.undefined());
    }

    public void testMapcodeCodeOK() throws Exception {
        LOG.info("testCheckMapcodeCodeOK");
        CheckArgs.checkMapcodeCode("test", "XX.XX");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapcodeCodeError() throws Exception {
        LOG.info("testCheckMapcodeCodeError");
        CheckArgs.checkMapcodeCode("test", "XX.XX-");
    }
}
