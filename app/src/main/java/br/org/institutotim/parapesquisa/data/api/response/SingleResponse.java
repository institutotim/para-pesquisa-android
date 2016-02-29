package br.org.institutotim.parapesquisa.data.api.response;

public class SingleResponse<E> {

    private E response;

    public E getResponse() {
        return response;
    }

    public void setResponse(E response) {
        this.response = response;
    }
}