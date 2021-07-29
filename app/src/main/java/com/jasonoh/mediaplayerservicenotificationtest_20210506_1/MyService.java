package com.jasonoh.mediaplayerservicenotificationtest_20210506_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Rational;
import android.view.Surface;
import android.view.TextureView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

//    todo :: V/MediaPlayerNative: message received msg=3, ext1=21, ext2=0
//     buffering 21
//     callback application
//     V/MediaPlayerNative: back from callback
//     이것들이 계속 나오는 이유는 background에서 작업을 하고 있기 때문에 지속적으로 나오는 것이다. 이것들은 MediaPlayer를 stop시켜주지 않는다면 계속 나오게 된다.

    MediaPlayer mp;
    String url1 = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4";
    String url3;
    String url2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    public static String MESSAGE_KEY = "MESSAGE_KEY";

    boolean isPlaying = false;

    MediaSessionCompat mediaSession;
    MediaSession mediaSession2;
    androidx.media.app.NotificationCompat.MediaStyle mediaStyle;

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
                }else{
//                    try{
//                        Uri uri = Uri.parse(url2);
//                        mp.setDataSource(MyService.this, uri);
//                        mp.setLooping(false);
//                        mp.prepareAsync();
//                        mp.setOnPreparedListener(listener);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
                }

                if(intent.getExtras() != null){
                    if(intent.getExtras().getBoolean(MyService.MESSAGE_KEY)){
                        Log.e("TAG", "MyService:  notification not null");

                        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = null;

                        //오레오버전(api 26) 부터 새로운 "알림채널"이라는 것이 생김
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                            // 롤리팝(api 21) 부터 미디어 세션, 스타일이 가능하다
                            mediaSession = new MediaSessionCompat(this, "PlayerService");
                            mediaSession2 = new MediaSession(this, "PlayerService");
//                            mediaStyle = new Notification.MediaStyle().setMediaSession(mediaSession.getSessionToken());
                            mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken());
                            mediaSession.setActive(true);

                            // todo :: Android 10 부터 Notification을 사용할때 SeekBar가 표시 된다.
                            //  https://ddolcat.tistory.com/619
                            // 비트 연산자 이용 kotlin -> or == java -> |
                            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                    .setState(PlaybackStateCompat.STATE_PLAYING, mp.getCurrentPosition(), 1.0f)
                                    .setActions(
                                            PlaybackStateCompat.ACTION_PLAY
                                                    | PlaybackStateCompat.ACTION_PAUSE
                                                    | PlaybackStateCompat.ACTION_SEEK_TO
                                                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                                    )
                                    .build()
                            );
                            mediaSession.setMetadata(
                                    new MediaMetadataCompat.Builder()
                                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Movie Play")
                                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "currentTrack.artist")
//                                    .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, R.drawable.one_zoro2)
                                    .putLong(MediaMetadata.METADATA_KEY_DURATION, mp.getDuration())
                                    .build()
                            );

                            Log.e("TAG", "MyService  ::  미디어세션 토큰 값  ::  " + mediaSession.getSessionToken().toString());

                            //알림 채널 객체 생성
                            NotificationChannel channel = new NotificationChannel("ch01", "channel #01", NotificationManager.IMPORTANCE_LOW);
                            notificationManager.createNotificationChannel( channel );
                            builder = new NotificationCompat.Builder( this, "ch01" );
                            builder.addAction(R.drawable.ic_prev_black, "", PendingIntent.getService(this, 99, new Intent("PREV"), 0));
                            builder.addAction(R.drawable.ic_play_black, "play", PendingIntent.getService(this, 99, new Intent("PLAY"), 0));
                            builder.addAction(R.drawable.ic_pause_black, "pause", PendingIntent.getService(this, 99, new Intent("PAUSE"), 0));
                            builder.addAction(R.drawable.ic_next_black, "next", PendingIntent.getService(this, 99, new Intent("NEXT"), 0));

                            if(mp != null){
                                builder.setProgress(mp.getDuration(), mp.getCurrentPosition(), false);
                                Log.e("TAG", "MyService  ::  Progress  ==  " + mp.getDuration());
                            }

                        }else {
                            builder = new NotificationCompat.Builder(this, null);
                        }
                        //만들어진 빌더에게 Notification의 모양을 설정
                        builder.setSmallIcon( R.drawable.ic_baseline_movie_filter_24 ); //상태표시줄에 보이는 아이콘
                        //확장 상태바[상태표시줄을 드래그하여 아래로 내리면 보이는 알림창]
                        //그 곳에 보이는 설정들
                        builder.setStyle(mediaStyle);
//                        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()));
                        builder.setContentTitle( "Movie Play" );
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.one_zoro2));

//                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_movie_filter_24)).bigLargeIcon(null));
                        builder.setContentText("Big Bug Bunny");
//                        builder.setSubText( "영상이 보여지는 부분일까?" );

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

            case "PREV":
                if(mp!=null && mp.isPlaying()){
                    mp.seekTo(mp.getCurrentPosition() - 5000);
                }
                break;

            case "PAUSE":
                if(mp!=null && mp.isPlaying()){
                    mp.pause();
                }
                break;

            case "NEXT":
                if(mp!=null && mp.isPlaying()){
                    mp.seekTo(mp.getCurrentPosition() + 5000);
                }
                break;

            case "PLAY":
                if(mp!=null && !mp.isPlaying()){
                    mp.start();

                }
                break;
        }

//        todo :: service 관련 참고 사이트   https://developer88.tistory.com/36
        return START_STICKY; //메모리 문제로 서비스를 강제로 kill 시켰을때 메모리 문제가 해결되는 자동으로 서비스를 다시 실행해달라는 의미 (media에서 적합)
//        return START_REDELIVER_INTENT; //전달된 intent 값까지 모두 유지, 파일다운로드와 같은 중간에 값을 잃으면 안되는 경우에 적합
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

           url3 ="android.resource://" + getPackageName() + "/raw/sample_mp4_file";

           mp.setSurface(new Surface(surfaceTexture));
           Uri uri = Uri.parse(url2);
           mp.setDataSource(MyService.this, uri);
           mp.setLooping(false);
           mp.prepareAsync();
           mp.setOnPreparedListener(listener);

           Log.e("TAG", "MyService: initMediaPlayer   " + mp.isPlaying());
       } catch (Exception e){
           e.printStackTrace();

           try{
               if(mp != null && !mp.isPlaying()) mp.start();
           }catch (Exception e2){
               e2.printStackTrace();
           }

       }
    }// initMediaPlayer

    //영상 재생기능
    public void playVideo(SurfaceTexture surfaceTexture) {

        if(mp == null) {
            try{
                Log.e("TAG", "mp == null");
//                todo :: Service 여기서 새로 new MediaPlayer를 명시해주는데 왜 바로 반영이 안되는 것일까..???
                mp = new MediaPlayer();
                initMediaPlayer(surfaceTexture);

                Log.e("TAG", "MyService:  mp == null 일때 mp.islplaying??   ::   " + mp.isPlaying());
            } catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                Log.e("TAG", "MyService:  mp != null;");
                initMediaPlayer(surfaceTexture);
                Log.e("TAG", "MyService:  mp != null 일때 mp.islplaying??   ::   " + mp.isPlaying());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

//        mp.start(); //처음 실행 또는 이어하기(resume)
//        Toast.makeText(this,  mp.getDuration() + "", Toast.LENGTH_SHORT).show();
    }

    public void playVideo(){
        mp.start(); //처음 실행 또는 이어하기(resume)
    }

    MediaPlayer.OnPreparedListener listener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Toast.makeText(MyService.this, "동영상 재생이 가능합니다.", Toast.LENGTH_SHORT).show();
            playVideo();
//            mp.start();
//            Toast.makeText(MyService.this, mp.isPlaying() + "  ::  aaa", Toast.LENGTH_SHORT).show();
//            처음 줄때는 여기서 주어야한다!!
        }
    };

    //영상 일시정지기능
    public void pauseVideo() {

        if(mp != null && mp.isPlaying()) {
            mp.pause();
        }
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
