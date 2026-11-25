package com.kumuluz.ee.rest.smoke;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Same tests against the packaged application (java -jar target/quarkus-app/quarkus-run.jar, RunnerClassLoader),
 * started without any --add-opens flags.
 */
@QuarkusIntegrationTest
public class ThingResourceIT extends ThingResourceTest {
}
