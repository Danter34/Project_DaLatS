package com.example.dalats.api;

import com.example.dalats.model.CreateQuestionDTO;
import com.example.dalats.model.QuestionCategory;
import com.example.dalats.model.QuestionResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface QAService {
    // Lấy danh sách câu hỏi (Kèm câu trả lời bên trong)
    @GET("api/QA")
    Call<List<QuestionResponseDTO>> getAllQuestions();

    @GET("api/QA/my-questions")
    Call<List<QuestionResponseDTO>> getMyQuestions();
    // Tạo câu hỏi mới
    @POST("api/QA")
    Call<QuestionResponseDTO> createQuestion(@Body CreateQuestionDTO dto);


    @GET("api/QA/categories")
    Call<List<QuestionCategory>> getCategories();
}