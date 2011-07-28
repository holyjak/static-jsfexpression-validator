package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.AuthorStats;
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.BookDao;
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao.InMemoryDaoImpl;

public class BookController {

    private final BookDao dao = new InMemoryDaoImpl();
    private Collection<Book> books;
    private List<SelectItem> authors;
    private String authorFilter = "";

    public BookController() {
        findAll();
    }

    public String findAll() {
        System.err.println("findAll called");
        books = dao.getAllBooks();
        return null;
    }

    public String findTopFive() {
        System.err.println("findTopFive called");
        books = dao.findTopBestRatedBooks();
        return null;
    }

    private void findByAuthor(String author) {
        System.err.println("findByAuthor called");
        if ("*".equals(author)) {
            books = dao.getAllBooks();
        } else {
            books = dao.findBooksByAuthor(author);
        }
    }

    public void authorFilterChanged(ValueChangeEvent event) {
        System.err.println("authorFilterChanged called: " + event + ", " + event.getNewValue());
        String author = (String) event.getNewValue(); // Must however be the exact page URL. E.g. "contact.jsf".
        findByAuthor(author);
        FacesContext.getCurrentInstance().renderResponse();
    }

    public String sortByAuthor() {
        List<Book> bookList;
        if (books instanceof List) {
            bookList = (List<Book>) books;
        } else {
            bookList = new LinkedList<Book>(books);
        }
        Collections.sort(bookList, new Comparator<Book>() {
            @Override public int compare(Book b1, Book b2) {
                return b1.getAuthor().compareTo(b2.getAuthor());
            }
        });
        books = bookList;
        return null;
    }

    public String sortByName() {
        List<Book> bookList;
        if (books instanceof List) {
            bookList = (List<Book>) books;
        } else {
            bookList = new LinkedList<Book>(books);
        }
        Collections.sort(bookList, new Comparator<Book>() {
            @Override public int compare(Book b1, Book b2) {
                return b1.getName().compareTo(b2.getName());
            }
        });
        books = bookList;
        return null;
    }

    public String sortByNameAndAvailability() {
        List<Book> bookList;
        if (books instanceof List) {
            bookList = (List<Book>) books;
        } else {
            bookList = new LinkedList<Book>(books);
        }
        Collections.sort(bookList, new Comparator<Book>() {
            @Override public int compare(Book b1, Book b2) {
                int availCmp = Boolean.valueOf(b1.isAvailable()).compareTo(b2.isAvailable());
                if (availCmp != 0) {
                    return availCmp * -1;
                }
                return b1.getName().compareTo(b2.getName());
            }
        });
        books = bookList;
        return null;
    }

    public String sortByNameAndRanking() {
        List<Book> bookList;
        if (books instanceof List) {
            bookList = (List<Book>) books;
        } else {
            bookList = new LinkedList<Book>(books);
        }
        Collections.sort(bookList, new Comparator<Book>() {
            @Override public int compare(Book b1, Book b2) {
                int rateCmp = Integer.valueOf(b1.getRanking()).compareTo(b2.getRanking()) * -1;
                if (rateCmp != 0) {
                    return rateCmp;
                }
                return b1.getName().compareTo(b2.getName());
            }
        });
        books = bookList;
        return null;
    }


    public void setAuthorFilter(String author) {
        this.authorFilter = author;
    }

    public String getAuthorFilter() {
        return authorFilter;
    }

    public void setBooks(Collection<Book> books) {
        this.books = books;
    }

    public Collection<Book> getBooks() {
        return books;
    }

    public List<SelectItem> getAuthors() {
        if (authors == null) {
            authors = new LinkedList<SelectItem>();
            SortedSet<AuthorStats> stats = dao.findAuthors();
            for (AuthorStats author : stats) {
                authors.add(new SelectItem(author.getName(), author.getLabel()));
            }
        }
        return authors;
    }

}
