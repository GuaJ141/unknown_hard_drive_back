package com.unknownharddrivesystem.controller;

import com.unknownharddrivesystem.entity.HdComment;
import com.unknownharddrivesystem.entity.HdPost;
import com.unknownharddrivesystem.mapper.HdCommentMapper;
import com.unknownharddrivesystem.mapper.HdFileMapper;
import com.unknownharddrivesystem.mapper.HdPostMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController //标识控制器
@RequestMapping("/post") //控制器的前缀地址
public class HdPostController {
    @Autowired
    HdFileMapper hdFileMapper;
    @Autowired
    HdUserMapper hdUserMapper;
    @Autowired
    HdPostMapper hdPostMapper;
    @Autowired
    HdCommentMapper hdCommentMapper;

    @PostMapping("/selectall")
    public List<HdPost> selectAll(){
        List<HdPost> post = hdPostMapper.selectAll();
        return post;
    }

    @PostMapping("/selectone")
    public HdPost selectone(@RequestParam("postId") int id){
        HdPost post = hdPostMapper.selectById(id);
        return post;
    }

    @PostMapping("/search")
    public List<HdPost> search(@RequestParam("search") String search){
        List<HdPost> post = hdPostMapper.searchPost(search);
        return post;
    }

    @PostMapping("/insert")
    public int insert(
            @RequestParam("username") String username,
            @RequestParam("title") String title,
            @RequestParam("describe") String describe,
            @RequestParam("fileid") int id){

        HdPost isNotPost = hdPostMapper.isTheFilePosted(id);
        if (isNotPost == null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String Time = now.format(formatter);
            Timestamp timestamp = Timestamp.valueOf(Time);

            HdPost post = new HdPost();
            post.setUsername(username);
            post.setTitle(title);
            post.setDescribe(describe);
            post.setFileId(id);
            post.setUploadTime(timestamp);

            int res = hdPostMapper.insertPost(post);
            return res;
        }

        return 0;
    }

    @PostMapping("/selectmy")
    public List<HdPost> selectmy(@RequestParam("username") String username){
        List<HdPost> post = hdPostMapper.selectMy(username);
        return post;
    }

    @PostMapping("/searchmy")
    public List<HdPost> searchmy(
            @RequestParam("search") String search,
            @RequestParam("username") String username){
        List<HdPost> post = hdPostMapper.searchMy(search, username);
        return post;
    }

    @PostMapping("/delpost")
    public int delpost(@RequestParam("id") int id){
        boolean tag = true;
        if ( !(hdPostMapper.delPost(id) > 0)){
            tag = false;
        }

        //评论删除
        List<HdComment> comments = hdCommentMapper.selectComment(id);
        for (HdComment i: comments){
            if (!(hdCommentMapper.delComment(i.getId()) > 0)){
                tag = false;
            }
        }
        return tag? 1 : 0;
    }

    @PostMapping("/isexist")
    public int isexist(@RequestParam("id") int id){
        HdPost post = hdPostMapper.isExist(id);
        if (post != null){
            return 1;
        }else{
            return 0;
        }
    }
}
