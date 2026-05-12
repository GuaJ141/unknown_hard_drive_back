package com.unknownharddrivesystem.mapper;


import com.unknownharddrivesystem.entity.HdComment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


public interface HdCommentMapper {

    List<HdComment> selectComment(int postId);

    int Comment(HdComment comment);

    int delComment(int commentId);
}
