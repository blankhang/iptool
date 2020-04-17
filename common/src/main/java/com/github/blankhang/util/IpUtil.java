package com.github.blankhang.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONObject;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * IP工具类
 *
 * @author blank
 * @since 1.0.0
 */
@Slf4j
public class IpUtil {


    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String INTERNAL_NETWORK = "INTERNAL_NETWORK";

    private static final String GET_IP_INFO_API = "https://api.ip.sb/geoip/";

    private static final String ZH_CN = "zh-CN";

    private static DatabaseReader reader;

    static {
        InputStream classPathResource = FileUtil.getClassPathResource("GeoLite2-City.mmdb");
        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.
        try {
            reader = new DatabaseReader.Builder(classPathResource).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest请求
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    public static String getIpAddr(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals(LOCALHOST_IP)) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                assert inet != null;
                ip = inet.getHostAddress();
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    /**
     * 返回IP信息
     *
     * @param ip 请求IP
     * @return cn.hutool.json.JSONObject
     * @author blank
     * @since 1.0.0
     */
    public static JSONObject getIpInfo(String ip) {
        Assert.isTrue(StringUtils.isNotBlank(ip), "ip can not be null");
        return HttpUtil.getRequest(GET_IP_INFO_API + ip);
    }

    /**
     * 判断给定的 IP 是否为局域网 IP
     *
     * @param ipAddress ip
     * @return boolean
     * @author blank
     * @since 1.0.0
     */
    public static boolean isInnerIP(String ipAddress) {
        if (LOCALHOST.equals(ipAddress)) {
            return true;
        }
        boolean isInnerIp = false;
        long ipNum = getIpNum(ipAddress);
        /**
         私有IP：A类  10.0.0.0-10.255.255.255
         B类  172.16.0.0-172.31.255.255
         C类  192.168.0.0-192.168.255.255
         当然，还有127这个网段是环回地址
         **/
        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");
        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");
        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");
        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || ipAddress.equals(LOCALHOST_IP);
        return isInnerIp;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);

        return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * 返回浏览器信息
     *
     * @param request 请求
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    public static String getBrowser(HttpServletRequest request) {
        UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
        Browser browser = userAgent.getBrowser();
        return browser.getName() + " " + userAgent.getVersion();
    }


    /**
     * 返回城市名
     *
     * @param request  请求
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getCityName(HttpServletRequest request, String language) {

        return getCityName(getIpAddr(request), language);
    }

    /**
     * 返回城市名
     *
     * @param ipAddr   ip地址
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getCityName(String ipAddr, String language) {
        if (IpUtil.isInnerIP(ipAddr)) {
            return INTERNAL_NETWORK;
        }

        InetAddress ipAddress = InetAddress.getByName(ipAddr);

        CityResponse response = reader.city(ipAddress);
        return response.getCity().getNames().get(StringUtils.isBlank(language) ? ZH_CN : language);
    }

    /**
     * 返回州/省名
     *
     * @param request  请求
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getStateName(HttpServletRequest request, String language) {

        return getStateName(getIpAddr(request), language);
    }

    /**
     * 返回州/省中文名
     *
     * @param ipAddr   ip地址
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getStateName(String ipAddr, String language) {
        if (IpUtil.isInnerIP(ipAddr)) {
            return INTERNAL_NETWORK;
        }

        InetAddress ipAddress = InetAddress.getByName(ipAddr);

        CityResponse response = reader.city(ipAddress);
        return response.getMostSpecificSubdivision().getNames().get(StringUtils.isBlank(language) ? ZH_CN : language);
    }

    /**
     * 返回国家名
     *
     * @param request  请求
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getCountryName(HttpServletRequest request, String language) {

        return getCountryName(getIpAddr(request), language);
    }

    /**
     * 返回国家名
     *
     * @param ipAddr   ip地址
     * @param language 指定语言 不传 默认 zh-CN 中文
     * @return java.lang.String
     * @author blank
     * @since 1.0.0
     */
    @SneakyThrows
    public static String getCountryName(String ipAddr, String language) {
        if (IpUtil.isInnerIP(ipAddr)) {
            return INTERNAL_NETWORK;
        }

        InetAddress ipAddress = InetAddress.getByName(ipAddr);

        CityResponse response = reader.city(ipAddress);
        return response.getCountry().getNames().get(StringUtils.isBlank(language) ? ZH_CN : language);
    }


    public static void main(String[] args) throws IOException, GeoIp2Exception {

//        JSONObject ipInfo = IpInfoUtil.getIpInfo("183.111.234.23");
//        System.out.println(ipInfo);
        System.out.println(isInnerIP(LOCALHOST));
        System.out.println(isInnerIP("10.0.0.2"));
        System.out.println(isInnerIP("127.0.0.1"));

        // A File object pointing to your GeoIP2 or GeoLite2 database
//        File database = new File("d:/java/GeoLite2-City.mmdb");

        InputStream classPathResource = FileUtil.getClassPathResource("GeoLite2-City.mmdb");

        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.
        DatabaseReader reader = new DatabaseReader.Builder(classPathResource).build();

        InetAddress ipAddress = InetAddress.getByName("128.101.101.101");

        // Replace "city" with the appropriate method for your database, e.g.,
        // "country".
        CityResponse response = reader.city(ipAddress);

        Country country = response.getCountry();
        // 'US'
        System.out.println(country.getIsoCode());
        // 'United States'
        System.out.println(country.getName());
        // '美国'
        System.out.println(country.getNames().get("zh-CN"));

        Subdivision subdivision = response.getMostSpecificSubdivision();
        // 'Minnesota'
        System.out.println(subdivision.getName());
        System.out.println(subdivision.getNames().get("zh-CN"));
        // 'MN'
        System.out.println(subdivision.getIsoCode());

        City city = response.getCity();
        // 'Minneapolis'
        System.out.println(city.getName());

        System.out.println(city.getNames().get("zh-CN"));

        Postal postal = response.getPostal();
        // '55455'
        System.out.println(postal.getCode());

        Location location = response.getLocation();
        // 44.9733
        System.out.println(location.getLatitude());
        // -93.2323
        System.out.println(location.getLongitude());

    }

}
