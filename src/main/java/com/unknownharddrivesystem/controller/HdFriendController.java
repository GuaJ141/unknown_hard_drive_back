package com.unknownharddrivesystem.controller;

import com.unknownharddrivesystem.entity.FriendAddMessage;
import com.unknownharddrivesystem.entity.FriendAddMessagePlus;
import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.FriendMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.ws.ChatEndPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Serial;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController //标识控制器
@RequestMapping("/friend") //控制器的前缀地址
public class HdFriendController {

    @Autowired
    FriendMapper friendMapper;
    @Autowired
    HdUserMapper userMapper;

    FriendList friendList;

    FriendAddMessage friendAddMessage;

    @GetMapping("/searchUser")
    public List<HdUser> searchUser(
            @RequestParam("input") String input,
            HttpServletRequest request){
        HttpSession session = request.getSession(false);
        HdUser user = (HdUser) session.getAttribute("user");

        List<HdUser> users = friendMapper.searchUser(input, user.getId());
        return users;
    }

    @PostMapping("/sendIt")
    public int sendMessage(
            @RequestParam("toUserId") int toUserId,
            @RequestParam("message") String message,
            HttpServletRequest request){
        // 0找不到sesison-用户id  1发送成功  2已经是好友  3发送过好友申请还未响应
        HttpSession session = request.getSession(false);
        int masterId = (int) session.getAttribute("id");


        //是不是已经是好友
        friendList = friendMapper.isMyFriend(masterId, toUserId);
        if (friendList != null){
            return 2;
        }

        //是不是已经加过好友但是还没响应
        friendAddMessage = friendMapper.isSentAlready(masterId, toUserId);
        if(friendAddMessage != null){
            return 3;
        }

        //处理request time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String Time = now.format(formatter);
        Timestamp timestamp = Timestamp.valueOf(Time);

        FriendAddMessage friendAddMessage = new FriendAddMessage();
        friendAddMessage.setMessage(message);
        friendAddMessage.setSourceUserId(masterId);
        friendAddMessage.setToUserId(toUserId);
        friendAddMessage.setRequestTime(timestamp);

        int res = friendMapper.sendMessage(friendAddMessage);
        return res;
    }


    @PostMapping("/selectUserMessage")
    public List<FriendAddMessagePlus> listMessage(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        int id = (int) session.getAttribute("id");

        //用用户id找消息
        List<FriendAddMessage> listMessage = friendMapper.listMessageById(id);
        List<FriendAddMessagePlus> res = new ArrayList<>();

        for (FriendAddMessage i: listMessage){
            FriendAddMessagePlus plus = new FriendAddMessagePlus();

            HdUser user = new HdUser();
            user = userMapper.selectUserById(i.getSourceUserId());

            //原
            plus.setId(i.getId());
            plus.setRequestTime(i.getRequestTime());
            plus.setMessage(i.getMessage());
            plus.setSourceUserId(i.getSourceUserId());
            //plus
            plus.setUsername(user.getUsername());
            plus.setUniqueId(user.getUniqueId());
            res.add(plus);
        }
        return res;
    }

    @GetMapping("/friendRefuse")
    public int friendRequestRefuse(@RequestParam("id") int id){
        return friendMapper.deleMessageById(id);
    }

    @Transactional
    @GetMapping("/friendAccep")
    public int friendRequestAccep(
            @RequestParam("messageId") int messageId,
            @RequestParam("friendId") int friendId,
            HttpServletRequest request){
        HttpSession session = request.getSession(false);
        int masterId = (int) session.getAttribute("id");

        //判断是否已经为好友
        friendList = friendMapper.isMyFriend(masterId, friendId);
        if (friendList != null){
            return 2;
        }

        int res = 0;
        //删除发送的消息
        res += friendMapper.deleMessageById(messageId);
        //给两边都加个好友
        res += friendMapper.friendAddToList(friendId, masterId);
        res += friendMapper.friendAddToList(masterId, friendId);
        if(res != 3){
            throw new RuntimeException("添加好友失败，数据回滚");
        }else{
            return 1;
        }

    }

    //胎死腹中的孩子啊，请原谅我
//    @PostMapping("/searchFriend")
//    public List<HdUser> searchFriend(
//            HttpServletRequest request,
//            @RequestParam("input")String input){
//        HttpSession session = request.getSession(false);
//        HdUser me = (HdUser) session.getAttribute("user");
//
//        List<FriendList> friendIdLists = friendMapper.selectFriend(me.getId());
//        List<HdUser> friendsInfo = friendMapper.selectFirendInfo(friendIdLists, input);
//
//        return friendsInfo;
//    }


    @PostMapping("/selectFriend")
    public List<HdUser> selectFriend(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        int userId = (int) session.getAttribute("id");

        List<FriendList> friendIdLists = friendMapper.selectFriend(userId);

        List<HdUser> resLists = new ArrayList<>();
        for(FriendList i : friendIdLists){
            HdUser friendInfo = userMapper.selectUserById(i.getFriendId());

            HdUser friendInfoAfterSelect = new HdUser();
            friendInfoAfterSelect.setUsername(friendInfo.getUsername());
            friendInfoAfterSelect.setUniqueId(friendInfo.getUniqueId());
            friendInfoAfterSelect.setId(friendInfo.getId());
            resLists.add(friendInfoAfterSelect);
        }
        return resLists;
    }

    @Transactional
    @GetMapping("/deleFriend")
    public int deleFriend(
            @RequestParam("friendId") int friendId,
            HttpServletRequest request){
        HttpSession session = request.getSession(false);
        int masterId = (int) session.getAttribute("id");


        int res = 0;
        //删除master用户的好友
        res += friendMapper.deleFriendById(friendId, masterId);
        //删除friend用户的master好友
        res += friendMapper.deleFriendById(masterId, friendId);
        if(res != 2){
            throw new RuntimeException("删除好友失败，数据回滚");
        }else{
            return 1;
        }

    }

    @GetMapping("/getOnlineFriend")
    public List<Integer> getOnlineFriend(HttpServletRequest request){
        //Request获取session 再获取session里存的id
        HttpSession session = request.getSession(false);
        int myUserId = (int)session.getAttribute("id");

        ChatEndPoint ws = new ChatEndPoint();
        Set<Integer> onlineUser = ws.getUId();

        //我的id查好友id
        List<FriendList> myFriends= friendMapper.selectFriend(myUserId);

        //存好友uid
        List<Integer> FriendLists = new ArrayList<>();
        for(FriendList i: myFriends){
            //好友id查好友信息
            HdUser friend = userMapper.selectUserById(i.getFriendId());
            //把好友uid加到FriendLists
            FriendLists.add(friend.getUniqueId());
        }

        //用来筛选在线好友
        List<Integer> onlineFriendLists = new ArrayList<>();
        for(Integer onlineFriendUid:onlineUser){
            //在线用户有好友就加到onlineFriendLists
            if(FriendLists.contains(onlineFriendUid)){
                onlineFriendLists.add(onlineFriendUid);
            }
        }

        return onlineFriendLists;
    }
}
