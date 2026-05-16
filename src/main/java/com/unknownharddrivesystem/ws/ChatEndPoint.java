package com.unknownharddrivesystem.ws;

import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.FriendMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.MessageJSON;
import com.unknownharddrivesystem.utils.SpringBeanUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.util.JSONPObject;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
public class ChatEndPoint {

    //存储每一个用户对应的ChatEndPoint 对象
    private static Map<Integer, ChatEndPoint> onlineUser = new ConcurrentHashMap<>();

    private final FriendMapper friendMapper = SpringBeanUtils.getBean(FriendMapper.class);
    private final HdUserMapper hdUserMapper = SpringBeanUtils.getBean(HdUserMapper.class);

    //sessiong对象，通过对象可以发送消息给指定用户
    private Session session;

    //httpSession对象，获取在httpSession存储的数据
    private HttpSession httpSession;

    //好友列表
    private List<HdUser> friendList = new ArrayList<>();

    //连接连接时调用 （用户在线）
    @OnOpen
    public void onOpen(Session session, EndpointConfig config){

        this.session = session;

        //获取httpsession对象
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        this.httpSession = httpSession;

        //获取session里的数据
        int userUid = (int) httpSession.getAttribute("uId");
        int userId = (int) httpSession.getAttribute("id");
        //System.out.println(((HdUser)httpSession.getAttribute("user")).getUsername());

        //查用户好友
        selectFriend(userId);
        System.out.println(userId);

        //将当前对象存储到容器中
        onlineUser.put(userUid, this);

        //创建Json字符串
        MessageJSON messageJSON = new MessageJSON(true, "user now live", getUId());
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(messageJSON);

        //将当前在线用户推送给自己的好友所有客户端
        broadCastAllUsers(message);


    }

    private void selectFriend(int userId){
        List<FriendList> friendIds = friendMapper.selectFriend(userId);
        for(FriendList friend: friendIds){
            HdUser friendInfo = hdUserMapper.selectUserById(friend.getFriendId());
            friendList.add(friendInfo);
        }
    }

    //上线就广播
    private void broadCastAllUsers(String message){
        if (friendList == null)return;

        try{
            Set<Integer> onlineUserUId = onlineUser.keySet();
            for(Integer uid: onlineUserUId){
                for (HdUser friend: friendList){

                    if (uid == friend.getUniqueId()){
                        ChatEndPoint chatEndPoint = onlineUser.get(uid);
                        chatEndPoint.session.getBasicRemote().sendText(message);
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private Set<Integer> getUId(){
        return onlineUser.keySet();
    }

    //接收到用户发送的数据时调用
    @OnMessage
    public void onMessage(String message, Session session){

    }

    //连接关闭时调用
    @OnClose
    public void onClose(Session session){

    }

}
