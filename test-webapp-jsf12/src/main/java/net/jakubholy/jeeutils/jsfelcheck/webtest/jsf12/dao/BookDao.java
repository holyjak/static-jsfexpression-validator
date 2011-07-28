package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

import java.util.Collection;
import java.util.SortedSet;

import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.Book;

public interface BookDao {

    Collection<Book> getAllBooks();

    Collection<Book> findTopBestRatedBooks();

    Collection<Book> findBooksByAuthor(String author);

    SortedSet<AuthorStats> findAuthors();

    void markSoldOut(Collection<Book> books);

}