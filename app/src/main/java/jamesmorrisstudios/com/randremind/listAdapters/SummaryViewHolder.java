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

package jamesmorrisstudios.com.randremind.listAdapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.controls.ButtonCircleFlat;
import com.jamesmorrisstudios.appbaselibrary.controls.CircleProgressDeterminate;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.appbaselibrary.math.UtilsMath;
import com.jamesmorrisstudios.appbaselibrary.time.UtilsTime;

import jamesmorrisstudios.com.randremind.R;
import jamesmorrisstudios.com.randremind.reminder.ReminderItem;
import jamesmorrisstudios.com.randremind.reminder.ReminderList;
import jamesmorrisstudios.com.randremind.reminder.ReminderLogDay;

/**
 * Reminder view holder for use in RecyclerView
 */
public final class SummaryViewHolder extends BaseRecycleViewHolder {
    //Header
    private TextView title, hour1, minute1, AM1, PM1, hour2, minute2, AM2, PM2, hour3, minute3, AM3, PM3, dash1, dash2;
    private SwitchCompat enabled;
    private View top1, top2, top3;
    private ButtonCircleFlat[] dayButtons;
    private TextView message;
    //private Toolbar toolbar;

    //Item
    private TextView date, show, acked, percent, shownAgain, snoozed;
    private CircleProgressDeterminate percentImage;

    /**
     * Constructor
     *
     * @param view      Parent view
     * @param isHeader  True if header reminder, false for normal
     * @param mListener Click listener. Null if none desired
     */
    public SummaryViewHolder(@NonNull View view, boolean isHeader, boolean isDummyItem, @Nullable cardClickListener mListener) {
        super(view, isHeader, isDummyItem, mListener);
    }

    @Override
    protected void initHeader(View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.card);
        title = (TextView) view.findViewById(R.id.reminder_title_text);
        topLayout.setOnClickListener(this);
        enabled = (SwitchCompat) view.findViewById(R.id.reminder_enabled);
        message = (TextView) view.findViewById(R.id.message);

        //toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        //toolbar.inflateMenu(R.menu.menu_add_new);

        top1 = view.findViewById(R.id.reminder_time_1);
        hour1 = (TextView) top1.findViewById(R.id.time_hour);
        minute1 = (TextView) top1.findViewById(R.id.time_minute);
        AM1 = (TextView) top1.findViewById(R.id.time_am);
        PM1 = (TextView) top1.findViewById(R.id.time_pm);
        top2 = view.findViewById(R.id.reminder_time_2);
        hour2 = (TextView) top2.findViewById(R.id.time_hour);
        minute2 = (TextView) top2.findViewById(R.id.time_minute);
        AM2 = (TextView) top2.findViewById(R.id.time_am);
        PM2 = (TextView) top2.findViewById(R.id.time_pm);
        top3 = view.findViewById(R.id.reminder_time_3);
        hour3 = (TextView) top3.findViewById(R.id.time_hour);
        minute3 = (TextView) top3.findViewById(R.id.time_minute);
        AM3 = (TextView) top3.findViewById(R.id.time_am);
        PM3 = (TextView) top3.findViewById(R.id.time_pm);
        dash1 = (TextView) view.findViewById(R.id.timing_dash_1);
        dash2 = (TextView) view.findViewById(R.id.timing_dash_2);

        dayButtons = new ButtonCircleFlat[7];
        dayButtons[0] = (ButtonCircleFlat) view.findViewById(R.id.daySun);
        dayButtons[1] = (ButtonCircleFlat) view.findViewById(R.id.dayMon);
        dayButtons[2] = (ButtonCircleFlat) view.findViewById(R.id.dayTue);
        dayButtons[3] = (ButtonCircleFlat) view.findViewById(R.id.dayWed);
        dayButtons[4] = (ButtonCircleFlat) view.findViewById(R.id.dayThu);
        dayButtons[5] = (ButtonCircleFlat) view.findViewById(R.id.dayFri);
        dayButtons[6] = (ButtonCircleFlat) view.findViewById(R.id.daySat);
        String[] week = UtilsTime.getWeekStringFirstLetterArray();
        for (int i = 0; i < week.length; i++) {
            TextView text = dayButtons[i].getTextView();
            if (text != null) {
                text.setText(week[i]);
            }
        }
    }

    @Override
    protected void initItem(View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.card);
        topLayout.setOnClickListener(this);
        date = (TextView) view.findViewById(R.id.date);
        percent = (TextView) view.findViewById(R.id.percentage);
        percentImage = (CircleProgressDeterminate) view.findViewById(R.id.percentage_image);
        show = (TextView) view.findViewById(R.id.shown);
        acked = (TextView) view.findViewById(R.id.acked);
        shownAgain = (TextView) view.findViewById(R.id.shownAgain);
        snoozed = (TextView) view.findViewById(R.id.snoozed);
    }

    @Override
    protected void bindHeader(BaseRecycleItem baseRecycleItem, boolean expanded) {
        final ReminderItem reminder = (ReminderItem) baseRecycleItem;

        String title = reminder.getTitle();
        int lastMessage = UtilsMath.inBoundsInt(0, reminder.getMessageList().size()-1, reminder.getCurMessage());
        if(lastMessage != -1) {
            String messageText = reminder.getMessageList().get(Math.max(0, lastMessage));
            message.setText(messageText);
        }
        if (title == null || title.isEmpty()) {
            title = AppBase.getContext().getString(R.string.title);
        }
        this.title.setText(title);
        if (reminder.isRangeTiming()) {
            UtilsTime.setTime(hour1, minute1, AM1, PM2, reminder.getStartTime());
            UtilsTime.setTime(hour2, minute2, AM2, PM2, reminder.getEndTime());
            dash1.setText(AppBase.getContext().getString(R.string.dash));
            top1.setVisibility(View.VISIBLE);
            top2.setVisibility(View.VISIBLE);
            dash1.setVisibility(View.VISIBLE);
            dash2.setVisibility(View.INVISIBLE);
            top3.setVisibility(View.INVISIBLE);
        } else {
            dash1.setText(AppBase.getContext().getString(R.string.comma));
            if(reminder.getSpecificTimeList().size() == 0) {
                top1.setVisibility(View.INVISIBLE);
                top2.setVisibility(View.INVISIBLE);
                top3.setVisibility(View.INVISIBLE);
            } else {
                top1.setVisibility(View.VISIBLE);
                top2.setVisibility(View.VISIBLE);
                top3.setVisibility(View.VISIBLE);
            }
            if(reminder.getSpecificTimeList().size() >= 1) {
                UtilsTime.setTime(hour1, minute1, AM1, PM1, reminder.getSpecificTimeList().get(0));
            }
            if(reminder.getSpecificTimeList().size() >= 2) {
                UtilsTime.setTime(hour2, minute2, AM2, PM2, reminder.getSpecificTimeList().get(1));
                dash1.setVisibility(View.VISIBLE);
                top2.setVisibility(View.VISIBLE);
            } else {
                dash1.setVisibility(View.INVISIBLE);
                top2.setVisibility(View.INVISIBLE);
            }
            if(reminder.getSpecificTimeList().size() >= 3) {
                UtilsTime.setTime(hour3, minute3, AM3, PM3, reminder.getSpecificTimeList().get(2));
                dash2.setVisibility(View.VISIBLE);
                top3.setVisibility(View.VISIBLE);
            } else {
                dash2.setVisibility(View.INVISIBLE);
                top3.setVisibility(View.INVISIBLE);
            }
        }
        enabled.setOnCheckedChangeListener(null);
        enabled.setChecked(reminder.isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ReminderList.getInstance().setReminderEnable(reminder.getUniqueName(), isChecked);
            }
        });
        for (int i = 0; i < reminder.getDaysToRun().length; i++) {
            setDayOfWeek(i, reminder.getDaysToRun()[i]);
        }
    }

    @Override
    protected void bindItem(BaseRecycleItem baseRecycleItem, boolean expanded) {
        final ReminderLogDay day = (ReminderLogDay) baseRecycleItem;
        if (day.lifetime) {
            date.setText(AppBase.getContext().getString(R.string.lifetime));
            float percentage = (100.0f * day.timesClickedLifetime) / day.timesShownLifetime;
            percent.setText(Integer.toString(Math.round(percentage)) + AppBase.getContext().getResources().getString(R.string.percent_char));
            percentImage.setMax(day.timesShownLifetime);
            percentImage.setProgress(day.timesClickedLifetime);
            show.setText(UtilsMath.formatDisplayNumber(day.timesShownLifetime));
            acked.setText(UtilsMath.formatDisplayNumber(day.timesClickedLifetime));
            shownAgain.setText(UtilsMath.formatDisplayNumber(day.timesShownAgainLifetime));
            snoozed.setText(UtilsMath.formatDisplayNumber(day.timesSnoozedLifetime));
        } else {
            if (day.date.equals(UtilsTime.getDateNow())) {
                date.setText(AppBase.getContext().getResources().getString(R.string.today));
            } else {
                date.setText(UtilsTime.getLongDateFormatted(day.date));
            }
            float percentage = (100.0f * day.getTimesClicked()) / day.getTimesShown();
            percent.setText(Integer.toString(Math.round(percentage)) + AppBase.getContext().getResources().getString(R.string.percent_char));
            percentImage.setMax(day.getTimesShown());
            percentImage.setProgress(day.getTimesClicked());
            show.setText(UtilsMath.formatDisplayNumber(day.getTimesShown()));
            acked.setText(UtilsMath.formatDisplayNumber(day.getTimesClicked()));
            shownAgain.setText(UtilsMath.formatDisplayNumber(day.getTimesShownAgain()));
            snoozed.setText(UtilsMath.formatDisplayNumber(day.getTimesSnoozed()));
        }
    }

    /**
     * Set the active state of the day of week reminder
     *
     * @param dayIndex Index for the day
     * @param active   True to enable
     */
    private void setDayOfWeek(int dayIndex, boolean active) {
        final ButtonCircleFlat dayButton = dayButtons[dayIndex];
        dayButton.setActive(active);
        if (active) {
            dayButton.getTextView().setTextColor(AppBase.getContext().getResources().getColor(R.color.textLightMain));
        } else {
            dayButton.getTextView().setTextColor(AppBase.getContext().getResources().getColor(R.color.textDarkMain));
        }
    }

}
