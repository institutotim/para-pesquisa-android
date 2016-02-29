package br.org.institutotim.parapesquisa.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils {

    public static List<Long> toLongList(String raw) {
        if (raw == null) return Collections.emptyList();

        String[] values = StringUtils.split(raw, "\\\\");
        List<Long> list = new ArrayList<>();
        for (String value : values) {
            list.add(Long.valueOf(value));
        }
        return list;
    }

    public static List<String> toStringList(String raw) {
        String[] values = StringUtils.split(raw, "\\\\");
        return Arrays.asList(values);
    }
}
