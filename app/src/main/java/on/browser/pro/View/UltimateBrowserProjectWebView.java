package on.browser.pro.View;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.MailTo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import org.xdevs23.debugUtils.Logging;

import java.net.URISyntaxException;

import on.browser.pro.Activity.BrowserActivity;
import on.browser.pro.Browser.AdBlock;
import on.browser.pro.Browser.AlbumController;
import on.browser.pro.Browser.BrowserController;
import on.browser.pro.Browser.UltimateBrowserProjectClickHandler;
import on.browser.pro.Browser.UltimateBrowserProjectDownloadListener;
import on.browser.pro.Browser.UltimateBrowserProjectGestureListener;
import on.browser.pro.Browser.UltimateBrowserProjectJavaScriptInterface;
import on.browser.pro.Browser.UltimateBrowserProjectWebChromeClient;
import on.browser.pro.Browser.UltimateBrowserProjectWebViewClient;
import on.browser.pro.Database.Record;
import on.browser.pro.Database.RecordAction;
import on.browser.pro.R;
import on.browser.pro.Unit.BrowserUnit;
import on.browser.pro.Unit.IntentUnit;
import on.browser.pro.Unit.ViewUnit;

import static on.browser.pro.Activity.BrowserActivity.anchor;
import static on.browser.pro.Activity.BrowserActivity.getTabSwitcher;
import static on.browser.pro.Activity.BrowserActivity.omnibox;

public class UltimateBrowserProjectWebView extends WebView implements AlbumController {

    private static final float[] NEGATIVE_COLOR = {
            -1.0f, 0, 0, 0, 255, // Red
            0, -1.0f, 0, 0, 255, // Green
            0, 0, -1.0f, 0, 255, // Blue
            0, 0, 0, 1.0f, 0     // Alpha
    };

    public static final int
            DEFAULT_ANIMATION_DURATION = 420
            ;

    public static int
            oh   = 0,
            cy   = 0,
            ny   = 0,
            opos = 0;

    private Context context;
    private int flag = BrowserUnit.FLAG_UltimateBrowserProject;
    private int dimen144dp,
                dimen108dp;
    private int animTime;
    private float y1;


    public boolean isInitialized = false;

    @SuppressWarnings("unused")
    String url;

    @SuppressWarnings("unused")
    static String TAG = "UltimateBrowserProject";

    private Album album;
    private UltimateBrowserProjectWebViewClient webViewClient;
    private UltimateBrowserProjectWebChromeClient webChromeClient;
    private UltimateBrowserProjectDownloadListener downloadListener;
    private UltimateBrowserProjectClickHandler clickHandler;
    public  UltimateBrowserProjectJavaScriptInterface jsInterface;
    private GestureDetector gestureDetector;

    private static UltimateBrowserProjectWebView thisWebView = null;

    private AdBlock adBlock;
    public AdBlock getAdBlock() {
        return adBlock;
    }

    private boolean foreground;
    public boolean isForeground() {
        return foreground;
    }

    private String userAgentOriginal;
    @SuppressWarnings("unused")
    public String getUserAgentOriginal() {
        return userAgentOriginal;
    }

    private BrowserController browserController = null;
    public BrowserController getBrowserController() {
        return browserController;
    }
    public void setBrowserController(BrowserController browserController) {
        this.browserController = browserController;
        this.album.setBrowserController(browserController);
    }

    public UltimateBrowserProjectWebView(Context context) {
        super(context); // Cannot create a dialog, the WebView context is not an Activity.

        this.context = context;
        this.dimen144dp = getResources().getDimensionPixelSize(R.dimen.layout_width_144dp);
        this.dimen108dp = getResources().getDimensionPixelSize(R.dimen.layout_height_108dp);
        this.animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        this.foreground = false;

        this.adBlock = new AdBlock(this.context);
        this.album = new Album(this.context, this, this.browserController);
        this.webViewClient = new UltimateBrowserProjectWebViewClient(this);
        this.webChromeClient = new UltimateBrowserProjectWebChromeClient(this);
        this.downloadListener = new UltimateBrowserProjectDownloadListener(this.context);
        this.clickHandler = new UltimateBrowserProjectClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new UltimateBrowserProjectGestureListener(this));
        this.jsInterface = new UltimateBrowserProjectJavaScriptInterface();

        initWebView();
        initWebSettings();
        initPreferences();
        initAlbum();
    }

    public void setWebViewCustomLayoutParams(boolean iInit) {
        if (iInit) {
            Logging.logd("Setting WebView custom layout params...");
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            oh = ViewUnit.goh(context);
            p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            if (BrowserActivity.anchor == 0) {
                p.setMargins(0, 0, 0, (BrowserActivity.fullscreen ?
                        -(ViewUnit.getStatusBarHeight(context)) : 0));
            } else {
                p.setMargins(0, 0, 0,
                        (BrowserActivity.fullscreen ? -(ViewUnit.getStatusBarHeight(context)) : 0));
            }

            p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            this.setMinimumHeight(ViewUnit.getAdjustedWindowHeight(context) + (BrowserActivity.fullscreen ?
                    ViewUnit.getStatusBarHeight(context) : 0));

            p.height = this.getMinimumHeight();

            this.setLayoutParams(p);


            this.setMinimumWidth(ViewUnit.getWindowWidth(context));
        }
    }

    public void setWebViewCustomLayoutParams() {
        setWebViewCustomLayoutParams(isInitialized);
    }

    public static boolean isBottom() {
        return anchor == 1;
    }

    public static boolean isTop() {
        return !isBottom();
    }

    public static int getOmniboxPositionInt() {
        return anchor;
    }

    protected static ViewPropertyAnimator omniboxAnimate() {
        return omnibox.animate().setDuration(DEFAULT_ANIMATION_DURATION);
    }

    protected static ViewPropertyAnimator webAnimate() {
        return BrowserActivity.getUbpWebView().animate().setDuration(DEFAULT_ANIMATION_DURATION);
    }

    public static void moveOmni(int posY) {
        if(isBottom()) return;
        float mov = (float) posY;
        omnibox.bringToFront();
        omnibox.setTranslationY(mov);
        BrowserActivity.getUbpWebView().setTranslationY(
                mov + omnibox.getHeight()
        );
    }

    public static void animateOmni(int posY) {
        float mov = (float) posY;
        if(mov > 0) omnibox.bringToFront();
        if(posY == 0 && isTop()) opos = 0; cy = 0; ny = 0;
        omniboxAnimate().translationY(mov + (isBottom() ? oh : 0))
                // This listener is to fix glitches
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        omnibox.bringToFront();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        omnibox.bringToFront();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // Not necessary
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // Not necessary
                    }
                });
        if(isTop())
            BrowserActivity.getUbpWebView()
                    .setTranslationY((mov + omnibox.getHeight()));
    }

    public static void resetOmni() {
        omnibox.bringToFront();
        animateOmni(isBottom() ? -oh : 0);
    }


    @SuppressWarnings("deprecation")
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    private synchronized void initWebView() {
        thisWebView = this;

        setWebViewCustomLayoutParams(true);

        setAlwaysDrawnWithCacheEnabled(true);
        setAnimationCacheEnabled(true);
        setDrawingCacheBackgroundColor(0x00000000);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        setSaveEnabled(true);

        getRootView().setBackgroundColor(context.getResources().getColor(R.color.white));
        setBackgroundColor(context.getResources().getColor(R.color.white));

        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setScrollbarFadingEnabled(true);

        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        setDownloadListener(downloadListener);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            addJavascriptInterface(jsInterface, "JsIface");

        setOnTouchListener(new OnTouchListener() {
            int oh = ViewUnit.goh(context),
                    ym1 = 0, ym2 = 0, lastM = 0, cpo = 0, cpwo = oh, lagf = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view != null && !view.hasFocus())
                    view.requestFocus();

                BrowserActivity.omnibox.setVisibility(View.VISIBLE);

                int action = motionEvent.getAction();
                y1 = motionEvent.getY();

                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getTabSwitcher().collapse();
                        if(-opos <= oh / 2) cy = (int)motionEvent.getRawY();
                        else cy = (int)motionEvent.getRawY() - opos;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(-opos > oh / 2) {
                            animateOmni( (isBottom() ? 0 : -oh) );
                            opos =       (isBottom() ? 0 : -oh)  ;
                        } else {
                            animateOmni( (isBottom() ? -oh : 0) );
                            opos =       (isBottom() ? -oh : 0)  ;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        ny = ((int)motionEvent.getRawY()) - cy;
                        opos = ny;
                        if(opos > 0  ) opos = 0;
                        if(opos < -oh) opos = -oh;

                        moveOmni(opos);
                        break;
                    default: break;
                }

                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });

        isInitialized = true;
    }

    private synchronized void initWebSettings() {
        WebSettings webSettings = getSettings();
        userAgentOriginal = webSettings.getUserAgentString();

        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getCacheDir().toString());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath(context.getFilesDir().toString());

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setDefaultTextEncodingName(BrowserUnit.URL_ENCODING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public synchronized void initPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        WebSettings webSettings = getSettings();

        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);

        webSettings.setBlockNetworkImage(!sp.getBoolean(context.getString(R.string.sp_images), true));
        webSettings.setJavaScriptEnabled(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setGeolocationEnabled(sp.getBoolean(context.getString(R.string.sp_location), true));
        webSettings.setSupportMultipleWindows(sp.getBoolean(context.getString(R.string.sp_multiple_windows), false));
        webSettings.setSaveFormData(sp.getBoolean(context.getString(R.string.sp_passwords), true));

        boolean textReflow = sp.getBoolean(context.getString(R.string.sp_text_reflow), true);
        if (textReflow) {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                } catch (Exception e) {
                    /* Do nothing */
                }
            }
        } else webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);


        int userAgent = Integer.valueOf(sp.getString(context.getString(R.string.sp_user_agent), "0"));
        if (userAgent == 1)
            webSettings.setUserAgentString(BrowserUnit.UA_DESKTOP);
        else if (userAgent == 2)
            webSettings.setUserAgentString(sp.getString(context.getString(R.string.sp_user_agent_custom), userAgentOriginal));
        else
            webSettings.setUserAgentString(userAgentOriginal);


        int mode = Integer.valueOf(sp.getString(context.getString(R.string.sp_rendering), "0"));
        initRendering(mode);

        webViewClient.enableAdBlock(sp.getBoolean(context.getString(R.string.sp_ad_block), true));
    }

    private synchronized void initAlbum() {
        album.setAlbumCover(null);
        album.setAlbumTitle(context.getString(R.string.album_untitled));
        album.setBrowserController(browserController);
    }

    private void initRendering(int mode) {
        Paint paint = new Paint();

        switch (mode) {
            case 0: { // Default
                paint.setColorFilter(null);
                break;
            } case 1: { // Grayscale
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                paint.setColorFilter(filter);

                break;
            } case 2: { // Inverted
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(NEGATIVE_COLOR);
                paint.setColorFilter(filter);
                break;
            } case 3: { // Inverted grayscale
                ColorMatrix matrix = new ColorMatrix();
                matrix.set(NEGATIVE_COLOR);

                ColorMatrix gcm = new ColorMatrix();
                gcm.setSaturation(0);

                ColorMatrix concat = new ColorMatrix();
                concat.setConcat(matrix, gcm);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(concat);
                paint.setColorFilter(filter);
                break;
            } default: {
                paint.setColorFilter(null);
                break;
            }
        }

        // maybe sometime LAYER_TYPE_NONE would better?
        setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }


    @Override
    public synchronized void loadUrl(String url) {

        if (url == null || url.length() == 0) {
            UltimateBrowserProjectToast.show(context, R.string.toast_load_error);
            return;
        }

        url = BrowserUnit.queryWrapper(context, url.trim());

        if (url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)) {
            Intent intent = IntentUnit.getEmailIntent(MailTo.parse(url));
            context.startActivity(intent);
            reload();

            return;
        } else if (url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                context.startActivity(intent);
            } catch (URISyntaxException u) {
                /* Do nothing */
            }

            return;
        }

        webViewClient.updateWhite(adBlock.isWhite(url));

        Logging.logd("Loading url " + url);

        setWebViewCustomLayoutParams();

        super.loadUrl(url);
        if (browserController != null && foreground)
            browserController.updateBookmarks();

    }

    @Override
    public void reload() {
        webViewClient.updateWhite(adBlock.isWhite(getUrl()));
        super.reload();
    }

    @Override
    public int getFlag() {
        return flag;
    }

    @Override
    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public View getAlbumView() {
        return album.getAlbumView();
    }

    @Override
    public void setAlbumCover(Bitmap bitmap) {
        album.setAlbumCover(bitmap);
    }

    @Override
    public String getAlbumTitle() {
        return album.getAlbumTitle();
    }

    @Override
    public void setAlbumTitle(String title) {
        album.setAlbumTitle(title);
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public synchronized void activate() {
        requestFocus();
        foreground = true;
        album.activate();
    }

    @Override
    public synchronized void deactivate() {
        clearFocus();
        foreground = false;
        album.deactivate();
    }

    public synchronized void update(int progress) {
        if (foreground) {
            browserController.updateProgress(progress);
        }

        setAlbumCover(ViewUnit.capture(this, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        if (isLoadFinish()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp.getBoolean(context.getString(R.string.sp_scroll_bar), true)) {
                setHorizontalScrollBarEnabled(true);
                setVerticalScrollBarEnabled(true);
            } else {
                setHorizontalScrollBarEnabled(false);
                setVerticalScrollBarEnabled(false);
            }
            setScrollbarFadingEnabled(true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setAlbumCover(ViewUnit.capture(UltimateBrowserProjectWebView.this, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }, animTime);

            if (prepareRecord()) {
                RecordAction action = new RecordAction(context);
                action.open(true);
                action.addHistory(new Record(getTitle(), getUrl(), System.currentTimeMillis()));
                action.close();
                browserController.updateAutoComplete();
            }
        }
    }

    public synchronized void update(String title, String url) {
        album.setAlbumTitle(title);
        if (foreground) {
            browserController.updateBookmarks();
            browserController.updateInputBox(url);
        }
    }



    @SuppressWarnings("unused")
    public synchronized void pause() {
        onPause();
        pauseTimers();
    }

    @SuppressWarnings("unused")
    public synchronized void resume() {
        onResume();
        resumeTimers();
    }

    @Override
    public synchronized void destroy() {
        stopLoading();
        onPause();
        clearHistory();
        setVisibility(GONE);
        removeAllViews();
        destroyDrawingCache();
        super.destroy();
    }

    public boolean isLoadFinish() {
        return getProgress() >= BrowserUnit.PROGRESS_MAX;
    }

    public void onLongPress() {
        Message click = clickHandler.obtainMessage();
        if (click != null) {
            click.setTarget(clickHandler);
        }
        requestFocusNodeHref(click);
    }

    private boolean prepareRecord() {
        String title = getTitle();
        String url = getUrl();

        return (title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT));
    }

    @SuppressWarnings("unused")
    public static int convertDpToPixels(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dp * metrics.density + 0.5f);
    }

}