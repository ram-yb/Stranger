package com.silence.im.service;

import com.silence.im.service.Contact;
interface IXmppManager {

        /**XMPP连接*/
        boolean connect();

        /**登录*/
        boolean login();

        /**断开连接*/
        boolean disconnect();
        
        /**注销*/
        void logout();
        
        /**检测连接状态*/
        boolean isConnected();
        
        /**检测登录状态*/
        boolean isLogin();

        /**发送信息*/
        void sendMessage(String sessionJID, String sessionName, String message, String type);

        /**设置好友备注*/
        boolean setRosterEntryName(String JID, String name);

        /**设置个人名片，个人信息*/
        boolean setVCard(in Contact contact);

        /**搜索陌生人*/
        String[] searchAccount(String accountName,boolean stranger);

        /**获取某人名片*/
        Contact getVCard(String JID);
        
        /**添加好友*/
        int addGroupFriend(String group, String friendJid,String name_by_me);
        
        /**删除好友*/
        boolean removeFriend(String friendJid);
        
        /**发送语音或图片 */
       boolean sendBase64File(String sessionJID, String sessionName,String filename, String filePath, String type);
       
       /**HTTP发送文件 ，不需要同意*/
       void sendFileByHTTPNoRequest(String sessionID,String sessionName, String pathStr,String filename, String type);
       
       /**HTTP发送文件，需要经过同意*/
       void sendFileByHTTPNeedRequest(String sessionID, String pathStr,String filename, String type);
       
       /**修改在线状态*/
       boolean setOnlineStatus(int status);
       
       /**上传位置信息*/
       String[] uploadLocation(String longitude, String latitude);
       
       /**找回密码*/
       int findPasswordByEmail(String accountString, String emailString,String code,boolean type);
       
       /**修改密码*/
       boolean changePassword(String password);
       
       /**上传保存密码*/
       boolean savePassword(String accountString, String passwordString,String emailString, boolean type);
}