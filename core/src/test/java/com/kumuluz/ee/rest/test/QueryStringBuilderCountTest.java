package com.kumuluz.ee.rest.test;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.QueryFormatError;
import com.kumuluz.ee.rest.exceptions.QueryFormatException;
import com.kumuluz.ee.rest.utils.QueryStringDefaults;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tilen Faganel
 */
public class QueryStringBuilderCountTest {

    @Test
    public void testEmpty() {
        QueryParameters query = new QueryStringDefaults().builder().query("").build();

        Assert.assertNotNull(query);
        Assert.assertTrue(query.getCount());
    }

    @Test
    public void testCountTrue() {
        QueryParameters query = new QueryStringDefaults().builder().query("count=true").build();

        Assert.assertNotNull(query);
        Assert.assertTrue(query.getCount());
    }

    @Test
    public void testCountFalse() {
        QueryParameters query = new QueryStringDefaults().builder().query("count=false").build();

        Assert.assertNotNull(query);
        Assert.assertFalse(query.getCount());
    }

    @Test
    public void testCountCaseInsensitiveFalse() {
        QueryParameters query = new QueryStringDefaults().builder().query("count=FAlse").build();

        Assert.assertNotNull(query);
        Assert.assertFalse(query.getCount());
    }

    @Test
    public void testCountMultiple() {
        QueryParameters query = new QueryStringDefaults().builder().query("count=true&count=false").build();

        Assert.assertNotNull(query);
        Assert.assertFalse(query.getCount());
    }

    @Test
    public void testCountMalformed() {
        try {

            new QueryStringDefaults().builder().query("count=tru").build();
            Assert.fail("No exception was thrown");
        } catch (QueryFormatException e) {
            Assert.assertEquals("count", e.getField());
            Assert.assertEquals(QueryFormatError.NOT_A_BOOLEAN, e.getReason());
        }
    }
}
