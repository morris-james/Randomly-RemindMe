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

package jamesmorrisstudios.com.randremind.reminder;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.FileWriter;
import com.jamesmorrisstudios.appbaselibrary.Serializer;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.notification.Notifier;
import com.jamesmorrisstudios.appbaselibrary.preferences.Prefs;
import com.jamesmorrisstudios.appbaselibrary.time.DateTimeItem;
import com.jamesmorrisstudios.appbaselibrary.time.UtilsTime;

import java.util.ArrayList;

import jamesmorrisstudios.com.randremind.R;

/**
 * Reminder list control class. Add, remove, save, delete reminders
 * <p/>
 * Created by James on 4/20/2015.
 */
public final class ReminderList {
    //Constants
    private static final String saveName = "SAVEDATA";
    //Reminder singleton instance
    private static ReminderList instance = null;
    //Load/Save tasks
    AsyncTask<Void, Void, Boolean> taskLoad = null;
    AsyncTask<Void, Void, Boolean> taskSave = null;
    //Reminder Data List
    private ReminderListData reminderListData = null;
    //Handler that accepts a single data item
    private ReminderItem selectedItem = new ReminderItem();
    private int selectedItemIndex = -1;

    /**
     * Required private constructor to maintain singleton
     */
    private ReminderList() {}

    /**
     * @return The singleton instance of the reminderList
     */
    public static ReminderList getInstance() {
        if (instance == null) {
            instance = new ReminderList();
        }
        return instance;
    }

    /**
     * Events to post
     *
     * @param event Enum to post
     */
    private static void postReminderListEvent(@NonNull ReminderListEvent event) {
        Bus.postEnum(event);
    }

    /**
     * Loads the reminder list data on the calling thread instead of an asynctask
     *
     * @return True if successful
     */
    public final boolean loadDataSync() {
        boolean status = loadFromFile();
        if (status) {
            validateSaveData();
        }
        return status;
    }

    /**
     * Loads the reminder list from disk. If already loaded it posts instantly
     * subscribe to Event.DATA_LOAD_PASS and Event.DATA_LOAD_FAIL for callbacks
     *
     * @param forceRefresh True to force reload from disk
     */
    public final void loadData(boolean forceRefresh) {
        if (!forceRefresh && hasReminders()) {
            ReminderList.postReminderListEvent(ReminderListEvent.DATA_LOAD_PASS);
        } else {
            taskLoad = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return loadFromFile();
                }

                @Override
                protected void onPostExecute(Boolean value) {
                    if (value) {
                        validateSaveData();
                        ReminderList.postReminderListEvent(ReminderListEvent.DATA_LOAD_PASS);
                    } else {
                        ReminderList.postReminderListEvent(ReminderListEvent.DATA_LOAD_FAIL);
                    }
                    taskLoad = null;
                }
            };
            taskLoad.execute();
        }
    }

    public final boolean isLoadInProgress() {
        return taskLoad != null;
    }

    /**
     * Saves the reminder list data on the calling thread instead of an asynctask
     *
     * @return True if successful
     */
    public final boolean saveDataSync() {
        return saveToFile();
    }

    /**
     * Saves the reminderItem data back to disk with an asynctask
     * Subscribe to Event.DATA_SAVE_PASS and Event.DATA_SAVE_FAIL for completion events
     */
    public final void saveData() {
        taskSave = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return saveToFile();
            }

            @Override
            protected void onPostExecute(Boolean value) {
                if (value) {
                    ReminderList.postReminderListEvent(ReminderListEvent.DATA_SAVE_PASS);
                } else {
                    ReminderList.postReminderListEvent(ReminderListEvent.DATA_SAVE_FAIL);
                }
                taskSave = null;
            }
        };
        taskSave.execute();
    }

    public final boolean isSaveInProgress() {
        return taskSave != null;
    }

    /**
     * @return True if reminders exist
     */
    public final boolean hasReminders() {
        return reminderListData != null && reminderListData.reminderItemList != null && !reminderListData.reminderItemList.isEmpty();
    }

    @NonNull
    public final ArrayList<ReminderItemSummary> getReminderSummaryList() {
        if(!hasReminders()) {
            return null;
        }
        ArrayList<ReminderItemSummary> list = new ArrayList<>();
        for(ReminderItemData data : reminderListData.reminderItemList) {
            list.add(new ReminderItemSummary(data));
        }
        return list;
    }

    @Nullable
    private ReminderItemData getReminderData(@NonNull String uniqueName) {
        for (ReminderItemData item : reminderListData.reminderItemList) {
            if (item.uniqueName.equals(uniqueName)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Creates and returns a temp ReminderItem that will be GC after the caller is done with it
     * The reminder item data is a copy and no changes will be saved
     * @param uniqueName
     * @return
     */
    @Nullable
    public final ReminderItem getReminderCopy(@NonNull String uniqueName) {
        ReminderItemData item = getReminderData(uniqueName);
        if(item == null) {
            return null;
        }
        ReminderItem itemNew = new ReminderItem();
        itemNew.setReminderItemData(new ReminderItemData(item));
        return itemNew;
    }

    public final void saveReminderItem(ReminderItem reminderItem) {
        ReminderItemData item = getReminderData(reminderItem.getUniqueName());
        if(item == null) {
            return;
        }
        reminderItem.commitChanges(item);
        reminderItem.clearDirty();
    }

    public final void setReminderEnable(@NonNull String uniqueName, boolean enable) {
        ReminderItem reminder = getReminderCopy(uniqueName);
        int index = getReminderIndex(uniqueName);
        if (reminder == null || index == -1) {
            return;
        }
        if(reminder.isEnabled() != enable) {
            reminder.setEnabled(enable);
            if (enable) {
                reminder.rescheduleNextWake(UtilsTime.getDateTimeNow());
            } else {
                reminder.deleteNextWake();
            }
            reminder.commitChanges(reminderListData.reminderItemList.get(index));
            reminder.clearDirty();
        }
    }

    private int getReminderIndex(@NonNull String uniqueName) {
        int index = 0;
        for (ReminderItemData itemInt : reminderListData.reminderItemList) {
            if (itemInt.uniqueName.equals(uniqueName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * @param uniqueName Reminder unique name
     */
    public final void setCurrentReminder(@NonNull String uniqueName) {
        int index = 0;
        for (ReminderItemData itemInt : reminderListData.reminderItemList) {
            if (itemInt.uniqueName.equals(uniqueName)) {
                this.selectedItemIndex = index;
                this.selectedItem.setReminderItemData(new ReminderItemData(itemInt)); //Clone the reminder data
                return;
            }
            index++;
        }
    }

    /**
     * Clears the currently set reminder. If not saved any changes to it will be lost
     */
    public final void clearCurrentReminder() {
        this.selectedItemIndex = -1;
        this.selectedItem.clearReminderItemData();
    }

    /**
     * Resets the current reminder back to the last saved version
     */
    public final void cancelCurrentReminderChanges() {
        if (selectedItemIndex != -1) {
            this.selectedItem.clearReminderItemData();
            this.selectedItem.setReminderItemData(new ReminderItemData(reminderListData.reminderItemList.get(selectedItemIndex)));
        }
    }

    /**
     * Deletes the current reminder from the list.
     * Clears it from the currently set
     */
    public final void deleteCurrentReminder() {
        if (selectedItemIndex != -1) {
            this.selectedItem.deleteReminderLog();
            this.reminderListData.reminderItemList.remove(selectedItemIndex);
        }
        clearCurrentReminder();
    }

    /**
     * Create a new reminder with default values and set it to current
     */
    public final void createNewReminder() {
        clearCurrentReminder();
        String uniqueName = Utils.generateUniqueString();
        selectedItemIndex = reminderListData.reminderItemList.size();
        reminderListData.reminderItemList.add(new ReminderItemData(uniqueName));
        setCurrentReminder(uniqueName);
    }

    /**
     * Displays a notification preview of the current reminder
     */
    public final void previewCurrent() {
        ReminderItem item = getCurrentReminder();
        if (item == null) {
            return;
        }
        Notifier.buildNotification(item.getNotification(true, UtilsTime.getDateTimeNow(), true, null));
    }

    /**
     * Saves the current reminder back to the list.
     * If its a new reminder it is added to the end of the list
     * The current reminder is NOT cleared
     */
    public final boolean saveCurrentReminder() {
        if (selectedItemIndex != -1 && selectedItem.isAnyDirty()) {
            selectedItem.updateAlertTimes();
            selectedItem.rescheduleNextWake(UtilsTime.getDateTimeNow());
            selectedItem.commitChanges(reminderListData.reminderItemList.get(selectedItemIndex));
            selectedItem.clearDirty();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Duplicates the currently selected reminder reminder and moves the new reminder to right after the original.
     * This duplicates the last saved version so ensure it is saved prior to duplication
     * The current reminder stays selected
     */
    public final void duplicateReminder() {
        if(selectedItemIndex == -1) {
            return;
        }
        ReminderItemData newItem = new ReminderItemData(reminderListData.reminderItemList.get(selectedItemIndex), Utils.generateUniqueString());
        reminderListData.reminderItemList.add(selectedItemIndex + 1, newItem);
    }

    /**
     * @return True if a current reminder is set
     */
    public final boolean hasCurrentReminder() {
        return selectedItemIndex != -1;
    }

    /**
     * @return The current reminder, null if none
     */
    @Nullable
    public final ReminderItem getCurrentReminder() {
        return selectedItem;
    }

    /**
     * Updates all reminders wake times
     * This is stored in the Android shared prefs. Not the primary serialized save for this app.
     */
    public final void recalculateWakes() {
        ReminderItem reminderItem = new ReminderItem();
        for (ReminderItemData item : reminderListData.reminderItemList) {
            reminderItem.setReminderItemData(item);
            reminderItem.updateAlertTimes();
            reminderItem.clearReminderItemData();
        }
    }

    public final void scheduleAllWakes(DateTimeItem timeNow) {
        ReminderItem reminderItem = new ReminderItem();
        for (ReminderItemData item : reminderListData.reminderItemList) {
            reminderItem.setReminderItemData(item);
            reminderItem.rescheduleNextWake(timeNow);
            reminderItem.clearReminderItemData();
        }
    }

    public final void logLastWake(@NonNull DateTimeItem dateTime) {
        reminderListData.lastWake = dateTime;
    }

    @NonNull
    public final DateTimeItem getLastWake() {
        return reminderListData.lastWake;
    }

    /**
     * This is to fix any bugs with old save versions.
     */
    private void validateSaveData() {
        if(reminderListData.lastWake == null) {
            reminderListData.lastWake = UtilsTime.getDateTimeNow();
        }
    }

    /**
     * Saves the reminder list to file
     *
     * @return True if successful
     */
    private boolean saveToFile() {
        if (reminderListData != null && reminderListData.reminderItemList != null) {
            reminderListData.version = Utils.getVersionName();
            byte[] bytes = Serializer.serializeClass(reminderListData);
            return bytes != null && FileWriter.writeFile(saveName, bytes, FileWriter.FileLocation.INTERNAL);
        }
        return false;
    }

    /**
     * Loads the reminder list from file
     *
     * @return True if successful
     */
    private boolean loadFromFile() {
        if (!FileWriter.doesFileExist(saveName, FileWriter.FileLocation.INTERNAL)) {
            reminderListData = new ReminderListData();
            return true;
        }
        byte[] bytes = FileWriter.readFile(saveName, FileWriter.FileLocation.INTERNAL);
        if (bytes == null) {
            return false;
        }
        reminderListData = Serializer.deserializeClass(bytes, ReminderListData.class);
        if (reminderListData != null && reminderListData.reminderItemList != null) {
            updateVersion(reminderListData);
            Log.v("ReminderList", "load save pass");
            return true;
        }
        return false;
    }

    private void updateVersion(@NonNull ReminderListData reminders) {
        ReminderItem reminderItem = new ReminderItem();
        for (ReminderItemData item : reminders.reminderItemList) {
            reminderItem.setReminderItemData(item); //Using original not a copy here
            reminderItem.updateVersion();
            reminderItem.clearReminderItemData();
        }
    }

    /**
     * Events
     */
    public enum ReminderListEvent {
        DATA_LOAD_PASS,
        DATA_LOAD_FAIL,
        DATA_SAVE_PASS,
        DATA_SAVE_FAIL
    }

}
