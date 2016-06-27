package com.silence.im.service;

import com.silence.im.service.Contact;
interface IXmppManager {

        /**XMPP����*/
        boolean connect();

        /**��¼*/
        boolean login();

        /**�Ͽ�����*/
        boolean disconnect();
        
        /**ע��*/
        void logout();
        
        /**�������״̬*/
        boolean isConnected();
        
        /**����¼״̬*/
        boolean isLogin();

        /**������Ϣ*/
        void sendMessage(String sessionJID, String sessionName, String message, String type);

        /**���ú��ѱ�ע*/
        boolean setRosterEntryName(String JID, String name);

        /**���ø�����Ƭ��������Ϣ*/
        boolean setVCard(in Contact contact);

        /**����İ����*/
        String[] searchAccount(String accountName,boolean stranger);

        /**��ȡĳ����Ƭ*/
        Contact getVCard(String JID);
        
        /**��Ӻ���*/
        int addGroupFriend(String group, String friendJid,String name_by_me);
        
        /**ɾ������*/
        boolean removeFriend(String friendJid);
        
        /**����������ͼƬ */
       boolean sendBase64File(String sessionJID, String sessionName,String filename, String filePath, String type);
       
       /**HTTP�����ļ� ������Ҫͬ��*/
       void sendFileByHTTPNoRequest(String sessionID,String sessionName, String pathStr,String filename, String type);
       
       /**HTTP�����ļ�����Ҫ����ͬ��*/
       void sendFileByHTTPNeedRequest(String sessionID, String pathStr,String filename, String type);
       
       /**�޸�����״̬*/
       boolean setOnlineStatus(int status);
       
       /**�ϴ�λ����Ϣ*/
       String[] uploadLocation(String longitude, String latitude);
       
       /**�һ�����*/
       int findPasswordByEmail(String accountString, String emailString,String code,boolean type);
       
       /**�޸�����*/
       boolean changePassword(String password);
       
       /**�ϴ���������*/
       boolean savePassword(String accountString, String passwordString,String emailString, boolean type);
}