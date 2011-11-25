/*
 * Copyright (c) 2011, Walter Lowe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the author nor the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.lowetek.caltrainalerts.android.activity;

import net.lowetek.caltrainalerts.android.Constants.TrainUpdates;
import net.lowetek.caltrainalerts.android.R;
import net.lowetek.caltrainalerts.android.UpdatesCursorAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

/**
 * @author nopayne
 *
 */
public class AlertsListFragment extends ListFragment
{
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            TrainUpdates._ID,
            TrainUpdates.DATE_HEADER,
            TrainUpdates.DATE,
            TrainUpdates.TEXT
    };
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
        
        Intent intent = getActivity().getIntent();
        intent.setData(TrainUpdates.CONTENT_URI);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = getActivity().managedQuery(intent.getData(), PROJECTION, null, null,
                TrainUpdates.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new UpdatesCursorAdapter(
        		getActivity(), 
        		R.layout.two_line_list_item, 
        		cursor,
                new String[] { TrainUpdates.DATE_HEADER, TrainUpdates.TEXT, TrainUpdates.DATE }, 
                new int[] { R.id.dateHeader, R.id.updateText, R.id.date});
        setListAdapter(adapter);	
        
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.alerts_list, container, false);
	}
}
