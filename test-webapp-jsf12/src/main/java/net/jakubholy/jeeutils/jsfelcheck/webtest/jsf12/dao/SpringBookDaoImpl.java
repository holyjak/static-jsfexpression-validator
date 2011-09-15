package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private RowMapper<Book> bookMapper = new BeanPropertyRowMapper<Book>(Book.class);

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
        jdbcTemplate.setMaxRows(5);
        return jdbcTemplate.query("SELECT * FROM book ORDER BY ranking", bookMapper);
    }

    @Override
    public Collection<Book> findBooksByAuthor(String author) {
        return createJdbcTemplate().query("SELECT * FROM book WHERE author=?"
                , new Object[] {author}, bookMapper);
    }

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
        return new JdbcTemplate(getDataSource());
    }

    @Override
    public SortedSet<AuthorStats> findAuthors() {
        List<AuthorStats> authorStats = createJdbcTemplate().query(
                "SELECT author as name, count(*) as frequency FROM book GROUP BY author"
                    , new BeanPropertyRowMapper<AuthorStats>(AuthorStats.class));
        return new TreeSet<AuthorStats>(authorStats);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
