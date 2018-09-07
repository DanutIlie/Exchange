package com.danut.exchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;



import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements Callback<CurrencyExchange>, CurrencyItemClickListener {

    private ListView lvCurrency;
    int cacheSize=10*1024*1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvCurrency = (ListView) findViewById(R.id.lvCurrency);



    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCurrencyExchangeData();
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworcInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworcInfo != null&& activeNetworcInfo.isConnected();
    }

    private void loadCurrencyExchangeData(){


        Cache cache=new Cache(getCacheDir(), cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                        Request request=chain.request();
                        if(!isNetworkAvailable()){
                            int maxStale=60*60*24*28;
                            request =request
                                    .newBuilder()
                                    .header("Cache-Control","public, only-if-cached, maxStale="+maxStale)
                                    .build();
                        }
                        return chain.proceed(request);
                    }
                }).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.exchangeratesapi.io/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CurrencyExchangeService service = retrofit.create(CurrencyExchangeService.class);
        Call<CurrencyExchange> call = service.loadCurrencyExchange();
        call.enqueue(this);

    }


    @Override
    public void onResponse(Call<CurrencyExchange> call, Response<CurrencyExchange> response) {
        //Toast.makeText(this, response.body().getBase(), Toast.LENGTH_LONG).show();
        CurrencyExchange currencyExchange = response.body();
        lvCurrency.setAdapter(new CurrencyAdapter(this, currencyExchange.getCurrencyList(), this));
    }

    @Override
    public void onFailure(Call<CurrencyExchange> call, Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCurrencyItemClick(Currency c) {
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("currency_name", c.getName());
        intent.putExtra("currency_rate", c.getRate());

        startActivity(intent);
    }
}
