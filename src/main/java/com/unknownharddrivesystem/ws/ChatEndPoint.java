package com.unknownharddrivesystem.ws;

import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.FriendMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.SpringBeanUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

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
        int myUId = (int) httpSession.getAttribute("uId");
        int myId = (int) httpSession.getAttribute("id");

        //查用户好友
        selectFriend(myId);

        //将当前对象存储到容器中
        onlineUser.put(myUId, this);

        //创建Json字符串
        SendOnLineUserMessage sendOnLineUserMessage = new SendOnLineUserMessage(true, getUId());
        ObjectMapper objectMapper = new ObjectMapper();
        String onlineMessage = objectMapper.writeValueAsString(sendOnLineUserMessage);

        //将当前在线用户推送给自己的好友所有客户端
        broadCastAllUsers(onlineMessage, myUId);

        System.out.println("用户连接：" + myUId);
    }

    private void selectFriend(int userId){
        List<FriendList> friendIds = friendMapper.selectFriend(userId);
        for(FriendList friend: friendIds){
            HdUser friendInfo = hdUserMapper.selectUserById(friend.getFriendId());
            friendList.add(friendInfo);
        }
    }

    //上线就广播
    private void broadCastAllUsers(String message, int myUId){
        if (friendList == null)return;
        try{
            Set<Integer> onlineUserUId = getUId();
            for(Integer uid: onlineUserUId){
                for (HdUser friend: friendList){
                    //给好友发，并且给自己发在线用户
                    if (uid == friend.getUniqueId() || uid == myUId){
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
    public Set<Integer> getUId(){
        return onlineUser.keySet();
    }

    //接收到用户发送的数据时调用
    @OnMessage
    public void onMessage(String message, Session session){
        try {
            ObjectMapper jackson = new ObjectMapper();
            System.out.println(message);
            //前端传来的message是JSON格式，先转成对象
            WebSocketMessage objectMessage = jackson.readValue(message, WebSocketMessage.class);

            //获取 给谁发 和 谁发的信息
            int toUser = objectMessage.getToUser();
            int fromUser = objectMessage.getFromUser();

            //转JSON格式
            WebSocketMessage tempResultMessage = new WebSocketMessage(false, objectMessage.getMessage(), toUser, fromUser);
            String sendMessage = jackson.writeValueAsString(tempResultMessage);

            //防止用户不在线，系统提示
            WebSocketMessage tempResultMessage2 = new WebSocketMessage(true, "用户不在线 :(", fromUser, -1);
            String ifUserNotOnline = jackson.writeValueAsString(tempResultMessage2);
            System.out.println("res: " + sendMessage);

            //判断用户在不在，
            ChatEndPoint isOnline = onlineUser.get(toUser);
            if(isOnline != null){
                onlineUser.get(toUser).session.getBasicRemote().sendText(sendMessage);
            }else{
                this.session.getBasicRemote().sendText(ifUserNotOnline);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //连接关闭时调用
    @OnClose
    public void onClose(Session session){

    }

}
