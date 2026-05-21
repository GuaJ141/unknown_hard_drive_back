package com.unknownharddrivesystem.controller;

import com.sun.tools.javac.Main;
import com.unknownharddrivesystem.entity.HdComment;
import com.unknownharddrivesystem.entity.HdFile;
import com.unknownharddrivesystem.entity.HdPost;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.HdCommentMapper;
import com.unknownharddrivesystem.mapper.HdFileMapper;
import com.unknownharddrivesystem.mapper.HdPostMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import jakarta.servlet.http.*;
import jakarta.websocket.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.tomcat.websocket.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController //标识控制器
@RequestMapping("/hduser") //控制器的前缀地址
public class HdUserController {
    @Autowired
    HdUserMapper hdUserMapper;
    @Autowired
    HdFileMapper hdFileMapper;
    @Autowired
    HdPostMapper hdPostMapper;
    @Autowired
    HdCommentMapper hdCommentMapper;

    private final String MainPath = System.getProperty("user.dir") + "\\" + "UserFiles";

    @PostMapping("/login") //请求的地址，发送get请求 /user/login
    public HdUser verify(
            HttpServletRequest request,
            @RequestParam Map params) {

        //获取前端发送的用户名和密码
        String username = params.get("username").toString();
        String password = params.get("password").toString();
        //调用数据接口进行查询
        HdUser user = new HdUser();
        user.setUsername(username);
        user.setPassword(password);
        HdUser res = hdUserMapper.verify(user);


        if(res != null){
            HttpSession session = request.getSession(true);
            session.setAttribute("uId", res.getUniqueId());
            session.setAttribute("id", res.getId());
            session.setAttribute("user", res);
        }

        //System.out.println("uId： " + session.getAttribute("uId"));
        //System.out.println("id： " + session.getAttribute("id"));
        //System.out.println("uId： " + session.getAttribute("user"));

        //返回查询的用户实体数据给前端
        return res;
    }

    @PostMapping("/isLoggedIn")
    public int isLoggedIn(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        //System.out.println("session: " + session);
        if (session == null){
            return 0;
        }else{
            return 1;
        }
    }

    @PostMapping("/getUserInfo")
    public HdUser getUserBySession(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        //System.out.println("session: " + session);
        if(session != null){
            int id = (int) session.getAttribute("id");
            HdUser newInfo = hdUserMapper.selectUserById(id);
            session.setAttribute("user", newInfo);
            return newInfo;
        }else{
            return null;
        }
    }

    @PostMapping("/quit")
    public void userLogout(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        session.invalidate();
    }

    @Transactional
    @PostMapping("/register")
    public int register(@RequestParam Map params){
        String username = params.get("username").toString();
        String password = params.get("password").toString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String Time = now.format(formatter);

        //判断有没有人注册，没人注册用1
        Integer uId = hdUserMapper.selectLatestUId();
        uId = (uId == null)? 1 : uId + 1;

        HdUser user = new HdUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreateTime(Time);
        user.setSpace(1073741824);
        user.setUniqueId(uId);

        String token = UUID.randomUUID().toString().replace("-", "");
        user.setFileAccessToken(token);

        //判断UserFile文件是否存在
        File userFiles = new File(MainPath);
        if(!userFiles.exists()){
            userFiles.mkdir();
        }

        File folder = new File(MainPath + "\\" + username);
        boolean create = folder.mkdir();
        int res = hdUserMapper.register(user);

        if (!create)throw new RuntimeException("已有存在相同用户名文件夹，数据库数据回滚");

        if (create && res == 1){
            return res;
        }
        return 0;
    }

    @PostMapping("/select")
    public HdUser selectUser(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        int id = (int) session.getAttribute("id");
        HdUser user = hdUserMapper.selectUserById(id);

        return user;
    }

    @PostMapping("/resetpassword")
    public int resetpassword(
        @RequestParam("newpassword") String newpassword,
        HttpServletRequest request){
        HttpSession session = request.getSession(false);
        HdUser user = (HdUser)session.getAttribute("user");
        int res = hdUserMapper.userPswUpdate(user.getId(), newpassword);
        //System.out.println(res);
        return res;
    }

    @PostMapping("/accountdelete")
    public int accountdelete(HttpServletRequest request){

        HttpSession session = request.getSession(false);
        HdUser user = (HdUser)session.getAttribute("user");
        boolean tag = true;
        File folder = new File(MainPath + "\\" + user.getUsername());
        HdUploadController hdUploadController = new HdUploadController();

        System.out.println(folder.getPath());
        if (hdUploadController.fileDelete(folder)){
            List<HdFile> file  = hdFileMapper.selectFileByPathLike(user.getUsername(), user.getUsername());
            if(!file.isEmpty()){
                if (!(hdFileMapper.deleteFileByUsername(user.getUsername()) > 0)){
                    tag = false;
                }
            }

            if (!(hdUserMapper.userdelete(user.getId()) > 0)){
                tag = false;
            }

            //文章&评论删除
            List<HdPost> posts = hdPostMapper.selectMy(user.getUsername());
            //System.out.println(posts != null);
            List<HdComment> comments;
            if (posts != null && !posts.isEmpty()){
                comments = hdCommentMapper.selectComment(posts.get(0).getId());
                for (HdPost i : posts) {
                    if (!(hdPostMapper.delPost(i.getId()) > 0)){
                        tag = false;
                    }
                }

                for (HdComment j: comments){
                    if (!(hdCommentMapper.delComment(j.getId()) > 0)){
                        tag = false;
                    }
                }
            }

        }else{
            tag = false;
        }
        if(tag){
            session.invalidate();
            return 1;
        }else{
            return 0;
        }
    }
}
