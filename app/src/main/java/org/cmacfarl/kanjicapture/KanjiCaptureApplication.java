/*
 * Copyright 2021 Craig MacFarlane.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cmacfarl.kanjicapture;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class KanjiCaptureApplication extends Application
{
    private static Context context;

    public void onCreate() {
        super.onCreate();
        KanjiCaptureApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return KanjiCaptureApplication.context;
    }

    public static String getVersionName()
    {
        String version = "0.0.0";
        try {
            PackageInfo pInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

}
