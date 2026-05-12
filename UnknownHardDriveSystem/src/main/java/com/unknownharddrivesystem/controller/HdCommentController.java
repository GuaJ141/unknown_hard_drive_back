package com.unknownharddrivesystem.controller;

import com.unknownharddrivesystem.entity.HdComment;
import com.unknownharddrivesystem.entity.HdPost;
import com.unknownharddrivesystem.mapper.HdCommentMapper;
import com.unknownharddrivesystem.mapper.HdFileMapper;
import com.unknownharddrivesystem.mapper.HdPostMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController //标识控制器
@RequestMapping("/comment") //控制器的前缀地址
public class HdCommentController {

    @Autowired
    HdCommentMapper hdCommentMapper;
    @Autowired
    HdPostMapper hdPostMapper;

    @PostMapping("/selectcomment")
    public List<HdComment> selectcomment(@RequestParam("postId") int id){
        List<HdComment> comments = hdCommentMapper.selectComment(id);
        return comments;
    }

    @PostMapping("/comment")
    public int comment(
            @RequestParam("postId") int id,
            @RequestParam("username") String username,
            @RequestParam("comment") String comment){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String Time = now.format(formatter);
        Timestamp timestamp = Timestamp.valueOf(Time);

        HdComment userComment = new HdComment();
        userComment.setCommentTime(timestamp);
        userComment.setComment(comment);
        userComment.setUsername(username);
        userComment.setPostId(id);

        int res = hdCommentMapper.Comment(userComment);
        return res;
    }

    @PostMapping("/delcomment")
    public int delcomment(@RequestParam("id") int id){
        int res = hdCommentMapper.delComment(id);
        return res;
    }
}
