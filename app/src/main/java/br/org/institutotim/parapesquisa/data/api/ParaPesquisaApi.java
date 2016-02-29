package br.org.institutotim.parapesquisa.data.api;

import br.org.institutotim.parapesquisa.data.api.request.Moderation;
import br.org.institutotim.parapesquisa.data.api.request.RescheduleRequest;
import br.org.institutotim.parapesquisa.data.api.request.SignInRequest;
import br.org.institutotim.parapesquisa.data.api.request.SubmissionUpdate;
import br.org.institutotim.parapesquisa.data.api.response.PaginatedResponse;
import br.org.institutotim.parapesquisa.data.api.response.SignInData;
import br.org.institutotim.parapesquisa.data.api.response.SingleResponse;
import br.org.institutotim.parapesquisa.data.model.AboutText;
import br.org.institutotim.parapesquisa.data.model.Attribution;
import br.org.institutotim.parapesquisa.data.model.AttributionTransfer;
import br.org.institutotim.parapesquisa.data.model.Submission;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.data.model.UserForm;
import br.org.institutotim.parapesquisa.data.model.UserSubmission;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface ParaPesquisaApi {

    String USER_ID = "userId";

    @POST("/" + ApiModule.API_VERSION + "/session")
    Observable<SingleResponse<SignInData>> signIn(@Body SignInRequest request);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}")
    Observable<SingleResponse<UserData>> getUser(@Path(USER_ID) long userId);

    @DELETE("/" + ApiModule.API_VERSION + "/session")
    Observable<Response> signOut();

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}/forms")
    PaginatedResponse<UserForm> getUserForms(@Path(USER_ID) long userId);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}/forms")
    PaginatedResponse<UserForm> getUserForms(@Path(USER_ID) long userId, @Query("page") int page);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}/users")
    PaginatedResponse<Attribution> getAttributions(@Path(USER_ID) long userId);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}/submissions")
    PaginatedResponse<UserSubmission> getSubmissions(@Path(USER_ID) long userId, @Query("form_id") long formId);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}/submissions")
    PaginatedResponse<UserSubmission> getSubmissions(@Path(USER_ID) long userId, @Query("form_id") long formId, @Query("page") int page);

    @GET("/" + ApiModule.API_VERSION + "/forms/{formId}/users/{userId}/submissions")
    PaginatedResponse<UserSubmission> getUserSubmissions(@Path("formId") long formId, @Path("userId") long userId);

    @GET("/" + ApiModule.API_VERSION + "/forms/{formId}/users/{userId}/submissions")
    PaginatedResponse<UserSubmission> getUserSubmissions(@Path("formId") long formId, @Path("userId") long userId, @Query("page") int page);

    @POST("/" + ApiModule.API_VERSION + "/forms/{formId}/submissions")
    SingleResponse<Submission> sendSubmission(@Path("formId") long formId, @Body Submission submission);

    @POST("/" + ApiModule.API_VERSION + "/forms/{formId}/submissions/{submissionId}/reschedule")
    Response reschedule(@Path("formId") long formId, @Path("submissionId") Long submissionId, @Body RescheduleRequest request);

    @PUT("/" + ApiModule.API_VERSION + "/forms/{formId}/submissions/{submissionId}")
    Response updateSubmission(@Path("formId") long formId, @Path("submissionId") Long submissionId, @Body SubmissionUpdate update);

    @GET("/" + ApiModule.API_VERSION + "/users/{userId}")
    SingleResponse<UserData> getUserData(@Path(USER_ID) long userId);

    @POST("/" + ApiModule.API_VERSION + "/assignments/transfer")
    Response transferAssignment(@Body AttributionTransfer transfer);

    @POST("/" + ApiModule.API_VERSION + "/forms/{formId}/submissions/{submissionId}/moderate")
    Response moderate(@Path("formId") long formId, @Path("submissionId") Long submissionId, @Body Moderation moderation);

    @GET("/" + ApiModule.API_VERSION + "/texts")
    PaginatedResponse<AboutText> getAboutText();
}
