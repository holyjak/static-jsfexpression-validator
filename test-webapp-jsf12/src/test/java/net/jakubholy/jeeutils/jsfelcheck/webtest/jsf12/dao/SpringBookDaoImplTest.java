package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

import static org.junit.Assert.*;

import java.util.SortedSet;

import net.jakubholy.testing.dbunit.embeddeddb.EmbeddedDbTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpringBookDaoImplTest {

    private EmbeddedDbTester testDb = new EmbeddedDbTester();
    private SpringBookDaoImpl dao;

    @Before
    public void setUp() throws Exception {
        testDb.onSetup();

        dao = new SpringBookDaoImpl();
        dao.setDataSource(testDb.getDataSource());
    }

    @After
    public void tearDown() throws Exception {
        testDb.onTearDown();
    }

    @Test
    public void testFindAuthors() {

        SortedSet<AuthorStats> authors = dao.findAuthors();

        assertEquals(1, authors.size());
        AuthorStats author1 = authors.iterator().next();

        assertEquals("Iterate (1)", author1.getLabel());
    }

}
