package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.Book;

public class InMemoryDaoImpl implements BookDao {

    private static final List<Book> BOOKS = Arrays.asList(
            new Book("123", "Star Wars 1", "Lucas, G.", 1, true)
            , new Book("122", "Star Wars 2", "Lucas, G.", 1, true)
            , new Book("124", "Fairy Tales", "Grimms, Bros", 2, true)
            , new Book("200", "Patterns of OOP", "GoF", 0, false)
            , new Book("301", "Warlock", "Sapkowski, A.", 3, true)
            , new Book("405", "Bible", "God", 2, true)
            , new Book("500", "Deserted Island", "Verne, J.", 1, true)
            , new Book("501", "Paris 2010", "Verne, J.", 1, true)
            , new Book("607", "Catch 22", "N/A", 1, true)
            , new Book("608", "Madam Lescaut", "Balzac, H.", 2, true)
            , new Book("609", "Space Odyssey", "Clarke, A. C.", 5, true)
            , new Book("610", "Valhall 4", "Madsen, P.", 4, true)
            );

    @Override
    public Collection<Book> getAllBooks() {
        return books();
    }

    @Override
    public Collection<Book> findTopBestRatedBooks() {
        List<Book> result = books();
        Collections.sort(result, new Comparator<Book>() {

            @Override
            public int compare(Book b1, Book b2) {
                return Integer.valueOf(b1.getRanking()).compareTo(b2.getRanking()) * -1;
            }
        });
        result = result.subList(0, 5);
        return result;
    }

    @Override
    public Collection<Book> findBooksByAuthor(String author) {
        List<Book> result = new LinkedList<Book>();
        for (Book book : BOOKS) {
            if (book.getAuthor().equals(author)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void markSoldOut(Collection<Book> booksToMark) {
        for (Book book : BOOKS) {
            if (booksToMark.contains(book)) {
                book.setAvailable(false);
            }
        }

    }

    private List<Book> books() {
        return new LinkedList<Book>(BOOKS);
    }

    @Override
    public SortedSet<AuthorStats> findAuthors() {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Book book : BOOKS) {
            Integer oldCount = counts.get(book.getAuthor());
            int newCount = (oldCount == null)? 1 : oldCount + 1;
            counts.put(book.getAuthor(), newCount);
        }

        SortedSet<AuthorStats> authors = new TreeSet<AuthorStats>();
        for (Entry<String, Integer> count : counts.entrySet()) {
            authors.add(new AuthorStats(count.getKey(), count.getValue()));
        }
        return authors;
    }

}
