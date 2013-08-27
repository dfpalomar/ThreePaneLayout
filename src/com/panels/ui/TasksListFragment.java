package com.panels.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.panels.controls.ThreePaneLayout;
import com.panels.controls.ThreePaneLayout.VisibilityState;
import com.panels.R;

public class TasksListFragment extends Fragment implements 
		OnClickListener,  ThreePaneLayout.OnStateChangeListener {

	OnTasksListSizeControlListener mListener;
	
	private ImageView mLeftArrowSizeControl;
	private ImageView mRightArrowSizeControl;
	
	private Handler mHandler = new Handler();

	public TasksListFragment() { /**/ }

	public interface OnTasksListSizeControlListener {
		
		/**
		 * Method invoked when was pressed some redirection arrow on panel.
		 * 
		 * <p><b>NOTA:</b> Arrows are only visible for tablets.   
		 * 
		 * @param leftControl   <i>true</i> si se pulsa la flecha izquierda, <i>false</i>
		 *                      otherwise.
		 */
		void onTasksListSizeControlSelected(boolean leftControl);
	}
	
    public static TasksListFragment newInstance() {
    	return new TasksListFragment();
    }


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = (OnTasksListSizeControlListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_tasks_list, container, false); 
		
		mLeftArrowSizeControl = (ImageView) view.findViewById(R.id.leftArrow);
		mRightArrowSizeControl = (ImageView) view.findViewById(R.id.rightArrow);
		
		mLeftArrowSizeControl.setOnClickListener(this);
		mRightArrowSizeControl.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View view) {
		
		if (view  == mLeftArrowSizeControl) {
			mListener.onTasksListSizeControlSelected(true);
		} else if (view == mRightArrowSizeControl) {
			mListener.onTasksListSizeControlSelected(false);
		}
	}


	@Override
	public void onBeginTransitionState(VisibilityState oldState, VisibilityState newState) { /**/ }


	@Override
	public void onNewStateVisible(final VisibilityState newState) {
		
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				
				if (newState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
					
					mLeftArrowSizeControl.setImageResource(R.drawable.ic_left);
					mRightArrowSizeControl.setImageResource(R.drawable.ic_left);
					
				} else if (newState == VisibilityState.MIDDLE_VISIBLE) {
					
					mLeftArrowSizeControl.setImageResource(R.drawable.ic_right);
					mRightArrowSizeControl.setImageResource(R.drawable.ic_left);
				
				} else if (newState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
					
					mLeftArrowSizeControl.setImageResource(R.drawable.ic_right);
					mRightArrowSizeControl.setImageResource(R.drawable.ic_right);
				} 
			}
			
		}, ThreePaneLayout.ANIMATION_DURATION);
	}
}
