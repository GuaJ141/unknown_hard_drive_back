package com.unknownharddrivesystem.mapper;

import com.unknownharddrivesystem.entity.HdPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


public interface HdPostMapper {
    List<HdPost> selectAll();

    List<HdPost> searchPost(String search);

    List<HdPost> searchMy(String search, String username);

    List<HdPost> selectMy(String username);

    int insertPost(HdPost hdPost);

    HdPost isTheFilePosted(int fileId);

    HdPost selectById(int id);

    int delPost(int id);

    HdPost isExist(int id);

    int delPostByUsername(String username);

}
