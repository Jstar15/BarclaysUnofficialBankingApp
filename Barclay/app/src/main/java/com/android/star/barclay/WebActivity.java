package com.android.star.barclay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import database.Credentials;
import database.MySQLiteHelper;
import im.delight.android.webview.AdvancedWebView;

public class WebActivity extends Activity implements AdvancedWebView.Listener {

    private AdvancedWebView mWebView;
    private String url = "https://bank.barclays.co.uk/olb/auth/LoginLink.action";
    private Boolean firstlogin = true;
    private Credentials c;
    FrameLayout progressBarHolder;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.loadUrl(url);
        mWebView.setListener(this, this);
        mWebView.setGeolocationEnabled(false);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setInitialScale(45);


        //setup progress bar animation
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        progressBarHolder.bringToFront();
        new LoadingTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();

        Thread LoginThread = new Thread() {
            @Override
            public void run() {
                Intent intent = new Intent(WebActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                WebActivity.this.finish();
            }
        };
        LoginThread.start();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed(){
        Thread LoginThread = new Thread() {
                @Override
                public void run() {
                    Intent intent = new Intent(WebActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    WebActivity.this.finish();
                }
            };
            LoginThread.start();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBarHolder.setVisibility(View.VISIBLE);

    }
    @Override
    public void onPageFinished(String url) {
        initJavascriptLogin();
        if(!firstlogin){
            progressBarHolder.setVisibility(View.GONE);

        }
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }



    //javascript injection to login
    private void initJavascriptLogin(){
        if(firstlogin) {
            MySQLiteHelper db = new MySQLiteHelper(getBaseContext());
            c = db.getCred(1);
            mWebView.loadUrl("javascript:$(document.getElementById(\"surname\").value =\"" + c.getSurname() + "\");"); //input surname
            String[] barray = c.getSort().split("-"); //split sort code into array 20-20-20

            //select and load sort code and accout number
            mWebView.loadUrl("javascript:(document.getElementById(\"account-radio\").checked = true);");  //select radio box
            mWebView.loadUrl("javascript:$(document.getElementById(\"sortCodeSet1\").value = \"" + barray[0] + "\")"); // input sort code
            mWebView.loadUrl("javascript:$(document.getElementById(\"sortCodeSet2\").value = \"" + barray[1] + "\")");
            mWebView.loadUrl("javascript:$(document.getElementById(\"sortCodeSet3\").value = \"" + barray[2] + "\")");
            mWebView.loadUrl("javascript:$(document.getElementById(\"the-account-number\").value = \"" + c.getAccount() + "\")"); //input accout number

            //select next page and wait a second
            mWebView.loadUrl("javascript:$(document.getElementById(\"forward\").click())");

            //quick work around to allow time for page top load a bit hacky should be changed
            try {
                Thread.sleep(1200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            //insert passcode
            mWebView.loadUrl("javascript:$(document.getElementById(\"passcode\").value = \"" + c.getPass() + "\")");
        }

        //get reqested characters for secret password and login
        mWebView.evaluateJavascript("(function(){return $(document.getElementsByClassName(\"letter-select\"))[0].innerHTML})();",  //note could get better function to minimize html request data
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String html) {
                        if(firstlogin){
                            //extract passpahase index from html
                            char[]  secretpassarray = c.getSecret().toCharArray();
                            ArrayList<Integer> PassphaseIndex = ExtractRE(html);
                            char answ1 = secretpassarray[PassphaseIndex.get(0)-1];
                            char answ2 = secretpassarray[PassphaseIndex.get(1)-1];

                            //select and load answer for secret passphase
                            mWebView.loadUrl("javascript:$(document.getElementById(\"nameOne\").value = \""+answ1+"\");");
                            mWebView.loadUrl("javascript:$(document.getElementById(\"nameTwo\").value = \""+answ2+"\");");

                            //select login button
                            mWebView.loadUrl("javascript:$(document.getElementById(\"log-in-to-online-banking2\").click())");
                            firstlogin = false; //esnure code block can only run once
                        }
                    }
                });
    }

    //extract requested secret passphase index characters from html could be much better
    private ArrayList<Integer> ExtractRE(String html){
        ArrayList<Integer> output = new ArrayList<Integer>();

        char[] htmlArray = html.replaceAll("003", "").toCharArray();  //clean unwanted characters hacky
        int lastIntIndex = -2;

        for(int x = 0; x < htmlArray.length; x++){
            int i = Character.getNumericValue(htmlArray[x]); //convert char to int
            if(htmlArray.length == 4) {  //break if found characters
                break;
            }else if(i < 10 && i > 0){//only look for numeral 1 - 9
                if(lastIntIndex + 1 == x){ //if a two letter number concat with previous
                    int temp = output.get(output.size());
                    output.remove(output.size());
                    output.add(concat(temp, i));
                }else{
                    lastIntIndex = x;
                    output.add(i);
                }
            }
        }
        return output;
    }

    //concat two integers together and return an integer
    private int concat(int a, int b){
        String concat = a + b + "";
        return Integer.parseInt(concat);
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < 5; i++) {
                    TimeUnit.SECONDS.sleep(1);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}