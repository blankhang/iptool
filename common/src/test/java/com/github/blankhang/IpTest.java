package com.github.blankhang;

import cn.hutool.json.JSONObject;
import com.github.blankhang.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class IpTest {

    @Test
    public void TestIp(){

        JSONObject ipInfo = IpUtil.getIpInfo("230.233.200.30");
        log.info(ipInfo.toString());
    }
}
