package com.panels.controls;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.panels.R;

/**
 * <p>Control to display up to three panels with a maximum of two simultaneously visible.</p>
 
 * @author Diego Palomar <dfpalomar@gmail.com>
 *
 */
public class ThreePaneLayout extends LinearLayout {
	
	/**
	 * Time control takes for the state change animation
	 */
	public static final int ANIMATION_DURATION = 300;
	
	private boolean isScrollingViews;
	
	/**
	 * Possible control visibility states. The states are exclusive, ie, if the control is 
	 * in state {@ link LEFT_AND_MIDDLE_VISIBLE} implies that the right pane is not visible.
	 */
	public enum VisibilityState {
		LEFT_VISIBLE,
		LEFT_AND_MIDDLE_VISIBLE, 
		MIDDLE_VISIBLE, 
		MIDDLE_AND_RIGHT_VISIBLE,
		RIGHT_VISIBLE
	}
	
	/**
	 * Interface to be implemented by clients that require to be notified
	 * when the visibility state change.
	 */
	public interface OnStateChangeListener {
		
		/**
		 * Method invoked by the control just prior to the transition state of visibility	
		 * 
		 * @param oldState   Estado actual de visibilidad control
		 * @param newState   Proximo estado de visibilidad del control
		 */
		void onBeginTransitionState(VisibilityState oldState, VisibilityState newState);
		
		/**
		 * Method invoked by the control when its visibility status has been updated
		 * 
		 * @param newState   New visibility state of the control panels
		 */
		void onNewStateVisible(VisibilityState newState);
		
	}
	
	// Reference to the three panels of the control
	private View mLeftView;
	private View mMiddleView;
	private View mRightView;
	
	// Variables that store the minimum and maximum widths that can have different panels
	private int mMinPaneWidth = -1;
	private int mMaxPaneWidth;
	private int mFullScreenWidth;
	
	// Stores the current device orientation
	private int mScreenOrientation;

	// Reference to the current state of the panels
	private VisibilityState mVisibilityState;
		
	/**
	 * Lista de observadores de cambio de estado del control
	 */
	private List<OnStateChangeListener> mStateListeners;
	
	private Handler mHandler = new Handler();
	
	private Context mContext;
	
	public ThreePaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Log.i("Log", "Llamado el constructor del control");
		
		mContext = context;
		
		mScreenOrientation = getResources().getConfiguration().orientation;
		
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {

		setOrientation(HORIZONTAL);
		
	    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThreePaneLayout);
	    String initialState = a.getString(R.styleable.ThreePaneLayout_initialState);
	    
	    if ( initialState != null ) { 
	    	// The initial state has been defined in the XML
	    	
	    	if (initialState.equals("left_visible")) {
	    		mVisibilityState = VisibilityState.LEFT_VISIBLE;
	    	} else if (initialState.equals("left_and_middle_visible")) {
	    		mVisibilityState = VisibilityState.LEFT_AND_MIDDLE_VISIBLE;
	    	} 
	    	
	    } else { 
	    	// The initial state is not defined in the XML, set the default state
	    	
	    	mVisibilityState = VisibilityState.LEFT_AND_MIDDLE_VISIBLE;
	    }
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		if ( getChildCount() != 3 ) {
			throw new IllegalStateException("ThreePaneLayout requires defining three daughters views in the XML");
		}
		
		// Get a reference to the views that make control
		mLeftView   = getChildAt(0);
		mMiddleView = getChildAt(1);
		mRightView  = getChildAt(2);
        
		configureWidth();
	}
	
	/**
	 * Set the weights of each of the views of the layout container.
	 * 
	 * If the method takes no arguments recalculates the weights based on the current  visibility 
	 * state of the control. If this method receive a parameter is used as visibility state from 
	 * which will be held on recalculation
	 * 
	 * @param args
	 */
	private void configureWidth(VisibilityState ... args) {
		
		LayoutParams leftPaneLayoutParams   = (LayoutParams) mLeftView.getLayoutParams();
		LayoutParams middlePaneLayoutParams = (LayoutParams) mMiddleView.getLayoutParams();
		LayoutParams rightPaneLayoutParams  = (LayoutParams) mRightView.getLayoutParams();
		
		VisibilityState visibilityState = args.length == 0 ? mVisibilityState : args[0];
	
		if (args.length > 0) {
			
			leftPaneLayoutParams.width   = 0;
			middlePaneLayoutParams.width = 0;
			rightPaneLayoutParams.width  = 0;
		}
		
		if ( visibilityState == VisibilityState.LEFT_VISIBLE ) {
			
			leftPaneLayoutParams.weight   = 1.0f; 
			middlePaneLayoutParams.weight = 0.0f; 
			rightPaneLayoutParams.weight  = 0.0f; 
			
		} else if ( visibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE ) {

			leftPaneLayoutParams.weight   = 0.35f;
			middlePaneLayoutParams.weight = 0.65f;
			rightPaneLayoutParams.weight  = 0.0f; 
			
		} else if ( visibilityState == VisibilityState.MIDDLE_VISIBLE ) {
			
			leftPaneLayoutParams.weight   = 0.0f; 
			middlePaneLayoutParams.weight = 1.0f;
			rightPaneLayoutParams.weight  = 0.0f;
			
		} else if ( visibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE ) {
			
			leftPaneLayoutParams.weight   = 0.0f;
			middlePaneLayoutParams.weight = 0.35f;
			rightPaneLayoutParams.weight  = 0.65f;
			
		} else if ( visibilityState == VisibilityState.RIGHT_VISIBLE ) {
			
			leftPaneLayoutParams.weight   = 0.0f;
			middlePaneLayoutParams.weight = 0.0f;
			rightPaneLayoutParams.weight  = 1.0f;
		}
		
		// Refresh the view and compute the size of the view in the screen. 
		requestLayout();
	}
	
	/**
	 * Method that performs the visibility state transition control (redistribution of the panels)
	 * 
	 * @param newVisibilityState   New visibility state required.
	 * @param resetDimensions      This parameter is optional and should be used only when you are performing a 
	 *                             state transcion due to a change of device orientation so we can recalculated 
	 *                             weights and widths of the panels.
	 */
	public void setVisibilityState(VisibilityState newVisibilityState, boolean ... resetDimensions) {
		
		// Ignore the request if the control is being resized or if the requested state is equal to the current
		if ( isScrollingViews || newVisibilityState == mVisibilityState ) {
			return;
		}
		
		// If requested any state that contains more than one panel and device orientation is portrait, 
		// ignore the request (this control only supports multiple panels visible in landscape)
		if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
			if ( newVisibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE || 
				 newVisibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE ) {
				return;
			}
		}
		
		if (resetDimensions.length > 0 && resetDimensions[0] == true) {
			
			configureWidth(newVisibilityState);
			mMinPaneWidth = -1;
		}
		
		// Calculate the maximum and minimum widths of the control panel if the have not been defined
		if (mMinPaneWidth == -1) {
			
			DisplayMetrics displayMetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displayMetrics);
			int screenWidth = displayMetrics.widthPixels;
			
			if ( mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
				
				mMinPaneWidth    = (int) (screenWidth * 0.35);
				mMaxPaneWidth    = screenWidth - mMinPaneWidth;
				mFullScreenWidth = screenWidth;
				
			} else { // Configuration.ORIENTATION_PORTRAIT
				
				mMinPaneWidth = mMaxPaneWidth = mFullScreenWidth = screenWidth;
			}
			
			resetWidget(mLeftView, mMinPaneWidth); 
			resetWidget(mMiddleView, mMaxPaneWidth);
			resetWidget(mRightView, mMaxPaneWidth);
			
			requestLayout();
		}
		
		isScrollingViews = true;
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				isScrollingViews = false;
			}
		}, ANIMATION_DURATION + 100);
		
		
		VisibilityState currentVisibilityState = mVisibilityState;
		
		// Notify control observers will produce a state transition 
		if ( mStateListeners != null ) {
			for ( OnStateChangeListener observer : mStateListeners ) {
				observer.onBeginTransitionState(currentVisibilityState, newVisibilityState);
			}
		}
		
		if (resetDimensions.length == 0) {
			// Perform the movement of the panels to match the new state required
			animateVisibilityStateTransition(currentVisibilityState, newVisibilityState);			
		}

		// Update the reference to the current state
		mVisibilityState = newVisibilityState;
		
		// Notify the new control state to the control's observers 
		if ( mStateListeners != null ) {
			for ( OnStateChangeListener observer : mStateListeners ) {
				observer.onNewStateVisible(mVisibilityState);
			}
		}
	}
	
	/**
	 * Moves on the x axis and resize the panels to suit new visibility state required.
	 * 
	 * @param currentVisibilityState    Current state control visibility
	 * @param requiredVisibilityState   New visibility state required
	 */
	private void animateVisibilityStateTransition(VisibilityState currentVisibilityState, 
			VisibilityState requiredVisibilityState) {
		
		switch (requiredVisibilityState) {
		
		case LEFT_VISIBLE:
		
			if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			
				if (currentVisibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
					
					ObjectAnimator.ofInt(this, "leftWidth", mMinPaneWidth, mFullScreenWidth)
							.setDuration(ANIMATION_DURATION).start();
				}
				
			} else { // Configuration.ORIENTATION_PORTRAIT
				
				if (currentVisibilityState == VisibilityState.MIDDLE_VISIBLE) {
					
					translateView(mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
					
				} else if (currentVisibilityState == VisibilityState.RIGHT_VISIBLE) {
					
					translateView(2 * mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
				}
			} 

			break;
			
		case LEFT_AND_MIDDLE_VISIBLE:
			// Este estado solo es posible en orientacion panoramica
			
			if (currentVisibilityState == VisibilityState.MIDDLE_VISIBLE) {
				
				translateView(mMinPaneWidth, mLeftView, mMiddleView, mRightView);
				
				ObjectAnimator.ofInt(this, "middleWidth", mFullScreenWidth, mMaxPaneWidth)
						.setDuration(ANIMATION_DURATION).start();

			} else if (currentVisibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
				
				translateView(mMinPaneWidth, mLeftView, mMiddleView, mRightView);

				ObjectAnimator.ofInt(this, "middleWidth", mMinPaneWidth, mMaxPaneWidth)
						.setDuration(ANIMATION_DURATION).start();
				
			} else if (currentVisibilityState == VisibilityState.LEFT_VISIBLE) {

				ObjectAnimator.ofInt(this, "leftWidth", mFullScreenWidth, mMinPaneWidth)
						.setDuration(ANIMATION_DURATION).start();
			}
			
			break;
			
		case MIDDLE_VISIBLE:
			
			if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) { 
				
				if (currentVisibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
					
					translateView(-1 * mMinPaneWidth, mLeftView, mMiddleView, mRightView);
					
					ObjectAnimator.ofInt(this, "middleWidth", mMaxPaneWidth, mFullScreenWidth)
							.setDuration(ANIMATION_DURATION).start();
				
				} else if (currentVisibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
					
					ObjectAnimator.ofInt(this, "middleWidth", mMinPaneWidth, mFullScreenWidth)
							.setDuration(ANIMATION_DURATION).start();
				}
				
			} else { // Configuration.ORIENTATION_PORTRAIT
				
				if (currentVisibilityState == VisibilityState.LEFT_VISIBLE) {
					
					translateView(-1 * mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
					
				} else if (currentVisibilityState == VisibilityState.RIGHT_VISIBLE) {
					
					translateView(mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
				}
				
			}
			
			break;
			
		case MIDDLE_AND_RIGHT_VISIBLE:
			// Este estado solo es posible en orientacion panoramica
			
			if (currentVisibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
				
				translateView(-1 * mMinPaneWidth, mLeftView, mMiddleView, mRightView);

				ObjectAnimator.ofInt(this, "middleWidth", mMaxPaneWidth, mMinPaneWidth)
						.setDuration(ANIMATION_DURATION).start();

			} else if (currentVisibilityState == VisibilityState.MIDDLE_VISIBLE) {

				ObjectAnimator.ofInt(this, "middleWidth", mFullScreenWidth, mMinPaneWidth)
						.setDuration(ANIMATION_DURATION).start();
				
			
			} else if (currentVisibilityState == VisibilityState.RIGHT_VISIBLE) {
				
				translateView(mMinPaneWidth, mLeftView, mMiddleView, mRightView);

				ObjectAnimator.ofInt(this, "rightWidth", mFullScreenWidth, mMaxPaneWidth)
						.setDuration(ANIMATION_DURATION).start();
			}
			
			break;
			
		case RIGHT_VISIBLE:
			
			if (mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) { 
				
				if (currentVisibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
					
					translateView(-1 * mMinPaneWidth, mLeftView, mMiddleView, mRightView);

					ObjectAnimator.ofInt(this, "rightWidth", mMaxPaneWidth, mFullScreenWidth)
							.setDuration(ANIMATION_DURATION).start();
				}
				
			} else { // Configuration.ORIENTATION_PORTRAIT
				
				if (currentVisibilityState == VisibilityState.LEFT_VISIBLE) {
					
					translateView(-2 * mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
					
				} else if (currentVisibilityState == VisibilityState.MIDDLE_VISIBLE) {
					
					translateView(-1 * mMaxPaneWidth, mLeftView, mMiddleView, mRightView);
				}
			}
			
			break;
		}
	}
	
	public VisibilityState getVisibityState() {
		return mVisibilityState;
	}

	@SuppressWarnings("unused")
	private void setLeftWidth(int value) {
		mLeftView.getLayoutParams().width = value;
		requestLayout();
	}
	
	@SuppressWarnings("unused")
	private void setMiddleWidth(int value) {
		mMiddleView.getLayoutParams().width = value;
		requestLayout();
	}
	
	@SuppressWarnings("unused")
	private void setRightWidth(int value) {
		mRightView.getLayoutParams().width = value;
		requestLayout();
	}

	/**
	 * Moves in the X axis the views received as parameter.
	 *   
	 * @param deltaX   Number of pixels that are shifted in the x-axis views
	 * @param views    Views on which it will move
	 */
	private void translateView(int deltaX, View... views) {
		
		for (final View view : views) {
			
			view.setLayerType(View.LAYER_TYPE_NONE, null);

			view.animate().translationXBy(deltaX).setDuration(ANIMATION_DURATION)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							view.setLayerType(View.LAYER_TYPE_NONE, null);
						}
					});
		}
	}

	/**
	 * Updates the properties of length and weight on the layout of the view passed as parameter 
	 * 
	 * @param view    Vista on which perform the update of the properties
	 * @param width   New width
	 */
	private void resetWidget(View view, int width) {
		
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) view.getLayoutParams();
		p.width = width;
		p.weight = 0;
	}
	
	/**
	 * Adds the component passed as a parameter to the list of control observers. The observers are notified
	 * every time the control change the panels distribution. Observers also receive a notification just before 
	 * starting the transition state by the control.
	 * 
	 * @param observer   component that implements the {@ link OnStateChangeListener} interface
	 *                   to recieve notifications when the visibility control state change
	 */
	public void addStateObserver(OnStateChangeListener observer) {
		
		if ( mStateListeners == null ) {
			mStateListeners = new ArrayList<OnStateChangeListener>();
		}
		
		mStateListeners.add(observer);
	}
	
	/**
	 * Removes the component passed as parameter from the list of observers of 
	 * state change. Observers are added to the list through the method {@ link # addStateObserver}
	 * 
	 * @param observer   component that implements the {@ link OnStateChangeListener} interface
	 *                   to recieve notifications when the visibility control state change
	 */
	public void deleteStatetObserver(OnStateChangeListener observer) {
		
		if ( mStateListeners == null ) return;
		
		mStateListeners.remove(observer);
	}


	/**
	 * Method invoked by the host activity when the orientation of the device has changed. Based on the 
	 * new orientation the control redistributes panels to match the new orientation and improve usability
	 */
	public void deviceOrientationHasChange() 
	{
		int newScreenOrientation = getResources().getConfiguration().orientation;

		VisibilityState currentVisibilityState = getVisibityState();
		VisibilityState newVisibilityState = null;

		if (newScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			// When orientation chamge to landscape the only possible states in the previous orientation 
			// (portrait) can only be those where not coexist multiple views (eg LEFT_VISIBLE, 
			// MIDDLE_VISIBLE, RIGHT_VISIBLE)

			if (currentVisibilityState == VisibilityState.LEFT_VISIBLE) {
				newVisibilityState = VisibilityState.LEFT_AND_MIDDLE_VISIBLE;
			} else if (currentVisibilityState == VisibilityState.MIDDLE_VISIBLE) {
				newVisibilityState = VisibilityState.MIDDLE_VISIBLE;
			} else if (currentVisibilityState == VisibilityState.RIGHT_VISIBLE) {
				newVisibilityState = VisibilityState.RIGHT_VISIBLE;
			}

		} else { // Configuration.ORIENTATION_PORTRAIT

			if (currentVisibilityState == VisibilityState.LEFT_VISIBLE) {
				newVisibilityState = VisibilityState.LEFT_VISIBLE;
			} else if ( currentVisibilityState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE || 
					    currentVisibilityState == VisibilityState.MIDDLE_VISIBLE ) {
				newVisibilityState = VisibilityState.MIDDLE_VISIBLE;
			} else if (currentVisibilityState == VisibilityState.MIDDLE_AND_RIGHT_VISIBLE) {
				newVisibilityState = VisibilityState.RIGHT_VISIBLE;
			} else if (currentVisibilityState == VisibilityState.RIGHT_VISIBLE) {
				newVisibilityState = VisibilityState.RIGHT_VISIBLE;
			}
		}

		mScreenOrientation = newScreenOrientation;
		
		mMinPaneWidth = -1;
		configureWidth(newVisibilityState);
		
		mVisibilityState = newVisibilityState;
	}
		
	public View getLeftView() {
		return mLeftView;
	}

	public View getMiddleView() {
		return mMiddleView;
	}

	public View getRightView() {
		return mRightView;
	}

}