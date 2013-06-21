/**
 * This is a client for the FBIUHH network control patch for Neverball 1.5.4.
 * This client is an android app, which uses the gyrosensor to control the game.
 * 
 * (C) 2013 Julian Stiller
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.flyinggecko.nb_android_remote;

import org.flyinggecko.nb_android_remote.helper.IPAddressValidator;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Startscreen extends Activity {
	Button _connectButton;
	EditText _connectionText;
	TextView _wrongIPText;
	ColorStateList _oldColors_IP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startscreen);
		_connectButton = (Button) findViewById(R.id.buttonConnect);
		_connectionText = (EditText) findViewById(R.id.fieldConnection);
		_wrongIPText = (TextView) findViewById(R.id.textWrongIP);
		 _oldColors_IP =  _wrongIPText.getTextColors();
		
		// This registers a listener for the click on the connection-button
		_connectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_wrongIPText.setText("");
				_wrongIPText.setTextColor(_oldColors_IP);
				buttonConnect_Click();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startscreen, menu);
		return true;
	}

	/**
	 * Gets invoked when the button in activity is clicked. This should invoke
	 * the connection to the Neverball-server and load the next activity (screen
	 * for playing). This function should read the ip-address.
	 * 
	 * It also checks the ip-address for validity
	 */
	public void buttonConnect_Click() {
		IPAddressValidator validator = new IPAddressValidator();
		String ip = _connectionText.getText().toString();
		if (validator.validate(ip)) {
			//TODO: Invoke connection!
		}
		else
		{
			_wrongIPText.setTextColor(Color.parseColor("#AF1111"));
			_wrongIPText.setText(R.string.noIP);
		}
	}

}
