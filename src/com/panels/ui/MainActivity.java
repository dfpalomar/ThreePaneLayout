package com.panels.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;

import com.panels.R;
import com.panels.controls.ThreePaneLayout;
import com.panels.controls.ThreePaneLayout.OnStateChangeListener;
import com.panels.controls.ThreePaneLayout.VisibilityState;

public class MainActivity extends Activity implements
	CategoriesListFragment.OnCategoriesListSizeControlListener,
	TasksListFragment.OnTasksListSizeControlListener,
	TaskDetailFragment.OnTaskDetailSizeControlListener, OnStateChangeListener {

	// Multipanel control that will contain the fragments associated with the 
	// category list, task list and task detail.
	private ThreePaneLayout mMultiPaneControl;
	
	// Fragments
	private CategoriesListFragment mCategoriesListFragment;
	private TasksListFragment mTasksListFragment;
	private TaskDetailFragment mTaskDetailFragment;

	private int mScreenOrientation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get the initial orientation of the device
		mScreenOrientation = getResources().getConfiguration().orientation;

		mMultiPaneControl = (ThreePaneLayout) findViewById(R.id.multiPaneControl);
		
		mCategoriesListFragment = CategoriesListFragment.newInstance();
		mTasksListFragment      = TasksListFragment.newInstance();
		mTaskDetailFragment     = TaskDetailFragment.newInstance();
		
		// Add the state change observers of the ThreePaneLayout control.
		// The client fragments use the events of the OnstateChangeListener interface to 
		// update the arrows orientation that user can use to redimension the panels
		mMultiPaneControl.addStateObserver(mCategoriesListFragment);
		mMultiPaneControl.addStateObserver(mTasksListFragment);
		mMultiPaneControl.addStateObserver(mTaskDetailFragment);
		
		mMultiPaneControl.addStateObserver(this);
		
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.categoriesList, mCategoriesListFragment, "tag");
		transaction.add(R.id.tasksList, mTasksListFragment);
		transaction.add(R.id.taskDetail, mTaskDetailFragment);
		transaction.commit();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
		mScreenOrientation = getResources().getConfiguration().orientation;
		
		// When device orientationcchange notify the ThreePaneLyout control associated with the activity 
		// to update the distribution of its panels in order to improve usability
	    mMultiPaneControl.deviceOrientationHasChange();
	}
	
	@Override
	public void onCategoriesListSizeControlSelected() {
	
		if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			
			VisibilityState visibilityState = mMultiPaneControl.getVisibityState();
			
			if (visibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE ) {
				mMultiPaneControl.setVisibilityState(VisibilityState.LEFT_VISIBLE);
			} else if ( visibilityState == VisibilityState.LEFT_VISIBLE) {
				mMultiPaneControl.setVisibilityState(VisibilityState.LEFT_AND_MIDDLE_VISIBLE);
			}
			
		} else { // Configuration.ORIENTATION_PORTRAIT
		
			// The only possible state if this event is received while the device in 
			// portrait orientation is MIDDLE_VISIBLE
			mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_VISIBLE);
		}
	}
	
	@Override
	public void onTasksListSizeControlSelected(boolean leftControl) {
		
		VisibilityState visibilityState = mMultiPaneControl.getVisibityState();
		
		if (leftControl) {
			
			if ( mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
				
				if (visibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE ) {
					mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_VISIBLE);
				} else if (visibilityState == VisibilityState.MIDDLE_VISIBLE ) {
					mMultiPaneControl.setVisibilityState(VisibilityState.LEFT_AND_MIDDLE_VISIBLE);
				} else if (visibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE ) {
					mMultiPaneControl.setVisibilityState(VisibilityState.LEFT_AND_MIDDLE_VISIBLE);
				}	
				
			} else { // Configuration.ORIENTATION_PORTRAIT
				
				// The only possible state if this event is received while the device in 
				// portrait orientation is LEFT_VISIBLE
				mMultiPaneControl.setVisibilityState(VisibilityState.LEFT_VISIBLE);
			}
			
		} else {
			
			if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
				
				if (visibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
					mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_AND_RIGHT_VISIBLE);
				} else if (visibilityState == VisibilityState.MIDDLE_VISIBLE) {
					mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_AND_RIGHT_VISIBLE);
				} else if (visibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
					mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_VISIBLE);
				}	
				
			} else { // Configuration.ORIENTATION_PORTRAIT
			
				// The only possible state if this event is received while the device in 
				// portrait orientation is RIGHT_VISIBLE
				mMultiPaneControl.setVisibilityState(VisibilityState.RIGHT_VISIBLE);
			}
		}
	}
	
	@Override
	public void onDetailTaskSizeControlSelected() {
		
		VisibilityState visibilityState = mMultiPaneControl.getVisibityState();
		
		if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			
			if ( visibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
				mMultiPaneControl.setVisibilityState(VisibilityState.RIGHT_VISIBLE);
			} else if ( visibilityState == VisibilityState.RIGHT_VISIBLE) {
				mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_AND_RIGHT_VISIBLE);
			}	
			
		} else { // Configuration.ORIENTATION_PORTRAIT
		
			// The only possible state if this event is received while the device in 
			// portrait orientation is MIDDLE_VISIBLE
			mMultiPaneControl.setVisibilityState(VisibilityState.MIDDLE_VISIBLE); 
		}
	}
	

	@Override
	public void onBeginTransitionState(VisibilityState oldState,
			VisibilityState newState) {
	}

	@Override
	public void onNewStateVisible(VisibilityState newState) { /**/ }
}