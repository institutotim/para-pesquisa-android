package br.org.institutotim.parapesquisa.data.event;

public class ScrollEvent {

    private final int position;

    public ScrollEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
