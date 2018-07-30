package com.hipla.smartoffice_new.fragment;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.SplashActivity;
import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.services.MapNavigationService;
import com.hipla.smartoffice_new.services.MapNavigationService;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.LocationPoint;
import com.navigine.naviginesdk.LocationView;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.RoutePath;
import com.navigine.naviginesdk.SubLocation;
import com.navigine.naviginesdk.Venue;
import com.navigine.naviginesdk.Zone;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import io.paperdb.Paper;

public class NavigineMapDialogNew extends DialogFragment {
    private static final String TAG = "NAVIGINE.Demo";
    private static final String NOTIFICATION_CHANNEL = "NAVIGINE_DEMO_NOTIFICATION_CHANNEL";
    private static final int UPDATE_TIMEOUT = 100;  // milliseconds
    private static final int ADJUST_TIMEOUT = 5000; // milliseconds
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private static final boolean ORIENTATION_ENABLED = true; // Show device orientation?
    private static final boolean NOTIFICATIONS_ENABLED = true; // Show zone notifications?

    // UI Parameters
    private LocationView mLocationView = null;
    private Button mPrevFloorButton = null;
    private Button mNextFloorButton = null;
    private View mBackView = null;
    private View mPrevFloorView = null;
    private View mNextFloorView = null;
    private View mZoomInView = null;
    private View mZoomOutView = null;
    private View mAdjustModeView = null;
    private TextView mCurrentFloorLabel = null;
    private TextView mErrorMessageLabel = null;
    private Handler mHandler = new Handler();
    private float mDisplayDensity = 0.0f;

    private boolean mAdjustMode = false;
    private long mAdjustTime = 0;

    // Location parameters
    private Location mLocation = null;
    private int mCurrentSubLocationIndex = -1;

    // Device parameters
    private DeviceInfo mDeviceInfo = null; // Current device
    private LocationPoint mPinPoint = null; // Potential device target
    private LocationPoint mTargetPoint = null; // Current device target
    private RectF mPinPointRect = null;

    private Bitmap mVenueBitmap = null;
    private Venue mTargetVenue = null;
    private Venue mSelectedVenue = null;
    private RectF mSelectedVenueRect = null;
    private Zone mSelectedZone = null;
    private BroadcastReceiver mErrorReceiver;
    private BroadcastReceiver mReceiver;
    private View mView;
    private String PointX = null, PointY = null;
    private boolean preDefineLocationSet = true;
    private boolean isMapLoadedSuccessfully = false;
    private LinearLayout ll_drop_down;
    private ImageView img_choose_path;
    private ImageView img_back;
    private boolean isDropDownShowing = false;

    final Runnable mRunnable =
            new Runnable() {
                public void run() {
                    handleDeviceUpdate();
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "NavigineMapDialogNew started");

        super.onCreate(savedInstanceState);

        mView = inflater.inflate(R.layout.fragment_navigine_layout_new, container, false);

        try {
            if (getArguments() != null) {
                PointX = getArguments().getString(MapNavigationService.POINTX);
                PointY = getArguments().getString(MapNavigationService.POINTY);
            }

            getActivity().startService(new Intent(getActivity(), MapNavigationService.class));
            initNavigation(mView);
            setDropDownView(mView);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mView;
    }

    private void setDropDownView(View root) {
        ll_drop_down = (LinearLayout) root.findViewById(R.id.ll_drop_down);
        img_choose_path = (ImageView) root.findViewById(R.id.img_choose_path);
        img_back = (ImageView) root.findViewById(R.id.img_back);

        if (PointX != null && PointY != null)
            img_choose_path.setVisibility(View.GONE);
        else
            img_choose_path.setVisibility(View.VISIBLE);

        img_choose_path.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (!isDropDownShowing) {
                        isDropDownShowing = true;
                        ll_drop_down.setVisibility(View.VISIBLE);
                    } else {
                        isDropDownShowing = false;
                        ll_drop_down.setVisibility(View.GONE);
                    }

                } catch (Exception ex) {

                }
            }
        });

        img_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        root.findViewById(R.id.tv_location1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(22.98f, 18.81f));

            }
        });

        root.findViewById(R.id.tv_location2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(16.17f, 39.93f));

            }
        });

        root.findViewById(R.id.tv_location3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(19.20f, 5.57f));

            }
        });

        root.findViewById(R.id.tv_location4).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(15.46f, 12.77f));

            }
        });

        root.findViewById(R.id.tv_location5).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(22.89f, 30.01f));

            }
        });

        root.findViewById(R.id.tv_location6).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ll_drop_down.setVisibility(View.GONE);
                isDropDownShowing = false;
                onCancelRoute();

                setPredefinePointAndPath(new PointF(22.99f, 24.13f));

            }
        });
    }

    private void initNavigation(View mView) {
        // Setting up GUI parameters
        mBackView = (View) mView.findViewById(R.id.navigation__back_view);
        mPrevFloorButton = (Button) mView.findViewById(R.id.navigation__prev_floor_button);
        mNextFloorButton = (Button) mView.findViewById(R.id.navigation__next_floor_button);
        mPrevFloorView = (View) mView.findViewById(R.id.navigation__prev_floor_view);
        mNextFloorView = (View) mView.findViewById(R.id.navigation__next_floor_view);
        mCurrentFloorLabel = (TextView) mView.findViewById(R.id.navigation__current_floor_label);
        mZoomInView = (View) mView.findViewById(R.id.navigation__zoom_in_view);
        mZoomOutView = (View) mView.findViewById(R.id.navigation__zoom_out_view);
        mAdjustModeView = (View) mView.findViewById(R.id.navigation__adjust_mode_view);
        mErrorMessageLabel = (TextView) mView.findViewById(R.id.navigation__error_message_label);

        mBackView.setVisibility(View.GONE);
        mPrevFloorView.setVisibility(View.INVISIBLE);
        mNextFloorView.setVisibility(View.INVISIBLE);
        mCurrentFloorLabel.setVisibility(View.INVISIBLE);
        mZoomInView.setVisibility(View.INVISIBLE);
        mZoomOutView.setVisibility(View.INVISIBLE);
        mAdjustModeView.setVisibility(View.INVISIBLE);
        mErrorMessageLabel.setVisibility(View.GONE);

        mVenueBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.elm_venue);

        // Initializing location view
        mLocationView = (LocationView) mView.findViewById(R.id.navigation__location_view);
        mLocationView.setBackgroundColor(0xffebebeb);
        mLocationView.setListener
                (
                        new LocationView.Listener() {
                            @Override
                            public void onClick(float x, float y) {
                                //handleClick(x, y);
                            }

                            @Override
                            public void onLongClick(float x, float y) {
                                //handleLongClick(x, y);
                            }

                            @Override
                            public void onScroll(float x, float y, boolean byTouchEvent) {
                                handleScroll(x, y, byTouchEvent);
                            }

                            @Override
                            public void onZoom(float ratio, boolean byTouchEvent) {
                                handleZoom(ratio, byTouchEvent);
                            }

                            @Override
                            public void onDraw(Canvas canvas) {
                                drawZones(canvas);
                                drawPoints(canvas);
                                drawVenues(canvas);
                                drawDevice(canvas);

                                if (isMapLoadedSuccessfully) {
                                    if (preDefineLocationSet) {
                                        if (PointX != null && PointY != null) {
                                            setPredefinePointAndPath(new PointF(Float.parseFloat(PointX), Float.parseFloat(PointY)));
                                        }
                                    }
                                }

                            }
                        }
                );

        // Loading map only when location view size is known
        mLocationView.addOnLayoutChangeListener
                (
                        new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                int width = right - left;
                                int height = bottom - top;
                                if (width == 0 || height == 0)
                                    return;

                                Log.d(TAG, "Layout chaged: " + width + "x" + height);

                                int oldWidth = oldRight - oldLeft;
                                int oldHeight = oldBottom - oldTop;
                                if (oldWidth != width || oldHeight != height)
                                    isMapLoadedSuccessfully = loadMap();

                                if (isMapLoadedSuccessfully) {
                                    if (preDefineLocationSet) {
                                        if (PointX != null && PointY != null) {
                                            setPredefinePointAndPath(new PointF(Float.parseFloat(PointX), Float.parseFloat(PointY)));
                                        }
                                    }
                                }
                            }
                        }
                );

        mDisplayDensity = getResources().getDisplayMetrics().density;

        /*if (NOTIFICATIONS_ENABLED) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26)
                notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, "default",
                        NotificationManager.IMPORTANCE_LOW));
        }*/

        mBackView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelRoute();
            }
        });

        mPrevFloorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrevFloor();
            }
        });

        mNextFloorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextFloor();
            }
        });

        mZoomInView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onZoomIn();
            }
        });

        mZoomOutView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onZoomOut();
            }
        });

        mAdjustModeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdjustMode();
            }
        });
    }

    @Override
    public void onDestroy() {
        /*if (MapNavigationService.mNavigation != null)
        {
            NavigineSDK.finish();
            MapNavigationService.mNavigation = null;
        }*/
        super.onDestroy();
    }

    public void toggleAdjustMode() {
        mAdjustMode = !mAdjustMode;
        mAdjustTime = 0;
        Button adjustModeButton = (Button) mView.findViewById(R.id.navigation__adjust_mode_button);
        adjustModeButton.setBackgroundResource(mAdjustMode ?
                R.drawable.btn_adjust_mode_on :
                R.drawable.btn_adjust_mode_off);
        mLocationView.redraw();
    }

    public void onNextFloor() {
        if (loadNextSubLocation())
            mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    public void onPrevFloor() {
        if (loadPrevSubLocation())
            mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    public void onZoomIn() {
        mLocationView.zoomBy(1.25f);
    }

    public void onZoomOut() {
        mLocationView.zoomBy(0.8f);
    }

    public void onMakeRoute() {
        if (MapNavigationService.mNavigation == null)
            return;

        if (mPinPoint == null)
            return;

        mTargetPoint = mPinPoint;
        mTargetVenue = null;
        mPinPoint = null;
        mPinPointRect = null;

        MapNavigationService.mNavigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.GONE);
        mLocationView.redraw();
    }

    public void onCancelRoute() {
        if (MapNavigationService.mNavigation == null)
            return;

        mTargetPoint = null;
        mTargetVenue = null;
        mPinPoint = null;
        mPinPointRect = null;

        MapNavigationService.mNavigation.cancelTargets();
        mBackView.setVisibility(View.GONE);
        mLocationView.redraw();
    }

    private void handleClick(float x, float y) {
        Log.d(TAG, String.format(Locale.ENGLISH, "Click at (%.2f, %.2f)", x, y));

        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (mPinPoint != null) {
            if (mPinPointRect != null && mPinPointRect.contains(x, y)) {
                mTargetPoint = mPinPoint;
                mTargetVenue = null;
                mPinPoint = null;
                mPinPointRect = null;
                MapNavigationService.mNavigation.setTarget(mTargetPoint);
                mBackView.setVisibility(View.GONE);
                return;
            }
            cancelPin();
            return;
        }

        if (mSelectedVenue != null) {
            if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y)) {
                mTargetVenue = mSelectedVenue;
                mTargetPoint = null;
                MapNavigationService.mNavigation.setTarget(new LocationPoint(mLocation.id, subLoc.id, mTargetVenue.x, mTargetVenue.y));
                mBackView.setVisibility(View.GONE);
            }
            cancelVenue();
            return;
        }

        // Check if we touched venue
        mSelectedVenue = getVenueAt(x, y);
        mSelectedVenueRect = new RectF();

        // Check if we touched zone
        if (mSelectedVenue == null) {
            Zone Z = getZoneAt(x, y);
            if (Z != null)
                mSelectedZone = (mSelectedZone == Z) ? null : Z;
        }

        mLocationView.redraw();
    }

    private void handleLongClick(float x, float y) {
        Log.d(TAG, String.format(Locale.ENGLISH, "Long click at (%.2f, %.2f)", x, y));
        setPredefinePointAndPath(mLocationView.getAbsCoordinates(x, y));
        cancelVenue();
    }

    private void handleScroll(float x, float y, boolean byTouchEvent) {
        if (byTouchEvent)
            mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    private void handleZoom(float ratio, boolean byTouchEvent) {
        if (byTouchEvent)
            mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
    }

    private void handleLeaveZone(Zone z) {
        Log.d(TAG, "Leave zone " + z.name);
        if (NOTIFICATIONS_ENABLED) {
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(z.id);
        }
    }

    private void handleDeviceUpdate() {
        mDeviceInfo = Paper.book().read(MapNavigationService.DEVICE_LOCATION, mDeviceInfo);
        ;
        if (mDeviceInfo == null)
            return;

        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        if (mDeviceInfo.isValid()) {
            cancelErrorMessage();
            mBackView.setVisibility(mTargetPoint != null || mTargetVenue != null ?
                    View.GONE : View.GONE);
            if (mAdjustMode)
                adjustDevice();
        } else {
            mBackView.setVisibility(View.GONE);
            switch (mDeviceInfo.errorCode) {
                case 4:
                    setErrorMessage("You are out of navigation zone! Please, check that your bluetooth is enabled!");
                    break;

                case 8:
                case 30:
                    setErrorMessage("Not enough beacons on the location! Please, add more beacons!");
                    break;

                default:
                    setErrorMessage(String.format(Locale.ENGLISH,
                            "Something is wrong with location '%s' (error code %d)! " +
                                    "Please, contact technical support!",
                            mLocation.name, mDeviceInfo.errorCode));
                    break;
            }
        }

        // This causes map redrawing
        mLocationView.redraw();
    }

    private void setErrorMessage(String message) {
        mErrorMessageLabel.setText(message);
        mErrorMessageLabel.setVisibility(View.VISIBLE);
    }

    private void cancelErrorMessage() {
        mErrorMessageLabel.setVisibility(View.GONE);
    }

    private boolean loadMap() {
        if (MapNavigationService.mNavigation == null) {
            Log.e(TAG, "Can't load map! Navigine SDK is not available!");
            return false;
        }

        mLocation = MapNavigationService.mNavigation.getLocation();
        mCurrentSubLocationIndex = -1;

        if (mLocation == null) {
            Log.e(TAG, "Loading map failed: no location");
            return false;
        }

        if (mLocation.subLocations.size() == 0) {
            Log.e(TAG, "Loading map failed: no sublocations");
            mLocation = null;
            return false;
        }

        if (!loadSubLocation(0)) {
            Log.e(TAG, "Loading map failed: unable to load default sublocation");
            mLocation = null;
            return false;
        }

        if (mLocation.subLocations.size() >= 2) {
            mPrevFloorView.setVisibility(View.VISIBLE);
            mNextFloorView.setVisibility(View.VISIBLE);
            mCurrentFloorLabel.setVisibility(View.VISIBLE);
        }
        mZoomInView.setVisibility(View.VISIBLE);
        mZoomOutView.setVisibility(View.VISIBLE);
        mAdjustModeView.setVisibility(View.VISIBLE);

        MapNavigationService.mNavigation.setMode(NavigationThread.MODE_NORMAL);

        if (MainApplication.WRITE_LOGS) {
            MapNavigationService.mNavigation.setLogFile(getLogFile("log"));
            MapNavigationService.mNavigation.setTrackFile(getLogFile("trk"));
        }

        mLocationView.redraw();
        return true;
    }

    private boolean loadSubLocation(int index) {
        if (MapNavigationService.mNavigation == null)
            return false;

        if (mLocation == null || index < 0 || index >= mLocation.subLocations.size())
            return false;

        SubLocation subLoc = mLocation.subLocations.get(index);
        Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.name, subLoc.width, subLoc.height));

        if (subLoc.width < 1.0f || subLoc.height < 1.0f) {
            Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.width, subLoc.height));
            return false;
        }

        if (!mLocationView.loadSubLocation(subLoc)) {
            Log.e(TAG, "Loading sublocation failed: invalid image");
            return false;
        }

        float viewWidth = mLocationView.getWidth();
        float viewHeight = mLocationView.getHeight();
        float minZoomFactor = Math.min(viewWidth / subLoc.width, viewHeight / subLoc.height);
        float maxZoomFactor = LocationView.ZOOM_FACTOR_MAX;
        mLocationView.setZoomRange(minZoomFactor, maxZoomFactor);
        mLocationView.setZoomFactor(minZoomFactor);
        Log.d(TAG, String.format(Locale.ENGLISH, "View size: %.1f x %.1f", viewWidth, viewHeight));

        mAdjustTime = 0;
        mCurrentSubLocationIndex = index;
        mCurrentFloorLabel.setText(String.format(Locale.ENGLISH, "%d", mCurrentSubLocationIndex));

        if (mCurrentSubLocationIndex > 0) {
            mPrevFloorButton.setEnabled(true);
            mPrevFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
        } else {
            mPrevFloorButton.setEnabled(false);
            mPrevFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
        }

        if (mCurrentSubLocationIndex + 1 < mLocation.subLocations.size()) {
            mNextFloorButton.setEnabled(true);
            mNextFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
        } else {
            mNextFloorButton.setEnabled(false);
            mNextFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
        }

        cancelVenue();
        mLocationView.redraw();
        return true;
    }

    private boolean loadNextSubLocation() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return false;
        return loadSubLocation(mCurrentSubLocationIndex + 1);
    }

    private boolean loadPrevSubLocation() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return false;
        return loadSubLocation(mCurrentSubLocationIndex - 1);
    }

    private void setPredefinePointAndPath(PointF P) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (P.x < 0.0f || P.x > subLoc.width ||
                P.y < 0.0f || P.y > subLoc.height) {
            // Missing the map
            return;
        }

        if (mTargetPoint != null || mTargetVenue != null)
            return;

        if (mDeviceInfo == null || !mDeviceInfo.isValid())
            return;

        mPinPoint = new LocationPoint(mLocation.id, subLoc.id, P.x, P.y);
        mPinPointRect = new RectF();
        onMakeRoute();
        mLocationView.redraw();
        preDefineLocationSet = false;
    }

    private void cancelPin() {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        if (mTargetPoint != null || mTargetVenue != null || mPinPoint == null)
            return;

        mPinPoint = null;
        mPinPointRect = null;
        mLocationView.redraw();
    }

    private void cancelVenue() {
        mSelectedVenue = null;
        mLocationView.redraw();
    }

    private Venue getVenueAt(float x, float y) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return null;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return null;

        Venue v0 = null;
        float d0 = 1000.0f;

        for (int i = 0; i < subLoc.venues.size(); ++i) {
            Venue v = subLoc.venues.get(i);
            PointF P = mLocationView.getScreenCoordinates(v.x, v.y);
            float d = Math.abs(x - P.x) + Math.abs(y - P.y);
            if (d < 30.0f * mDisplayDensity && d < d0) {
                v0 = new Venue(v);
                d0 = d;
            }
        }

        return v0;
    }

    private Zone getZoneAt(float x, float y) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return null;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return null;

        PointF P = mLocationView.getAbsCoordinates(x, y);
        LocationPoint LP = new LocationPoint(mLocation.id, subLoc.id, P.x, P.y);

        for (int i = 0; i < subLoc.zones.size(); ++i) {
            Zone Z = subLoc.zones.get(i);
            if (Z.contains(LP))
                return Z;
        }
        return null;
    }

    private void drawPoints(Canvas canvas) {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        if (subLoc == null)
            return;

        final int solidColor = Color.argb(255, 64, 163, 205);  // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
        final int arrowColor = Color.argb(255, 64, 163, 205); // White color
        final float dp = mDisplayDensity;
        final float textSize = 16 * dp;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Drawing pin point (if it exists and belongs to the current sublocation)
        if (mPinPoint != null && mPinPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
            final float tRadius = 10 * dp;

            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(4 * dp);
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            paint.setColor(solidColor);
            paint.setStrokeWidth(0);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);

            final String text = "Make route";
            final float textWidth = paint.measureText(text);
            final float h = 50 * dp;
            final float w = Math.max(120 * dp, textWidth + h / 2);
            final float x0 = T.x;
            final float y0 = T.y - 75 * dp;

            mPinPointRect.set(x0 - w / 2, y0 - h / 2, x0 + w / 2, y0 + h / 2);

            paint.setColor(solidColor);
            canvas.drawRoundRect(mPinPointRect, h / 2, h / 2, paint);

            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(text, x0 - textWidth / 2, y0 + textSize / 4, paint);
        }

        // Drawing target point (if it exists and belongs to the current sublocation)
        if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
            final float tRadius = 10 * dp;

            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(4 * dp);
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            paint.setColor(solidColor);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
        }
    }

    private void drawVenues(Canvas canvas) {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        final float dp = mDisplayDensity;
        final float textSize = 16 * dp;
        final float venueSize = 30 * dp;
        final int venueColor = Color.argb(255, 0xCD, 0x88, 0x50); // Venue color

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(venueColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        for (int i = 0; i < subLoc.venues.size(); ++i) {
            Venue v = subLoc.venues.get(i);
            if (v.subLocation != subLoc.id)
                continue;

            final PointF P = mLocationView.getScreenCoordinates(v.x, v.y);
            final float x0 = P.x - venueSize / 2;
            final float y0 = P.y - venueSize / 2;
            final float x1 = P.x + venueSize / 2;
            final float y1 = P.y + venueSize / 2;
            canvas.drawBitmap(mVenueBitmap, null, new RectF(x0, y0, x1, y1), paint);
        }

        if (mSelectedVenue != null) {
            final PointF T = mLocationView.getScreenCoordinates(mSelectedVenue.x, mSelectedVenue.y);
            final float textWidth = paint.measureText(mSelectedVenue.name);

            final float h = 50 * dp;
            final float w = Math.max(120 * dp, textWidth + h / 2);
            final float x0 = T.x;
            final float y0 = T.y - 50 * dp;
            mSelectedVenueRect.set(x0 - w / 2, y0 - h / 2, x0 + w / 2, y0 + h / 2);

            paint.setColor(venueColor);
            canvas.drawRoundRect(mSelectedVenueRect, h / 2, h / 2, paint);

            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(mSelectedVenue.name, x0 - textWidth / 2, y0 + textSize / 4, paint);
        }
    }

    private void drawZones(Canvas canvas) {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
            return;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (int i = 0; i < subLoc.zones.size(); ++i) {
            Zone Z = subLoc.zones.get(i);
            if (Z.points.size() < 3)
                continue;

            boolean selected = (Z == mSelectedZone);

            Path path = new Path();
            final LocationPoint P0 = Z.points.get(0);
            final PointF Q0 = mLocationView.getScreenCoordinates(P0);
            path.moveTo(Q0.x, Q0.y);

            for (int j = 0; j < Z.points.size(); ++j) {
                final LocationPoint P = Z.points.get((j + 1) % Z.points.size());
                final PointF Q = mLocationView.getScreenCoordinates(P);
                path.lineTo(Q.x, Q.y);
            }

            int zoneColor = Color.parseColor(Z.color);
            int red = (zoneColor >> 16) & 0xff;
            int green = (zoneColor >> 8) & 0xff;
            int blue = (zoneColor >> 0) & 0xff;
            paint.setColor(Color.argb(selected ? 200 : 100, red, green, blue));
            canvas.drawPath(path, paint);
        }
    }

    private void drawDevice(Canvas canvas) {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Check if navigation is available
        if (mDeviceInfo == null || !mDeviceInfo.isValid())
            return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        if (subLoc == null)
            return;

        final int solidColor = Color.argb(255, 64, 163, 205); // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205); // Semi-transparent light-blue color
        final int arrowColor = Color.argb(255, 64, 163, 205); // White color
        final float dp = mDisplayDensity;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        /// Drawing device path (if it exists)
        if (mDeviceInfo.paths != null && mDeviceInfo.paths.size() > 0) {
            RoutePath path = mDeviceInfo.paths.get(0);
            if (path.points.size() >= 2) {
                paint.setColor(solidColor);

                for (int j = 1; j < path.points.size(); ++j) {
                    LocationPoint P = path.points.get(j - 1);
                    LocationPoint Q = path.points.get(j);
                    if (P.subLocation == subLoc.id && Q.subLocation == subLoc.id) {
                        paint.setStrokeWidth(3 * dp);
                        PointF P1 = mLocationView.getScreenCoordinates(P);
                        PointF Q1 = mLocationView.getScreenCoordinates(Q);
                        canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
                    }
                }
            }
        }

        paint.setStrokeCap(Paint.Cap.BUTT);

        // Check if device belongs to the current sublocation
        if (mDeviceInfo.subLocation != subLoc.id)
            return;

        final float x = mDeviceInfo.x;
        final float y = mDeviceInfo.y;
        final float r = mDeviceInfo.r;
        final float angle = mDeviceInfo.azimuth;
        final float sinA = (float) Math.sin(angle);
        final float cosA = (float) Math.cos(angle);
        final float radius = mLocationView.getScreenLengthX(r);  // External radius: navigation-determined, transparent
        final float radius1 = 16 * dp;                            // Internal radius: fixed, solid

        PointF O = mLocationView.getScreenCoordinates(x, y);
        PointF P = new PointF(O.x - radius1 * sinA * 0.22f, O.y + radius1 * cosA * 0.22f);
        PointF Q = new PointF(O.x + radius1 * sinA * 0.55f, O.y - radius1 * cosA * 0.55f);
        PointF R = new PointF(O.x + radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y + radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);
        PointF S = new PointF(O.x - radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y - radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);

        // Drawing transparent circle
        paint.setStrokeWidth(0);
        paint.setColor(circleColor);
        canvas.drawCircle(O.x, O.y, radius, paint);

        // Drawing solid circle
        paint.setColor(solidColor);
        canvas.drawCircle(O.x, O.y, radius1, paint);

        if (ORIENTATION_ENABLED) {
            // Drawing arrow
            paint.setColor(arrowColor);
            Path path = new Path();
            path.moveTo(Q.x, Q.y);
            path.lineTo(R.x, R.y);
            path.lineTo(P.x, P.y);
            path.lineTo(S.x, S.y);
            path.lineTo(Q.x, Q.y);
            canvas.drawPath(path, paint);
        }
    }

    private void adjustDevice() {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
            return;

        // Check if navigation is available
        if (mDeviceInfo == null || !mDeviceInfo.isValid())
            return;

        long timeNow = System.currentTimeMillis();

        // Adjust map, if necessary
        if (timeNow >= mAdjustTime) {
            // Firstly, set the correct sublocation
            SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
            if (mDeviceInfo.subLocation != subLoc.id) {
                for (int i = 0; i < mLocation.subLocations.size(); ++i)
                    if (mLocation.subLocations.get(i).id == mDeviceInfo.subLocation)
                        loadSubLocation(i);
            }

            // Secondly, adjust device to the center of the screen
            PointF center = mLocationView.getScreenCoordinates(mDeviceInfo.x, mDeviceInfo.y);
            float deltaX = mLocationView.getWidth() / 2 - center.x;
            float deltaY = mLocationView.getHeight() / 2 - center.y;
            mAdjustTime = timeNow;
            mLocationView.scrollBy(deltaX, deltaY);
        }
    }

    private String getLogFile(String extension) {
        try {
            final String extDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Navigine.Demo";
            (new File(extDir)).mkdirs();
            if (!(new File(extDir)).exists())
                return null;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            return String.format(Locale.ENGLISH, "%s/%04d%02d%02d_%02d%02d%02d.%s", extDir,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND),
                    extension);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        IntentFilter intentFilter1 = new IntentFilter(
                "android.intent.action.SUCCESSLOCATION");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                if (!isMapLoadedSuccessfully) {
                    isMapLoadedSuccessfully = loadMap();

                    if (isMapLoadedSuccessfully) {
                        if (preDefineLocationSet) {
                            if (PointX != null && PointY != null) {
                                setPredefinePointAndPath(new PointF(Float.parseFloat(PointX), Float.parseFloat(PointY)));
                            }
                        }
                    }
                }
                mHandler.post(mRunnable);
            }
        };

        mErrorReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                setErrorMessage(intent.getStringExtra("error"));
                mBackView.setVisibility(View.GONE);
            }
        };
        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);
        getActivity().registerReceiver(mErrorReceiver, intentFilter1);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mReceiver != null && mErrorReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            getActivity().unregisterReceiver(mErrorReceiver);
        }
        //mSensorManager.unregisterListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(getActivity()!=null){
            getActivity().stopService(new Intent(getActivity(), MapNavigationService.class));
        }
    }
}
