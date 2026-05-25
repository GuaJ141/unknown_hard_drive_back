package com.unknownharddrivesystem;

import com.unknownharddrivesystem.UnknownharddrivesystemApplication;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnknownharddrivesystemApplicationTests {

	@Autowired
	RedisUtil redisUtil;
	@Autowired
	HdUserMapper userMapper;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	void contextLoads() {
		String key = "69";
		HdUser user = userMapper.selectUserById(69);

		System.out.println(redisUtil.get(key));

	}
}