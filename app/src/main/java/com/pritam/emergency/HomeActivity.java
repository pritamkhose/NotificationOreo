package com.pritam.emergency;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void btn(View view) {
        // https://stackoverflow.com/questions/50852210/how-to-create-download-progress-notification-in-oreo

        // https://stackoverflow.com/questions/48395961/progress-notification-not-showing-up-in-android-8-0

       // https://stackoverflow.com/questions/48922806/progress-bar-in-notification-not-updates-in-android-o
       new BackTask().execute("http://www.universityofcalicut.info/SDE/Social_Research_Methods_on25Feb2016.pdf");
       // new BackTask().execute("https://nodejs.org/dist/v10.15.0/node-v10.15.0-x64.msi");
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "1";

    String storeDir = Environment.getExternalStorageDirectory().getPath().toString()+ "";
    Context context = HomeActivity.this;
        private class BackTask extends AsyncTask<String,Integer,Void> {
            NotificationManager mNotifyManager;
            NotificationCompat.Builder mBuilder;
            NotificationChannel notificationChannel;

            protected void onPreExecute() {
                super.onPreExecute();
                mNotifyManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(selectedUri, "resource/folder");
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                mBuilder = new NotificationCompat.Builder(context, null);
                mBuilder.setContentTitle("Downloading - "+"file") // lblTitle.getText()
                        .setContentText("Download in progress")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    mNotifyManager.createNotificationChannel(notificationChannel);
                }
                else {
                    mBuilder.setContentTitle("Downloading - "+"file")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                            .setVibrate(new long[]{100, 250})
                            .setLights(Color.YELLOW, 500, 5000)
                            .setAutoCancel(true);
                }

                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                Toast.makeText(HomeActivity.this.getApplicationContext(), "Downloading the file...", Toast.LENGTH_SHORT).show();
            }

            protected Void doInBackground(String...params){
                URL url;
                int count;
                try {
                    url = new URL(params[0].replaceAll(" ", "%20"));
                    String pathl="";
                    try {
                        File f=new File(storeDir);
                        if(f.exists()){
                            Log.d("progress --> ","Start Download ");
                            HttpURLConnection con=(HttpURLConnection)url.openConnection();
                            InputStream is=con.getInputStream();
                            String pathr=url.getPath();
                            String filename=pathr.substring(pathr.lastIndexOf('/')+1);
                            pathl=storeDir+"/"+filename;
                            FileOutputStream fos=new FileOutputStream(pathl);
                            int lenghtOfFile = con.getContentLength();
                            byte data[] = new byte[1024];
                            long total = 0;
                            while ((count = is.read(data)) != -1) {
                                total += count;
                               // Log.d("progress --> ", total + " / "+ (int)((total*100)/lenghtOfFile));
                                // publishing the progress
                                publishProgress((int)((total*100)/lenghtOfFile));
                                // writing data to output file
                                fos.write(data, 0, count);
                            }

                            is.close();
                            fos.flush();
                            fos.close();
                            Log.d("progress --> ","Done Download ");
                        }
                        else{
                            Log.e("Error","Not found: "+storeDir);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;

            }

            protected void onProgressUpdate(Integer... progress) {

                Log.d("onProgressUpdate --> ", progress[0] + " ");
               if(progress[0] == 100){
                   mBuilder.setContentText("Download complete");
                   // Removes the progress bar
                   mBuilder.setProgress(0,0,false);
               } else {
                   mBuilder.setProgress(100, progress[0], false);
               }

                // Displays the progress bar on notification
                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
            }

            protected void onPostExecute(Void result){
                Log.d("onPostExecute --> ",   "Download complete ");
                mBuilder.setContentText("Download complete");
                // Removes the progress bar
                mBuilder.setProgress(0,0,false);
                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNotifyManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
                }
                //  mNotifyManager.cancel(NOTIFICATION_ID);
                mNotifyManager.cancelAll();
                Log.d("onPostExecute --> ",   "cancel " +NOTIFICATION_ID);
            }
        }


}
