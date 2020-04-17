package com.github.blankhang.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP请求工具类
 *
 * @author blank
 */
@Slf4j
public class HttpUtil {

    /**
     * 发送 POST 请求
     *
     * @param url           请求url
     * @param requestParams 请求参数
     * @param customHeaders 自定义请求头
     * @return com.fasterxml.jackson.databind.ObjectMapper
     * @author blank
     * @since 1.0.0
     */
    public static JSONObject sendPostRequest(String url, Map<String, Object> requestParams, Map<String, String> customHeaders) {

        Assert.notNull(url, "request url can not be null!");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        // 设置请求的header
        if (customHeaders != null && !customHeaders.isEmpty()) {
            customHeaders.forEach(httpPost::addHeader);
        }

        if (requestParams != null && !requestParams.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            requestParams.forEach((s, o) -> nameValuePairs.add(new BasicNameValuePair(s, String.valueOf(o))));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
        }

        StringBuilder sb = new StringBuilder();
        // 执行请求
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            sb.append(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
            //确认消费 关闭http连接
            EntityUtils.consume(response.getEntity());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打印执行结果
        log.info(sb.toString());
        if (StringUtils.isNotBlank(sb)) {
            return new JSONObject(sb.toString());
        }
        return null;
    }

    /**
     * 发送 GET 请求
     *
     * @param url 请求url
     * @return cn.hutool.json.JSONObject
     * @author blank
     * @since 1.0.0
     */
    public static JSONObject sendGetRequest(String url) {

        Assert.notNull(url, "request url can not be null!");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            //确认消费 关闭http连接
            EntityUtils.consume(entity);
            String text = entity.toString();
            if (StringUtils.isNotBlank(text)) {
                return new JSONObject(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 使用 OKHTTP3 发送同步 GET 请求
     *
     * @param url 请求URL
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    public static String syncGetCall(String url) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        String string = null;
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            string = Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug(string);
        return string;
    }

    /**
     * 使用 OKHTTP3 发送异步 GET 请求
     *
     * @param url 请求URL
     * @return java.lang.String
     * @throws Exception 异常
     * @author blank
     * @since 1.0.0
     */
    public static String asyncGetCall(String url) throws Exception {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        final String[] string = new String[1];
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("onFailure: ");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                string[0] = Objects.requireNonNull(response.body()).string();
                log.debug("onResponse: " + string[0]);
            }
        });
        //等待请求线程，否则主线程结束无法看到请求结果
        Thread.currentThread().join(5000);
        return string[0];
    }



    private void processResponse(@NotNull Response response) throws IOException {
        log.debug(response.protocol() + " " + response.code() + " " + response.message());
        Headers headers = response.headers();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(headers.name(i)).append(":").append(headers.value(i)).append("\n");
        }
        log.debug("headers:\n{}", sb.toString());
        log.debug("onResponse: " + response.body().string());
    }

    public void postFormData() throws InterruptedException {
        OkHttpClient okHttpClient = new OkHttpClient();
        okhttp3.RequestBody requestBody = new FormBody.Builder()
                .add("search", "java")
                .build();
        Request request = new Request.Builder()
                .url("https://en.wikipedia.org/w/index.php")
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.debug("onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processResponse(response);
            }
        });
        Thread.currentThread().join(5000);
    }

    public void postMutilPartFormData() throws Exception {
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("extra", "extra args")
                .addFormDataPart("file", "logo.png",
                        RequestBody.create(MEDIA_TYPE_PNG, new File("D:\\code\\work\\egovaCloud\\src\\main\\resources\\static\\image\\logo.png")))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:8888/base-platform/upload/uploadFile")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        log.debug(response.body().string());
    }


    /**
     * get 请求 返回JSON对象
     *
     * @param url 请求URL
     * @return cn.hutool.json.JSONObject
     * @author blank
     * @since 1.0.0
     */
    public static JSONObject getRequest(String url) {
        Assert.notNull(url, "request url can not be null!");

        String s = HttpUtil.syncGetCall(url);
        if (StringUtils.isNotBlank(s)) {
            return new JSONObject(s);
        }
        return null;
    }


    /**
     * 判断字符串是否为URL
     *
     * @param urls 用户头像key
     * @return true:是URL、false:不是URL
     * @since 1.0.0
     */
    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-"
                + "Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
                + "2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}"
                + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
                + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-"
                + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
                + "-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/"
                + "[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$\\=~_\\-@]*)*$";
        //比对
        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        //判断是否匹配
        isurl = mat.matches();
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

    /**
     * 判断字符串是否为URL
     *
     * @param urls 用户头像key
     * @return true:是URL、false:不是URL
     * @throws Exception 异常
     * @since 1.0.0
     */
    public static String getUrlHost(String urls) throws Exception {
        if (!isHttpUrl(urls)) {
            throw new Exception("Invalid url");
        }
        URL url = new URL(urls);
        return url.getHost();
    }

    public static void main(String[] args) {

    }
}
