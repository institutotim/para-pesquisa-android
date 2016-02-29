package br.org.institutotim.parapesquisa.data.event;

import java.util.List;

public class FieldActionEvent {

    public static final int DISABLE = 1;
    public static final int ENABLE = 2;
    public static final int DISABLE_SECTION = 3;

    private final List<Long> actions;
    private final boolean select;
    private final int type;

    public FieldActionEvent(List<Long> actions, boolean select, int type) {

        this.actions = actions;
        this.select = select;
        this.type = type;
    }

    public static int getDisable() {
        return DISABLE;
    }

    public static int getEnable() {
        return ENABLE;
    }

    public static int getDisableSection() {
        return DISABLE_SECTION;
    }

    public List<Long> getActions() {
        return actions;
    }

    public boolean isSelect() {
        return select;
    }

    public int getType() {
        return type;
    }
}
