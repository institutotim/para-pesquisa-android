package br.org.institutotim.parapesquisa.ui;

import android.app.Application;

import javax.inject.Singleton;

import br.org.institutotim.parapesquisa.data.db.ParaPesquisaOpenHelper;
import br.org.institutotim.parapesquisa.data.db.ParaPesquisaPreferences;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.ui.adapter.AgentHelpAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.AgentSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.FormAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.ModeratorHelpAdapter;
import br.org.institutotim.parapesquisa.ui.adapter.ModeratorSubmissionAdapter;
import br.org.institutotim.parapesquisa.ui.helper.AnswerHelper;
import br.org.institutotim.parapesquisa.ui.helper.CorrectionHelper;
import br.org.institutotim.parapesquisa.ui.helper.FieldActionHelper;
import br.org.institutotim.parapesquisa.ui.helper.FieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.FormHelper;
import br.org.institutotim.parapesquisa.ui.helper.ModeratorFieldHelper;
import br.org.institutotim.parapesquisa.ui.helper.ModeratorHelper;
import br.org.institutotim.parapesquisa.ui.helper.NotificationHelper;
import br.org.institutotim.parapesquisa.ui.helper.SectionHelper;
import br.org.institutotim.parapesquisa.ui.helper.StatsHelper;
import br.org.institutotim.parapesquisa.ui.helper.SubmissionHelper;
import br.org.institutotim.parapesquisa.ui.helper.SummaryHelper;
import br.org.institutotim.parapesquisa.ui.validator.FieldValidator;
import br.org.institutotim.parapesquisa.ui.validator.SectionValidator;
import dagger.Module;
import dagger.Provides;

@Module
public class UiModule {

    @Provides
    public FormAdapter provideFormAdapter(Application app, ParaPesquisaOpenHelper helper, UserData user) {
        return new FormAdapter(app, helper, user);
    }

    @Provides
    public ModeratorHelpAdapter provideModeratorHelpAdapter(Application app) {
        return new ModeratorHelpAdapter(app);
    }

    @Provides
    public AgentHelpAdapter provideAgentHelpAdapter(Application app) {
        return new AgentHelpAdapter(app);
    }

    @Provides
    @Singleton
    public FieldHelper provideFieldHelper(AnswerHelper answerHelper, ModeratorHelper moderatorHelper,
                                          FieldValidator fieldValidator, ParaPesquisaPreferences preferences) {
        return new FieldHelper(answerHelper, moderatorHelper, preferences, fieldValidator);
    }

    @Provides
    @Singleton
    public ModeratorFieldHelper provideModeratorFieldHelper(ParaPesquisaPreferences preferences, AnswerHelper answerHelper,
                                                            ModeratorHelper moderatorHelper, FieldActionHelper fieldActionHelper) {
        return new ModeratorFieldHelper(preferences, answerHelper, moderatorHelper, fieldActionHelper);
    }

    @Provides
    @Singleton
    public StatsHelper provideStatsHelper(ParaPesquisaOpenHelper helper) {
        return new StatsHelper(helper);
    }

    @Provides
    @Singleton
    public FieldValidator provideFieldValidator() {
        return new FieldValidator();
    }

    @Provides
    @Singleton
    public ModeratorHelper provideModeratorHelper(ParaPesquisaOpenHelper helper, ParaPesquisaPreferences preferences) {
        return new ModeratorHelper(preferences, helper);
    }

    @Provides
    @Singleton
    public SectionValidator provideSectionValidator(FieldValidator fieldValidator) {
        return new SectionValidator(fieldValidator);
    }

    @Provides
    @Singleton
    public SubmissionHelper provideSubmissionHelper(SectionHelper sectionHelper, FieldHelper fieldHelper, CorrectionHelper correctionHelper) {
        return new SubmissionHelper(sectionHelper, fieldHelper, correctionHelper);
    }

    @Provides
    @Singleton
    public CorrectionHelper provideCorrectionHelper() {
        return new CorrectionHelper();
    }

    @Provides
    @Singleton
    public NotificationHelper provideNotificationHelper(ParaPesquisaOpenHelper helper, ParaPesquisaPreferences preferences, SummaryHelper summaryHelper) {
        return new NotificationHelper(helper, preferences, summaryHelper);
    }

    @Provides
    @Singleton
    public FieldActionHelper provideFieldActionHelper() {
        return new FieldActionHelper();
    }

    @Provides
    @Singleton
    public FormHelper provideFormHelper() {
        return new FormHelper();
    }

    @Provides
    @Singleton
    public SummaryHelper provideSummaryHelper(ParaPesquisaOpenHelper helper) {
        return new SummaryHelper(helper);
    }

    @Provides
    @Singleton
    public SectionHelper provideSectionHelper(SectionValidator validator) {
        return new SectionHelper(validator);
    }

    @Provides
    @Singleton
    public AnswerHelper provideAnswerHelper() {
        return new AnswerHelper();
    }

    @Provides
    public AgentSubmissionAdapter provideAgentSubmissionAdapter(Application app, ParaPesquisaOpenHelper helper) {
        return new AgentSubmissionAdapter(app);
    }

    @Provides
    public ModeratorSubmissionAdapter provideModeratorSubmissionAdapter(Application app, ParaPesquisaOpenHelper helper) {
        return new ModeratorSubmissionAdapter(app, helper);
    }
}
