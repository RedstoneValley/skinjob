package skinjob.internal.peer;

import android.widget.ListView;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.peer.MenuBarPeer;

/**
 * Created by cryoc on 2016-10-11.
 */
public class SkinJobMenuBarPeer extends SkinJobComponentPeerForView<ListView>
    implements MenuBarPeer {
  public SkinJobMenuBarPeer(MenuBar target) {
    super((ListView) target.androidWidget);
  }

  @Override
  public void addMenu(Menu m) {
    androidWidget.addView(m.sjAndroidWidget);
  }

  @Override
  public void delMenu(int index) {
    androidWidget.removeViews(index, 1);
  }

  @Override
  public void addHelpMenu(Menu m) {
    androidWidget.addFooterView(m.sjAndroidWidget);
  }
}
