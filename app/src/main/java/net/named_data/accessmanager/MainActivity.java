package net.named_data.accessmanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
  implements DrawerFragment.DrawerCallbacks {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FragmentManager fragmentManager = getSupportFragmentManager();

    if (savedInstanceState != null) {
      m_drawerFragment = (DrawerFragment) fragmentManager.findFragmentByTag(DrawerFragment.class.toString());
    }

    if (m_drawerFragment == null) {
      ArrayList<DrawerFragment.DrawerItem> items = new ArrayList<>();

      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_general, 0,
        DRAWER_ITEM_GENERAL));

      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_manager_list, 0,
        DRAWER_ITEM_MANAGER_LIST));

      m_drawerFragment = DrawerFragment.newInstance(items);

      fragmentManager
        .beginTransaction()
        .replace(R.id.navigation_drawer, m_drawerFragment, DrawerFragment.class.toString())
        .commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!m_drawerFragment.shouldHideOptionsMenu()) {
      updateActionBar();
      return super.onCreateOptionsMenu(menu);
    }
    else
      return true;
  }

  private void updateActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    if (m_actionBarTitleId != -1) {
      actionBar.setTitle(m_actionBarTitleId);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  /**
   * Convenience method that replaces the main fragment container with the
   * new fragment and adding the current transaction to the backstack.
   *
   * @param fragment Fragment to be displayed in the main fragment container.
   */
  private void replaceContentFragmentWithBackstack(Fragment fragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
      .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
      .replace(R.id.main_fragment_container, fragment)
      .addToBackStack(null)
      .commit();
  }

  @Override
  public void
  onDrawerItemSelected(int itemCode, int itemNameId) {

    String fragmentTag = "net.named-data.accessmanager.content-" + String.valueOf(itemCode);
    FragmentManager fragmentManager = getSupportFragmentManager();

    // Create fragment according to user's selection
    Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
    if (fragment == null) {
      switch (itemCode) {
        case DRAWER_ITEM_GENERAL:
          fragment = MainFragment.newInstance();
          break;
        case DRAWER_ITEM_MANAGER_LIST:
          fragment = ManagerList.newInstance();
          break;
        default:
          return;
      }
    }

    // Update ActionBar title
    m_actionBarTitleId = itemNameId;

    fragmentManager.beginTransaction()
      .replace(R.id.main_fragment_container, fragment, fragmentTag)
      .commit();
  }

  //////////////////////////////////////////////////////////////////////////////

  /** Title that is to be displayed in the ActionBar */
  private int m_actionBarTitleId = -1;

  /** Reference to drawer fragment */
  private DrawerFragment m_drawerFragment;

  /** Item code for drawer items: For use in onDrawerItemSelected() callback */
  public static final int DRAWER_ITEM_GENERAL = 1;
  public static final int DRAWER_ITEM_MANAGER_LIST = 2;
}
