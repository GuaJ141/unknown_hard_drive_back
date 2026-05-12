package com.unknownharddrivesystem;

import com.unknownharddrivesystem.entity.FriendAddMessage;
import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.FriendMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class UnknownharddrivesystemApplicationTests {

	@Autowired
	HdUserMapper hdUserMapper;

	@Autowired
	FriendMapper friendMapper;
	@Test
	void contextLoads() {
		FriendAddMessage friendAddMessage = new FriendAddMessage();
		friendAddMessage.setMessage("你还好吗");
		friendAddMessage.setSourceUserId(55);
		friendAddMessage.setToUserId(57);

		int res = friendMapper.sendMessage(friendAddMessage);
		System.out.println(res);
	}

}
