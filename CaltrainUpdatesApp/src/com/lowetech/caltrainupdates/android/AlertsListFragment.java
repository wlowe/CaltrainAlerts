/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

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
