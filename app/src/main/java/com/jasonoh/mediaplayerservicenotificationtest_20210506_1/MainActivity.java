package com.jasonoh.mediaplayerservicenotificationtest_20210506_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonoh.kotlinre.HelloKt;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    TextView tvTime;
    TextureView ttvVideo;
    SeekBar skbVideo;
    String url, url2, url3;
    ProgressBar progressBar;

    Handler handler = new Handler();

    MyService myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pgb_video);
        ttvVideo = findViewById(R.id.ttv_video);
        tvTime = findViewById(R.id.tv_time);
        skbVideo = findViewById(R.id.skb_video);

//        ttvVideo.setSurfaceTextureListener(ttvVideoListener);

        initSeekBar();

    }// onCreate method

    @Override
    protected void onResume() {
        super.onResume();

        //Service 객체 실행 및 연결(bind)
        if(myService == null) {  //연결되어 있는 뮤직서비스가 없다면?
            Intent intent = new Intent( this, MyService.class );
            intent.setAction("com.jasonoh.action.PLAY");
            startService( intent ); //일단 서비스 객체 생성!! [ 만약 서비스 객체가 없다면 만들고 onStartCommand() 가 발동, 있다면 생성은 안하고 onStartCommand()만 발동함 ]

            //만들어진 서비스 객체와 연결
            //bindService() 호출하면 Service class 안의 onBind()메소드 발동하고
            //이 onBind()가 Service 객체의 참조값을 가진 객체를 리턴해줌.
            bindService( intent, conn, 0 ); //flags가 0이면 AUTOCREATE 를 안한다는 의미 -> AUTOCREATE를 하면 startService를 안해도 되긴 하지만 좀 다르게 실행 됨.
            //bind 할때 만들어 진것이 없이 flags를 0을 주면 아무 것도 없다고 생각 해도 된다.

        }

    }// onResume

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myService.getMediaPlay() != null){
            Intent intent = new Intent(this, MyService.class);
            intent.setAction("com.jasonoh.action.PLAY");
            intent.putExtra(MyService.MESSAGE_KEY, true);
            startService(intent);
        }

    } // onDestroy method

    private void initSeekBar(){
        skbVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    // 사용자가 시크바를 움직이면
                    if(myService.getMediaPlay() != null){
                        myService.getMediaPlay().seekTo(progress); // 재생위치를 바꿔준다 (움직인 곳에서의 재생)
                        myService.getMediaPlay().start();
                        seekBarPlay();
                    }

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void seekBarPlay(){
        if(myService.getMediaPlay()==null)return;
        skbVideo.setProgress(myService.getMediaPlay().getCurrentPosition());

        int min = myService.getMediaPlay().getDuration()/60000;
        int sec = (myService.getMediaPlay().getDuration()-60000*min)/1000;
        int min2 = myService.getMediaPlay().getCurrentPosition()/60000;
        int sec2 = (myService.getMediaPlay().getCurrentPosition()-60000*min2)/1000;

        tvTime.setText(min2 + " 분" + sec2 + " 초  //  " + min + " 분" + sec + " 초");

        if(myService.getMediaPlay().isPlaying()){

            try {
//                Toast.makeText(this, myService.getMediaPlay().getCurrentPosition() + "", Toast.LENGTH_SHORT).show();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        seekBarPlay();
                    }
                };
//                Handler handler = new Handler();
                handler.postDelayed(runnable, 1000);


//                tvTime.setText(min2 + " 분" + sec2 + " 초  //  " + min + " 분" + sec + " 초");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

//    rx java로 AsyncTask 이용
    Disposable backgroundTask;
    public void BackgroundTask(){
        backgroundTask = Observable.fromCallable(()->{

            myService.playVideo(surfaceTexture);

            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result ->{

                    if(myService.getMediaPlay() != null && !result){
                        progressBar.setVisibility(View.GONE);
//                        todo :: 여기서 명시적으로 start를 해줘야만 하는지는 추후 알아봐야 하는 과정...
                        myService.getMediaPlay().start();
                        Log.e("TAG", myService.getMediaPlay().isPlaying() + "   rx 자바에서 플레이 중인지??");
//                        initSeekBar();

                        skbVideo.setMax(myService.getMediaPlay().getDuration());
                        Log.e("TAG", myService.getMediaPlay().getDuration() + ""); // 125952
                        seekBarPlay();
                    }
                    backgroundTask.dispose();
                });
    }

    SurfaceTexture surfaceTexture;

    TextureView.SurfaceTextureListener ttvVideoListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

            try {
                Toast.makeText(MainActivity.this, "surface 객체 형성", Toast.LENGTH_SHORT).show();
                surfaceTexture = surface;

                if(myService != null && surfaceTexture != null){
//                    myService.playVideo(surfaceTexture);

//                    todo :: 코틀린에서 async await 하는 방법!!
//                    Coroutine.Companion.BackgroundTask(myService, surfaceTexture, MainActivity.this);
//                    Log.e("TAG",  "코틀린에서 온 값");

                    BackgroundTask();

//                    Toast.makeText(MainActivity.this, HelloKt.formatMessage("안녕하세요") + " asdf ",  Toast.LENGTH_SHORT).show();

                    // (MediaPlayer)mp가 null이 아니라면
//                    if(myService.getMediaPlay() != null){
//                        progressBar.setVisibility(View.GONE);
////                        initSeekBar();
//                        skbVideo.setMax(myService.getMediaPlay().getDuration());
//                        Log.e("TAG", myService.getMediaPlay().getDuration() + ""); // 0
//                        seekBarPlay();
//                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) { }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) { }
    };

    //bindService() 를 했을때 Service객체와 연결된 통로 객체
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //두번째 파라미터 : 서비스객체의 참조주소를 주는 메소드가 있는 객체
            MyService.MyBinder binder = (MyService.MyBinder)service;
            myService = binder.getService(); //리턴해준 서비스 객체 주소 참조

            Log.e("TAG", "서비스와 연결 완료");

            Toast.makeText(MainActivity.this, " 서비스와 연결되었습니다. ", Toast.LENGTH_SHORT).show();
            ttvVideo.setSurfaceTextureListener(ttvVideoListener);

//            todo ::/// 이유를 확인 못함.. 왜 여기서 안되는거지..????? serfaceTexture가 null인가?? -- 위치 바꿈!! SurfaceTextureListener로 위치 변경후 됨
//            Log.e("TAG", surfaceTexture.toString() + ""); // null
//            if(myService != null && surfaceTexture != null){
//                myService.playVideo(surfaceTexture);
//
//                if(myService.getMediaPlay() != null && myService.getMediaPlay().isPlaying()){
//                    progressBar.setVisibility(View.GONE);
//                }
//            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void clickBtn(View view) {
        switch (view.getId()){
            case R.id.btn_start:
                if(myService != null && surfaceTexture != null){

//                    todo :: 스타트 버튼 누를시에 !!!  MainActivity

                    BackgroundTask();

//                    myService.playVideo(surfaceTexture);

//            todo ::/// 여기서 실행시 문제는
//             1. 프로그레스바가 바로 안사라짐 - 한번 더 스타트눌러야 사라진다. 그 이유는 맞다 한번 눌렀을 경우 영상은 실행되지만 처음에는 영상이 플레이중이 아니기 때문이다.
//             2. onDestroy 되었을때 재실행시 소리는 나는데 영상이 안나옴

//                    if(myService.getMediaPlay() != null){
//                        Log.e("TAG", myService.getMediaPlay().isPlaying() + "스타트 버튼 클릭시 실행되는지??");
//                        myService.getMediaPlay().start();
//                        skbVideo.setMax(myService.getMediaPlay().getDuration());
//                        seekBarPlay();
//                        progressBar.setVisibility(View.GONE);
//                        Log.e("TAG", myService.getMediaPlay().isPlaying() + "스타트 버튼 클릭시 실행되는지??");
//                    }

                }
                break;

            case R.id.btn_pause:
                if(myService != null){
                    myService.pauseVideo();
                }
                break;

            case R.id.btn_stop:
                if(myService != null){
                    myService.stopVideo();
                }
                break;
        }// switch
    }// clickBtn method
}