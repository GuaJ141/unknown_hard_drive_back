package com.unknownharddrivesystem.mapper;

import com.unknownharddrivesystem.entity.FriendAddMessage;
import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendMapper {
    int[] listFriends(int id);

    List<HdUser> searchUser(String input, int id);

    int sendMessage(FriendAddMessage message);

    FriendList isMyFriend(int masterId,int friendId);

    FriendAddMessage isSentAlready(int sourceUserId,int toUserId);

    List<FriendAddMessage> listMessageById(int userid);

    int deleMessageById(int id);

    int friendAddToList(int friendId, int masterId);

    int deleFriendById(int friendId, int masterId);

    List<FriendList> selectFriend(int msterId);

//    List<HdUser> selectFirendInfo(@Param("friendIds") List<FriendList> friendIds, String input);
}
