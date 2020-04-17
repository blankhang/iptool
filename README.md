# iptool ip 辅助工具
ip locating agent tool ip 定位辅助工具

This work is licensed under  
1. [Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/)  
2. [GeoLite2 End User License Agreement](https://www.maxmind.com/en/geolite2/eula)

## how to use 用法
add 添加 maven 依赖
```shell script
  <dependency>
    <groupId>com.github.blankhang</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
  </dependency>
```
this dependency to your pom.xml
then just like this 然后就像这样
support both English and Chinese 支持返回中英文国家/省/州/市名

```java

        String countryName = IpUtil.getCountryName("183.22.183.2", null);
        // or  String countryName = IpUtil.getCountryName("183.22.183.2", "zh-CN");
        //中国
        System.out.println(countryName);
        countryName = IpUtil.getCountryName("183.22.183.2", "en");
        //China
        System.out.println(countryName);

        String stateName = IpUtil.getStateName("183.22.183.2", null);
        // or String stateName = IpUtil.getStateName("183.22.183.2", "zh-CN");
        //广东
        System.out.println(stateName);
        stateName = IpUtil.getStateName("183.22.183.2", "en");
        //Guangdong
        System.out.println(stateName);

        //Guangdong
        String cityName = IpUtil.getCityName("183.22.183.2", null);
        // or String cityName = IpUtil.getCityName("183.22.183.2", "zh-CN");
        //东莞市
        System.out.println(cityName);
        cityName = IpUtil.getCityName("183.22.183.2", "en");
        //Dongguan
        System.out.println(cityName);
```