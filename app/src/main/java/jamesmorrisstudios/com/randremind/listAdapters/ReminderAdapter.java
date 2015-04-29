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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.materialuilibrary.recyclemanager.GridSLM;
import com.jamesmorrisstudios.materialuilibrary.recyclemanager.LinearSLM;
import com.jamesmorrisstudios.utilitieslibrary.Utils;

import java.util.ArrayList;

import jamesmorrisstudios.com.randremind.R;

/**
 * Reminder adapter class to manage the recyclerView
 */
public final class ReminderAdapter extends BaseRecycleAdapter {
    public static final String TAG = "ReminderAdapter";

    /**
     * Constructor
     * @param headerMode Header mode to use
     * @param mListener Item Click listener
     */
    public ReminderAdapter(int headerMode, @NonNull OnItemClickListener mListener) {
        super(headerMode, mListener);
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new ReminderViewHolder(view, b, cardClickListener);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.reminder_header_item;
    }

    @Override
    protected int getItemResId() {
        return R.layout.reminder_line_item;
    }
}
