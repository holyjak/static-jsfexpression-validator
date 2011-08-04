package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

public class Book {

    private String isbn;
    private int ranking;
    private String author;
    private String name;
    private boolean available;

    public Book() {}

    public Book(String isbn, String name, String author, int ranking,
            boolean available) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
        this.ranking = ranking;
        this.available = available;
    }
    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public int getRanking() {
        return ranking;
    }
    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

}
