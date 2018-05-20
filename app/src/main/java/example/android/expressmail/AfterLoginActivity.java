package example.android.expressmail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class AfterLoginActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private boolean statusConfirm = false;
    private int statusConfirmValue=-1;
    String TAG = "AfterLoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_login_activity);
        tts = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    else {
                        speak("You are successfully logged in your account. Kindly tell me what you want to do ? Say Compose to compose a mail or say read to read mails from inbox");
                    }
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
    }

    private void speak(String text){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
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
            Toast.makeText(AfterLoginActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }


    private void exitFromApp() throws InterruptedException {
        Thread.sleep(5000);
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
                if(result.get(0).toLowerCase().equals("close"))
                {
                    Log.e(TAG,"I just said Close");
                    speak("Closing the application!");
                    
                    try {
                        exitFromApp();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(result.get(0).toLowerCase().equals("back") || result.get(0).toLowerCase().equals("log out") )
                {
        
                    speak("Logging out successfully.");
                    Log.e(TAG,"I just said back or logout");
                    Intent i = new Intent(AfterLoginActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
                else {
                    if(statusConfirm==true)
                    {
                        String status = result.get(0);
                        boolean flag1 = status.toLowerCase().contains("confirm") || status.toLowerCase().contains("proceed");
                        if(flag1==true)
                        {
                            if(statusConfirmValue==0)
                            {
                                Log.e(TAG,"Confirmed to Composing mail");
                                speak("You have confirmed to compose the mail. Redirecting to composing mail");
                                Intent hello = new Intent(AfterLoginActivity.this,SendingActivity.class);
                                startActivity(hello);
                            }
                            else if(statusConfirmValue==1)
                            {
                                Log.e(TAG,"Confirmed to Reading mail");
                                speak("You have confirmed to Read your mails. Redirecting to Read your mails");
                                Intent intent = new Intent(AfterLoginActivity.this,ReadingMailActivity.class);
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            Log.e(TAG,"Not Confirmed yet");
                            speak("You have not confirmed yet. Kindly select your choice again");
                            statusConfirm=false;
                            statusConfirmValue=-1;
                        }
                    }
                    else
                    {
                        String output;
                        output= result.get(0);
                        if(output.toLowerCase().contains("compose"))
                        {
                            Log.e(TAG,"Choice is to compose a mail");
                
                            speak("You have chosen to compose a mail. Say confirm to proceed or say cancel to choose again");
                            
                            statusConfirm = true;
                            statusConfirmValue = 0;
                        }
                        else if(output.toLowerCase().contains("read"))
                        {
                            Log.e(TAG,"Choice is to read a mail");
                
                            speak("You have chosen to read your mails. Say confirm to proceed or say cancel to choose again");
                            
                            statusConfirm = true;
                            statusConfirmValue = 1;
                        }
                        else
                        {
                            Log.e(TAG,"It is not valid");
                
                            speak("You can only Compose or Read a Mail. Kindly Select one of these options");
                            
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
