package biz.rpcodes.apps.handwritingtest;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;

    // fonts
    ArrayList<Typeface> mFontList;
    private Typeface mCurrentFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        // Initialize Pen.
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled =
                    spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            Toast.makeText(mContext, "This device does not support Spen.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Pen.",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

    // Create PenView.
        RelativeLayout spenViewLayout =
                (RelativeLayout) findViewById(R.id.spenViewLayout);
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);
// Get the dimensions of the screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
// Create SpenNoteDoc.
        try {

            mSpenNoteDoc =
                    new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.",
                    Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
// After adding a page to NoteDoc, get an instance and set it
// as a member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFFFFFFF);
        mSpenPageDoc.clearHistory();
// Set PageDoc to View.
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        CheckBox cb = (CheckBox) findViewById(R.id.cb1);
        boolean checkbox = cb.isChecked();

        // WAS: setToolType(isSpenFeatureEnabled)
        setToolType(checkbox);

        //
        // Tap to zoom etc
        //
        mSpenSurfaceView.setZoomable(false);

                //
        //
//        Checkbox
        //
                //
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setToolType(isChecked);
                TextView t = (TextView) findViewById(R.id.tv1);
                if (isChecked){
                    t.setText(R.string.pen_only_enabled);
                } else {
                    t.setText(R.string.pen_only_disabled);
                }
            }
        });

        //
        // Lights
        ///

        cb = (CheckBox) findViewById(R.id.cbLights);

        lightsOn(cb.isChecked());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lightsOn(isChecked);
            }
        });

        //
        // Font
        //
        initFontTest();
        refreshFont();
    }

    private void lightsOn(boolean checked) {
        if (checked){
            mSpenPageDoc.setBackgroundColor(0xFFFFFFFF);
        } else {
            mSpenPageDoc.setBackgroundColor(0xFF000000);
        }
        mSpenSurfaceView.update();

    }

    private void setToolType(boolean checkbox) {

        // Dont let these interfere with touch vs SPEN
        mSpenSurfaceView.setToolTipEnabled(false);
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_MULTI_TOUCH,
                SpenSurfaceView.ACTION_STROKE);
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_MOUSE,
                SpenSurfaceView.ACTION_NONE);
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_ERASER,
                SpenSurfaceView.ACTION_NONE);
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_SPEN,
                SpenSurfaceView.ACTION_NONE);
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_UNKNOWN,
                SpenSurfaceView.ACTION_NONE);

        if(checkbox == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER,
                    SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext,
                    "Device does not support Spen.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext,
                    "Device supporting Spen.",
                    Toast.LENGTH_SHORT).show();
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_SPEN,
                    SpenSurfaceView.ACTION_STROKE);
//            This activates the zoom in, zoom out, and pan with finger gestures. To
//            disable the zoom and pan features, call SpenSurfaceView.setToolTypeAction() and set
//            TOOL_FINGER to ACTION_NONE
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER,
                    SpenSurfaceView.ACTION_NONE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }
        if(mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    };

    public void initFontTest(){
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/5.ttf");
        mFontList = new ArrayList<Typeface>(5);
        mFontList.add(myTypeface);
        mCurrentFont = mFontList.get(0);
    }

    /**
     * applies new font to
     * whatever elements
     */
    public void refreshFont(){
        TextView t = (TextView) findViewById(R.id.tv1);
        t.setTypeface(mCurrentFont);
        CheckBox c = (CheckBox) findViewById(R.id.cb1);
        c.setTypeface(mCurrentFont);

    }


}
