/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.telephony;

import android.annotation.NonNull;
import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.Rlog;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.RemoteException;

import com.android.internal.telephony.ISub;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;

import java.util.ArrayList;
import java.util.List;
import java.lang.Throwable;
//////////////////////////zhoguanghui
import android.util.Log;
import android.provider.Settings;
import com.mediatek.internal.telephony.ITelephonyEx;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;

import com.android.internal.telephony.PhoneConstants;
//////////////////////////END  zgh

/**
 * SubscriptionManager is the application interface to SubscriptionController
 * and provides information about the current Telephony Subscriptions.
 * * <p>
 * You do not instantiate this class directly; instead, you retrieve
 * a reference to an instance through {@link #from}.
 * <p>
 * All SDK public methods require android.Manifest.permission.READ_PHONE_STATE.
 */
public class SubscriptionManager {
    private static final String LOG_TAG = "SubscriptionManager";
    private static final boolean DBG = false;
    private static final boolean VDBG = false;

    /** An invalid subscription identifier */
    /** @hide */
    public static final int INVALID_SUBSCRIPTION_ID = -1;

    /** Base value for Dummy SUBSCRIPTION_ID's. */
    /** FIXME: Remove DummySubId's, but for now have them map just below INVALID_SUBSCRIPTION_ID
    /** @hide */
    public static final int DUMMY_SUBSCRIPTION_ID_BASE = INVALID_SUBSCRIPTION_ID - 1;

    /** An invalid phone identifier */
    /** @hide */
    public static final int INVALID_PHONE_INDEX = -1;

    /** An invalid slot identifier */
    /** @hide */
    public static final int INVALID_SIM_SLOT_INDEX = -1;

    /** Indicates the caller wants the default sub id. */
    /** @hide */
    public static final int DEFAULT_SUBSCRIPTION_ID = Integer.MAX_VALUE;

    /**
     * Indicates the caller wants the default phone id.
     * Used in SubscriptionController and PhoneBase but do we really need it???
     * @hide
     */
    public static final int DEFAULT_PHONE_INDEX = Integer.MAX_VALUE;

    /** Indicates the caller wants the default slot id. NOT used remove? */
    /** @hide */
    public static final int DEFAULT_SIM_SLOT_INDEX = Integer.MAX_VALUE;

    /** Minimum possible subid that represents a subscription */
    /** @hide */
    public static final int MIN_SUBSCRIPTION_ID_VALUE = 0;

    /** Maximum possible subid that represents a subscription */
    /** @hide */
    public static final int MAX_SUBSCRIPTION_ID_VALUE = DEFAULT_SUBSCRIPTION_ID - 1;

    /** @hide */
    public static final int LTE_DC_PHONE_ID = TelephonyManager.getDefault().getPhoneCount();
    /**
     * M: Indicates the specified phone id for slot1 LteDcPhone.
     */
    /** @hide */
    public static final int LTE_DC_PHONE_ID_1 = 10;
    /**
     * M: Indicates the specified phone id for slot2 LteDcPhone.
     */
    /** @hide */
    public static final int LTE_DC_PHONE_ID_2 = 11;

    /**
     * M: Indicates the specified subscription identifier for LteDcPhone.
     */
    /** @hide */
    public static final int LTE_DC_SUB_ID = -999;
    /**
     * M: Indicates the specified subscription identifier for slot1 LteDcPhone.
     */
    /** @hide */
    public static final int LTE_DC_SUB_ID_1 = -10;
    /**
     * M: Indicates the specified subscription identifier for slot2 LteDcPhone.
     */
    /** @hide */
    public static final int LTE_DC_SUB_ID_2 = -11;

    /** @hide */
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");

    /** @hide */
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    /** @hide */
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    /** @hide */
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    /** @hide */
    public static final int EXTRA_VALUE_NOCHANGE = 4;

    /** @hide */
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    /** @hide */
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    /** @hide */
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    /** @hide */
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";

	/*HQ_xionghaifeng 20150915 add for HW 4G on off start*/
    private static final int NT_MODE_LTE_CDMA_AND_EVDO        = 8;
    private static final int NT_MODE_LTE_GSM_WCDMA            = 9;
    private static final int NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA  = 10;
    private static final int NT_MODE_LTE_ONLY                 = 11;
    private static final int NT_MODE_LTE_WCDMA                = 12;
	private static final int PREFERRED_NT_MODE        = (SystemProperties.getInt("ro.mtk_lte_support", 0) == 1) ? NT_MODE_LTE_GSM_WCDMA : 0;
    public static final int SVLTE_RAT_MODE_4G = 0;
    public static final int SVLTE_RAT_MODE_3G = 1;
	/*HQ_xionghaifeng 20150915 add for HW 4G on off start*/

    /**
     * TelephonyProvider unique key column name is the subscription id.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String UNIQUE_KEY_SUBSCRIPTION_ID = "_id";

    /**
     * TelephonyProvider column name for SIM ICC Identifier
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String ICC_ID = "icc_id";

    /**
     * TelephonyProvider column name for user SIM_SlOT_INDEX
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String SIM_SLOT_INDEX = "sim_id";

    /** SIM is not inserted */
    /** @hide */
    public static final int SIM_NOT_INSERTED = -1;

    /**
     * TelephonyProvider column name for user displayed name.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String DISPLAY_NAME = "display_name";

    /**
     * TelephonyProvider column name for the service provider name for the SIM.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String CARRIER_NAME = "carrier_name";

    /**
     * Default name resource
     * @hide
     */
    public static final int DEFAULT_NAME_RES = com.android.internal.R.string.unknownName;

    /**
     * TelephonyProvider column name for source of the user displayed name.
     * <P>Type: INT (int)</P> with one of the NAME_SOURCE_XXXX values below
     *
     * @hide
     */
    public static final String NAME_SOURCE = "name_source";

    /**
     * The name_source is undefined
     * @hide
     */
    public static final int NAME_SOURCE_UNDEFINDED = -1;

    /**
     * The name_source is the default
     * @hide
     */
    public static final int NAME_SOURCE_DEFAULT_SOURCE = 0;

    /**
     * The name_source is from the SIM
     * @hide
     */
    public static final int NAME_SOURCE_SIM_SOURCE = 1;

    /**
     * The name_source is from the user
     * @hide
     */
    public static final int NAME_SOURCE_USER_INPUT = 2;

    /**
     * TelephonyProvider column name for the color of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String COLOR = "color";

    /** @hide */
    public static final int COLOR_1 = 0;

    /** @hide */
    public static final int COLOR_2 = 1;

    /** @hide */
    public static final int COLOR_3 = 2;

    /** @hide */
    public static final int COLOR_4 = 3;

    /** @hide */
    public static final int COLOR_DEFAULT = COLOR_1;

    /**
     * TelephonyProvider column name for the phone number of a SIM.
     * <P>Type: TEXT (String)</P>
     */
    /** @hide */
    public static final String NUMBER = "number";

    /**
     * TelephonyProvider column name for the number display format of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";

    /** @hide */
    public static final int DISPLAY_NUMBER_NONE = 0;

    /** @hide */
    public static final int DISPLAY_NUMBER_FIRST = 1;

    /** @hide */
    public static final int DISPLAY_NUMBER_LAST = 2;

    /** @hide */
    public static final int DISPLAY_NUMBER_DEFAULT = DISPLAY_NUMBER_FIRST;

    /**
     * TelephonyProvider column name for permission for data roaming of a SIM.
     * <P>Type: INTEGER (int)</P>
     */
    /** @hide */
    public static final String DATA_ROAMING = "data_roaming";

    /** Indicates that data roaming is enabled for a subscription */
    public static final int DATA_ROAMING_ENABLE = 1;

    /** Indicates that data roaming is disabled for a subscription */
    public static final int DATA_ROAMING_DISABLE = 0;

    /** @hide */
    public static final int DATA_ROAMING_DEFAULT = DATA_ROAMING_DISABLE;

    /**
     * TelephonyProvider column name for the MCC associated with a SIM.
     * <P>Type: INTEGER (int)</P>
     * @hide
     */
    public static final String MCC = "mcc";

    /**
     * TelephonyProvider column name for the MNC associated with a SIM.
     * <P>Type: INTEGER (int)</P>
     * @hide
     */
    public static final String MNC = "mnc";

    /**
     * Broadcast Action: The user has changed one of the default subs related to
     * data, phone calls, or sms</p>
     *
     * TODO: Change to a listener
     * @hide
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String SUB_DEFAULT_CHANGED_ACTION =
        "android.intent.action.SUB_DEFAULT_CHANGED";

    private final Context mContext;
    private static final String MODE_STATUS = "mode_status";
    private static final String LTETDD_CDMA = "ltetdd_cdma";
	
    /** @hide */
    public static Context mContextEx;  //add by zhouguanghui
    private static final String KeyguardClassName = "com.android.keyguard.KeyguardUpdateMonitor";//add by caoxuhao for emergency button

    /**
     * A listener class for monitoring changes to {@link SubscriptionInfo} records.
     * <p>
     * Override the onSubscriptionsChanged method in the object that extends this
     * class and pass it to {@link #addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener)}
     * to register your listener and to unregister invoke
     * {@link #removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener)}
     * <p>
     * Permissions android.Manifest.permission.READ_PHONE_STATE is required
     * for #onSubscriptionsChanged to be invoked.
     */
    public static class OnSubscriptionsChangedListener {
        /** @hide */
        public static final String PERMISSION_ON_SUBSCRIPTIONS_CHANGED =
                android.Manifest.permission.READ_PHONE_STATE;

        private final Handler mHandler  = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //if (DBG) {
                    log("handleMessage: invoke the overriden onSubscriptionsChanged()");
                //}
                OnSubscriptionsChangedListener.this.onSubscriptionsChanged();
            }
        };

        /**
         * Callback invoked when there is any change to any SubscriptionInfo. Typically
         * this method would invoke {@link #getActiveSubscriptionInfoList}
         */
        public void onSubscriptionsChanged() {
            if (DBG) log("onSubscriptionsChanged: NOT OVERRIDDEN");
        }

        /**
         * The callback methods need to be called on the handler thread where
         * this object was created.  If the binder did that for us it'd be nice.
         */
        IOnSubscriptionsChangedListener callback = new IOnSubscriptionsChangedListener.Stub() {
            @Override
            public void onSubscriptionsChanged() {
                /*if (DBG)*/ log("callback: received, sendEmptyMessage(0) to handler");
                mHandler.sendEmptyMessage(0);
            }
        };

        private void log(String s) {
            Rlog.d(LOG_TAG, s);
        }
    }

    /** @hide */
    public SubscriptionManager(Context context) {
        if (DBG) logd("SubscriptionManager created");
        mContext = context;
        mContextEx = context;
    }
	
	/*HQ_xionghaifeng 20150916 add for HW 4G start*/
    //////////////////////////////zhouguanghui
    //dianxin
    public static void setLteServiceAbilityEx(boolean isChecked) throws RemoteException{
      Log.d("MingYue","isChecked  vaule : " + isChecked + "     mContext vaule : " + (mContextEx == null));
      setData(isChecked);
      switchSvlte(isChecked);
    }

    //yidong & liantong & dianxin
    public static void setLteServiceAbilityExTwo(boolean isChecked , int subId){
      Log.d("MingYue","setLteServiceAbilityExTwo : " + isChecked + ",subId = "+ subId);
	  int validSlotId = getValidSlotId(subId);
      Log.d("MingYue","validSlotId : " + validSlotId);
      final Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setComponent(new ComponentName("com.android.phone","com.android.phone.MobileNetworkSettingsEx"));
      intent.putExtra("isChecked",isChecked);
	/**add by liruihong for HQ01393421 begin*/
      int defaultDataSubId = SubscriptionManager.getDefaultDataSubId();
       Log.d("MingYue", "Default data sub id : " + defaultDataSubId);
	/**add by liruihong for HQ01393421 end*/
      intent.putExtra("slotId",validSlotId);
      mContextEx.startActivity(intent);
    }
	
	private static int getValidSlotId(int slotId)
	{
		TelephonyManager tm = TelephonyManager.getDefault();
		int subId[] = getSubId(slotId);
		
		if (!isValidSubscriptionId(subId[0]))
		{
			Log.d("xionghaifeng", " Invalid slotId = " + slotId);

			if (tm.hasIccCard(0))
			{
				return 0;
			}
			else if (tm.hasIccCard(1))
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return slotId;
		}
	}
	
	public static int getLteServiceAbiltiy(int slotId)
	{
		Log.i("liruihong","getLteServiceAbiltiy");
		int lteServices = 1;
		int current_lte_mode = -1;
		//int phoneId = SubscriptionManager.getPhoneId(subId[0]);
		int validSlotId = getValidSlotId(slotId);
		
		int subId[] = getSubId(validSlotId);

		//add by liruihong
		int mSubId = SubscriptionManager.getDefaultDataSubId();

		int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(mSubId);
		Log.i("liruihong","getLteServiceAbiltiy:mSubId"+mSubId+",phoneType"+phoneType);

		if (phoneType == PhoneConstants.PHONE_TYPE_CDMA)
		{
	       // int ratMode = Settings.Global.getInt(mContextEx.getContentResolver(),
            //	Settings.Global.LTE_ON_CDMA_RAT_MODE, SVLTE_RAT_MODE_4G);
		     int ratMode = Settings.Global.getInt(mContextEx.getContentResolver(),
               		 Settings.Global.LTE_ON_CDMA_RAT_MODE + mSubId, SVLTE_RAT_MODE_4G);
		   		Log.i("liruihong","ratMode = "+ratMode);

			if (ratMode != SVLTE_RAT_MODE_3G)
			{
				lteServices = 1;
			}
			else
			{
				lteServices = 0;
			}
		}
		else
		{//GSM phone
			//current_lte_mode = Settings.Global.getInt(mContextEx.getContentResolver(), 
				//android.provider.Settings.Global.PREFERRED_NETWORK_MODE, PREFERRED_NT_MODE);	
                 current_lte_mode = Settings.Global.getInt(mContextEx.getContentResolver(),
                        Settings.Global.PREFERRED_NETWORK_MODE + mSubId,PREFERRED_NT_MODE);
		  Log.i("liruihong","current_lte_mode = "+current_lte_mode);
			
			if (current_lte_mode == NT_MODE_LTE_CDMA_AND_EVDO
				|| current_lte_mode == NT_MODE_LTE_GSM_WCDMA
				|| current_lte_mode == NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA
				|| current_lte_mode == NT_MODE_LTE_ONLY
				|| current_lte_mode == NT_MODE_LTE_WCDMA)
			{
				lteServices = 1;
			}
			else
			{
				lteServices = 0;
			}
		}
		Log.i("liruihong","lteServices = "+lteServices);
		Log.d("xionghaifeng", "validSlotId = " + validSlotId + " current_lte_mode = " + current_lte_mode
			+ " lteServices : " + lteServices + " phoneType : " + phoneType);
		return lteServices;
	}
	/*HQ_xionghaifeng 20150916 add for HW 4G end*/
	
    private static void setData(boolean isChecked) throws RemoteException{
      if(!isChecked){
        int lastMode = Settings.Global.getInt(mContextEx.getContentResolver(), Settings.Global.LTE_ON_CDMA_RAT_MODE, 0);
        Log.d("MingYue", "saveData isChecked = false lastMode = " + lastMode);
        mContextEx.getSharedPreferences(MODE_STATUS, Context.MODE_PRIVATE)
                .edit().putInt(LTETDD_CDMA, lastMode).commit();
        Settings.Global.putInt(mContextEx.getContentResolver(),Settings.Global.LTE_ON_CDMA_RAT_MODE, 1);
      }else{
        int lte_cdma = mContextEx.getSharedPreferences(MODE_STATUS, Context.MODE_PRIVATE)
              .getInt(LTETDD_CDMA, -1);
        Log.i("MingYue", "saveData isChecked = true lte_cdma = " + lte_cdma);
        if (lte_cdma != -1) {
          Settings.Global.putInt(mContextEx.getContentResolver(),Settings.Global.LTE_ON_CDMA_RAT_MODE,lte_cdma);
        }else{
          Settings.Global.putInt(mContextEx.getContentResolver(),Settings.Global.LTE_ON_CDMA_RAT_MODE,0);
        }
      }
    }

    private static void switchSvlte (boolean isChecked) throws RemoteException{
      ITelephonyEx telephony = ITelephonyEx.Stub.asInterface(
                  ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));
      if(telephony != null){
        if(!isChecked){
           Log.i("MingYue", "switchSvlte isChecked = false switchSvlteRatMode 3g");
           telephony.switchRadioTechnology(16|32);//2G|3G
        }else{
          int lte_cdma = mContextEx.getSharedPreferences(MODE_STATUS,Context.MODE_PRIVATE).getInt(LTETDD_CDMA, -1);
          Log.i("MingYue", "switchSvlte isChecked = true last mode lte_cdma = " + lte_cdma);
          if (lte_cdma != -1) {
            Log.i("MingYue", "switchSvlte saveData isChecked = true put LTETDD_CDMA = -1");
            mContextEx.getSharedPreferences(MODE_STATUS, Context.MODE_PRIVATE).edit().putInt(LTETDD_CDMA, -1).commit();
            if(lte_cdma == 2){
              Log.i("MingYue", "switchSvlte last mode SVLTE_RAT_MODE_4G_DATA_ONLY");
              telephony.switchRadioTechnology(64);//4G
            }else if(lte_cdma == 0){
              Log.i("MingYue", "switchSvlte last mode SVLTE_RAT_MODE_4G");
              telephony.switchRadioTechnology(16|32|64);
            }else{
              Log.i("MingYue", "switchSvlte last mode error, do open 4G mode");
              telephony.switchRadioTechnology(16|32|64);
            }
          }else{
            telephony.switchRadioTechnology(16|32|64);
          }
        }
      }
    }
    /////////////////////////////END zgh

    /**
     * Get an instance of the SubscriptionManager from the Context.
     * This invokes {@link android.content.Context#getSystemService
     * Context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)}.
     *
     * @param context to use.
     * @return SubscriptionManager instance
     */
    public static SubscriptionManager from(Context context) {
        return (SubscriptionManager) context.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    }

    /**
     * Register for changes to the list of active {@link SubscriptionInfo} records or to the
     * individual records themselves. When a change occurs the onSubscriptionsChanged method of
     * the listener will be invoked immediately if there has been a notification.
     *
     * @param listener an instance of {@link OnSubscriptionsChangedListener} with
     *                 onSubscriptionsChanged overridden.
     */
    public void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = mContext != null ? mContext.getPackageName() : "<unknown>";
        if (DBG) {
            logd("register OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug
                    + " listener=" + listener);
        }
        try {
            // We use the TelephonyRegistry as it runs in the system and thus is always
            // available. Where as SubscriptionController could crash and not be available
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
            if (tr != null) {
                tr.addOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException ex) {
            // Should not happen
        }
    }

    /**
     * Unregister the {@link OnSubscriptionsChangedListener}. This is not strictly necessary
     * as the listener will automatically be unregistered if an attempt to invoke the listener
     * fails.
     *
     * @param listener that is to be unregistered.
     */
    public void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = mContext != null ? mContext.getPackageName() : "<unknown>";
        if (DBG) {
            logd("unregister OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug
                    + " listener=" + listener);
        }
        try {
            // We use the TelephonyRegistry as its runs in the system and thus is always
            // available where as SubscriptionController could crash and not be available
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
            if (tr != null) {
                tr.removeOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException ex) {
            // Should not happen
        }
    }

    /**
     * Get the active SubscriptionInfo with the subId key
     * @param subId The unique SubscriptionInfo key in database
     * @return SubscriptionInfo, maybe null if its not active.
     */
    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        if (VDBG) logd("[getActiveSubscriptionInfo]+ subId=" + subId);
        if (!isValidSubscriptionId(subId)) {
            logd("[getActiveSubscriptionInfo]- invalid subId, subId = " + subId);
            return null;
        }

        SubscriptionInfo subInfo = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getActiveSubscriptionInfo(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subInfo;

    }

    /**
     * Get the active SubscriptionInfo associated with the iccId
     * @param iccId the IccId of SIM card
     * @return SubscriptionInfo, maybe null if its not active
     * @hide
     */
    public SubscriptionInfo getActiveSubscriptionInfoForIccIndex(String iccId) {
        if (VDBG) logd("[getActiveSubscriptionInfoForIccIndex]+ iccId=" + iccId);
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }

        SubscriptionInfo result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForIccId(iccId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get the active SubscriptionInfo associated with the slotIdx
     * @param slotIdx the slot which the subscription is inserted
     * @return SubscriptionInfo, maybe null if its not active
     */
    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx) {
        if (VDBG) logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx);
        if (!isValidSlotId(slotIdx)) {
            logd("[getActiveSubscriptionInfoForSimSlotIndex]- invalid slotIdx, slotIdx = "
                + slotIdx);
            return null;
        }

        SubscriptionInfo result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForSimSlotIndex(slotIdx);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return List of all SubscriptionInfo records in database,
     * include those that were inserted before, maybe empty but not null.
     * @hide
     */
    public List<SubscriptionInfo> getAllSubscriptionInfoList() {
        if (VDBG) logd("[getAllSubscriptionInfoList]+");

        List<SubscriptionInfo> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (result == null) {
            result = new ArrayList<SubscriptionInfo>();
        }
        return result;
    }

    /**
     * Get the SubscriptionInfo(s) of the currently inserted SIM(s). The records will be sorted
     * by {@link SubscriptionInfo#getSimSlotIndex} then by {@link SubscriptionInfo#getSubscriptionId}.
     *
     * @return Sorted list of the currently {@link SubscriptionInfo} records available on the device.
     * <ul>
     * <li>
     * If null is returned the current state is unknown but if a {@link OnSubscriptionsChangedListener}
     * has been registered {@link OnSubscriptionsChangedListener#onSubscriptionsChanged} will be
     * invoked in the future.
     * </li>
     * <li>
     * If the list is empty then there are no {@link SubscriptionInfo} records currently available.
     * </li>
     * <li>
     * if the list is non-empty the list is sorted by {@link SubscriptionInfo#getSimSlotIndex}
     * then by {@link SubscriptionInfo#getSubscriptionId}.
     * </li>
     * </ul>
     */
    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        List<SubscriptionInfo> result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }
        return result;
    }

    /**
     * @return the count of all subscriptions in the database, this includes
     * all subscriptions that have been seen.
     * @hide
     */
    public int getAllSubscriptionInfoCount() {
        if (VDBG) logd("[getAllSubscriptionInfoCount]+");

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return the current number of active subscriptions. There is no guarantee the value
     * returned by this method will be the same as the length of the list returned by
     * {@link #getActiveSubscriptionInfoList}.
     */
    public int getActiveSubscriptionInfoCount() {
        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoCount();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * @return the maximum number of active subscriptions that will be returned by
     * {@link #getActiveSubscriptionInfoList} and the value returned by
     * {@link #getActiveSubscriptionInfoCount}.
     */
    public int getActiveSubscriptionInfoCountMax() {
        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoCountMax();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Add a new SubscriptionInfo to SubscriptionInfo database if needed
     * @param iccId the IccId of the SIM card
     * @param slotId the slot which the SIM is inserted
     * @return the URL of the newly created row or the updated row
     * @hide
     */
    public Uri addSubscriptionInfoRecord(String iccId, int slotId) {
        if (VDBG) logd("[addSubscriptionInfoRecord]+ iccId:" + iccId + " slotId:" + slotId);
        if (iccId == null) {
            logd("[addSubscriptionInfoRecord]- null iccId");
        }
        if (!isValidSlotId(slotId)) {
            logd("[addSubscriptionInfoRecord]- invalid slotId = " + slotId);
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                // FIXME: This returns 1 on success, 0 on error should should we return it?
                iSub.addSubInfoRecord(iccId, slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        // FIXME: Always returns null?
        return null;

    }

    /**
     * Set SIM icon tint color by simInfo index
     * @param tint the RGB value of icon tint color of the SIM
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     * @hide
     */
    public int setIconTint(int tint, int subId) {
        if (VDBG) logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        if (!isValidSubscriptionId(subId)) {
            logd("[setIconTint]- fail, subId = " + subId + ", tint = " + tint);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setIconTint(tint, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set display name by simInfo index
     * @param displayName the display name of SIM card
     * @param subId the unique SubscriptionInfo index in database
     * @return the number of records updated
     * @hide
     */
    public int setDisplayName(String displayName, int subId) {
        return setDisplayName(displayName, subId, NAME_SOURCE_UNDEFINDED);
    }

    /**
     * Set display name by simInfo index with name source
     * @param displayName the display name of SIM card
     * @param subId the unique SubscriptionInfo index in database
     * @param nameSource 0: NAME_SOURCE_DEFAULT_SOURCE, 1: NAME_SOURCE_SIM_SOURCE,
     *                   2: NAME_SOURCE_USER_INPUT, -1 NAME_SOURCE_UNDEFINED
     * @return the number of records updated or < 0 if invalid subId
     * @hide
     */
    public int setDisplayName(String displayName, int subId, long nameSource) {
        if (VDBG) {
            logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId
                    + " nameSource:" + nameSource);
        }
        if (!isValidSubscriptionId(subId)) {
            logd("[setDisplayName]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNameUsingSrc(displayName, subId, nameSource);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set phone number by subId
     * @param number the phone number of the SIM
     * @param subId the unique SubscriptionInfo index in database
     * @return the number of records updated
     * @hide
     */
    public int setDisplayNumber(String number, int subId) {
        if (number == null || !isValidSubscriptionId(subId)) {
            logd("[setDisplayNumber]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNumber(number, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;

    }

    /**
     * Set data roaming by simInfo index
     * @param roaming 0:Don't allow data when roaming, 1:Allow data when roaming
     * @param subId the unique SubscriptionInfo index in database
     * @return the number of records updated
     * @hide
     */
    public int setDataRoaming(int roaming, int subId) {
        if (VDBG) logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        if (roaming < 0 || !isValidSubscriptionId(subId)) {
            logd("[setDataRoaming]- fail, subId = " + subId);
            return -1;
        }

        int result = 0;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDataRoaming(roaming, subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }

    /**
     * Get slotId associated with the subscription.
     * @return slotId as a positive integer or a negative value if an error either
     * SIM_NOT_INSERTED or < 0 if an invalid slot index
     * @hide
     */
    public static int getSlotId(int subId) {
        if (!isValidSubscriptionId(subId)) {
            logd("[getSlotId]- fail, subId = " + subId);
        }

        int result = INVALID_SIM_SLOT_INDEX;
        
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSlotId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
        
        //add by HQ_caoxuhao at 20150909 HQ01339963 begin
        //add for emergency button
        //subId = -2 when there is no Sim card
        if (subId == -2) {
            Throwable ex = new Throwable();
            StackTraceElement[] stackElements = ex.getStackTrace();
            if (stackElements != null) {
                for (int i = 0; i < stackElements.length; i++) {
                	String className = stackElements[i].getClassName();
                	int keyguardClassLength = KeyguardClassName.length();
                	String tmpStr = "";
                	if (className.length() >= keyguardClassLength) {
                		tmpStr = className.substring(0, keyguardClassLength);
                    		if (tmpStr.equals(KeyguardClassName)){
                    			return 0;
        			}
			}
                }
            }
	}
        
        //add by HQ_caoxuhao at 20150909 HQ01339963 end
        
        return result;

    }

    /** @hide */
    public static int[] getSubId(int slotId) {
        return getSubIdUsingSlotId(slotId);
    }

    /**
     * Get subId associated with the slotId.
     * @param slotId the specified slotId
     * @return subId as a positive integer
     * null if an invalid slot index
     * @hide
     */
    public static int[] getSubIdUsingSlotId(int slotId) {
        if (VDBG) logd("[getSubIdUsingSlotId]+ slotId:" + slotId);

        if (!isValidSlotId(slotId)) {
            logd("[getSubIdUsingSlotId]- fail, slotId = " + slotId);
            return null;
        }

        int[] subId = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getSubIdUsingSlotId(slotId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subId;
    }

    /**
     * Get subId associated with the slotId.
     * @param phoneId the specified phoneId
     * @return subId as a positive integer
     * INVALID_SUBSCRIPTION_ID if an invalid phone index
     * @hide
     */
    public static int getSubIdUsingPhoneId(int phoneId) {
        if (VDBG) logd("[getSubIdUsingPhoneId]+ phoneId:" + phoneId);

        int subId = INVALID_SUBSCRIPTION_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getSubIdUsingPhoneId(phoneId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subId;
    }

	// /added by guofeiyao for phoneManager
	public static int getSubState(int subId) {
        if (!isValidSubscriptionId(subId)) {
            logd("[getSlotId]- fail, subId = " + subId);
			return 0;
        }
		int simState = TelephonyManager.getDefault().getSimState(getSlotId(subId));
		
		if (simState != TelephonyManager.SIM_STATE_READY)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}

	public static int getPreferredDataSubscription() {
            return getDefaultDataSubId();    
	}
	// /end

    /** @hide */
    public static int getPhoneId(int subId) {
        int result = INVALID_PHONE_INDEX;

        if (!isValidSubscriptionId(subId)) {
            if (subId > DUMMY_SUBSCRIPTION_ID_BASE - TelephonyManager.getDefault().getSimCount()) {
                result = (int) (SubscriptionManager.DUMMY_SUBSCRIPTION_ID_BASE  - subId);
            } else {
                result = SubscriptionManager.INVALID_PHONE_INDEX;
            }

            logd("[getPhoneId]- invalid subId = " + subId + " return = " + result);
            return result;
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getPhoneId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("[getPhoneId]- phoneId=" + result);
        return result;

    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /**
     * Set subId as default SubId.
     * @param subId the specified subId
     * @hide
     */
    public static void setDefaultSubId(int subId) {
        if (VDBG) logd("setDefaultSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultFallbackSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /**
     * @return the "system" defaultSubId on a voice capable device this
     * will be getDefaultVoiceSubId() and on a data only device it will be
     * getDefaultDataSubId().
     * @hide
     */
    public static int getDefaultSubId() {
        int subId = INVALID_SUBSCRIPTION_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public static int getDefaultVoiceSubId() {
        int subId = INVALID_SUBSCRIPTION_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultVoiceSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultVoiceSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public void setDefaultVoiceSubId(int subId) {
        if (VDBG) logd("setDefaultVoiceSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultVoiceSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultVoiceSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public SubscriptionInfo getDefaultVoiceSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultVoiceSubId());
    }

    /** @hide */
    public static int getDefaultVoicePhoneId() {
        return getPhoneId(getDefaultVoiceSubId());
    }

    /**
     * @return subId of the DefaultSms subscription or
     * a value < 0 if an error.
     *
     * @hide
     */
    public static int getDefaultSmsSubId() {
        int subId = INVALID_SUBSCRIPTION_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSmsSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultSmsSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public void setDefaultSmsSubId(int subId) {
        if (VDBG) logd("setDefaultSmsSubId sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultSmsSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSmsSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public SubscriptionInfo getDefaultSmsSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultSmsSubId());
    }

    /** @hide */
    public int getDefaultSmsPhoneId() {
        return getPhoneId(getDefaultSmsSubId());
    }

    /** @hide */
    public static int getDefaultDataSubId() {
        int subId = INVALID_SUBSCRIPTION_ID;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultDataSubId();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (VDBG) logd("getDefaultDataSubId, sub id = " + subId);
        return subId;
    }

    /** @hide */
    public void setDefaultDataSubId(int subId) {
        if (VDBG) logd("setDataSubscription sub id = " + subId);

        if (subId <= 0) {
            printStackTrace("setDefaultDataSubId subId 0");
        }

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /** @hide */
    public SubscriptionInfo getDefaultDataSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultDataSubId());
    }

    /** @hide */
    public int getDefaultDataPhoneId() {
        return getPhoneId(getDefaultDataSubId());
    }

    /** @hide */
    public void clearSubscriptionInfo() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearSubInfo();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return;
    }

    //FIXME this is vulnerable to race conditions
    /** @hide */
    public boolean allDefaultsSelected() {
        if (!isValidSubscriptionId(getDefaultDataSubId())) {
            return false;
        }
        if (!isValidSubscriptionId(getDefaultSmsSubId())) {
            return false;
        }
        if (!isValidSubscriptionId(getDefaultVoiceSubId())) {
            return false;
        }
        return true;
    }

    /**
     * If a default is set to subscription which is not active, this will reset that default back to
     * an invalid subscription id, i.e. < 0.
     * @hide
     */
    public void clearDefaultsForInactiveSubIds() {
        if (VDBG) logd("clearDefaultsForInactiveSubIds");
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearDefaultsForInactiveSubIds();
            }
        } catch (RemoteException ex) {
            // ignore it
        }
    }

    /**
     * @return true if a valid subId else false
     * @hide
     */
    public static boolean isValidSubscriptionId(int subId) {
        // MTK-START
        // Add the special handle for SVLTE
        if ("1".equals(SystemProperties.get("ro.mtk_svlte_support"))) {
            return subId > INVALID_SUBSCRIPTION_ID || subId == LTE_DC_SUB_ID_1
                    || subId == LTE_DC_SUB_ID_2;
        }
        // MTK-END
        return subId > INVALID_SUBSCRIPTION_ID ;
    }

    /**
     * @return true if subId is an usable subId value else false. A
     * usable subId means its neither a INVALID_SUBSCRIPTION_ID nor a DEFAULT_SUB_ID.
     * @hide
     */
    public static boolean isUsableSubIdValue(int subId) {
        return subId >= MIN_SUBSCRIPTION_ID_VALUE && subId <= MAX_SUBSCRIPTION_ID_VALUE;
    }

    /** @hide */
    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < TelephonyManager.getDefault().getSimCount();
    }

    /** @hide */
    public static boolean isValidPhoneId(int phoneId) {
        // MTK-START
        // Add the special handle for SVLTE
        if ("1".equals(SystemProperties.get("ro.mtk_svlte_support"))) {
            return (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount())
                    || phoneId == LTE_DC_PHONE_ID_1 || phoneId == LTE_DC_PHONE_ID_2;
        }
        // MTK-END
        return phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount();
    }

    /** @hide */
    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds != null && subIds.length > 0) {
            putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        } else {
            logd("putPhoneIdAndSubIdExtra: no valid subs");
        }
    }

    /** @hide */
    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId, int subId) {
        if (VDBG) logd("putPhoneIdAndSubIdExtra: phoneId=" + phoneId + " subId=" + subId);
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        intent.putExtra(PhoneConstants.PHONE_KEY, phoneId);
        //FIXME this is using phoneId and slotId interchangeably
        //Eventually, this should be removed as it is not the slot id
        intent.putExtra(PhoneConstants.SLOT_KEY, phoneId);
    }

    /**
     * @return the list of subId's that are active,
     *         is never null but the length maybe 0.
     * @hide
     */
    public @NonNull int[] getActiveSubscriptionIdList() {
        int[] subId = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getActiveSubIdList();
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        if (subId == null) {
            subId = new int[0];
        }

        return subId;

    }

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        logd("StackTrace - " + msg);
        StackTraceElement[] st = re.getStackTrace();
        for (StackTraceElement ste : st) {
            logd(ste.toString());
        }
    }

    /**
     * Returns true if the device is considered roaming on the current
     * network for a subscription.
     * <p>
     * Availability: Only when user registered to a network.
     *
     * @param subId The subscription ID
     * @return true if the network for the subscription is roaming, false otherwise
     */
    public boolean isNetworkRoaming(int subId) {
        final int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            // What else can we do?
            return false;
        }
        return TelephonyManager.getDefault().isNetworkRoaming(subId);
    }

    /**
     * Returns a constant indicating the state of sim for the subscription.
     *
     * @param subId
     *
     * {@See TelephonyManager#SIM_STATE_UNKNOWN}
     * {@See TelephonyManager#SIM_STATE_ABSENT}
     * {@See TelephonyManager#SIM_STATE_PIN_REQUIRED}
     * {@See TelephonyManager#SIM_STATE_PUK_REQUIRED}
     * {@See TelephonyManager#SIM_STATE_NETWORK_LOCKED}
     * {@See TelephonyManager#SIM_STATE_READY}
     * {@See TelephonyManager#SIM_STATE_NOT_READY}
     * {@See TelephonyManager#SIM_STATE_PERM_DISABLED}
     * {@See TelephonyManager#SIM_STATE_CARD_IO_ERROR}
     *
     * {@hide}
     */
    public static int getSimStateForSubscriber(int subId) {
        int simState;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            simState = iSub.getSimStateForSubscriber(subId);
        } catch (RemoteException ex) {
            simState = TelephonyManager.SIM_STATE_UNKNOWN;
        }
        logd("getSimStateForSubscriber: simState=" + simState + " subId=" + subId);
        return simState;
    }

    /**
     * Get the SubscriptionInfo with the subId key.
     * @param subId The unique SubscriptionInfo key in database
     * @return SubscriptionInfo, maybe null if not found
     * @hide
     */
    public SubscriptionInfo getSubscriptionInfo(int subId) {
        if (VDBG) {
            logd("[getSubscriptionInfo]+ subId=" + subId);
        }

        if (!isValidSubscriptionId(subId)) {
            logd("[getSubscriptionInfo]- invalid subId, subId = " + subId);
            return null;
        }

        SubscriptionInfo subInfo = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getSubscriptionInfo(subId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return subInfo;
    }

    /**
     * Get the SubscriptionInfo associated with the iccId.
     * @param iccId the IccId of SIM card
     * @return SubscriptionInfo, maybe null if not found
     * @hide
     */
    public SubscriptionInfo getSubscriptionInfoForIccId(String iccId) {
        if (VDBG) {
            logd("[getSubscriptionInfoForIccId]+ iccId=" + iccId);
        }

        if (iccId == null) {
            logd("[getSubscriptionInfoForIccId]- null iccid");
            return null;
        }

        SubscriptionInfo result = null;

        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubscriptionInfoForIccId(iccId);
            }
        } catch (RemoteException ex) {
            // ignore it
        }

        return result;
    }
}
