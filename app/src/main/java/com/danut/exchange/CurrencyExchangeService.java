package com.danut.exchange;

import retrofit2.Call;
import retrofit2.http.GET;


public interface CurrencyExchangeService {
    @GET("latest")
    Call<CurrencyExchange> loadCurrencyExchange();
}
