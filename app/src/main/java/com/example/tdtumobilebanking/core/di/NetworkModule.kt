package com.example.tdtumobilebanking.core.di

import com.example.tdtumobilebanking.core.di.DirectionsRetrofit
import com.example.tdtumobilebanking.core.di.VietQrRetrofit
import com.example.tdtumobilebanking.core.di.StripeBackendRetrofit
import com.example.tdtumobilebanking.data.remote.api.BankApi
import com.example.tdtumobilebanking.data.remote.api.StripePaymentApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/maps/api/"
    private const val VIETQR_BASE_URL = "https://api.vietqr.io/"
    // Backend riêng của bạn để tạo PaymentIntent với Stripe (chạy trên localhost:4242, map vào 10.0.2.2 cho emulator)
    private const val STRIPE_BACKEND_BASE_URL = "http://10.0.2.2:4242/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @DirectionsRetrofit
    fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(DIRECTIONS_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @VietQrRetrofit
    fun provideVietQrRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(VIETQR_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideBankApi(@VietQrRetrofit retrofit: Retrofit): BankApi = retrofit.create(BankApi::class.java)

    @Provides
    @Singleton
    @StripeBackendRetrofit
    fun provideStripeBackendRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(STRIPE_BACKEND_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideStripePaymentApi(@StripeBackendRetrofit retrofit: Retrofit): StripePaymentApi =
        retrofit.create(StripePaymentApi::class.java)
}

