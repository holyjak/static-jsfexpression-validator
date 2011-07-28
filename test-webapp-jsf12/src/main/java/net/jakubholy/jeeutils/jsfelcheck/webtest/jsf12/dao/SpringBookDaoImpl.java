package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.SortedSet;

import javax.sql.DataSource;

import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.Book;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

/**
 * Access DB using {@link JdbcTemplate} and {@link BeanPropertyRowMapper}.
 */
public class SpringBookDaoImpl implements BookDao {

    private DataSource dataSource;
    private RowMapper<Book> bookMapper = new BeanPropertyRowMapper<Book>();

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.BookDao#getAllBooks()
     */
    @Override
    public Collection<Book> getAllBooks() {
        return createJdbcTemplate().query("SELECT * FROM book", bookMapper);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.BookDao#findTopBestRatedBooks()
     */
    @Override
    public Collection<Book> findTopBestRatedBooks() {
        JdbcTemplate jdbcTemplate = createJdbcTemplate();
        jdbcTemplate.setMaxRows(10);
        return jdbcTemplate.query("SELECT * FROM book ORDER BY ranking", bookMapper);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.BookDao#findBooksByAuthor(java.lang.String)
     */
    @Override
    public Collection<Book> findBooksByAuthor(String author) {
        return createJdbcTemplate().query("SELECT * FROM book WHERE author=?"
                , new Object[] {author}, bookMapper);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.BookDao#markSoldOut(java.util.Collection)
     */
    @Override
    public void markSoldOut(Collection<Book> books) {
        final Book[] booksArray = books.toArray(new Book[0]);

        createJdbcTemplate().batchUpdate(
                "UPDATE book SET available=?"
                , new AbstractInterruptibleBatchPreparedStatementSetter() {

                    @Override
                    protected boolean setValuesIfAvailable(
                            PreparedStatement ps, int i) throws SQLException {
                        if (i >= booksArray.length) return false;
                        ps.setString(0, booksArray[i].getIsbn());
                        return true;
                    }});
    }

    private JdbcTemplate createJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Override
    public SortedSet<AuthorStats> findAuthors() {
        // TODO Auto-generated method stub
        return null;
    }

}
