```shell
# 查看gpg密钥
D:\IdeaProjects\iptool>gpg --list-keys
C:/Users/blank/AppData/Roaming/gnupg/pubring.kbx
------------------------------------------------
pub   rsa3072 2021-02-01 [SC] [expires: 2023-02-01]
5F03BD841609C890D5A7D565D86B505C8ECBFA36
uid           [ultimate] blank <blankhang@gmail.com>
sub   rsa3072 2021-02-01 [E] [expires: 2023-02-01]

# 将公钥信息上传到服务器
D:\IdeaProjects\iptool>gpg --keyserver hkp://keyserver.ubuntu.com --send-keys 5F03BD841609C890D5A7D565D86B505C8ECBFA36
gpg: sending key D86B505C8ECBFA36 to hkp://keyserver.ubuntu.com

# 运行打包发布命令
mvn clean deploy -P release -Dmaven.test.skip=true
```
