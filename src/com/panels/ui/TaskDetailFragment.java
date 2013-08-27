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

public class TaskDetailFragment extends Fragment implements 
		OnClickListener,  ThreePaneLayout.OnStateChangeListener {
	
	private OnTaskDetailSizeControlListener mListener;
	
	private ImageView mArrowSizeControl;
	
	private Handler mHandler = new Handler();
	
	public TaskDetailFragment() {/**/}

	public interface OnTaskDetailSizeControlListener {
		
		/**
		 * Method invoked when was pressed redirection arrow panel.
		 * 
		 * <p><b>NOTA:</b> The arrow is only visible for tablets.</p>   
		 */
		void onDetailTaskSizeControlSelected();
	}
	
    public static TaskDetailFragment newInstance() {
    	return new TaskDetailFragment();
    }
    
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = (OnTaskDetailSizeControlListener) activity;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_task_detail, container, false);
		
		mArrowSizeControl = (ImageView) view.findViewById(R.id.detailLeftArrow);
		mArrowSizeControl.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View view) {
		
		if (view  == mArrowSizeControl) {
			mListener.onDetailTaskSizeControlSelected();
		} 
	}

	@Override
	public void onBeginTransitionState(VisibilityState oldState, VisibilityState newState) { /**/ }


	@Override
	public void onNewStateVisible(final VisibilityState newState) {
		
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				
				if (newState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
					
					mArrowSizeControl.setImageResource(R.drawable.ic_left);
					
				} else if (newState == VisibilityState.RIGHT_VISIBLE) {
					
					mArrowSizeControl.setImageResource(R.drawable.ic_right);
				}
			}
			
		}, ThreePaneLayout.ANIMATION_DURATION);
	}
}
