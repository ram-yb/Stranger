/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Java\\Android\\项目开发\\XMPP-IM\\中国软件杯项目文件\\光盘\\客户端\\解密陌生人源码\\src\\com\\silence\\im\\service\\IXmppManager.aidl
 */
package com.silence.im.service;
public interface IXmppManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.silence.im.service.IXmppManager
{
private static final java.lang.String DESCRIPTOR = "com.silence.im.service.IXmppManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.silence.im.service.IXmppManager interface,
 * generating a proxy if needed.
 */
public static com.silence.im.service.IXmppManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.silence.im.service.IXmppManager))) {
return ((com.silence.im.service.IXmppManager)iin);
}
return new com.silence.im.service.IXmppManager.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_connect:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.connect();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_login:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.login();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_disconnect:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.disconnect();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_logout:
{
data.enforceInterface(DESCRIPTOR);
this.logout();
reply.writeNoException();
return true;
}
case TRANSACTION_isConnected:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isConnected();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isLogin:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isLogin();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_sendMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
this.sendMessage(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_setRosterEntryName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.setRosterEntryName(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setVCard:
{
data.enforceInterface(DESCRIPTOR);
com.silence.im.service.Contact _arg0;
if ((0!=data.readInt())) {
_arg0 = com.silence.im.service.Contact.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.setVCard(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_searchAccount:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
java.lang.String[] _result = this.searchAccount(_arg0, _arg1);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getVCard:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.silence.im.service.Contact _result = this.getVCard(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_addGroupFriend:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _result = this.addGroupFriend(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_removeFriend:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.removeFriend(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_sendBase64File:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
boolean _result = this.sendBase64File(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_sendFileByHTTPNoRequest:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
this.sendFileByHTTPNoRequest(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_sendFileByHTTPNeedRequest:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
this.sendFileByHTTPNeedRequest(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnlineStatus:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.setOnlineStatus(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_uploadLocation:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String[] _result = this.uploadLocation(_arg0, _arg1);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_findPasswordByEmail:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _arg3;
_arg3 = (0!=data.readInt());
int _result = this.findPasswordByEmail(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_changePassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.changePassword(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_savePassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _arg3;
_arg3 = (0!=data.readInt());
boolean _result = this.savePassword(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.silence.im.service.IXmppManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**XMPP连接*/
@Override public boolean connect() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**登录*/
@Override public boolean login() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_login, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**断开连接*/
@Override public boolean disconnect() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**注销*/
@Override public void logout() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_logout, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**检测连接状态*/
@Override public boolean isConnected() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isConnected, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**检测登录状态*/
@Override public boolean isLogin() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isLogin, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**发送信息*/
@Override public void sendMessage(java.lang.String sessionJID, java.lang.String sessionName, java.lang.String message, java.lang.String type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionJID);
_data.writeString(sessionName);
_data.writeString(message);
_data.writeString(type);
mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**设置好友备注*/
@Override public boolean setRosterEntryName(java.lang.String JID, java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(JID);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_setRosterEntryName, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**设置个人名片，个人信息*/
@Override public boolean setVCard(com.silence.im.service.Contact contact) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((contact!=null)) {
_data.writeInt(1);
contact.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setVCard, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**搜索陌生人*/
@Override public java.lang.String[] searchAccount(java.lang.String accountName, boolean stranger) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(accountName);
_data.writeInt(((stranger)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_searchAccount, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**获取某人名片*/
@Override public com.silence.im.service.Contact getVCard(java.lang.String JID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.silence.im.service.Contact _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(JID);
mRemote.transact(Stub.TRANSACTION_getVCard, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.silence.im.service.Contact.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**添加好友*/
@Override public int addGroupFriend(java.lang.String group, java.lang.String friendJid, java.lang.String name_by_me) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(group);
_data.writeString(friendJid);
_data.writeString(name_by_me);
mRemote.transact(Stub.TRANSACTION_addGroupFriend, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**删除好友*/
@Override public boolean removeFriend(java.lang.String friendJid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(friendJid);
mRemote.transact(Stub.TRANSACTION_removeFriend, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**发送语音或图片 */
@Override public boolean sendBase64File(java.lang.String sessionJID, java.lang.String sessionName, java.lang.String filename, java.lang.String filePath, java.lang.String type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionJID);
_data.writeString(sessionName);
_data.writeString(filename);
_data.writeString(filePath);
_data.writeString(type);
mRemote.transact(Stub.TRANSACTION_sendBase64File, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**HTTP发送文件 ，不需要同意*/
@Override public void sendFileByHTTPNoRequest(java.lang.String sessionID, java.lang.String sessionName, java.lang.String pathStr, java.lang.String filename, java.lang.String type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionID);
_data.writeString(sessionName);
_data.writeString(pathStr);
_data.writeString(filename);
_data.writeString(type);
mRemote.transact(Stub.TRANSACTION_sendFileByHTTPNoRequest, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**HTTP发送文件，需要经过同意*/
@Override public void sendFileByHTTPNeedRequest(java.lang.String sessionID, java.lang.String pathStr, java.lang.String filename, java.lang.String type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionID);
_data.writeString(pathStr);
_data.writeString(filename);
_data.writeString(type);
mRemote.transact(Stub.TRANSACTION_sendFileByHTTPNeedRequest, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**修改在线状态*/
@Override public boolean setOnlineStatus(int status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(status);
mRemote.transact(Stub.TRANSACTION_setOnlineStatus, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**上传位置信息*/
@Override public java.lang.String[] uploadLocation(java.lang.String longitude, java.lang.String latitude) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(longitude);
_data.writeString(latitude);
mRemote.transact(Stub.TRANSACTION_uploadLocation, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**找回密码*/
@Override public int findPasswordByEmail(java.lang.String accountString, java.lang.String emailString, java.lang.String code, boolean type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(accountString);
_data.writeString(emailString);
_data.writeString(code);
_data.writeInt(((type)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_findPasswordByEmail, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**修改密码*/
@Override public boolean changePassword(java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_changePassword, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**上传保存密码*/
@Override public boolean savePassword(java.lang.String accountString, java.lang.String passwordString, java.lang.String emailString, boolean type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(accountString);
_data.writeString(passwordString);
_data.writeString(emailString);
_data.writeInt(((type)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_savePassword, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_connect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_login = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_logout = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_isConnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isLogin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setRosterEntryName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_setVCard = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_searchAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getVCard = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_addGroupFriend = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_removeFriend = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_sendBase64File = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_sendFileByHTTPNoRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_sendFileByHTTPNeedRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_setOnlineStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_uploadLocation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_findPasswordByEmail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_changePassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_savePassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
}
/**XMPP连接*/
public boolean connect() throws android.os.RemoteException;
/**登录*/
public boolean login() throws android.os.RemoteException;
/**断开连接*/
public boolean disconnect() throws android.os.RemoteException;
/**注销*/
public void logout() throws android.os.RemoteException;
/**检测连接状态*/
public boolean isConnected() throws android.os.RemoteException;
/**检测登录状态*/
public boolean isLogin() throws android.os.RemoteException;
/**发送信息*/
public void sendMessage(java.lang.String sessionJID, java.lang.String sessionName, java.lang.String message, java.lang.String type) throws android.os.RemoteException;
/**设置好友备注*/
public boolean setRosterEntryName(java.lang.String JID, java.lang.String name) throws android.os.RemoteException;
/**设置个人名片，个人信息*/
public boolean setVCard(com.silence.im.service.Contact contact) throws android.os.RemoteException;
/**搜索陌生人*/
public java.lang.String[] searchAccount(java.lang.String accountName, boolean stranger) throws android.os.RemoteException;
/**获取某人名片*/
public com.silence.im.service.Contact getVCard(java.lang.String JID) throws android.os.RemoteException;
/**添加好友*/
public int addGroupFriend(java.lang.String group, java.lang.String friendJid, java.lang.String name_by_me) throws android.os.RemoteException;
/**删除好友*/
public boolean removeFriend(java.lang.String friendJid) throws android.os.RemoteException;
/**发送语音或图片 */
public boolean sendBase64File(java.lang.String sessionJID, java.lang.String sessionName, java.lang.String filename, java.lang.String filePath, java.lang.String type) throws android.os.RemoteException;
/**HTTP发送文件 ，不需要同意*/
public void sendFileByHTTPNoRequest(java.lang.String sessionID, java.lang.String sessionName, java.lang.String pathStr, java.lang.String filename, java.lang.String type) throws android.os.RemoteException;
/**HTTP发送文件，需要经过同意*/
public void sendFileByHTTPNeedRequest(java.lang.String sessionID, java.lang.String pathStr, java.lang.String filename, java.lang.String type) throws android.os.RemoteException;
/**修改在线状态*/
public boolean setOnlineStatus(int status) throws android.os.RemoteException;
/**上传位置信息*/
public java.lang.String[] uploadLocation(java.lang.String longitude, java.lang.String latitude) throws android.os.RemoteException;
/**找回密码*/
public int findPasswordByEmail(java.lang.String accountString, java.lang.String emailString, java.lang.String code, boolean type) throws android.os.RemoteException;
/**修改密码*/
public boolean changePassword(java.lang.String password) throws android.os.RemoteException;
/**上传保存密码*/
public boolean savePassword(java.lang.String accountString, java.lang.String passwordString, java.lang.String emailString, boolean type) throws android.os.RemoteException;
}
