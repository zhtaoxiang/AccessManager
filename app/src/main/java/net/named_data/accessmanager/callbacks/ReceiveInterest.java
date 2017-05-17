package net.named_data.accessmanager.callbacks;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class ReceiveInterest implements OnInterestCallback {
  @Override
  public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {

  }
}
