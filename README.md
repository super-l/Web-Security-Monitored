# Web-Security-Monitored跨平台服务器文件安全监控工具## 使用说明：软件需要安装java运行环境。并设置java的相关环境变量。安装JAVA和设置环境变量的方法请搜索百度。检测环境是否成功: java -version## 运行方法：命令行下输入:java -jar Web-Security-Monitored.jar运行完毕后，即可开始监控。## 使用截图：Last login: Tue Apr 11 20:01:05 on ttys000superldeMacBook-Pro:~ superl$ cd /Users/superl/tools/原创源码/Web\ Security\ MonitoredsuperldeMacBook-Pro:Web Security Monitored superl$ java -jar Web-Security-Monitored.jarstart MyWatcherService ...文件:/Users/superl/Desktop/test.php被新建,时间:2017-04-11 20:03:01{"success": true, "id":"17041120030239600"}短信发送成功,内容为:文件:/Users/superl/Desktop/test.php被新建,时间:2017-04-11 20:03:01## 配置文件说明：软件运行之前，需要根据自己的需求，修改配置config.properties。### 例子：* monitored_path=/Users/superl/Desktop    要监控的路径* check_prefix=true                       是否只监控指定后缀的文件* file_prefix=php                         要监控的文件后缀，如php* monitored_directory=false               是否监控目录的操作，默认不监控* monitored_move_delete=false             是否监控文件的移动和删除操作，默认不监控* ruledout_dir=/Users/superl/Desktop/test,/Users/superl/Desktop/java,/Users/superl/Desktop/notorm   不监控的路径白名单* mobile=181xxxxxxxx                      短信通知的手机号码* username=xxxxxx                         短信平台的账号* password=xxxxxx                         短信平台的密码### 友情提示ruledout_dir表示不监控的路径白名单。请填写绝对路径。如果是windows系统，请填写格式如:d:/wwwroot/www/upload如果有多个目录不需要监控，请用英文逗号【,】隔开。## 使用步骤：1. 先配置好配置文件；2. 把Web-Security-Monitored.jar和config.properties复制到d:/safetools3. 打开命令提示符，使用cd命令切换到d:/safetools。（命令：cd d:/safetools）4. 输入java -jar Web-Security-Monitored.jar 命令即可。