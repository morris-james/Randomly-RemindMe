package jamesmorrisstudios.com.randremind.editReminder;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RelativeLayout;

import jamesmorrisstudios.com.randremind.R;
import jamesmorrisstudios.com.randremind.reminder.ReminderItem;
import jamesmorrisstudios.com.randremind.reminder.ReminderList;

/**
 * Created by James on 6/10/2015.
 */
public class EditReminderGeneral {
    private AppCompatEditText title;

    public EditReminderGeneral(RelativeLayout parent) {
        title = (AppCompatEditText) parent.findViewById(R.id.titleText);
    }

    public final void bindItem(EditReminderItem item) {
        final ReminderItem reminderItem = ReminderList.getInstance().getCurrentReminder();
        if(reminderItem == null) {
            return;
        }
        title.setText(reminderItem.title);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(@NonNull Editable s) {
                reminderItem.title = s.toString();
            }
        };
        title.addTextChangedListener(textWatcher);
    }

}
