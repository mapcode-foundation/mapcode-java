/*
 * Copyright (C) 2014-2016 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class DataModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(DataModelTest.class);

    public void testFileOK() {
        LOG.info("testFileOK");
        final DataModel dataModel = new DataModel("/com/mapcode/mminfo_ok.dat");
        Assert.assertTrue(dataModel != null);
    }

    @Test(expected = IncorrectDataModelException.class)
    public void testFileTooShort() {
        LOG.info("testFileTooShort");
        final DataModel dataModel = new DataModel("/com/mapcode/mminfo_too_short.dat");
        Assert.assertTrue(false);
    }

    @Test(expected = IncorrectDataModelException.class)
    public void testFileEndsEarly() {
        LOG.info("testFileEndsEarly");
        final DataModel dataModel = new DataModel("/com/mapcode/mminfo_ends_early.dat");
        Assert.assertTrue(false);
    }

    @Test(expected = IncorrectDataModelException.class)
    public void testFileNoHeader() {
        LOG.info("testFileNoHeader");
        final DataModel dataModel = new DataModel("/com/mapcode/mminfo_no_header.dat");
        Assert.assertTrue(false);
    }

    @Test(expected = IncorrectDataModelException.class)
    public void testFileWrongversion() {
        LOG.info("testFileWrongversion");
        final DataModel dataModel = new DataModel("/com/mapcode/mminfo_wrong_version.dat");
        Assert.assertTrue(false);
    }
}
