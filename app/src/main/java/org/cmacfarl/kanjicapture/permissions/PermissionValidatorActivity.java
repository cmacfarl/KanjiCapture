/*
 * Copyright (c) 2018 Craig MacFarlane
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Craig MacFarlane nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.cmacfarl.kanjicapture.permissions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.cmacfarl.kanjicapture.KanjiCaptureApplication;
import org.cmacfarl.kanjicapture.R;
import org.cmacfarl.kanjicapture.util.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * This class sits between app launch and FtcRobotControllerActivity acting as a gate
 * on launch of the latter pending acceptance of all required permissions Google has categorized
 * as "dangerous".
 *
 * This is part of Google's push for runtime permissions and is required for further updates to
 * the play store as of Nov 2018.  Google would like applications to gracefully cripple themselves when a runtime
 * permission is revoked.  This however does not work well with our use model of a phone as a
 * robot controller.  Hence we force require all permissions to be granted up front.  If a permission
 * is later revoked, we force it to be granted again.
 *
 * If any permission is denied with the checkbox "Do not ask again" enabled, then the robot controller
 * refuses to run and displays a message to user explaining how to fix the problem.
 */
public abstract class PermissionValidatorActivity extends Activity {

    public final static String PERMS_VALID_KEY = "org.cmacfarl.barcodescanner.permissions.PERMS_VALID_KEY";

    private final String TAG = "PermissionValidatorActivity";
    private PermissionValidator permissionValidator;
    private TextView permDenied;
    private TextView instructions;

    /*
     * The list of dangerous permissions the robot controller needs.
     */
    protected List<String> permissions;

    List<String> permanentDenials = new ArrayList<String>();

    protected abstract Class onStartApplication();
    public abstract String mapPermissionToExplanation(String permisssion);

    private class PermissionListenerImpl implements PermissionListener {

        @Override
        public void onPermissionDenied(String permission)
        {
            permissionValidator.explain(permissions.get(0));
        }

        @Override
        public void onPermissionGranted(String permission)
        {
            permissions.remove(permission);
            if (permissions.isEmpty()) {
                startRobotController();
            } else {
                permissionValidator.checkPermission(permissions.get(0));
            }
        }

        @Override
        public void onPermissionPermanentlyDenied(String permission)
        {
            /*
             * Display a message stating that the robot controller can not run.
             */
            Log.e(TAG, "Permission permanently denied for " + permission);
            Log.e(TAG, "Robot Controller will not run");

            instructions.setText(String.format(Misc.formatForUser(R.string.permPermanentlyDeniedRecovery), "Camera"));
            instructions.setVisibility(View.VISIBLE);

            instructions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        permissionValidator.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void startRobotController()
    {
        if (!permanentDenials.isEmpty()) {
            permDenied.setVisibility(View.VISIBLE);
            instructions.setVisibility(View.VISIBLE);
        } else {
            Log.i(TAG, "All permissions validated.  Starting BarcodeScanner");
            Intent robotControllerIntent = new Intent(KanjiCaptureApplication.getAppContext(), onStartApplication());
            startActivity(robotControllerIntent);
            finish();
        }
    }

    protected void enforcePermissions()
    {
        if (permissions.isEmpty()) {
            startRobotController();
        } else {
            permissionValidator.checkPermission(permissions.get(0));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_permissions_validator);

        permDenied = findViewById(R.id.permDeniedText);
        permDenied.setVisibility(View.INVISIBLE);
        instructions = findViewById(R.id.explanationText);
        instructions.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart");

        permissionValidator = new PermissionValidator(this, new PermissionListenerImpl());
        enforcePermissions();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume");
    }
}
