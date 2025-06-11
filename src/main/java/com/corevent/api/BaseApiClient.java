package com.corevent.api;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public abstract class BaseApiClient {
  
  @Value("${api.base-url}")
  protected String baseUrl;
  
  @Value("${api.timeout:30}")
  protected int timeout;
  
  protected Retrofit retrofit;
  
  protected Retrofit createRetrofit() {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chain -> {
              Request original = chain.request();
              Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json");
                
              // Add auth token if available
              String token = SessionManager.getInstance().getAuthToken();
              if (token != null) {
                requestBuilder.header("Authorization", "Bearer " + token);
              }
              
              Request request = requestBuilder.build();
              return chain.proceed(request);
            })
            .build();
    
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();
  }
}