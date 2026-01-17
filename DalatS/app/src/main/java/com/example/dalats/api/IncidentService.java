package com.example.dalats.api;

import com.example.dalats.model.CommentRequest;
import com.example.dalats.model.CommentResponse;
import com.example.dalats.model.CreateIncidentDTO;
import com.example.dalats.model.Incident;
import com.example.dalats.model.IncidentCategory;
import com.example.dalats.model.IncidentMap;
import com.example.dalats.model.IncidentSearchDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IncidentService {

    @GET("api/Incidents/map")
    Call<List<IncidentMap>> getMapData();

    @GET("api/Incidents/feed")
    Call<List<Incident>> getPublicFeed();

    @GET("api/Incidents/get-by-id/{id}")
    Call<Incident> getDetail(@Path("id") int id);

    // Lấy danh sách bình luận
    @GET("api/IncidentComments/Get")
    Call<List<CommentResponse>> getComments(@Query("incidentId") int incidentId);

    // Gửi bình luận
    @POST("api/IncidentComments/Create")
    Call<ResponseBody> postComment(@Query("incidentId") int incidentId, @Body CommentRequest request);
    @GET("api/IncidentCategories/get-all")
    Call<List<IncidentCategory>> getCategories();

    @POST("api/Incidents/search")
    Call<List<Incident>> searchIncidents(@Body IncidentSearchDTO dto);
    // 2. Tạo sự cố (Trả về IncidentResponse để lấy ID)
    @POST("api/Incidents/create")
    Call<Incident> createIncident(@Body CreateIncidentDTO body);

    // 3. Upload ảnh (Multipart)
    @Multipart
    @POST("api/IncidentImages")
    Call<List<String>> uploadImages(
            @Query("incidentId") int incidentId,
            @Part List<MultipartBody.Part> files
    );
}
