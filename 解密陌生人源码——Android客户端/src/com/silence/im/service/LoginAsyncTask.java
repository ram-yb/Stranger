package com.silence.im.service;

import android.os.AsyncTask;
import android.os.RemoteException;

/**
 * ��½����
 * 
 * @author JerSuen
 */
public class LoginAsyncTask extends AsyncTask<IXmppManager, Void, Integer> {
	public static final int LOGIN_OK = 1;
	public static final int LOGIN_ERROR = 2;
	public static final int CONNECTION_ERROR = 3;

	protected Integer doInBackground(IXmppManager... xmppBinders) {
		try {
			IXmppManager connection = xmppBinders[0];
			// ���ӳɹ�
			if (connection.connect()) {
				// ��½�ɹ�
				if (connection.login()) {
					return LOGIN_OK;
					// ��½ʧ��
				} else {
					return LOGIN_ERROR;
				}
				// ����ʧ��
			} else {
				return CONNECTION_ERROR;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
}