package com.unknownharddrivesystem.ws;

import com.unknownharddrivesystem.entity.FriendList;
import com.unknownharddrivesystem.entity.HdUser;
import com.unknownharddrivesystem.mapper.FriendMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.SpringBeanUtil;
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

    private final FriendMapper friendMapper = SpringBeanUtil.getBean(FriendMapper.class);
    private final HdUserMapper hdUserMapper = SpringBeanUtil.getBean(HdUserMapper.class);

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
        ObjectMapper jackson = new ObjectMapper();
        SendOnLineUserMessage temp1 = new SendOnLineUserMessage(true, 1, myUId);
        String onlineMessage = jackson.writeValueAsString(temp1);
        System.out.println(onlineMessage);
        //告诉在线好友我在线了
        broadCastAllFriends(onlineMessage, myUId);

        SendOnLineUserMessage temp2 = new SendOnLineUserMessage(true, 2, getUId());
        String onlineFriends = jackson.writeValueAsString(temp2);
        //给自己获取在线好友
        this.session.getAsyncRemote().sendText(onlineFriends);

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
    private void broadCastAllFriends(String message, int myUId){
        if (friendList == null)return;
        Set<Integer> onlineUserUId = getUId();
        synchronized(this.session){
            for(Integer uid: onlineUserUId){
                //跳过我自己
                if(uid == myUId)continue;
                for (HdUser friend: friendList){
                    //给每个在线好友发
                    if (uid == friend.getUniqueId()){
                        ChatEndPoint chatEndPoint = onlineUser.get(uid);
                        //chatEndPoint.session.getAsyncRemote().sendText(message);
                        if(chatEndPoint != null && chatEndPoint.session != null && chatEndPoint.session.isOpen()){
                            try {
                                // 使用同步发送，设置超时时间
                                chatEndPoint.session.getBasicRemote().sendText(message);
                            } catch (Exception e) {
                                // 捕获状态异常，记录日志但不影响其他用户
                                System.err.println("Failed to send message to user " + uid + ": " + e.getMessage());
                            }
                        }
                        break;
                    }
                }
            }
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
                onlineUser.get(toUser).session.getAsyncRemote().sendText(sendMessage);
            }else{
                this.session.getAsyncRemote().sendText(ifUserNotOnline);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //连接关闭时调用
    @OnClose
    public void onClose(Session session){
        //获取用户uid
        Integer myUid = (Integer) httpSession.getAttribute("uId");
        //从在线用户中移除
        onlineUser.remove(myUid);
        System.out.println("用户断开连接："+ myUid);
        //格式化发送消息
        SendOnLineUserMessage sendOnLineUserMessage = new SendOnLineUserMessage(true, 0, myUid);
        ObjectMapper objectMapper = new ObjectMapper();
        //将消息转为JSON格式
        String onlineMessage = objectMapper.writeValueAsString(sendOnLineUserMessage);
        //发送消息
        broadCastAllFriends(onlineMessage, myUid);

    }

}
