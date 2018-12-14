package com.zzteck.msafe.location;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Administrator on 2018/10/22 0022.
 */

public class AddressResultReceiver extends ResultReceiver {

    AddressResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        // Display the address string or an error message sent from the intent service.
    //    mAddressOutput = resultData.getString(LocationConstants.RESULT_DATA_KEY);
     //   displayAddressOutput();

        // Show a toast message if an address was found.
      /*  if (resultCode == Constants.SUCCESS_RESULT) {
            showToast(getString(R.string.address_found));
        }

        // Reset. Enable the Fetch Address button and stop showing the progress bar.
        mAddressRequested = false;
        updateUIWidgets();*/
    }
}
