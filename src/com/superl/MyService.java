package com.superl;

/**
 * @author superl  www.superl.org
 * @version V1.0
 * @Package com.superl
 * @Description 监控功能
 * @date 2017/4/11 下午6:07
 */

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.ParseException;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import static java.nio.file.StandardWatchEventKinds.*;

public class MyService {

    private WatchService watchService = null;

    private String  filePrefix;
    private String  monitoredPath;
    private Boolean checkPrefix;
    private Boolean monitoredDir;
    private Boolean monitoredMD;
    private String  ruledoutDir;
    private String  mobileNum;
    private String  userName;
    private String  passWord;

    private String smsContent;
    private String eventMethod;

    public void initialize() throws IOException{

        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        prop.load(bf);
        inputStream.close(); // 关闭流

        /*
        InputStream in = new BufferedInputStream(new FileInputStream("config.properties"));
        Properties prop = new Properties();
        prop.load(in);
        */

        filePrefix = prop.getProperty("file_prefix");
        monitoredPath = prop.getProperty("monitored_path");
        checkPrefix = new Boolean(prop.getProperty("check_prefix"));
        monitoredDir = new Boolean(prop.getProperty("monitored_directory"));
        monitoredMD = new Boolean(prop.getProperty("monitored_move_delete"));
        ruledoutDir = prop.getProperty("ruledout_dir");
        mobileNum = prop.getProperty("mobile");
        userName = prop.getProperty("username");
        passWord = prop.getProperty("password");
        //System.out.println(ruledoutDir); 测试是否中文乱码

        watchService = FileSystems.getDefault().newWatchService();
        Paths.get(monitoredPath).register(watchService, ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY);

        File file = new File(monitoredPath);
        LinkedList<File> fList = new LinkedList<File>();
        fList.addLast(file);
        while (fList.size() > 0 ) {
            File f = fList.removeFirst();
            if(f.listFiles() == null)
                continue;
            for(File file2 : f.listFiles()){
                if (file2.isDirectory()){//下一级目录
                    fList.addLast(file2);
                    //依次注册子目录
                    Paths.get(file2.getAbsolutePath()).register(watchService
                            , StandardWatchEventKinds.ENTRY_CREATE
                            , StandardWatchEventKinds.ENTRY_MODIFY
                            , StandardWatchEventKinds.ENTRY_DELETE);
                }
            }
        }
    }

    public void doMonitor() throws InterruptedException,IOException{
        final Properties PROPERTIES = new Properties(System.getProperties());
        String separator = PROPERTIES.getProperty("file.separator");

        while(true){
            try {
                WatchKey key = watchService.take();
                for(WatchEvent<?> event : key.pollEvents()){
                    WatchEvent.Kind kind = event.kind();

                    if(kind == OVERFLOW){
                        //事件可能lost or discarded
                        continue;
                    }

                    WatchEvent<Path> e = (WatchEvent<Path>)event;
                    //获取路径
                    Path path = (Path) key.watchable();
                    //获取分隔符号
                    String fullpath = path.toString()+separator;
                    //获取文件名
                    Path fileName = e.context();
                    //拼接文件名称和路径
                    String filepath = fullpath+fileName;
                    //获取文件后缀
                    String prefix = fileName.toString().substring(fileName.toString().lastIndexOf(".")+1);

                    File theFile = new File(filepath);



                    //判断是文件还是目录
                    if(theFile.isFile()){
                        //判断是否是白名单目录
                        String[] ruledoutDirArray = ruledoutDir.split(",");
                        Boolean haveStr = false;
                        if(ruledoutDirArray!=null||ruledoutDirArray.length!=0){
                            haveStr = Arrays.asList(ruledoutDirArray).contains(path.toString());
                        }else{
                            haveStr = false;
                        }
                        if(!haveStr){
                            if(checkPrefix){
                                if(prefix.equals(filePrefix)){
                                    if(kind.toString().equals("ENTRY_CREATE")){
                                        eventMethod = "被新建";
                                    }else if (kind.toString().equals("ENTRY_MODIFY")){
                                        eventMethod = "被修改";
                                    }else{
                                        eventMethod = "被操作";
                                    }
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                    smsContent = "文件:"+filepath+eventMethod+",时间:"+df.format(new Date());

                                    //输出提示信息
                                    System.out.println(smsContent);

                                    //发送提示信息到手机短信
                                    ChuanglanSMS client = new ChuanglanSMS(userName,passWord);
                                    CloseableHttpResponse response = null;
                                    try {
                                        response = client.sendMessage(mobileNum,smsContent);
                                        if(response != null && response.getStatusLine().getStatusCode()==200){
                                            System.out.println(EntityUtils.toString(response.getEntity()));
                                            System.out.println("短信发送成功,内容为:"+smsContent);
                                        }
                                    }catch (ParseException p1) {
                                        System.out.println("短信发送成失败");
                                        p1.printStackTrace();
                                    }catch (IOException i1) {
                                        System.out.println("短信发送成失败");
                                        i1.printStackTrace();
                                    }
                                    client.close();

                                }
                            }else{
                                if(kind.toString().equals("ENTRY_CREATE")){
                                    eventMethod = "被新建";
                                }else if (kind.toString().equals("ENTRY_MODIFY")){
                                    eventMethod = "被修改";
                                }else{
                                    eventMethod = "被操作";
                                }
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                smsContent = "文件:"+filepath+eventMethod+",时间:"+df.format(new Date());

                                //输出提示信息
                                System.out.println(smsContent);

                                //发送提示信息到手机短信
                                ChuanglanSMS client = new ChuanglanSMS(userName,passWord);
                                CloseableHttpResponse response = null;
                                try {
                                    response = client.sendMessage(mobileNum,smsContent);
                                    if(response != null && response.getStatusLine().getStatusCode()==200){
                                        System.out.println(EntityUtils.toString(response.getEntity()));
                                        System.out.println("短信发送成功,内容为:"+smsContent);
                                    }
                                }catch (ParseException p1) {
                                    System.out.println("短信发送成失败");
                                    p1.printStackTrace();
                                }catch (IOException i1) {
                                    System.out.println("短信发送成失败");
                                    i1.printStackTrace();
                                }
                                client.close();

                            }
                        }else{
                            //不监控白名单目录
                        }
                    }else if(theFile.isDirectory()){
                        //System.out.println("这是目录类型");

                        //判断是否是白名单目录
                        String[] ruledoutDirArray = ruledoutDir.split("||");
                        Boolean haveStr;
                        if(ruledoutDirArray!=null||ruledoutDirArray.length!=0){
                            haveStr = Arrays.asList(ruledoutDirArray).contains(fullpath);
                        }else{
                            haveStr = false;
                        }
                        if(!haveStr){
                            if(monitoredDir){
                                if(kind.toString().equals("ENTRY_CREATE")){
                                    eventMethod = "被新建";
                                }else if (kind.toString().equals("ENTRY_MODIFY")){
                                    eventMethod = "被修改";
                                }else{
                                    eventMethod = "被操作";
                                }
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                smsContent = "文件:"+filepath+eventMethod+",时间:"+df.format(new Date());

                                //输出提示信息
                                System.out.println(smsContent);

                                //发送提示信息到手机短信
                                ChuanglanSMS client = new ChuanglanSMS(userName,passWord);
                                CloseableHttpResponse response = null;
                                try {
                                    response = client.sendMessage(mobileNum,smsContent);
                                    if(response != null && response.getStatusLine().getStatusCode()==200){
                                        System.out.println(EntityUtils.toString(response.getEntity()));
                                        System.out.println("短信发送成功,内容为:"+smsContent);
                                    }
                                }catch (ParseException p1) {
                                    System.out.println("短信发送成失败");
                                    p1.printStackTrace();
                                }catch (IOException i1) {
                                    System.out.println("短信发送成失败");
                                    i1.printStackTrace();
                                }
                                client.close();
                            }else{
                                //不监控目录类型
                            }
                        }
                    }else{
                        //文件被删除,被移动

                        //判断是否是白名单目录
                        String[] ruledoutDirArray = ruledoutDir.split("||");
                        Boolean haveStr;
                        if(ruledoutDirArray!=null||ruledoutDirArray.length!=0){
                            haveStr = Arrays.asList(ruledoutDirArray).contains(fullpath);
                        }else{
                            haveStr = false;
                        }

                        if(!haveStr){
                            if(monitoredMD){
                                if(checkPrefix){
                                    if(prefix.equals(filePrefix)){
                                        if(kind.toString().equals("ENTRY_CREATE")){
                                            eventMethod = "新建操作";
                                        }else if (kind.toString().equals("ENTRY_MODIFY")){
                                            eventMethod = "修改操作";
                                        }else{
                                            eventMethod = "其他操作";
                                        }
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                        smsContent = "文件:"+filepath+"  发生了"+eventMethod+"  操作时间:"+df.format(new Date());

                                        System.out.println(smsContent);

                                        ReadWriteFile myfile = new ReadWriteFile();
                                        myfile.creatTxtFile();
                                        myfile.writeTxtFile(smsContent);
                                    }
                                }else{
                                    //System.out.printf("Event %s has happened,which fileName is %s%n",kind,filepath);
                                    if(kind.toString().equals("ENTRY_CREATE")){
                                        eventMethod = "新建操作";
                                    }else if (kind.toString().equals("ENTRY_MODIFY")){
                                        eventMethod = "修改操作";
                                    }else{
                                        eventMethod = "其他操作";
                                    }
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                    smsContent = "文件:"+filepath+"  发生了"+eventMethod+"  操作时间:"+df.format(new Date());

                                    System.out.println(smsContent);

                                    ReadWriteFile myfile = new ReadWriteFile();
                                    myfile.creatTxtFile();
                                    myfile.writeTxtFile(smsContent);
                                }
                            }else{
                                //配置文件设置了不监控文件被删除和移动操作
                            }
                        }
                    }
                }
                if(!key.reset()){
                    break;
                }
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: " + e.getMessage());
            }
        }
    }
}
