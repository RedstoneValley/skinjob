package skinjob.internal.peer;

import android.R.drawable;
import android.annotation.TargetApi;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Window;

import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.peer.TrayIconPeer;
import java.util.concurrent.atomic.AtomicInteger;

import skinjob.SkinJobGlobals;
import skinjob.util.SkinJobUtil;

import static android.app.Notification.PRIORITY_DEFAULT;
import static android.app.Notification.PRIORITY_HIGH;
import static android.app.Notification.PRIORITY_MAX;
import static skinjob.util.SkinJobUtil.newAndroidWindow;

/**
 * Created by cryoc on 2016-10-21.
 */
@TargetApi(16)
public class SkinJobTrayIconPeer implements TrayIconPeer {
  private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
  private static final String TAG = "SkinJobTrayIconPeer";

  private final TrayIcon thisTrayIcon;
  private final Builder notificationBuilder;
  private final NotificationManager notificationManager;
  private final int id;
  private final Context androidContext;
  private String text;

  public SkinJobTrayIconPeer(TrayIcon target) {
    thisTrayIcon = target;
    androidContext = SkinJobGlobals.getAndroidApplicationContext();
    notificationBuilder = new Builder(androidContext);
    notificationManager = (NotificationManager)
        androidContext.getSystemService(Context.NOTIFICATION_SERVICE);
    id = NEXT_ID.incrementAndGet();
  }

  @Override
  public void dispose() {
    notificationManager.cancel(TAG, id);
  }

  @Override
  public void setToolTip(String tooltip) {
    notificationBuilder.setContentTitle(tooltip);
    updateImage();
  }

  @Override
  public void updateImage() {
    Image image = thisTrayIcon.getImage();
    if (image != null) {
      notificationBuilder.setLargeIcon(SkinJobUtil.asAndroidBitmap(image));
    }
    notificationManager.notify(TAG, id, notificationBuilder.build());
  }

  @Override
  public void displayMessage(String caption, String text, String messageType) {
    notificationBuilder.setContentTitle(caption);
    notificationBuilder.setContentText(text);
    int icon;
    // Pick default icon (will override in updateImage if an icon's been set)
    switch (messageType) {
      case "ERROR":
        notificationBuilder.setPriority(PRIORITY_MAX);
        break;
      case "WARNING":
        notificationBuilder.setPriority(PRIORITY_HIGH);
        break;
      case "INFO":
        notificationBuilder.setPriority(PRIORITY_DEFAULT);
        break;
      case "NONE":
        notificationBuilder.setPriority(PRIORITY_DEFAULT);
        break;
      default:
        Log.e(TAG, "Unknown message type " + messageType);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      switch (messageType) {
        case "ERROR":
          // TODO: Get a copy of https://material.io/icons/#ic_error to use here
          notificationBuilder.setSmallIcon(drawable.stat_notify_error);
          break;
        case "WARNING":
          // ! in triangle
          notificationBuilder.setSmallIcon(drawable.stat_notify_error);
          break;
        case "INFO":
          // i in circle
          notificationBuilder.setSmallIcon(drawable.ic_dialog_info);
          break;
        case "NONE":
          notificationBuilder.setSmallIcon(null);
          // default: do nothing
      }
    }
    updateImage();
  }

  @Override
  public void showPopupMenu(int x, int y) {
    Window menuWindow = newAndroidWindow(androidContext);
    menuWindow.setContentView(thisTrayIcon.getPopupMenu().sjAndroidWidget);
    menuWindow.makeActive();
  }
}
