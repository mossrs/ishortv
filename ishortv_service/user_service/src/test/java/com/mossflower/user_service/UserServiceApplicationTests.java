package com.mossflower.user_service;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        String bcrypt = DigestUtil.bcrypt("123456");
        System.out.println(bcrypt.length());
        boolean b = DigestUtil.bcryptCheck("123456", bcrypt);
        System.out.println(b);
    }

}
