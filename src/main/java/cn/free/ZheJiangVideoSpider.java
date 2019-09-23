package cn.free;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ZheJiangVideoSpider {

    public void httpClient() {
        //1.生成httpclient，相当于该打开一个浏览器
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        //2.创建get请求，相当于在浏览器地址栏输入 网址
        HttpGet request = new HttpGet("http://www.cztv.com/videos/zjxwlb?tdsourcetag=s_pctim_aiomsg");
//        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        try {
            //3.执行get请求，相当于在输入地址栏后敲回车键
            response = httpClient.execute(request);

            //4.判断响应状态为200，进行处理
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //5.获取响应内容
                HttpEntity httpEntity = response.getEntity();
                String html = EntityUtils.toString(httpEntity, "utf-8");
//                System.out.println(html);
                Document doc = Jsoup.parse(html);//解析html文档
            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                System.out.println("返回状态不是200");
                System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //6.关闭
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
    }

    public void jsoup() {
        try {
            Document document = Jsoup.connect("http://www.cztv.com/videos/zjxwlb").get();
            Element body = document.body();
            Elements elements = body.getElementsByClass("box1");
            for (Element element : elements) {
                Elements a = element.getElementsByTag("a");
                String href = a.attr("href");
                String fileName = a.attr("title");
                if (href.equals("")) {
                    continue;
                }
//                System.out.println(href);
                Document videoDoc = Jsoup.connect(href).get();
                Elements videoElement = videoDoc.getElementsByClass("video");
                for (Element element1 : videoElement) {
                    Element script = element1.selectFirst("script");
                    String content = script.data();
                    int start = content.indexOf("path3") + 8;
                    int end = content.indexOf("}", start) - 1;
                    String videoUrl = content.substring(start, end);
                    videoUrl = videoUrl.replaceAll("\\\\", "");
                    System.out.println(videoUrl);
//                    http:\/\/ali.v.cztv.com\/cztv\/vod\/2019\/09\/21\/f84da4dc206744eaa3a29cbe7101076f\/h264_1500k_mp4.mp4

                    dowloadFromUrl(videoUrl, fileName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dowloadFromUrl(String videoUrl, String fileName) {
        try {
            URL url = new URL("HTTP", "www.cztv.com", 80,videoUrl);
//            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
//            connection.setDoOutput(true);
//            connection.connect();
            InputStream inputStream = connection.getInputStream();
            String outputPath = "F:\\Video\\" + fileName + ".mp4";
            OutputStream outputStream = new FileOutputStream(outputPath);
            byte[] bytes = new byte[4096];
            while (inputStream.read(bytes) != -1) {
                outputStream.write(bytes);
            }
            inputStream.close();
            outputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ZheJiangVideoSpider().jsoup();
    }
}
