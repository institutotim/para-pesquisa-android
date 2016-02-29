package br.org.institutotim.parapesquisa.data.event;

/**
 * Created by tpinho on 1/15/16.
 */
public class RefreshFieldEvent {

    private int correctionType;

    public RefreshFieldEvent(int correctionType) {
        this.correctionType = correctionType;
    }

    public int getCorrectionType() {
        return correctionType;
    }

}
