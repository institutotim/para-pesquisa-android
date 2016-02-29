package br.org.institutotim.parapesquisa;

import javax.inject.Singleton;

import br.org.institutotim.parapesquisa.data.DataModule;
import br.org.institutotim.parapesquisa.service.AgentUpdateService;
import br.org.institutotim.parapesquisa.service.ModeratorUpdateService;
import br.org.institutotim.parapesquisa.ui.UiModule;
import br.org.institutotim.parapesquisa.ui.activity.AgentFormActivity;
import br.org.institutotim.parapesquisa.ui.activity.AgentFormActivityOld;
import br.org.institutotim.parapesquisa.ui.activity.AgentMainActivity;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivity;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionCorrectionActivityOld;
import br.org.institutotim.parapesquisa.ui.activity.AgentSubmissionsActivity;
import br.org.institutotim.parapesquisa.ui.activity.ExtraDataActivity;
import br.org.institutotim.parapesquisa.ui.activity.FormReportActivity;
import br.org.institutotim.parapesquisa.ui.activity.GlobalReportActivity;
import br.org.institutotim.parapesquisa.ui.activity.HelpActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorMainActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivity;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionApprovalActivityOld;
import br.org.institutotim.parapesquisa.ui.activity.ModeratorSubmissionsActivity;
import br.org.institutotim.parapesquisa.ui.activity.SignInActivity;
import br.org.institutotim.parapesquisa.ui.activity.SplashActivity;
import br.org.institutotim.parapesquisa.ui.activity.SubmissionViewActivity;
import br.org.institutotim.parapesquisa.ui.fragment.AboutFragment;
import br.org.institutotim.parapesquisa.ui.fragment.AgentFormsFragment;
import br.org.institutotim.parapesquisa.ui.fragment.HelpFragment;
import br.org.institutotim.parapesquisa.ui.fragment.ModeratorFormsFragment;
import br.org.institutotim.parapesquisa.ui.fragment.SubmissionsFragment;
import br.org.institutotim.parapesquisa.ui.viewholder.PrivateViewHolder;
import dagger.Component;

@Singleton
@Component(modules = {DataModule.class, UiModule.class, ParaPesquisaModule.class})
public interface ParaPesquisaComponent {

    void inject(ParaPesquisaApp app);

    void inject(SplashActivity activity);
    void inject(SignInActivity activity);
    void inject(SubmissionViewActivity activity);
    void inject(HelpActivity activity);
    void inject(ExtraDataActivity activity);
    void inject(FormReportActivity activity);
    void inject(GlobalReportActivity activity);

    void inject(ModeratorMainActivity activity);
    void inject(ModeratorSubmissionsActivity activity);
    void inject(ModeratorSubmissionApprovalActivity activity);
    void inject(ModeratorSubmissionApprovalActivityOld activity);

    void inject(AgentMainActivity activity);
    void inject(AgentSubmissionsActivity activity);
    void inject(AgentFormActivity activity);
    void inject(AgentFormActivityOld activity);
    void inject(AgentSubmissionCorrectionActivity activity);
    void inject(AgentSubmissionCorrectionActivityOld activity);

    void inject(SubmissionsFragment fragment);
    void inject(HelpFragment fragment);
    void inject(AboutFragment fragment);

    void inject(AgentFormsFragment fragment);
    void inject(ModeratorFormsFragment fragment);

    void inject(AgentUpdateService service);
    void inject(ModeratorUpdateService service);

    void inject(PrivateViewHolder holder);
}
