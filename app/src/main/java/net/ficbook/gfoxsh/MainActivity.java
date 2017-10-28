package net.ficbook.gfoxsh;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.*;

public class MainActivity extends Activity
{
	final Activity activity = this;
	private WebView mainWebView;
	private ValueCallback<Uri> mUploadMessage;
	public ValueCallback<Uri[]> uploadMessage;
	public static final int REQUEST_SELECT_FILE = 100;
	private final static int FILECHOOSER_RESULTCODE = 1;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			if (requestCode == REQUEST_SELECT_FILE)
			{
				if (uploadMessage == null)
					return;
				uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
				uploadMessage = null;
			}
		}
		else if (requestCode == FILECHOOSER_RESULTCODE)
		{
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}

	}

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.main);

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        WebSettings webSettings = mainWebView.getSettings();
		webSettings.setAppCacheMaxSize(200 * 1024 * 1024);
		webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
		webSettings.setAllowFileAccess(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		if (!isNetworkAvailable())
		{
			webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        mainWebView.setWebViewClient(new MyCustomWebViewClient());
        mainWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mainWebView.setDownloadListener(new DownloadListener() {
				public void onDownloadStart(String url, String userAgent,
											String contentDisposition, String mimetype,
											long contentLength)
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				}
			});
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowContentAccess(true);

		mainWebView.setWebChromeClient(new WebChromeClient()
			{
				// For 3.0+ Devices (Start)
				// onActivityResult attached before constructor
				protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
				{
					mUploadMessage = uploadMsg;
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("file/*");
					startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
				}


				// For Lollipop 5.0+ Devices
				public boolean onShowFileChooser(WebView mainWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
				{
					if (uploadMessage != null)
					{
						uploadMessage.onReceiveValue(null);
						uploadMessage = null;
					}

					uploadMessage = filePathCallback;

					Intent intent = fileChooserParams.createIntent();
					try
					{
						startActivityForResult(intent, REQUEST_SELECT_FILE);
					}
					catch (ActivityNotFoundException e)
					{
						uploadMessage = null;
						return false;
					}
					return true;
				}

				//For Android 4.1 only
				protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
				{
					mUploadMessage = uploadMsg;
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("file/*");
					startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
				}

				protected void openFileChooser(ValueCallback<Uri> uploadMsg)
				{
					mUploadMessage = uploadMsg;
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("file/*");
					startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
				}
				
				public void onProgressChanged(WebView view, int progress)
				{
					activity.setTitle(R.string.loading);
					activity.setProgress(progress * 100);
  
					if(progress == 100)
						activity.setTitle(R.string.app_name);
				}
			});
		if (savedInstanceState ==null)
		{
			mainWebView.loadUrl("http://ficbook.net");
		}
    }


	private boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	public void onBackPressed()
	{
		if (mainWebView.canGoBack())
		{
			mainWebView.goBack();
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		mainWebView.saveState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		mainWebView.restoreState(savedInstanceState);
	}

    private class MyCustomWebViewClient extends WebViewClient
	{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
            view.loadUrl(url);
            return true;
        }
    }
}
