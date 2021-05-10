package com.jasonoh.mediaplayerservicenotificationtest_20210506_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Rational;
import android.view.Surface;
import android.view.TextureView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    MediaPlayer mp;
    String url1 = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4";
    String url3;
    String url2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    public static String MESSAGE_KEY = "MESSAGE_KEY";

    boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mp==null) mp = new MediaPlayer();
    }

    // startService()메소드로  실행했을때만 실행되는 메소드
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (intent.getAction()){
            case "com.jasonoh.action.PLAY" :
                if(mp == null){
                    mp = new MediaPlayer();

                    try{
//                        Uri uri = Uri.parse(url3);
//                        mp.setDataSource(MyService.this, uri);
//                        mp.setLooping(false);
//                        mp.prepareAsync();
//                        mp.setOnPreparedListener(listener);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if(intent.getExtras() != null){
                    if(intent.getExtras().getBoolean(MyService.MESSAGE_KEY)){
                        Log.e("TAG", "notification not null");

                        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = null;

                        //오레오버전(api 26) 부터 새로운 "알림채널"이라는 것이 생김
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            //알림 채널 객체 생성
                            NotificationChannel channel = new NotificationChannel("ch01", "channel #01", NotificationManager.IMPORTANCE_LOW);
                            notificationManager.createNotificationChannel( channel );
                            builder = new NotificationCompat.Builder( this, "ch01" );
                        }else {
                            builder = new NotificationCompat.Builder(this, null);
                        }
                        //만들어진 빌더에게 Notification의 모양을 설정
                        builder.setSmallIcon( R.drawable.ic_launcher_background ); //상태표시줄에 보이는 아이콘
                        //확장 상태바[상태표시줄을 드래그하여 아래로 내리면 보이는 알림창]
                        //그 곳에 보이는 설정들
                        builder.setContentTitle( "영상 플레이테스트" );
                        builder.setContentText("백그라운드에서 영상 재생");
//                        builder.setSubText( "Sub Text" );

                        Intent mMainIntent = new Intent(this, MainActivity.class);
                        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 99, mMainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent( mPendingIntent );
//                        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

                        //알림 확인했을때 자동으로 알림 취소 (setContentIntent() 했을때만 가능!!)
                        builder.setAutoCancel(true);

                        //알림 객체
                        Notification notification = builder.build();

                        //알림 관리자에게 알림을 보이도록 공지!!
                        notificationManager.notify(99, notification);
                    }
                }

                break;
        }

//        todo :: service 관련 참고 사이트   https://developer88.tistory.com/36
//        return START_STICKY; //메모리 문제로 서비스를 강제로 kill 시켰을때 메모리 문제가 해결되는 자동으로 서비스를 다시 실행해달라는 의미 (media에서 적합)
        return START_REDELIVER_INTENT; //전달된 intent 값까지 모두 유지, 파일다운로드와 같은 중간에 값을 잃으면 안되는 경우에 적합
    }

    // stopService() 를 통해 서비스가 종료되면 자동으로 실행되는 메소드
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // bindService()메소드로  실행했을때 자동으로 호출되는 메소드
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        MyBinder binder = new MyBinder();

        return binder; //MainActivity로 파견 나갈 객체(Binder)를 리턴
    }

    //이 MusicService 객체의 메모리 주소(객체 참조값)을 리턴해주는 기능을 가진 Binder 클래스 설계
    class MyBinder extends Binder {
        //이 서비스 객체의 주소를 리턴해주는 메소드
        public MyService getService(){
            return MyService.this;
        }
    }//MyBinder inner class

    public void initMediaPlayer(SurfaceTexture surfaceTexture){
       try{
           mp.setSurface(new Surface(surfaceTexture));
           Uri uri = Uri.parse(url2);
           mp.setDataSource(MyService.this, uri);
           mp.setLooping(false);
           mp.prepareAsync();
           mp.setOnPreparedListener(listener);
       } catch (Exception e){
           e.printStackTrace();
       }
    }// initMediaPlayer

    //영상 재생기능
    public void playVideo(SurfaceTexture surfaceTexture) {

        url3 ="android.resource://" + getPackageName() + "/raw/sample_mp4_file";

        if(mp == null) {
            try{
                Log.e("TAG", "mp == null");
//                todo :: Service 여기서 새로 new MediaPlayer를 명시해주는데 왜 바로 반영이 안되는 것일까..???
                mp = new MediaPlayer();
                initMediaPlayer(surfaceTexture);

                Log.e("TAG", "mp == null 일때 mp.islplaying??   ::   " + mp.isPlaying());
            } catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                Log.e("TAG", "mp != null;");
                initMediaPlayer(surfaceTexture);
                Log.e("TAG", "mp != null 일때 mp.islplaying??   ::   " + mp.isPlaying());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

//        mp.start(); //처음 실행 또는 이어하기(resume)
//        Toast.makeText(this,  mp.getDuration() + "", Toast.LENGTH_SHORT).show();
    }

    private Object pictureInPictureParamsBuilder;
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
        }else{
            pictureInPictureParamsBuilder = null;
        }
    }

    public void playVideo(){
        mp.start(); //처음 실행 또는 이어하기(resume)
//        Toast.makeText(this,  mp.isPlaying() + "", Toast.LENGTH_SHORT).show();

        // todo  ::  임시로 서비스에서 실행시 pip모드 실행
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                            Build.VERSION_CODE.N이상일 경우 기본 PIP모드 가능
//                            enterPictureInPictureMode();

            Log.e("TAG", "Service에서 미디어플레이어의 size width :: " + mp.getVideoWidth() + "  height  :: " + mp.getVideoHeight());

            Rational aspectRatio = new Rational(mp.getVideoWidth(), mp.getVideoHeight());
            PictureInPictureParams build = ((PictureInPictureParams.Builder)pictureInPictureParamsBuilder).setAspectRatio(aspectRatio).build();
//            enterPictureInPictureMode(build);
        }
    }

    MediaPlayer.OnPreparedListener listener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Toast.makeText(MyService.this, "prepared", Toast.LENGTH_SHORT).show();
            playVideo();
//            mp.start();
//            Toast.makeText(MyService.this, mp.isPlaying() + "  ::  aaa", Toast.LENGTH_SHORT).show();
//            처음 줄때는 여기서 주어야한다!!
        }
    };

    //영상 일시정지기능
    public void pauseVideo() {

        if(mp != null && mp.isPlaying()) mp.pause();
    }

    //영상 정지기능
    public void stopVideo() {

        if(mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }

    }

    public MediaPlayer getMediaPlay(){
//        무한루핑의 이유...
//        if(mp != null){
//            mp.start();
//        }
//        mp.start();

        return mp;
    }

}// MyService
