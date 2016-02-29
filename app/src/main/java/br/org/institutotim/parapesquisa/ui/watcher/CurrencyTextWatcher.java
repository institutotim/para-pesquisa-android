package br.org.institutotim.parapesquisa.ui.watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;

public class CurrencyTextWatcher implements TextWatcher {

    boolean editing;
    EditText textView;

    public CurrencyTextWatcher(EditText textView) {
        editing = false;
        this.textView = textView;
    }

    public synchronized void afterTextChanged(Editable s) {
        if (!editing) {
            editing = true;

            String digits = s.toString().replaceAll("\\D", "");
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            try {
                String formatted = nf.format(Double.parseDouble(digits) / 100);
                textView.setText(formatted);
                textView.setSelection(formatted.length());
            } catch (NumberFormatException nfe) {
                textView.setText("");
            }

            editing = false;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

}
