package org.flyinggecko.nb_android_remote.helper;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.io.OutputStream;

import org.flyinggecko.nb_android_remote.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;

public class ConnectionHolder {

	public static Connection _connection;

	private static String _hostaddress;

	public static void setHost(String ip) {
		_hostaddress = ip;
	}

	public static boolean getConnection(String ip) {
		_hostaddress = ip;
		if (_connection == null)
			return initialize(ip);
		return true;
	}

	private static boolean initialize(String ip) {
		Object[] params = new Object[2];
		params[0] = ip;
		_connection = (Connection) new Connection().execute(params);
		try {
			return _connection.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void write(short value) {
		_connection.write_to_output(value);
	}

	public static void flush() {
		_connection.flush_output();
	}
}

class Connection extends AsyncTask<Object, Void, Boolean> {
	private String _Host;
	private int _Port;
	private OutputStream _Output;
	private Socket _echoSocket;
	
	public Connection() {

	}

	public void write_to_output(short value) {
		if (_Output != null)
		{
			try {
				_Output.write(value);
				_Output.write(value >> 8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	public void flush_output() {
		try {
			_Output.flush();
		} catch (IOException e) {
			// nothing to do
		}
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// Activity activity = (Activity) params[1];
		_Host = (String) params[0];
		_Port = 33333;
		_echoSocket = null;
		OutputStream out = null;

		try {
			SocketAddress sockaddr = new InetSocketAddress(_Host, _Port);
			_echoSocket = new Socket();
			_echoSocket.connect(sockaddr, 5000);
			if (_echoSocket.isConnected()) {
				out = _echoSocket.getOutputStream();
			}
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		_Output = out;
		return true;
	}
}