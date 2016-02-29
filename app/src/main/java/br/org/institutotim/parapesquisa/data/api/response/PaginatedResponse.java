package br.org.institutotim.parapesquisa.data.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaginatedResponse<E> {

    private List<E> response;
    private int count;
    private PaginationJson pagination;

    public List<E> getResponse() {
        return response;
    }

    public void setResponse(List<E> response) {
        this.response = response;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public PaginationJson getPagination() {
        return pagination;
    }

    public void setPagination(PaginationJson pagination) {
        this.pagination = pagination;
    }

    public static class PaginationJson {

        private int pages;
        private int count;
        @JsonProperty("per_page")
        private int perPage;
        private int current;
        private Integer previous;
        private Integer next;

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public Integer getPrevious() {
            return previous;
        }

        public void setPrevious(Integer previous) {
            this.previous = previous;
        }

        public Integer getNext() {
            return next;
        }

        public void setNext(Integer next) {
            this.next = next;
        }
    }
}