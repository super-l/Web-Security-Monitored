package com.superl;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException{
        System.err.println("start MyWatcherService ...");

        /*
        ChuanglanSMS client = new ChuanglanSMS("xxxxx","xxxxx");
        CloseableHttpResponse response = null;
        try {
            //发送短信
            response = client.sendMessage("181xxxxxxxx","验证码为xxxxxxxx");
            if(response != null && response.getStatusLine().getStatusCode()==200){
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.close();
        */

        MyService myWatcherService = new MyService();
        myWatcherService.initialize();
        myWatcherService.doMonitor();
    }
}
