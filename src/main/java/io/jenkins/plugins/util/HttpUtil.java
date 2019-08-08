package io.jenkins.plugins.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import io.jenkins.plugins.enums.HttpExceptionCodeEnums;

/**
 * 
 * ClassName: HttpUtil Reason: 发送http/https的封装类 date: 2017年3月14日 下午2:03:11 company:你我贷
 * 
 * @author 张鹏
 * @version
 * @since JDK 1.7
 */
public class HttpUtil {
    
    private static Logger log = Logger.getLogger(HttpUtil.class.getName());
    
    /**
     * User-Agent 浏览器用户标识
     */
    public static final String USER_AGENT_KEY="User-Agent";
    public static final String USER_AGENT_VALUE="Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";
    
    /**
     * http请求参数编码
     */
    public static final String HTTP_PARAMS_CODING="UTF-8";
    
    private static SSLConnectionSocketFactory sslFactory = null;
    private static PoolingHttpClientConnectionManager connectionManager = null;
    private static SSLContextBuilder builder = null;
    private static RequestConfig requestConfig = null;
    private static RedirectStrategy redirectStrategy = null;
    
    static {
        try {
            builder = new SSLContextBuilder();
            // HTTPS全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory()).register("https", sslFactory).build();
            connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null, 30, TimeUnit.SECONDS);
            connectionManager.setMaxTotal(500);//连接池最大并发连接数
            connectionManager.setDefaultMaxPerRoute(500);//单路由的最大并发连接数
            
            /**
             * 设置请求和.设置从connect Manager获取Connection 超时时间.传输超时时间
             * .setConnectTimeout(5000) 建立连接的时间
             * .setConnectionRequestTimeout(5000) 从连接池中获取连接的时间
             * .setSocketTimeout(10000)  数据传输处理时间
             */
            requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000).build();
            //解决post重定向问题
            redirectStrategy = new LaxRedirectStrategy();
        } catch (Exception e) {
            log.log(Level.SEVERE, "设置httpclient参数失败", e);
        }
    }
    
    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * 获取HttpClient对象
     * @return
     */
    public static CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setRedirectStrategy(redirectStrategy).setSSLSocketFactory(sslFactory).setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).setConnectionManagerShared(true).build();
    }

    /**
     * @methodName: httpGet
     * @description: http get 请求
     * @author youxiang
     * @date 2019年3月13日 下午4:45:09
     * @param url
     * @param param
     * @param header
     * @return
     */
    public static String httpGet(String url, String param, Map<String, String> header) {
        HttpGet httpGet = null;
        try (
                CloseableHttpClient httpClient = HttpUtil.getHttpClient();
                ){
            if(StringUtils.isEmpty(url)) {
                throw new HttpException("GET请求失败,url为空");
            }
            if(!StringUtils.isEmpty(param)) {
                url = url.concat("?").concat(param);
            }
            httpGet = new HttpGet(url);
            //设置请求头
            httpGet.addHeader("Accept", "*/*");
            httpGet.addHeader(HttpUtil.USER_AGENT_KEY, HttpUtil.USER_AGENT_VALUE);
            if(header!=null) {
                for(Entry<String, String> entry:header.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            return body(httpResponse);
        } catch (Exception e) {
            throw new HttpException("GET请求失败", e);
        }
    }
    
    /**
     * @methodName: httpPost
     * @description: http post 请求
     * @author youxiang
     * @date 2019年3月13日 下午4:45:09
     * @param url
     * @param headers
     * @param values
     * @return
     */
    public static String httpPost(String url, Map<String, String> headers, Map<String, String> values) {
        try (
                CloseableHttpClient httpClient = HttpUtil.getHttpClient();
                ){
            if(StringUtils.isEmpty(url)) {
                throw new HttpException("POST请求失败,url为空");
            }
            HttpPost post = new HttpPost(url);
            //设置请求头
            post.addHeader(HttpUtil.USER_AGENT_KEY, HttpUtil.USER_AGENT_VALUE);
            if (headers != null && !headers.isEmpty()) {
                for(Map.Entry<String, String> header_entry : headers.entrySet()){
                    post.addHeader(header_entry.getKey(), header_entry.getValue());
                }
            }
            if (values != null && !values.isEmpty()) {
                List<NameValuePair> nvps = new ArrayList<>();
                for(Map.Entry<String, String> value_entry : values.entrySet()){
                    nvps.add(new BasicNameValuePair(value_entry.getKey(), value_entry.getValue()));
                }
                post.setEntity(new UrlEncodedFormEntity(nvps, HttpUtil.HTTP_PARAMS_CODING));
            }
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            return body(httpResponse);
        }catch (HttpHostConnectException e) {
        	throw new HttpException(HttpExceptionCodeEnums.ERROR_400.getCode(), "POST请求失败", e);
        } catch (Exception e) {
            throw new HttpException(HttpExceptionCodeEnums.ERROR_999.getCode(), "POST请求失败", e);
        }
    }
    
    /**
     * @methodName: httpPostJson
     * @description: http post json请求
     * @author youxiang
     * @date 2019年3月13日 下午4:45:09
     * @param url
     * @param headers
     * @param valueJson
     * @return
     */
    public static String httpPostJson(String url, Map<String, String> headers, String valueJson) {
        try (
                CloseableHttpClient httpClient = HttpUtil.getHttpClient();
                ){
            if(StringUtils.isEmpty(url)) {
                throw new HttpException("POST_JSON请求失败,url为空");
            }
            HttpPost post = new HttpPost(url);
            //设置请求头
            post.addHeader(HttpUtil.USER_AGENT_KEY, HttpUtil.USER_AGENT_VALUE);
            if (headers != null && !headers.isEmpty()) {
                for(Map.Entry<String, String> header_entry : headers.entrySet()){
                    post.addHeader(header_entry.getKey(), header_entry.getValue());
                }
            }
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json; charset="+HttpUtil.HTTP_PARAMS_CODING);
            if(!StringUtils.isEmpty(valueJson)) {
                StringEntity entity = new StringEntity(valueJson, StandardCharsets.UTF_8);
                entity.setContentEncoding(HttpUtil.HTTP_PARAMS_CODING);
                entity.setContentType("application/json");// 设置为 json数据
                post.setEntity(entity);
            }
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            return body(httpResponse);
        } catch (Exception e) {
            throw new HttpException("POST_JSON请求失败", e);
        }
    }
    
    private static String body(CloseableHttpResponse httpResponse) throws IOException {
        String body = null;
        String charsetStr = HttpUtil.HTTP_PARAMS_CODING;
        HttpEntity entity = httpResponse.getEntity();
        if(entity != null){
            byte [] bytes = EntityUtils.toByteArray(entity);
            EntityUtils.consume(entity);
            if(bytes!=null && bytes.length>0){
                Charset charset = null;
                // 如果头部Content-Type中包含了编码信息，那么我们可以直接在此处获取
                ContentType contentType = ContentType.getOrDefault(entity);
                charset = contentType.getCharset();
                if(charset!=null) {
                    charsetStr = charset.name();
                }
                // 至此，我们可以将原byte数组按照正常编码专成字符串输出（如果找到了编码的话）
                body = new String(bytes, charsetStr);
            }
        }
        return body;
    }
    
}