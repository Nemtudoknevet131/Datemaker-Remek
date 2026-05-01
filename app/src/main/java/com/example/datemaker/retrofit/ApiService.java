package com.example.datemaker.retrofit;

import com.example.datemaker.model.AiDateRequest;
import com.example.datemaker.model.AiDateResponse;
import com.example.datemaker.model.AuthResponse;
import com.example.datemaker.model.CategoryDto;
import com.example.datemaker.model.ContactInfoDto;
import com.example.datemaker.model.DateEventDto;
import com.example.datemaker.model.DateEventRequest;
import com.example.datemaker.model.FacebookLoginRequest;
import com.example.datemaker.model.GoogleLoginRequest;
import com.example.datemaker.model.IdeaDto;
import com.example.datemaker.model.Login;
import com.example.datemaker.model.LoveArrowsResultRequest;
import com.example.datemaker.model.LoveArrowsResultResponse;
import com.example.datemaker.model.Message;
import com.example.datemaker.model.PartnerRequestDto;
import com.example.datemaker.model.PartnerResponse;
import com.example.datemaker.model.PartnerSearchResponse;
import com.example.datemaker.model.PasswordResetRequest;
import com.example.datemaker.model.PhoneNumberRequest;
import com.example.datemaker.model.PhoneVerificationRequest;
import com.example.datemaker.model.ProfileDto;
import com.example.datemaker.model.ProgramItem;
import com.example.datemaker.model.ReactionRequest;
import com.example.datemaker.model.RegisterRequest;
import com.example.datemaker.model.ResendCodeRequest;
import com.example.datemaker.model.SendMessageRequest;
import com.example.datemaker.model.SnakeResultRequest;
import com.example.datemaker.model.SnakeResultResponse;
import com.example.datemaker.model.User;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.model.VerifyEmailRequest;
import com.google.android.gms.auth.api.Auth;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/register")
    Call<UserDto> createUser(@Body RegisterRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/login")
    Call<AuthResponse> loginUser(@Body Login loginUser);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/partner/isPartnered/{userId}")
    Call<Boolean> isPartnered(@Path("userId") Long userId);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/users/email")
    Call<UserDto> getUserByEmail(@Query("email") String email);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/partner/add")
    Call<String> addPartner(@Query("userId") Long userId, @Query("partnerEmail") String partnerEmail);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("/api/partner/{userId}")
    Call<PartnerResponse> getPartner(@Path("userId") long userId);

    @Headers("ngrok-skip-browser-warning: true")
    @Multipart
    @POST("api/images/upload")
    Call<Void> uploadImage(
            @Part("userId") String userId,
            @Part("partnerId") String partnerId,
            @Part("caption") String caption,
            @Part MultipartBody.Part image
    );

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/images/user/{userId}")
    Call<List<ProgramItem>> getImagesForUser(@Path("userId") Long userId);

    @Headers("ngrok-skip-browser-warning: true")
    @DELETE("api/images/{id}")
    Call<Void> deleteImage(@Path("id") long imageId);

    @Headers("ngrok-skip-browser-warning: true")
    @DELETE("api/partner/remove/{userId}")
    Call<Void> removePartner(@Path("userId") long userId);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/partner/request")
    Call<Void> sendPartnerRequest(@Query("fromUserId") long fromUserId, @Query("partnerEmail") String partnerEmail);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/partner/requests")
    Call<List<PartnerRequestDto>> getPartnerRequests(@Query("userId") long userId);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/users/search-partner")
    Call<PartnerSearchResponse> searchPartner(@Query("currentUserId") long currentUserId, @Query("query") String query);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/partner/accept/{requestId}")
    Call<Void> acceptPartnerRequest(@Path("requestId") long requestId);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/partner/reject/{requestId}")
    Call<Void> rejectPartnerRequest(@Path("requestId") long requestId);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/fcm-token")
    Call<Void> sendFcmToken(@Query("token") String token);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/verify-email")
    Call<ResponseBody> verifyEmail(@Body VerifyEmailRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/resend-code")
    Call<ResponseBody> resendCode(@Body ResendCodeRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/google-login")
    Call<AuthResponse> googleLogin(@Body GoogleLoginRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/google-auth")
    Call<AuthResponse> googleAuth(@Body GoogleLoginRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/facebook-auth")
    Call<AuthResponse> facebookAuth(@Body FacebookLoginRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @PATCH("api/users/me/avatar-url")
    Call<UserDto> setAvatarUrl(@Body java.util.Map<String, String> body);

    @Headers("ngrok-skip-browser-warning: true")
    @Multipart
    @POST("api/users/me/avatar-upload")
    Call<UserDto> uploadAvatar(@Part MultipartBody.Part file);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/messages/{partnerId}")
    Call<java.util.List<Message>> getMessages(@Path("partnerId") long partnerId);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/messages")
    Call<Message> sendMessage(@Body SendMessageRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/users/me/contact")
    Call<ContactInfoDto> getMyContact();

    @Headers("ngrok-skip-browser-warning: true")
    @PATCH("api/users/me/contact")
    Call<ContactInfoDto> updateMyContact(@Body ContactInfoDto contact);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/users/me")
    Call<ProfileDto> getMe();

    @Headers("ngrok-skip-browser-warning: true")
    @PATCH("api/users/me")
    Call<ProfileDto> updateMe(@Body Map<String, String> body);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/me/phone/send-code")
    Call<Void> sendPhoneVerificationCode(@Body PhoneNumberRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/me/phone/verify")
    Call<ContactInfoDto> verifyPhone(@Body PhoneVerificationRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @PATCH("api/partner/relationship-date/{userId}")
    Call<Void> setRelationshipDate(@Path("userId") long userId, @Query("date") String date);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/users/reset-password/request")
    Call<ResponseBody> requestPasswordReset(@Body PasswordResetRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/dates/day")
    Call<List<DateEventDto>> getDateEventsForDay(@Query("date") String isoDate);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/dates")
    Call<DateEventDto> createDateEvent(@Body DateEventRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @PUT("api/dates/{id}")
    Call<DateEventDto> updateDateEvent(@Path("id") long id, @Body DateEventRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @DELETE("api/dates/{id}")
    Call<Void> deleteDateEvent(@Path("id") long id);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/ai/date-planner")
    Call<AiDateResponse> getAiDateIdea(@Body AiDateRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/inspiration/categories")
    Call<List<CategoryDto>> getInspirationCategories();

    @Headers("ngrok-skip-browser-warning: true")
    @Multipart
    @POST("api/inspiration/upload")
    Call<String> uploadInspirationImage(@Part MultipartBody.Part file);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/inspiration/ideas")
    Call<IdeaDto> createInspirationIdea(
            @Query("categoryId") Long categoryId,
            @Query("title") String title,
            @Query("description") String description,
            @Query("imageUrl") String imageUrl
    );

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/inspiration/react")
    Call<IdeaDto> reactToIdea(@Body ReactionRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/games/love-arrows/result")
    Call<LoveArrowsResultResponse> submitLoveArrowsResult(@Body LoveArrowsResultRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/games/love-arrows/summary")
    Call<LoveArrowsResultResponse> getLoveArrowsSummary();

    @Headers("ngrok-skip-browser-warning: true")
    @PATCH("api/users/me/subscription")
    Call<UserDto> updateSubscription(@Body Map<String, Boolean> body);

    @Headers("ngrok-skip-browser-warning: true")
    @POST("api/games/snake/result")
    Call<SnakeResultResponse> submitSnakeResult(@Body SnakeResultRequest request);

    @Headers("ngrok-skip-browser-warning: true")
    @GET("api/games/snake/summary")
    Call<SnakeResultResponse> getSnakeSummary();
}