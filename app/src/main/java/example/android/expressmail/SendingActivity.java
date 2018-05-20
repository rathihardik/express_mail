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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class SendingActivity extends AppCompatActivity {

    private TextToSpeech tts;
    String TAG="SendingActivity";
    private TextView To,Subject,Message;
    public String stringTo,stringSubject,stringMessage;
    public int numberOfClicks = 0;
    public boolean isForward=false,isReply = false, isNormal = true;
    public boolean isBackPressed = false, afterBackPressed = false;
    int lock = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendingactivity);
        tts = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    else
                    {
                        speak("Please Wait");
                        foo();
                    }
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
       
        numberOfClicks = 1;
    }

    public void foo()
    {
        Bundle recieved = getIntent().getExtras();
        if(recieved!=null)
        {

            String checkforward = recieved.getString("forward","itIsNotforward");
            String checkReply = recieved.getString("reply","itIsNotReply");
            if(!checkforward.equals("itIsNotforward"))
            {
                speak("Now please enter the email address to whom you want to send the mail");
                String content = recieved.getString("message");
                setMessage(content);
                String subject = recieved.getString("subject");
                setSubject("Fwd : " + subject);
                isForward = true;
                isNormal = false;
            }
            else if(!checkReply.equals("itIsNotReply"))
            {
                String recipient = recieved.getString("to");
                setTo(recipient);
                String Subject = recieved.getString("subject");
                setSubject("Re : " + Subject);
                numberOfClicks = 3;
                speak("Now please enter the message you want to send");
                isReply = true;
                isNormal = false;
            }
            else {
                speak("Now please enter the email address to whom you want to send the mail");
                setTo("");
                setSubject("");
                setMessage("");
            }
        }
        else
        {

            speak("Now please enter the email address to whom you want to send the mail");

            setTo("");
            setSubject("");
            setMessage("");
        }

        To = (TextView) findViewById(R.id.to);
        To.setText(stringTo);
        Subject  =(TextView)findViewById(R.id.subject);
        Subject.setText(stringSubject);
        Message = (TextView) findViewById(R.id.message);
        Message.setText(stringMessage);
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
            Toast.makeText(SendingActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }


    private void exitFromApp() throws InterruptedException {
        Thread.sleep(5000);
        tts.stop();
        tts.shutdown();
        this.finishAffinity();
    }


    public void setSubject(String str)
    {
        this.stringSubject = str;
    }

    public void setTo(String str)
    {
        this.stringTo = str;
    }

    public void setMessage(String str)
    {
        this.stringMessage = str;
    }

    private void sendEmail() {
        Log.e(TAG,"Inside sendEmail()");
        String email = To.getText().toString().trim();
        String subject = Subject.getText().toString().trim();
        String message = Message.getText().toString().trim();
        SendMail sendmail = new SendMail(this, email, subject, message);
        sendmail.execute();
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(isBackPressed == true)
                {
                    if(afterBackPressed == false)
                    {
                        Log.e(TAG,"isBackPressed = true and afterBackPressed = false");
                       
                       speak("Your mail will be discarded. Say confirm to proceed or Cancel to prevent discarding");
                        
                        afterBackPressed = true;
                    }
                    else
                    {
                        Log.e(TAG,"isBackPressed = true and afterBackPressed = true");
                        if(result.get(0).toLowerCase().contains("confirm"))
                        {
                            Log.e(TAG,"Spoken Confirm");
                           
                           speak("Your mail is discarded. Going back to Main Page");
                            
                            Intent i = new Intent(SendingActivity.this,AfterLoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else if(result.get(0).toLowerCase().contains("cancel"))
                        {
                            Log.e(TAG,"Spoken Cancel");
                            isBackPressed = false;
                            afterBackPressed = false;
                           
                           speak("Your mail is not discarded. Please Continue");
                            
                        }
                        else
                        {
                            Log.e(TAG,"Invalid command");
                           
                           speak("Please either confirm or cancel");
                            
                        }
                    }

                }
                else if(result.get(0).toLowerCase().equals("close") || result.get(0).toLowerCase().equals("log out"))
                {
                    isBackPressed = false;
                    afterBackPressed = false;
                    Log.e(TAG,"My choice is to close");
                   
                   speak("Logging out and closing the application!");
                    
                    try {
                        exitFromApp();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(result.get(0).toLowerCase().equals("back") || result.get(0).toLowerCase().equals("cancel"))
                {
                    Log.e(TAG,"My choice is to go back");
                   
                   speak("Cancelling composing the mail");
                    
                    isBackPressed = true;

                    if(afterBackPressed == false)
                    {
                        Log.e(TAG,"isBackPressed = true and afterBackPressed = false");

                        speak("Your mail will be discarded. Say confirm to proceed or Cancel to prevent discarding");

                        afterBackPressed = true;
                    }
                }
                else {
                    isBackPressed = false;
                    afterBackPressed = false;
                    switch (numberOfClicks) {
                        case 1:
                            String stringTos;
                            stringTos = result.get(0).replaceAll("underscore", "_");
                            stringTos = stringTos.toLowerCase();
                            stringTos = stringTos.replaceAll(" ", "");
                            To.setText(stringTos);
                            setTo(stringTos);
                            if(isForward==false)
                            {
                               
                               speak("Now please Enter the subject");
                                
                                numberOfClicks++;
                            }
                            else
                            {
                                numberOfClicks = 3;
                               
                               speak("Please Confirm the mail. To : " + stringTo + ". Subject : " + stringSubject + ". Message : " + stringMessage + ". Speak Yes to confirm");
                                
                                numberOfClicks++;
                                break;
                            }
                            break;
                        case 2:
                            if(isForward==false)
                            {
                                Subject.setText(result.get(0));
                                setSubject(result.get(0));
                               
                               speak("Now please enter the message you want to send");
                                
                            }
                            numberOfClicks++;
                            break;
                        case 3:
                            if(isForward==false)
                            {
                                Message.setText(result.get(0));
                                setMessage(result.get(0));
                            }
                           
                           speak("Please Confirm the mail. To : " + stringTo + ". Subject : " + stringSubject + ". Message : " + stringMessage + ". Speak Yes to confirm");
                            
                            numberOfClicks++;
                            break;
                        case 4: {
                            if (result.get(0).toLowerCase().equals("yes")) {
                                Log.e(TAG,"Spoken Yes to send the mail ");
                               
                               speak("Sending the mail");
                                
                                sendEmail();
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (Configuration.messageSent == 1) {
                                   
                                   speak("Your message has been sent. Redirecting you to main page");
                                    Intent i = new Intent(SendingActivity.this, AfterLoginActivity.class);
                                    startActivity(i);
                                }
                                else {
                                   
                                   speak("I am facing a problem in sending the message. Check your network connection and the entered credentials and try again");
                                    
                                }
                            }
                            else if (result.get(0).toLowerCase().equals("no")) {
                                Log.e(TAG,"My choice is no");
                               
                               speak("You are ready to compose again");
                                
                                To.setText("");
                                Subject.setText("");
                                Message.setText("");
                                numberOfClicks = 1;
                            } else {
                                Log.e(TAG,"Not confirmed yet");
                               
                               speak("You have not confirmed yet. Say yes to confirm or no to compose again");
                                
                            }
                            break;
                        }
                        default:numberOfClicks=3;
                            break;
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
