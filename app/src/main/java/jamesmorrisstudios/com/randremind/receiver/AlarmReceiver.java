/*
 * Copyright (c) 2015.  James Morris Studios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jamesmorrisstudios.com.randremind.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import jamesmorrisstudios.com.randremind.reminder.Notifier;
import jamesmorrisstudios.com.randremind.reminder.ReminderList;
import jamesmorrisstudios.com.randremind.reminder.Scheduler;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            return;
        }

        //Get our wakelock
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        wl.acquire();

        ReminderList.getInstance().loadDataSync(); //TODO might need to make a service for this...

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.v("ALARM RECEIVER", "Device just woke");
            //Set up our midnight recalculate wake
            Scheduler.getInstance().scheduleRepeatingMidnight();
            //Make sure our wakes are cleaned up as we don't know how long we were off for
            ReminderList.getInstance().trimWakesToCurrent();
            //Schedule the next wake event
            Scheduler.getInstance().scheduleNextWake();
        } else if(intent.getExtras().containsKey("REPEAT")){
            Log.v("ALARM RECEIVER", "Midnight update");
            //Recalculate all wakes for a new day
            ReminderList.getInstance().recalculateWakes();
            //Post a notification if we have one (likely don't)
            Notifier.getInstance().postNextNotification();
            //Schedule the next wake event
            Scheduler.getInstance().scheduleNextWake();
        } else {
            Log.v("ALARM RECEIVER", "Reminder!");
            //Post a notification if we have one
            Notifier.getInstance().postNextNotification();
            //Schedule the next wake event
            Scheduler.getInstance().scheduleNextWake();
        }
        //Release the wakelock
        wl.release();
    }

}