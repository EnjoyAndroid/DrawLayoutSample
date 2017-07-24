package com.lee.drawlayoutsample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.lee.drawlayoutsample.utils.LogUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends FragmentActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";
    private final int DEGREE_0 = 0;
    private final int DEGREE_90 = 90;
    private final int DEGREE_180 = 180;
    private final int DEGREE_270 = 270;
    private ScaleGestureDetector mScaleGestureDetector;
    private ScaleGestureListener mScaleGestureListener = new ScaleGestureListener();

    private Set<View> mViewList = new HashSet<View>();
    private Set<View> mFocusViewList = new HashSet<View>();
    private long downTime = 0;
    private float downX = 0f;
    private float downY = 0f;
    private float downImageX = 0f;
    private float downImageY = 0f;

    private float fdownX = 0f;
    private float fdownY = 0f;

    private int imageW = 0;
    private int imageH = 0;

    private float locationX = 0f;
    private float locationY = 0f;
    private DisplayMetrics mDisplayMetrics;
    private int mStatusBarHeight = 0;
    private FrameLayout mRootView;
    private ImageView mCurrentImageView;

    private Bitmap mLineBitmap;
    private Bitmap mLineBitmapBlack;
    private Bitmap mLineBitmapGreen;
    private Bitmap mLineBitmapRed;


    private List<ViewInfo> mInfoList = new ArrayList<ViewInfo>();
    private FrameLayout mContent;
    private List<View> mRectList;
    private float mCurrentScale = 1f;
    private float mContentDownX = 0f;
    private float mContentDownY = 0f;
    private boolean mMoved = false;
    private boolean mSelectTextTools;
    private ImageView mTextTools;
    private List<Integer> mLefts = new ArrayList<Integer>();
    private List<Integer> mTops = new ArrayList<Integer>();
    private List<Integer> mRights = new ArrayList<Integer>();
    private List<Integer> mBottoms = new ArrayList<Integer>();
    private List<View> mLines = new ArrayList<View>();
    private static final int LIMIT = 50;
    private static final int OFFSET = 100;
    private static final int MARGER_LEFT = 312;
    private static final int MARGER_TOP = 107;
    private static final int PADDING = 20;

    private int mCurrentColor; // 0--黑色，1--绿色，2--红色
    private MementoCreataker mCreataker;
    private MementoOriginator mOriginator;
    private int mCurrentIndex = -1;
    private FrameLayout.LayoutParams mDefaultEditTextParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    private FrameLayout.LayoutParams mContentLayoutParams;
    private int mRealInfoId = 5000;
    private float mStartX;
    private float mStartY;
    private static final int STROKE_WIDTH = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mOriginator = new MementoOriginator();
        mCreataker = new MementoCreataker();


        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mStatusBarHeight = getStatusBarHeight(this);
        mRootView = (FrameLayout) findViewById(R.id.root);
        mContent = (FrameLayout) findViewById(R.id.content);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        mContentLayoutParams = (FrameLayout.LayoutParams) mContent.getLayoutParams();
        mContentLayoutParams.width = mDisplayMetrics.widthPixels - 312;
        mContentLayoutParams.height = mDisplayMetrics.heightPixels - mStatusBarHeight - 107;

        mContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mContentDownX = event.getX();
                        mContentDownY = event.getY();
                        for (View view : mViewList) {
                            if (((ViewInfo) view.getTag()).id == R.id.rectIcon) {
                                Log.d(TAG, "rectIcon onDown...");
                                view.setBackgroundResource(R.drawable.border_shape);
                            } else {
                                Log.i(TAG, "not rectIcon onDown...");
                                view.setBackgroundColor(Color.TRANSPARENT);
                            }
                            if (((ViewInfo) view.getTag()).id == R.id.rectIcon) {
                                view.setOnTouchListener(new MyTouchListener((ImageView) view));
                                int[] location = new int[2];
                                view.getLocationOnScreen(location);
                                Rect rect = new Rect();
                                rect.left = location[0];
                                rect.right = location[0] + view.getWidth();
                                rect.top = location[1];
                                rect.bottom = location[1] + view.getHeight();
                                if (rect.contains((int) event.getX(), (int) event.getY())) {
                                    view.setBackgroundResource(R.drawable.border_shape_blue);
                                }
                            }
                        }
                        mFocusViewList.clear();
                        mMoved = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int w = mContent.getWidth();
                        int h = mContent.getHeight();
                        float maxW = (w * (mCurrentScale - 1f)) / 2;
                        float maxH = (h * (mCurrentScale - 1f)) / 2;
                        if (event.getPointerCount() <= 1 && mCurrentScale > 1f && mMoved) {
                            Log.e(TAG, "move");
                            mMoved = true;
                            float disX = mContentDownX - event.getX();
                            float disY = mContentDownY - event.getY();
                            if (((int) disY + mContent.getScrollY()) >= -maxH
                                    && ((int) disY + mContent.getScrollY()) <= maxH
                                    && ((int) disX + mContent.getScrollX()) >= -maxW
                                    && ((int) disX + mContent.getScrollX()) <= maxW) {
                                mContent.scrollBy((int) disX, (int) disY);
                            }
                            mContentDownX = event.getX();
                            mContentDownY = event.getY();
                            return true;
                        } else {
                            Log.e(TAG, "not move");
                            mMoved = false;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:

                        mMoved = false;

                        if (mSelectTextTools) {

                            float x = event.getRawX();
                            float y = event.getRawY();
                            Log.i(TAG, "x: " + x + "### y：" + y);

                            if (x <= 312 || y <= 106) {
                                Log.d(TAG, "不在画布区域内 x: " + x + "### y：" + y);
                                return true;
                            } else {
                                EditText editText = new EditText(MainActivity.this);
                                editText.requestFocus();
                                editText.setX(x - 312);
                                editText.setY(y - 106);
                                editText.setId(mViewList.size() + 1);
                                ViewInfo viewInfo = new EditTextInfo(editText.getId(), 0);
                                viewInfo.realId = ++mRealInfoId;
                                viewInfo.type = ViewInfo.TYPE_EDITTEXT;
                                viewInfo.x = editText.getX();
                                viewInfo.y = editText.getY();
                                editText.setTag(viewInfo);
//                                editText.setOnTouchListener(new EditTextTouchListener(editText));

                                Log.d(TAG, "editTextId : " + editText.getId());
                                mViewList.add(editText);
                                editText.setBackgroundResource(R.drawable.border_shape_focus);
                                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                                mContent.addView(editText, layoutParams);
                                createMemento(editText, false, true);

                            }

                        }
                        return false;
                    default:
                        break;
                }
                return false;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(this, mScaleGestureListener);
        ImageView allImageView = (ImageView) findViewById(R.id.allIcon);
        allImageView.setOnTouchListener(mTouchListener);
        allImageView.setOnLongClickListener(mLongClickListener);
        ImageView smileImageView = (ImageView) findViewById(R.id.smileIcon);
        smileImageView.setOnTouchListener(mTouchListener);
        smileImageView.setOnLongClickListener(mLongClickListener);
        ImageView jewelryImageView = (ImageView) findViewById(R.id.jewelryIcon);
        jewelryImageView.setOnTouchListener(mTouchListener);
        jewelryImageView.setOnLongClickListener(mLongClickListener);
        ImageView hotImageView = (ImageView) findViewById(R.id.hotIcon);
        hotImageView.setOnTouchListener(mTouchListener);
        hotImageView.setOnLongClickListener(mLongClickListener);

        ImageView lineImageView = (ImageView) findViewById(R.id.lineIcon);
        lineImageView.setOnTouchListener(mTouchListener);
        lineImageView.setOnLongClickListener(mLongClickListener);
        ImageView rect = (ImageView) findViewById(R.id.rectIcon);
        rect.setOnTouchListener(mTouchListener);
        rect.setOnLongClickListener(mLongClickListener);


        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.rotate).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.copy).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.done).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.forward).setOnClickListener(this);
        mTextTools = (ImageView) findViewById(R.id.text);
        mTextTools.setOnClickListener(this);
        mViewList.clear();

        layoutViews();
    }

    private void layoutViews() {

        mViewList.clear();
        mContent.removeAllViews();

        if (mInfoList != null) {
            LogUtils.v("mInfoList : " + mInfoList.toString());
            for (ViewInfo viewInfo : mInfoList) {

                ViewInfo newViewInfo;

                if (viewInfo.type == ViewInfo.TYPE_EDITTEXT) {
                    newViewInfo = new EditTextInfo(viewInfo.id, viewInfo.degree);

                    newViewInfo.type = ViewInfo.TYPE_EDITTEXT;
                    String text = ((EditTextInfo) viewInfo).text;
                    ((EditTextInfo) newViewInfo).text = text;
                    EditText editText = new EditText(MainActivity.this);
                    editText.setTag(newViewInfo);
                    editText.setX(viewInfo.x);
                    editText.setY(viewInfo.y);
                    editText.setText(text);

                    mViewList.add(editText);
                    mContent.addView(editText, mDefaultEditTextParams);

                } else {
                    newViewInfo = new ViewInfo(viewInfo.id, viewInfo.degree);
                    newViewInfo.color = viewInfo.color;

                    mContentLayoutParams = new FrameLayout.LayoutParams(viewInfo.width, viewInfo.height);

                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setOnTouchListener(new MyTouchListener(imageView));
                    imageView.setTag(newViewInfo);
                    imageView.setX(viewInfo.x);
                    imageView.setY(viewInfo.y);
                    setImageResource(imageView, false);
                    if (newViewInfo.degree != 0) {
                        imageView.setRotation(viewInfo.degree);
                    }
                    mViewList.add(imageView);
                    mContent.addView(imageView, mContentLayoutParams);
                }
                newViewInfo.height = viewInfo.height;
                newViewInfo.width = viewInfo.width;
                newViewInfo.x = viewInfo.x;
                newViewInfo.y = viewInfo.y;
                newViewInfo.realId = viewInfo.realId;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mRootView.removeAllViews();

        recycle(mLineBitmap, mLineBitmapBlack, mLineBitmapGreen, mLineBitmapRed);
    }

    private void recycle(Bitmap... bitmaps) {
        if (bitmaps != null && bitmaps.length > 0) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null) bitmap.recycle();
            }
        }
    }

    public void setImageResource(ImageView v, boolean focus) {
        ViewInfo viewInfo = (ViewInfo) v.getTag();
        switch (viewInfo.id) {
            case R.id.allIcon:
                realSetImageResource(v, viewInfo, focus, R.drawable.all_selected, R.drawable.ic_all_black, R.drawable.ic_all_green, R.drawable.ic_all_red);
                break;
            case R.id.smileIcon:
                realSetImageResource(v, viewInfo, focus, R.drawable.smile_selected, R.drawable.ic_smile_black, R.drawable.ic_smile_green, R.drawable.ic_smile_red);
                break;
            case R.id.jewelryIcon:
                realSetImageResource(v, viewInfo, focus, R.drawable.jewelry_selected, R.drawable.ic_jewelry_black, R.drawable.ic_jewelry_green, R.drawable.ic_jewelry_red);
                break;
            case R.id.hotIcon:
                realSetImageResource(v, viewInfo, focus, R.drawable.hot_selected, R.drawable.ic_hot_black, R.drawable.ic_hot_green, R.drawable.ic_hot_red);
                break;

            case R.id.lineIcon:

                if (mLineBitmap == null) {

                    mLineBitmapBlack = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                    mLineBitmapGreen = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                    mLineBitmapRed = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(STROKE_WIDTH);

                    Canvas canvas = new Canvas(mLineBitmapBlack);
                    canvas.drawLine(0, 50, 100, 50, paint);

                    paint.setColor(Color.RED);
                    canvas = new Canvas(mLineBitmapRed);
                    canvas.drawLine(0, 50, 100, 50, paint);

                    paint.setColor(Color.GREEN);
                    canvas = new Canvas(mLineBitmapGreen);
                    canvas.drawLine(0, 50, 100, 50, paint);

                    mLineBitmap = mLineBitmapBlack;

                    if (viewInfo.color == 2) {
                        mLineBitmap = mLineBitmapRed;
                    } else if (mCurrentColor == 1) {
                        mLineBitmap = mLineBitmapGreen;
                    }
                }
                if (focus) {
                    v.setImageResource(R.drawable.line_selected);
                } else {
                    v.setImageBitmap(mLineBitmap);
                }
                break;

            case R.id.rectIcon:
                if (focus) {
                    v.setBackgroundResource(R.drawable.border_shape_focus);
                } else {
                    v.setBackgroundResource(R.drawable.border_shape);
                }
                break;
            default:
                break;
        }
    }


    private void realSetImageResource(ImageView v, ViewInfo viewInfo, boolean focus, int focusId, int blackId, int greenId, int redId) {
        if (focus) {
            v.setImageResource(focusId);
        } else {
            if (viewInfo.color == 2) {
                v.setImageResource(redId);
            } else if (viewInfo.color == 1) {
                v.setImageResource(greenId);
            } else {
                v.setImageResource(blackId);
            }
        }
    }


    private Bitmap getBitmap(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rotate:
                for (View view : mFocusViewList) {
                    LogUtils.d("start x: " + view.getX() + ", y :" + view.getY());
                    ViewInfo viewInfo = (ViewInfo) view.getTag();
                    if (viewInfo.id == R.id.lineIcon || viewInfo.id == R.id.xuxianIcon || viewInfo.id == R.id.rectIcon) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        params.width = view.getHeight();
                        params.height = view.getWidth();
                        view.requestLayout();
                    }
                    float degree = view.getRotation() + 90f;
                    LogUtils.d("degree: " + degree);
                    if (degree >= 360) {
                        degree = 0;
                    }
                    view.setRotation(degree);
                    LogUtils.d("end x: " + view.getX() + ", y :" + view.getY());
                    createMemento(view, false, false);

                }
                break;
            case R.id.clear:
                for (View view : mViewList) {
                    mContent.removeView(view);
                }
                mViewList.clear();
                mContent.requestLayout();
                break;
            case R.id.delete:
                for (View view : new ArrayList<View>(mFocusViewList)) {
                    removeViews(view);
                    createMemento(view, true, false);
                }
                mContent.requestLayout();
                break;
            case R.id.copy:
                LogUtils.i("mFocusViewList size: " + mFocusViewList.size());
                for (View view : mFocusViewList) {
                    copyView(view);
                    if (((ViewInfo) view.getTag()).id == R.id.rectIcon) {
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        int x = location[0];
                        int y = location[1];
                        Rect srcRect = new Rect();
                        srcRect.left = x;
                        srcRect.right = x + view.getWidth();
                        srcRect.top = y;
                        srcRect.bottom = y + view.getHeight();
                        List<View> rectList = getNeedMoveView(srcRect, (ImageView) view);
                        for (View view1 : rectList) {
                            copyView(view1);
                        }
                    }
                }
                break;
            case R.id.cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.done:


                Bitmap bitmap = captureScreen();
                FileOutputStream outputStream = null;
                if (bitmap != null) {
                    try {
                        outputStream = new FileOutputStream(getExternalCacheDir() + "/draw.jpg");
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }

                    }
                }


                mContent.setScaleX(1f);
                mContent.setScaleY(1f);
                mInfoList.clear();
                for (View view : mViewList) {

                    ViewInfo viewInfo = (ViewInfo) view.getTag();
                    ViewInfo newViewInfo;

                    if (viewInfo.type == ViewInfo.TYPE_EDITTEXT) {
                        String string = ((EditText) view).getText().toString();
                        if (TextUtils.isEmpty(string)) {
                            continue;
                        }
                        newViewInfo = new EditTextInfo(viewInfo.id, viewInfo.degree);
                        ((EditTextInfo) newViewInfo).text = ((EditText) view).getText().toString();
                        newViewInfo.type = ViewInfo.TYPE_EDITTEXT;
                    } else {
                        newViewInfo = new ViewInfo(viewInfo.id, viewInfo.degree);
                    }
                    newViewInfo.color = viewInfo.color;
                    newViewInfo.width = view.getWidth();
                    newViewInfo.height = view.getHeight();
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    newViewInfo.x = location[0];
                    newViewInfo.y = location[1];

                    mInfoList.add(newViewInfo);
                }
                Log.d(TAG, "end mInfoList : " + mInfoList.toString());

                finish();
                break;
            case R.id.text:
                mSelectTextTools = !mSelectTextTools;
                if (mSelectTextTools) {
                    mTextTools.setImageResource(R.drawable.text_select);
                } else {
                    mTextTools.setImageResource(R.drawable.text_normal);
                    if (mViewList != null && !mViewList.isEmpty()) {
                        ArrayList<View> removeViews = new ArrayList<>();
                        for (View view : mViewList) {
                            if (view instanceof EditText) {
                                String string = ((EditText) view).getText().toString();
                                if (TextUtils.isEmpty(string)) {
                                    removeViews.add(view);
                                    mContent.removeView(view);
                                    LogUtils.d("移除未输入内容的EditText...");
                                }
                            }
                        }
                        if (!removeViews.isEmpty()) {
                            mViewList.removeAll(removeViews);
                        }
                    }
                }
                break;
            case R.id.back:
                --mCurrentIndex;
                ElementMemento memento = mCreataker.restoreMemento(mCurrentIndex);
                if (memento == null) {
                    ++mCurrentIndex;
                } else {
                    mInfoList.clear();
                    mInfoList.addAll(mOriginator.restoreMemento(memento));
                    mOriginator.printMementos();
                    layoutViews();
                }
                LogUtils.d("back mCurrentIndex: " + mCurrentIndex);
                break;
            case R.id.forward:
                ++mCurrentIndex;
                memento = mCreataker.restoreMemento(mCurrentIndex);
                if (memento == null) {
                    --mCurrentIndex;
                } else {
                    mInfoList.clear();
                    mInfoList.addAll(mOriginator.restoreMemento(memento));
                    layoutViews();
                }
                LogUtils.d("forward mCurrentIndex: " + mCurrentIndex);
                break;

            default:
                break;
        }
    }

    private void removeViews(View view) {
        mContent.removeView(view);
        mViewList.remove(view);
        mFocusViewList.remove(view);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, final MotionEvent event) {
            int action = event.getAction();
            if (mCurrentImageView == null && MotionEvent.ACTION_DOWN != action) {
                return false;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    fdownX = event.getX();
                    fdownY = event.getY();
                    LogUtils.d("fdownX: " + fdownX + " ###fdownY: " + fdownY);
                    getLineCoordinate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float disX = event.getX() - fdownX - OFFSET;
                    float disY = event.getY() - fdownY - OFFSET;
                    LogUtils.d("disX: " + disX + " ###  disY: " + disY + " ###  getX: " + event.getX() + " ###  getY: " + event.getY());
                    mCurrentImageView.setX(mCurrentImageView.getX() + disX);
                    mCurrentImageView.setY(mCurrentImageView.getY() + disY);
                    LogUtils.i("mCurrentImageView.getX(): " + mCurrentImageView.getX() + " ###  mCurrentImageView.getY(): " + mCurrentImageView.getY());
                    fdownX = event.getX() - OFFSET;
                    fdownY = event.getY() - OFFSET;
                    LogUtils.v("fdownX: " + fdownX + " ###  fdownY: " + fdownY);

                    imageW = mCurrentImageView.getWidth();
                    imageH = mCurrentImageView.getHeight();

                    mCurrentImageView.setBackgroundResource(android.R.color.transparent);
                    setImageResource(mCurrentImageView, true);


                    return true;
                case MotionEvent.ACTION_UP:
                    float x = mCurrentImageView.getX();
                    float y = mCurrentImageView.getY();
                    if (x <= 212) {
                        cancelMoveView(x, y);
                        return true;
                    } else {
                        if (x > 212 && x < 312) {
                            x = 312;
                        } else if (x > (mDisplayMetrics.widthPixels - 100)) {
                            x = mDisplayMetrics.widthPixels - 100;
                        }

                        if (y <= 106) {
                            y = 106;
                        } else if (y > mDisplayMetrics.heightPixels - 100 - mStatusBarHeight) {
                            y = mDisplayMetrics.heightPixels - 100 - mStatusBarHeight;
                        }
                        mCurrentImageView.setX(x - 312);
                        mCurrentImageView.setY(y - 107);
                        setImageResource(mCurrentImageView, false);
                        mRootView.removeView(mCurrentImageView);
                        mContent.addView(mCurrentImageView);

                        createMemento(mCurrentImageView, false, true);

                        if (((ViewInfo) mCurrentImageView.getTag()).id == R.id.rectIcon) {
                            mCurrentImageView.setBackgroundResource(R.drawable.border_shape);
                        } else {
                            mCurrentImageView.setBackgroundResource(android.R.color.transparent);
                        }
                    }
                    mCurrentImageView = null;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    float x1 = mCurrentImageView.getX();
                    float y1 = mCurrentImageView.getY();
                    cancelMoveView(x1, y1);
                    break;
            }
            return false;
        }
    };



    private void createMemento(View view, boolean isDelete, boolean isNewElement) {

        if (mInfoList == null) {
            mInfoList = new ArrayList<>();
        }
        mCurrentIndex++;

//        LogUtils.d("add mCurrentIndex: " + mCurrentIndex);

        ViewInfo viewInfo = (ViewInfo) view.getTag();
//        LogUtils.d("viewInfo: " + viewInfo);
        if (isNewElement) { //刚刚从侧边栏拖拽出来的元素
//            LogUtils.d("新增元素");
            viewInfo.width = viewInfo.width == 0 ? view.getWidth() : viewInfo.width;
            viewInfo.height = viewInfo.height == 0 ? view.getHeight() : viewInfo.height;
            viewInfo.x = viewInfo.x == 0 ? (int) view.getX() : viewInfo.x;
            viewInfo.y = viewInfo.y == 0 ? (int) view.getY() : viewInfo.y;
            mInfoList.add(viewInfo);
        } else { //改变现有元素

//            LogUtils.d("改变现有元素");
            if (isDelete) {
//                LogUtils.i("删除元素");
                for (int i = mInfoList.size() - 1; i >= 0; i--) {
                    ViewInfo info = mInfoList.get(i);
                    if (info.id == viewInfo.id && info.realId == viewInfo.realId) {
//                        LogUtils.i("删除重复元素: " + info);
                        mInfoList.remove(info);
                    }
                }
            } else {
                ViewInfo newViewInfo = new ViewInfo(viewInfo.id, view.getRotation());
                newViewInfo.realId = ++mRealInfoId;
                newViewInfo.width = view.getWidth();
                newViewInfo.height = view.getHeight();
                newViewInfo.x = view.getX();
                newViewInfo.y = view.getY();
                newViewInfo.type = viewInfo.type;
                newViewInfo.color = mCurrentColor;

                for (int i = mInfoList.size() - 1; i >= 0; i--) {
                    ViewInfo info = mInfoList.get(i);
                    if (info.id == viewInfo.id && info.realId == viewInfo.realId) {
//                        LogUtils.i("删除重复元素: " + info);
                        mInfoList.remove(info);
                        view.setTag(newViewInfo);
                    }
                }
                mInfoList.add(newViewInfo);
            }
        }

        List<ViewInfo> infos = new ArrayList<>();

        infos.addAll(mInfoList);
        mOriginator.setInfos(infos);
        mCreataker.createMemento(mOriginator.createMemento(), mCurrentIndex);
//        mOriginator.printMementos();
    }

    private void cancelMoveView(float x, float y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, locationX - x, 0, locationY - y);
        translateAnimation.setDuration(200);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRootView.removeView(mCurrentImageView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mCurrentImageView.startAnimation(translateAnimation);
    }

    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final ImageView imageView = new ImageView(MainActivity.this);
            mCurrentImageView = imageView;
            ViewInfo viewInfo = new ViewInfo(v.getId(), 0);
            viewInfo.type = ViewInfo.TYPE_IMAGEVIEW;
            viewInfo.color = mCurrentColor;
            viewInfo.realId = ++mRealInfoId;
            imageView.setTag(viewInfo);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            setImageResource(imageView, true);
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            locationX = location[0];
            locationY = location[1];
            imageView.setX(locationX + 5);
            imageView.setY(locationY + 5);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(v.getWidth(), v.getHeight());
            mRootView.addView(imageView, params);
            mViewList.add(imageView);
            imageView.setOnTouchListener(new MyTouchListener(imageView));
            return true;
        }
    };


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

        switch (checkedId) {
            case R.id.rb_black:
                mCurrentColor = 0;
                mLineBitmap = mLineBitmapBlack;

                break;
            case R.id.rb_green:
                mCurrentColor = 1;
                mLineBitmap = mLineBitmapGreen;

                break;
            case R.id.rb_red:
                mCurrentColor = 2;
                mLineBitmap = mLineBitmapRed;

                break;
            default:
                LogUtils.e("default checkedId: " + checkedId, new IllegalArgumentException());
                break;
        }
        for (View view : mFocusViewList) {
            if (view instanceof ImageView) {
//                ViewInfo viewInfo = (ViewInfo) view.getTag();
//                viewInfo.color = mCurrentColor;
                createMemento(view, false, false);
                setImageResource((ImageView) view, false);
            }
        }
    }


    private class MyTouchListener implements View.OnTouchListener {
        private ImageView imageView;

        MyTouchListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            ViewInfo viewInfo = (ViewInfo) v.getTag();

            float targetX = 0;
            float targetY = 0;
            float targetRight = 0;
            float targetBottom = 0;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = imageView.getX();
                    mStartY = imageView.getY();
                    downX = event.getX();
                    downY = event.getY();
                    if (v.getRotation() == DEGREE_90) {
                        LogUtils.d("90 ");
                        downX = v.getHeight() - event.getY();
                        downY = v.getWidth() - (v.getWidth() - event.getX());
                    } else if (v.getRotation() == DEGREE_180) {
                        LogUtils.d("180 ");
                        downX = v.getWidth() - event.getX();
                        downY = v.getHeight() - event.getY();
                    } else if (v.getRotation() == DEGREE_270) {
                        LogUtils.d("270 ");
                        downX = event.getY();
                        downY = v.getWidth() - event.getX();
                    }
                    LogUtils.d("downX: " + downX + " , downY: " + downY);
                    downTime = System.currentTimeMillis();
                    targetX = downImageX = imageView.getX();
                    targetY = downImageY = imageView.getY();
                    imageW = imageView.getWidth();
                    imageH = imageView.getHeight();

                    targetBottom = targetY + imageH;
                    targetRight = targetY + imageW;

                    int[] location0 = new int[2];
                    imageView.getLocationOnScreen(location0);
                    int x0 = location0[0];
                    int y0 = location0[1];
                    Rect srcRect = new Rect();
                    srcRect.left = x0;
                    srcRect.right = x0 + imageView.getWidth();
                    srcRect.top = y0;
                    srcRect.bottom = y0 + imageView.getHeight();

//                    LogUtils.d("ACTION_DOWN " + (int) targetX + "," + (int) targetY + "," + (int) targetRight + "," + (int) targetBottom);
                    getLineCoordinate();

                    if (viewInfo.id == R.id.rectIcon) {
//                        int[] location = new int[2];
//                        mEditText.getLocationOnScreen(location);
//                        int x = location[0];
//                        int y = location[1];
//                        Rect srcRect = new Rect();
//                        srcRect.left = x;
//                        srcRect.right = x + mEditText.getWidth();
//                        srcRect.top = y;
//                        srcRect.bottom = y + mEditText.getHeight();
                        mRectList = getNeedMoveView(srcRect, imageView);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float x1 = event.getX();
                    float y1 = event.getY();

                    if (v.getRotation() == DEGREE_90) {
                        x1 = v.getHeight() - event.getY();
                        y1 = v.getWidth() - (v.getWidth() - event.getX());
                    } else if (v.getRotation() == DEGREE_180) {
                        x1 = v.getWidth() - event.getX();
                        y1 = v.getHeight() - event.getY();
                    } else if (v.getRotation() == DEGREE_270) {
                        x1 = event.getY();
                        y1 = v.getWidth() - event.getX();
                    }

                    float disX = x1 - downX;
                    float disY = y1 - downY;

                    LogUtils.i("disX : " + (int) disX + ", disY : " + (int) disY);

                    if ((viewInfo.id == R.id.lineIcon || viewInfo.id == R.id.xuxianIcon) && mFocusViewList.contains(imageView)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
//                        if (viewInfo.degree == 0 || viewInfo.degree == 180) {
//                            params.width = imageW + (int) disX;
//                            params.height = imageH + (int) disY;
//                        } else if (viewInfo.degree == 90 || viewInfo.degree == 270) {
//                            params.width = imageW + (int) disX;
//                            params.height = imageH + (int) disY;
//                        } else {
//                        }
                        params.width = imageW + (int) disX;
                        params.height = imageH + (int) disY;
//                        mEditText.setImageBitmap(mLineBitmap);
                        imageView.requestLayout();
                    } else if (viewInfo.id == R.id.rectIcon && mFocusViewList.contains(imageView)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                        params.width = imageW + (int) disX;
                        if (params.width < 5) {
                            params.width = 5;
                        }
                        params.height = imageH + (int) disY;
                        if (params.height < 5) {
                            params.height = 5;
                        }
                        imageView.requestLayout();
                    } else {
                        if (viewInfo.id == R.id.rectIcon) {
                            imageView.setX(imageView.getX() + disX);
                            imageView.setY(imageView.getY() + disY);
                            if (mRectList != null) {
                                for (View view : mRectList) {
                                    view.setX(view.getX() + disX);
                                    view.setY(view.getY() + disY);
                                }
                            }
                        } else {
                            if (mFocusViewList.contains(imageView)) {
                                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                                params.width = imageW + (int) disX;
                                params.height = imageH + (int) disY;
                                imageView.requestLayout();
                            } else { //处理图标拖动
                                float x = imageView.getX();
                                float y = imageView.getY();
//                                LogUtils.d("x: " + x + ", y: " + y);
                                imageView.setX(imageView.getX() + disX - OFFSET);
                                imageView.setY(imageView.getY() + disY - OFFSET);
                                imageView.setBackgroundResource(android.R.color.transparent);

                                targetX = imageView.getX();
                                targetY = imageView.getY();
                                targetRight = targetX + imageW;
                                targetBottom = targetY + imageH;
//                                LogUtils.d("ACTION_MOVE " + (int) targetX + "," + (int) targetY + "," + (int) targetRight + "," + (int) targetBottom);

//                                if (disX >= disY) {   //左右移动
//                                    Log.i(TAG, "左右移动");
                                for (int i = 0; i < mTops.size(); i++) {
                                    float consultTop = mTops.get(i);
                                    View line = mLines.get(i);
                                    ViewInfo lineInfo = (ViewInfo) line.getTag();
                                    if ((lineInfo.degree == 90 || lineInfo.degree == 270) && Math.abs(consultTop - targetBottom) <= LIMIT) { //在竖线上面
                                        int lineMiddleX = (2 * mRights.get(i) - line.getHeight()) / 2;//获取到线条的y/2
                                        if (disX >= 0) { //从左往右
                                            if (Math.abs(mLefts.get(i) - targetRight) <= OFFSET) {
                                                imageView.setX(lineMiddleX - imageW / 2);
                                                imageView.setY(consultTop - imageH + PADDING);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "竖线上边匹配成功 从左往右 targetBottom : " + targetBottom + ", consultTop: " + consultTop);
                                            }
                                        } else {
                                            if (Math.abs(targetX - mRights.get(i)) <= OFFSET) {
                                                imageView.setX(lineMiddleX - imageW / 2);
                                                imageView.setY(consultTop - imageH + PADDING);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "竖线上边匹配成功 从右往左 targetBottom : " + targetBottom + ", consultTop: " + consultTop);
                                            }
                                        }
                                    }
                                }

                                for (int i = 0; i < mBottoms.size(); i++) {
                                    float consultBottom = mBottoms.get(i);
                                    View line = mLines.get(i);
                                    ViewInfo lineInfo = (ViewInfo) line.getTag();
                                    if ((lineInfo.degree == 90 || lineInfo.degree == 270) && Math.abs(targetY - consultBottom) <= LIMIT) {
                                        int lineMiddleX = (2 * mRights.get(i) - line.getHeight()) / 2;//获取到线条的y/2
                                        if (disX >= 0) { //从左往右
                                            if (Math.abs(mLefts.get(i) - targetRight) <= OFFSET) {
                                                imageView.setX(lineMiddleX - imageW / 2);
                                                imageView.setY(consultBottom - PADDING);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.i(TAG, "竖线下边匹配成功 从左往右 targetY : " + targetY + ", consultBottom : " + consultBottom);
                                            }
                                        } else {
                                            if (Math.abs(targetX - mRights.get(i)) <= OFFSET) {
                                                imageView.setX(lineMiddleX - imageW / 2);
                                                imageView.setY(consultBottom - PADDING);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.i(TAG, "竖线下边匹配成功 从右往左 targetY : " + targetY + ", consultBottom : " + consultBottom);
                                            }
                                        }
                                    }
                                }
//                                } else { //上下移动
//                                    Log.d(TAG, "上下移动");
                                for (int i = 0; i < mLefts.size(); i++) {
                                    float consultLeft = mLefts.get(i);
                                    View line = mLines.get(i);
                                    ViewInfo lineInfo = (ViewInfo) line.getTag();
                                    if ((lineInfo.degree == 0 || lineInfo.degree == 180) && Math.abs(consultLeft - targetRight) <= LIMIT) {
                                        int lineMiddleY = (2 * mBottoms.get(i) - line.getHeight()) / 2;//获取到线条的y/2
                                        int newY = lineMiddleY - imageH / 2;
                                        if (disY >= 0) { //从上到下
                                            if (Math.abs(mTops.get(i) - targetBottom) <= OFFSET) {
                                                imageView.setX(consultLeft - imageW + PADDING);
                                                imageView.setY(newY);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "横线左边匹配成功 从上往下 targetRight : " + targetRight + " , consultLeft: " + consultLeft);
                                            }
                                        } else {
                                            if (Math.abs(targetY - mBottoms.get(i)) <= OFFSET) {
                                                imageView.setX(consultLeft - imageW + PADDING);
                                                imageView.setY(newY);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "横线左边匹配成功 从下往上 targetRight : " + targetRight + " , consultLeft: " + consultLeft);
                                            }
                                        }
                                    }
                                }

                                for (int i = 0; i < mRights.size(); i++) {
                                    float consultRight = mRights.get(i);
                                    View line = mLines.get(i);
                                    ViewInfo lineInfo = (ViewInfo) line.getTag();
                                    if ((lineInfo.degree == 0 || lineInfo.degree == 180) && Math.abs(targetX - consultRight) <= LIMIT) {
                                        int lineMiddleY = (2 * mBottoms.get(i) - line.getHeight()) / 2;//获取到线条的y/2
                                        int newY = lineMiddleY - imageH / 2;
                                        Log.d(TAG, "lineMiddleY : " + lineMiddleY + ", newY: " + newY);
                                        if (disY >= 0) { //从上往下 1.如果targetBottom - con
                                            if (Math.abs(mTops.get(i) - targetBottom) <= OFFSET) {
                                                imageView.setX(consultRight - PADDING);
                                                imageView.setY(newY);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "横线右边匹配成功 从上往下 targetX : " + targetX + " , consultRight: " + consultRight);
                                            }
                                        } else {
                                            if (Math.abs(targetY - mBottoms.get(i)) <= OFFSET) {
                                                imageView.setX(consultRight - PADDING);
                                                imageView.setY(newY);
                                                imageView.setBackgroundResource(R.drawable.border_shape_green);
                                                Log.d(TAG, "横线右边匹配成功 从下往上 targetX : " + targetX + " , consultRight: " + consultRight);
                                            }
                                        }

                                    }
                                }
//                                }
                            }
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    float x = imageView.getX();
                    float y = imageView.getY();

                    if (System.currentTimeMillis() - downTime > 1500) {
                        //it's long click
                    } else {
                        if (viewInfo.id == R.id.rectIcon && mRectList != null && !mRectList.isEmpty()) {
                            boolean hasFocusOtherView = false;
                            for (View view : mRectList) {
                                int[] location = new int[2];
                                view.getLocationOnScreen(location);
                                Rect rect = new Rect();
                                rect.left = location[0];
                                rect.right = location[0] + view.getWidth();
                                rect.top = location[1];
                                rect.bottom = location[1] + view.getHeight();

                                imageView.getLocationOnScreen(location);
                                int pX = location[0] + (int) event.getX();
                                int pY = location[1] + (int) event.getY();
                                if (rect.contains(pX, pY)) {
                                    hasFocusOtherView = true;
                                    mFocusViewList.add(view);
                                    view.setBackgroundResource(R.drawable.border_shape_blue);
                                    imageView.setBackgroundResource(R.drawable.border_shape);
                                    mFocusViewList.remove(imageView);
                                    imageView.setOnTouchListener(null);
                                    break;
                                }
                            }

                            if (!hasFocusOtherView) {
                                imageView.setOnTouchListener(new MyTouchListener(imageView));
                                mFocusViewList.add(imageView);
                                imageView.setBackgroundResource(R.drawable.border_shape_blue);
                                if (mRectList != null) {
                                    for (View view : mRectList) {
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        mFocusViewList.remove(view);
                                    }
                                }
                                imageView.requestLayout();
                            }
//                            removeViews(v);
                        } else {
                            mFocusViewList.add(imageView);
                            if (Math.abs(x - downImageX) <= 5 && Math.abs(y - downImageY) <= 5) {
                                imageView.setBackgroundResource(R.drawable.border_shape_blue);
                            }
                        }
                    }
                    if (x < 0) {
                        x = 0;
                        imageView.setX(x);
                    } else if (x > (mDisplayMetrics.widthPixels - 312 - 100)) {
                        x = mDisplayMetrics.widthPixels - 100 - 312;
                        imageView.setX(x);
                    }

                    if (y <= 0) {
                        y = 0;
                        imageView.setY(y);
                    } else if (y > mDisplayMetrics.heightPixels - 100 - mStatusBarHeight - 107) {
                        y = mDisplayMetrics.heightPixels - 100 - mStatusBarHeight - 107;
                        imageView.setY(y);
                    }

                    if (Math.abs(mStartX - x) >= 3 || Math.abs(mStartY - y) >= 3) {
                        createMemento(imageView, false, false);
                    }

                    if (mRectList != null) {
                        mRectList.clear();
                    }
                    return true;
            }
            return false;
        }
    }



    private void getLineCoordinate() {
        mLefts.clear();
        mTops.clear();
        mRights.clear();
        mBottoms.clear();
        mLines.clear();
        for (View view : mViewList) {
            ViewInfo viewInfo = (ViewInfo) view.getTag();
            if (R.id.lineIcon == viewInfo.id || R.id.xuxianIcon == viewInfo.id) {
                mLines.add(view);
//                int[] location = new int[2];
//                view.getLocationOnScreen(location);
//                float x = location[0];
//                float y = location[1];
                int x = (int) view.getX();
                int y = (int) view.getY();
                int bottom = y + view.getHeight();
                int right = x + view.getWidth();
                if (viewInfo.degree == 0 || viewInfo.degree == 180) {
//                    bottom = y + view.getHeight();
//                    right = x + view.getWidth();
                    Log.d(TAG, "横线");
                } else if (viewInfo.degree == 90 || viewInfo.degree == 270) {
//                    right = x + view.getWidth();
//                    bottom = view.getHeight();
                    Log.d(TAG, "竖线");
                } else {
//                    right = x + view.getWidth();
//                    bottom = y + view.getHeight();
                    Log.d(TAG, "其他");
                }

                mLefts.add(x);
                mTops.add(y);
                mRights.add(right);
                mBottoms.add(bottom);
            }
        }

        Log.d(TAG, "mLefts : " + mLefts.toString());
        Log.i(TAG, "mTops : " + mTops.toString());
        Log.d(TAG, "mRights : " + mRights.toString());
        Log.i(TAG, "mBottoms : " + mBottoms.toString());

    }

    public Bitmap captureScreen() {
        // 允许当前窗口保存缓存信息
        for (int i = 0; i < mContent.getChildCount(); i++) {
            View view = mContent.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setCursorVisible(false);
            }
            view.clearFocus();
            view.setBackgroundResource(android.R.color.transparent);
        }
        mContent.setBackgroundResource(android.R.color.white);
        mContent.setDrawingCacheEnabled(true);
        mContent.measure(View.MeasureSpec.makeMeasureSpec((int) (mContent.getWidth() * 1), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((int) (mContent.getHeight() * 1), View.MeasureSpec.EXACTLY));
        mContent.layout(0, 0, mContent.getWidth(), mContent.getHeight());
        mContent.buildDrawingCache();

        // 去掉状态栏
        Bitmap cacheBmp = mContent.getDrawingCache();
        Bitmap resultBmp = null;
        if (null != cacheBmp) {
            int width = (int) (mContent.getWidth() * 1);
            int height = (int) (mContent.getHeight() * 1);
            resultBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(resultBmp);
            Rect srcRect = new Rect();
            srcRect.left = 0;
            srcRect.top = 0;
            srcRect.right = cacheBmp.getWidth();
            srcRect.bottom = cacheBmp.getHeight();

            Rect dstRect = new Rect();
            dstRect.left = 0;
            dstRect.top = 0;
            dstRect.right = width;
            dstRect.bottom = height;
            canvas.drawBitmap(cacheBmp, srcRect, dstRect, new Paint());
            cacheBmp.recycle();
        }

        // 销毁缓存信息
        mContent.setDrawingCacheEnabled(false);
        mContent.destroyDrawingCache();
        return resultBmp;
    }


    private boolean inRange(Rect srcRect, Rect checkRect) {
        return srcRect.contains(checkRect);
    }

    private List<View> getNeedMoveView(Rect srcRect, ImageView imageView) {
        List<View> list = new ArrayList<View>();
        for (View view : mViewList) {
            if (view == imageView) {
                continue;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            Rect checkRect = new Rect();
            checkRect.left = x + 25;
            checkRect.right = x + view.getWidth() - 25;
            checkRect.top = y + 25;
            checkRect.bottom = y + view.getHeight() - 25;
            if (inRange(srcRect, checkRect)) {
                list.add(view);
            }
        }

        return list;
    }

    private void copyView(View view) {

        final ImageView imageView = new ImageView(MainActivity.this);
        ViewInfo tag = (ViewInfo) view.getTag();

        ViewInfo viewInfo = new ViewInfo(tag.id, tag.degree);
        viewInfo.type = tag.type;
        viewInfo.color = tag.color;
        viewInfo.realId = ++mRealInfoId;
        viewInfo.width = tag.width;
        viewInfo.height = tag.height;

        imageView.setTag(viewInfo);

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        setImageResource(imageView, false);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        locationX = location[0];
        locationY = location[1];
        imageView.setX(locationX + 5);
        imageView.setY(locationY + 5);
        imageView.setOnTouchListener(new MyTouchListener(imageView));
        if (viewInfo.degree != 0) {
            imageView.setRotation(viewInfo.degree);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(view.getWidth(), view.getHeight());
        mContent.addView(imageView, params);
        mViewList.add(imageView);

        createMemento(imageView, false, true);
    }

    public class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float dSpan = detector.getCurrentSpan() - detector.getPreviousSpan();
            float dScale = (dSpan * 0.1f) / 100f;
            float scale = mCurrentScale + dScale;
            if (scale > 4.0f) {
                scale = 4.0f;
            } else if (scale < 0.5f) {
                scale = 0.5f;
            }
            mContent.setScaleX(scale);
            mContent.setScaleY(scale);
            mCurrentScale = scale;
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMoved = false;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
