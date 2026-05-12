package com.unknownharddrivesystem.mapper;

import com.unknownharddrivesystem.entity.HdUser;


public interface HdUserMapper {
    HdUser verify(HdUser user);

    int register(HdUser user);

    int userUsedUpdate(long used, int id);

    HdUser selectUserById(int id);

    HdUser selectUserByUsername(String username);

    int userPswUpdate(int id, String password);

    int userdelete(int id);

    Integer selectLatestUId();
}
