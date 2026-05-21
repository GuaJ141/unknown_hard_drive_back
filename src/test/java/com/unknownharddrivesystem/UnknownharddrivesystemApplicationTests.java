package com.unknownharddrivesystem;

import com.unknownharddrivesystem.UnknownharddrivesystemApplication;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnknownharddrivesystemApplicationTests {

	@Autowired
	RedisUtil redisUtil;
	@Autowired
	HdUserMapper userMapper;

	@Test
	void contextLoads() {
		String key = "69";
		HdUser user = userMapper.selectUserById(69);
		redisUtil.set(key, user, 30, TimeUnit.SECONDS);

		ObjectMapper Jackson = new ObjectMapper();
		//HdUser temp = Jackson.readValue(redisUtil.get(key).toString(), HdUser.class);
		System.out.println(redisUtil.get(key).toString());

	}
}