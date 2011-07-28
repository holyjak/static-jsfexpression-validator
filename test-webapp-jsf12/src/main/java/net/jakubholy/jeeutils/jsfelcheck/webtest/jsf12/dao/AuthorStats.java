package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.dao;

public class AuthorStats implements Comparable<AuthorStats> {

    private String name;
    private int frequency;

    public AuthorStats(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }
    public int getFrequency() {
        return frequency;
    }

    public String getLabel() {
        return name + " (" + frequency + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuthorStats other = (AuthorStats) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(AuthorStats o) {
        return name.compareTo(o.name);
    }

}
