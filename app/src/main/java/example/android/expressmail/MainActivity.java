package example.android.expressmail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    public static TextToSpeech mainService;
    private int numberOfClicks = 0;
    String emailId = "",password = "";
    private int statusConfirm = 0;
    Context context;
    private android.speech.tts.TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        context = this;
        tts = new android.speech.tts.TextToSpeech(context, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    else
                    {
                        speak("Welcome to Express Mail. Please Enter your Email and Password to Login");
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
    }


    private void speak(String text){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null, null);
        }else{
            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }
    public void layoutClicked(View view)
    {
        tts.stop();
        listen();
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your Choice");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }


    private void exitFromApp() throws InterruptedException {
        try {
            Thread.sleep(8000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        tts.stop();
        tts.shutdown();
        this.finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(result.get(0).toLowerCase().equals("close") || result.get(0).toLowerCase().equals("back"))
                {
                    Log.e(TAG,"Either said Close or Back");
                    
                    speak("Closing the application!");
                    
                    try {
                        exitFromApp();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Configuration.loop=false;
                    if(statusConfirm==1)
                    {
                        String status = result.get(0);
                        if(status.toLowerCase().contains("confirm"))
                        {

                            emailId = emailId.replaceAll(" ","");
                            password = password.replaceAll(" ","");
                            emailId = emailId.replaceAll("dot",".");
                            Configuration.EMAIL = emailId;
                            Configuration.PASSWORD = password;
                            Log.e("my email id is ",emailId);
                            Log.e("my password is ",password);
                            
                            speak("You have confirmed your credentials. Kindly let me log in to your mail");
                            
                            Log.e(TAG,"statusConfirm is 1 and I have spoken confirm");
                            Context context = this;
                            CheckMail sm = new CheckMail(context);
                            sm.execute();
                            while(Configuration.loop!=true);
                            if(Configuration.authenticated==true)
                            {
                                Log.e(TAG,"Authentication successful");
                                Configuration.loop = false;
                                
                                
                                Intent intent = new Intent(MainActivity.this,AfterLoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Log.e(TAG,"Authentication unsuccessful");
                                
                                speak("The Credentials are Invalid. Kindly enter the credentials again");
                                
                                Configuration.authenticated = false;
                                Configuration.loop = false;
                                statusConfirm=0;
                                numberOfClicks = 0;

                            }
                        }
                        else if(status.toLowerCase().contains("cancel"))
                        {
                            Log.e(TAG,"I have spoken cancel");
                            
                            speak("Kindly Input the credentials again");
                            
                            Configuration.authenticated = false;
                            Configuration.loop = false;
                            numberOfClicks = 0;
                            statusConfirm = 0;
                        }
                        else
                        {
                            Log.e(TAG,"Not spoken cancel and confirm both");
                            
                            speak("Please confirm your credentials or Cancel it to enter again");
                            
                        }
                    }
                    else
                    {
                        switch(numberOfClicks)
                        {
                            case 0 : //email id not yet entered;
                            {
                                String output;
                                output= result.get(0);
                                output = output.replaceAll(" ","");
                                speak("Your emailId is " + output + ". Now please enter the password");
                                emailId = output;
                                numberOfClicks++;
                                break;
                            }
                            case 1 :
                            {
                                String output;
                                output= result.get(0);
                                output = output.replaceAll(" ","");
                                speak("Entered password is " + output + ". Say confirm to proceed with these credentials or cancel to enter credentials again");
                                password = output;
                                numberOfClicks++;
                                statusConfirm = 1;
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                Log.e(TAG,"Didn't recognize");
                
                speak("I didn't get you. Can you please repeat what you just said");
                
            }
        }
    }

}


