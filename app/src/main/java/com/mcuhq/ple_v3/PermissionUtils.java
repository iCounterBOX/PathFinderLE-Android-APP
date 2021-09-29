package com.mcuhq.ple_v3;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import org.altbeacon.beacon.BeaconManager;

/*
https://gist.github.com/mtsahakis/bd54dc595f8cf170eb1bce17f31722b5

endlich kann ich diese permission request in eine klasse packen


     * PERMISSION CHECK
     *  https://stackoverflow.com/questions/59192780/android-studio-request-location-permission-popup
     *  https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime

 */

public class PermissionUtils {
    public static boolean useRunTimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean hasPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestPermissions(Activity activity, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission, requestCode);
        }
    }

    // This is for Bluetooth
    public static void requestPermissionsBT(Activity activity) {
        if (useRunTimePermissions()) {
            activity.requestPermissions( new String[]{Manifest.permission.BLUETOOTH}, PackageManager.PERMISSION_GRANTED);
            activity.requestPermissions( new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PackageManager.PERMISSION_GRANTED);
            activity.requestPermissions( new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }
    }


    public static boolean shouldShowRational(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public static boolean shouldAskForPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission));
        }
        return false;
    }

    public static void goToAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean hasAskedForPermission(Activity activity, String permission) {
        return PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getBoolean(permission, false);
    }

    public static void markedPermissionAsAsked(Activity activity, String permission) {
        PreferenceManager
                .getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(permission, true)
                .apply();
    }

    // permission related ALERTS - speziell für BT, BLE, Location

    public static boolean hasRequiredPermissions(final Activity activity) {
        boolean hasBluetoothPermission = PermissionUtils.hasPermission(activity, Manifest.permission.BLUETOOTH);
        boolean hasBluetoothAdminPermission = PermissionUtils.hasPermission(activity, Manifest.permission.BLUETOOTH_ADMIN);
        boolean hasLocationPermission = PermissionUtils.hasPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        return hasBluetoothPermission && hasBluetoothAdminPermission && hasLocationPermission;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public static void displayPromptForEnablingGPS(final Activity activity)
    {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";
        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    public static void displayPromptForBTisNotExistingAtAll(final Activity activity)
    {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle("Bluetooth LE not available at all!!");
        builder.setMessage("Sorry, this device does not support Bluetooth LE.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.finish();
                System.exit(0);
            }
        });
        builder.show();
    }
    public static void displayPromptForEnablingLocation(final Activity activity)
    {
        new AlertDialog.Builder(activity)
                .setTitle("PathFinder uses  Location")
                .setMessage("Do you agree?")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown - this is not the SETTINGS gui!!
                        requestPermissions(activity,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .create()
                .show();
    }

    // THIS is for the GPS and BT Settings itself


    public static boolean checkComplete_LocationAndBTpermission(final Activity activity, BeaconManager beaconManager, LocationManager locationManager) {
        if (!PermissionUtils.hasRequiredPermissions(activity)) {
            PermissionUtils.requestPermissionsBT(activity);
        }
        // check if the BT is on in settings
        try {
            if (!beaconManager.getInstanceForApplication(activity).checkAvailability()) {
                PermissionUtils.displayPromptForEnablingBT(activity);
            }
        }
        catch (RuntimeException e) {
            PermissionUtils.displayPromptForBTisNotExistingAtAll(activity);
        }
        if (!PermissionUtils.hasPermission(activity,   android.Manifest.permission.ACCESS_FINE_LOCATION) ) {
            // Should we show an explanation?
            if (PermissionUtils.shouldShowRational(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.displayPromptForEnablingLocation(activity);
            } else {
                // No explanation needed, we can request the permission.
                PermissionUtils.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        /* Der checked ob denn auch das flag in den settings gesetzt ist...also enabled
        because logically you do not need location permissions for scanning beacons via
        Bluetooth / low energy. This actually holds true for pre Android 6.0 - but Google decided to change that.
         */
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            PermissionUtils.displayPromptForEnablingGPS(activity);
        }
        return true;
    }


    public static void displayPromptForEnablingBT(final Activity activity)
    {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_BLUETOOTH_SETTINGS;
        builder.setTitle("We need BT and GPS(Location)");
        final String message = "Please enable BT in settings\nAfter this please RESTART the APP!";
        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }




}