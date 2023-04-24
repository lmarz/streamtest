package org.lmarz.streamtest;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class DApplication extends Application {
	@Override
	public void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		Helper.install(this);
	}
}
