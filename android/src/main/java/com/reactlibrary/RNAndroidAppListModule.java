

package com.reactlibrary;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RNAndroidAppListModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNAndroidAppListModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNAndroidAppList";
  }

  private String getPermissionLabel(String permission, PackageManager packageManager) {
    Log.v("getPermissionLabel", permission);
    try {
      PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
      CharSequence cs = permissionInfo.loadLabel(packageManager);
      return cs != null ? cs.toString() : "";
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return "";
  }


  private String getPermissionDescription(String permission, PackageManager packageManager) {
    Log.v("getPermissionDesc", permission);
    try {
      PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
      CharSequence cs = permissionInfo.loadDescription(packageManager);
      return cs != null ? cs.toString() : "";
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return "";
  }

  
  @ReactMethod
  public void getAllPermissions(Promise promise) {

    PackageManager pm = reactContext.getPackageManager();
    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    ArrayList<ApplicationPermission> appPermissions = new ArrayList<ApplicationPermission>();

    for (ApplicationInfo applicationInfo : packages) {

      try {
        PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

        // Get Permissions
        String[] requestedPermissions = packageInfo.requestedPermissions;

        if (requestedPermissions != null) {
          for (int i = 0; i < requestedPermissions.length; i++) {
            boolean status = pm.checkPermission(requestedPermissions[i], applicationInfo.packageName) == PackageManager.PERMISSION_GRANTED ? true : false;
            String label = this.getPermissionLabel(requestedPermissions[i], pm);
            String desc = "";// this.getPermissionDescription(requestedPermissions[i], pm);
            ApplicationPermission permission = new ApplicationPermission(applicationInfo.packageName, requestedPermissions[i], status, label,desc);
            appPermissions.add(permission);
          }
        }
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
    }

    JSONArray jsonArray = new JSONArray();
    for (int i = 0; i < appPermissions.size(); i++) {
      jsonArray.put(appPermissions.get(i).getJSONObject());
    }

    promise.resolve(jsonArray.toString());
  }

  class ApplicationPermission {
    private String packageName;
    private String permissionName;
    private boolean granted;
    private  String permissionLabel;
    private String permissionDescription;

    public ApplicationPermission(String packageName, String permissionName, boolean granted, String label, String desc) {
      this.packageName = packageName;
      this.permissionName = permissionName;
      this.granted = granted;
      this.permissionLabel = label;
      this.permissionDescription = desc;
    }

    public void setPackageName(String packageName) {
      this.packageName = packageName;
    }

    public String getPackageName() {
      return this.packageName;
    }

    public void setPermissionName(String permissionName) {
      this.permissionName = permissionName;
    }

    public String getPermissionName() {
      return this.permissionName;
    }

    public void setGranted(boolean granted) {
      this.granted = granted;
    }

    public boolean isGranted() {
      return this.granted;
    }

    public  void setPermissionLabel(String label) {
      this.permissionLabel = label;
    }

    public  String getPermissionLabel(){
      return  this.permissionLabel;
    }

    public  void setPermissionDescription(String desc) {
      this.permissionDescription = desc;
    }

    public  String getPermissionDescription(){
      return this.permissionDescription;
    }

    public JSONObject getJSONObject() {
      JSONObject obj = new JSONObject();
      try {
        obj.put("packageName", getPackageName());
        obj.put("permissionName", getPermissionName());
        obj.put("granted", isGranted());
        obj.put("label", getPermissionLabel());
        obj.put("desc", getPermissionDescription());
      } catch (JSONException e) {
        
      }
      return obj;
    }
  }
}
