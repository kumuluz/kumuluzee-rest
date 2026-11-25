package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryFilterExpression;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.FilterExpressionOperation;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Verifies that the parboiled-based filter parser works without access to {@code sun.misc.Unsafe} memory-access
 * methods (JEP 471/498, which future JDKs remove) and without {@code --add-opens java.base/java.lang}.
 * <p>
 * Parboiled 1.4.1 obtains the {@code Lookup} for defining its generated classes from
 * {@code QueryFilterExpressionParser.lookup()}; if that method were missing it would fall back to {@code Unsafe} and
 * then to reflective {@code ClassLoader.defineClass}, both of which fail in the forked JVM below.
 */
public class ParboiledJvmCompatibilityTest {

    private static final String FILTER = "name:eq:'x' and (age:gt:3 or age:lt:1)";

    @Test
    public void parsesFilterWithoutUnsafeAndWithoutAddOpens() throws Exception {

        // --sun-misc-unsafe-memory-access exists since JDK 23
        Assume.assumeTrue("Requires JDK 23+", Runtime.version().feature() >= 23);

        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.add("--sun-misc-unsafe-memory-access=deny");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(ParseInForkedJvm.class.getName());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Assert.assertTrue("Forked JVM did not finish in time. Output:\n" + output, finished);
        Assert.assertEquals("Forked JVM failed. Output:\n" + output, 0, process.exitValue());
        Assert.assertTrue("Unexpected output:\n" + output, output.contains("PARSED AND 3"));
    }

    @Test
    public void parsesFilterInCurrentJvm() {

        QueryFilterExpression expression = QueryParameters.query("filter=" + FILTER).build().getFilterExpression();

        Assert.assertNotNull(expression);
        Assert.assertEquals(FilterExpressionOperation.AND, expression.operation());
        Assert.assertEquals(3, expression.getAllValues().size());
    }

    /**
     * Entry point executed in the forked JVM.
     */
    public static class ParseInForkedJvm {

        public static void main(String[] args) {
            QueryFilterExpression expression = QueryParameters.query("filter=" + FILTER).build().getFilterExpression();
            System.out.println("PARSED " + expression.operation() + " " + expression.getAllValues().size());
        }
    }
}
