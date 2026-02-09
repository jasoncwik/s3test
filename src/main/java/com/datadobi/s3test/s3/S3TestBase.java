/*
 *
 *  Copyright 2025 Datadobi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software

 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.datadobi.s3test.s3;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Path;

public class S3TestBase {
    public static ServiceDefinition DEFAULT;
    public static WireLogger WIRE_LOGGER;

    static {
        String testUri = System.getenv("S3TEST_URI");
        if (testUri != null) {
            try {
                DEFAULT = ServiceDefinition.fromURI(testUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String wireLogPath = System.getenv("S3TEST_WIRELOG");
        WIRE_LOGGER = new WireLogger(wireLogPath == null ? null : Path.of(wireLogPath));
    }

    private Description currentTest;

    @Rule
    public TestWatcher testName = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            S3TestBase.this.currentTest = description;
        }
    };

    protected final ServiceDefinition target;
    protected S3Client s3;
    protected S3Bucket bucket;

    public S3TestBase() throws IOException {
        this(DEFAULT != null ? DEFAULT : ServiceDefinition.fromS3Profile("default"));
    }

    public S3TestBase(ServiceDefinition parameter) {
        this.target = parameter;
    }

    @Before
    public final void setUp() throws IOException {
        s3 = S3.createClient(target);

        this.bucket = new S3Bucket(s3, target.bucket());
        if (target.createBucket()) {
            bucket.create();
        }

        WIRE_LOGGER.start(currentTest);
    }

    @After
    public final void tearDown() {
        WIRE_LOGGER.stop();

        S3.clearBucket(s3, target.bucket());
        if (target.createBucket()) {
            bucket.delete();
        }

        s3.close();
    }
}
