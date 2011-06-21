package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper;

import static org.junit.Assert.*;

import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;

import org.junit.Before;
import org.junit.Test;


public class CollectedValidationResultsImplTest {

    private CollectedValidationResultsImpl results;

    @Before
    public void setUp() throws Exception {
        results = new CollectedValidationResultsImpl();
    }

    @Test
    public void should_add_filtered_out_expressions_separately() throws Exception {
        ExpressionRejectedByFilterResult rejectionResult = new ExpressionRejectedByFilterResult();
        results.add(rejectionResult);
        assertFalse(results.failures().iterator().hasNext());
        assertFalse(results.goodResults().iterator().hasNext());
        assertSame(results.excluded().iterator().next(), rejectionResult);
    }

}
