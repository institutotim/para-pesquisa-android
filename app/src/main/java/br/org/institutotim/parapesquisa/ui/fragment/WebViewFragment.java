package br.org.institutotim.parapesquisa.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import br.org.institutotim.parapesquisa.R;
import br.org.institutotim.parapesquisa.data.model.AboutText;
import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewFragment extends BaseFragment {

    public static final String TEXT_EXTRA = "text_extra";

    @Bind(R.id.web_view)
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, view);

        StringBuilder content = new StringBuilder();
        AboutText text = getArguments().getParcelable(TEXT_EXTRA);
        if (text.getSubtitle() != null && !text.getSubtitle().trim().isEmpty()) {
            content.append("<h3>").append(text.getSubtitle()).append("</h3>");
        }
        content.append(text.getContent());

        webView.loadDataWithBaseURL("", content.toString(), "text/html", "UTF-8", "");

        return view;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }
}
