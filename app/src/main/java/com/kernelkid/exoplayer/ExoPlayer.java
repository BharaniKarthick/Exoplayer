package com.kernelkid.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;

import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionUtil;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EventListener;

import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;

public class ExoPlayer extends AppCompatActivity implements com.google.android.exoplayer2.ExoPlayer.EventListener {

    PlayerView playerView;
    ProgressBar progressBar;
    ImageView btFullScreen,bt_toogle,bt_audio;
    SimpleExoPlayer simpleExoPlayer;
    DefaultTrackSelector trackSelector;
    boolean flag=false;
    int screenType=1;
    private CheckedTextView[][] trackViews;
    ArrayList audioLanguages=new ArrayList();
    String selectedLanguage="-1";
    int checkedItem=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);

        //getting url from the previous activity
        Bundle extras = getIntent().getExtras();
        String url=extras.get("url").toString();



        //Initilaze the variables
        playerView=findViewById(R.id.player_view);
        progressBar=findViewById(R.id.progess_bar);
        btFullScreen=playerView.findViewById(R.id.bt_fullscreen);
        bt_toogle=playerView.findViewById(R.id.bt_toogle);
        bt_audio=playerView.findViewById(R.id.bt_audio);

        //landscape - potrait toggle button action
        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(flag){
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    flag=false;
                }else {
                    btFullScreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    flag=true;
                }
            }
        });


        //zoom,fit toggle button action
        bt_toogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(screenType==1){
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    bt_toogle.setImageDrawable(getResources().getDrawable(R.drawable.ic_full));
                    screenType=2;
                }
                else if(screenType==2){
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    bt_toogle.setImageDrawable(getResources().getDrawable(R.drawable.ic_zoom));
                    screenType=3;
                }else {
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    bt_toogle.setImageDrawable(getResources().getDrawable(R.drawable.ic_fit));
                    screenType=1;
                }
            }
        });



        bt_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleExoPlayer.setPlayWhenReady(false);
                simpleExoPlayer.getPlaybackState();
                audioLanguages=new ArrayList();
               // Log.e("Exoplayer", "tesing1-> "+simpleExoPlayer.getCurrentTrackGroups().length );
                for(int i = 0; i < simpleExoPlayer.getCurrentTrackGroups().length; i++) {
                    System.out.println("tesing" + simpleExoPlayer.getCurrentTrackGroups().length);
                    String format = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
                    String lang = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).language;
                    String id = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).id;
                    Log.i("Exoplayer lang:", lang + " format: " + format + " id:" + id);
                    if (format.contains("audio") && id != null && lang != null) {
                        if(!audioLanguages.contains(lang)){
                            audioLanguages.add(lang);
                        }
                    }
                }



                String[] listItems =new String[audioLanguages.size()];
                for(int i=0;i<audioLanguages.size();i++){
                    if(!( audioLanguages.get(i) == null)) {
                        listItems[i] = audioLanguages.get(i).toString();
                    }else{
                        listItems[i]="unknown";
                    }
                }


                //Dialog box to choose the required audio
                AlertDialog.Builder builder = new AlertDialog.Builder(ExoPlayer.this);
                builder.setTitle("Choose Audio Track");

               // checkedItem = 0; //this will checked the item when user open the dialog
                builder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedLanguage=listItems[which].toLowerCase();
                        checkedItem=which;
                       // Toast.makeText(ExoPlayer.this, "Position: " + which + " Value: " + listItems[which], Toast.LENGTH_LONG).show();
                    }
                });
            //action when the done button is clicked
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!selectedLanguage.equals(-1)){
                           // Toast.makeText(ExoPlayer.this, selectedLanguage , Toast.LENGTH_LONG).show();
                            //update the selected audio in the player
                            updateAudio(selectedLanguage,trackSelector);
                        }
                        simpleExoPlayer.setPlayWhenReady(true);
                        simpleExoPlayer.getPlaybackState();
                        dialog.dismiss();
                    }
                });

            //action when cancel button is clicked
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        simpleExoPlayer.setPlayWhenReady(true);
                        simpleExoPlayer.getPlaybackState();
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //it initializes the exoplayer
        initExoPlyaer(url);

    }

    private void initExoPlyaer(String url) {
        // bandwisthmeter is used for
        // getting default bandwidth
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
        //DefaultBandwidthMeter d = new DefaultBandwidthMeter.Builder(getApplication()).build();
        // track selector is used to navigate between
        // video using a default seekbar.
        trackSelector =
                new DefaultTrackSelector(getApplicationContext());

        simpleExoPlayer =
                new SimpleExoPlayer.Builder(getApplicationContext())
                        .setTrackSelector(trackSelector)
                        .build();
        //trackSelector.setParameters( trackSelector.getParameters().buildUpon().setPreferredAudioLanguage("eng"));
        // simpleExoPlayer= ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        //trackSelector.setParameters(BandwidthMeterbandwidthMeter);

        //Initialize data source factory
        //DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("exoplayer_video");
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Exo2"), defaultBandwidthMeter);

        //initializes exactors factory
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        Uri videoUri=Uri.parse(url);//update the url which is required to play
        // Uri videoUri=Uri.parse("http://www.rmp-streaming.com/media/big-buck-bunny-360p.mp4");//browser vdieo


        //MappingTrackSelector.MappedTrackInfo trackInfo =
        //trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();



        //Initialize media source
        MediaSource mediaSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);

        // Handler mainHandler = new Handler();
       /* HlsMediaSource hlsMediaSource =
                new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(videoUri));
        //prepare media
        // Set the media source to be played.
        simpleExoPlayer.setMediaSource(hlsMediaSource);
        // Prepare the player.
        simpleExoPlayer.prepare();*/

        simpleExoPlayer.prepare(mediaSource);
        //set  player
        playerView.setPlayer(simpleExoPlayer);

        //keep screen on
        playerView.setKeepScreenOn(true);

       /* trackSelector.setParameters(
                trackSelector
                        .buildUponParameters()
                        .setPreferredAudioLanguage("hi"));*/

        //play video when ready . This line will start the video
        simpleExoPlayer.setPlayWhenReady(true);




        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                //check condition
                if(playbackState == Player.STATE_BUFFERING){
                    progressBar.setVisibility(View.VISIBLE);
                }else if(playbackState == Player.STATE_READY){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }


        });
    }

    //update the audio selected,(eng or tamil or hindi)
    private void updateAudio(String selectedLanguage, DefaultTrackSelector simpleExoPlayer) {
        trackSelector.setParameters(
                trackSelector
                        .buildUponParameters()
                        .setPreferredAudioLanguage(selectedLanguage));

    }



    /*private void getAudioLanguages(Uri videoUri,ArrayList languages) {
        //Log.e("Exoplayer", "tesing11"+simpleExoPlayer.getCurrentTrackGroups().length);
        try{

            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(getApplicationContext(),videoUri,null);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if(mime.startsWith("audio/")){
                    String langugae= format.getString(MediaFormat.KEY_LANGUAGE);
                    languages.add(langugae);
                    Log.e("Exoplayer", "tesing1 "+langugae );
                    Log.e("Exoplayer", "tesing1-> "+simpleExoPlayer.getCurrentTrackGroups().length );
                }
            }

            //  String format = simpleExoPlayer.getCurrentTrackGroups().get(0).getFormat(0).sampleMimeType;
            //String lang = simpleExoPlayer.getTrackSelector();
            //Log.e("Exoplayer", "tesing1"+simpleExoPlayer.getAudioAttributes().contentType);
            //  Log.e("Exoplayer", "tesing1"+com.google.android.exoplayer2.ExoPlayer.Tra);
            // Log.e("Exoplayer", "tesing2"+simpleExoPlayer.getAudioComponent().toString());
            //  Log.e("Exoplayer", "tesing3"+simpleExoPlayer.getAudioFormat());
            // Log.e("Exoplayer", "tesing4"+simpleExoPlayer.getAudioAttributes().contentType);
            //Log.e("Exoplayer", "tesing5"+simpleExoPlayer.getCurrentTrackSelections());
            //Log.e("Exoplayer", "tesing6"+simpleExoPlayer.getCurrentTrackGroups().length);


            /*
            for(int i = 0; i < simpleExoPlayer.getCurrentTrackGroups().length; i++){
            System.out.println("tesing"+simpleExoPlayer.getCurrentTrackGroups().length);
            String format = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
            String lang = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).language;
            String id = simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).id;
            System.out.println("tesing111111111111111111111111");
            System.out.println(simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0));
            Log.e("Exoplayer", simpleExoPlayer.getCurrentTrackGroups().get(i).getFormat(0).toString());
            Log.e("Exoplayer", "tesing111111111111111111111111");
            if(format.contains("audio") && id != null && lang != null){
                //System.out.println(lang + " " + id);
                audioLanguages.add(new Pair<>(id, lang));
            }
        }
             //finishe here

        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    @Override
    protected void onPause() {
        super.onPause();

        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.getPlaybackState();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        simpleExoPlayer.setPlayWhenReady(false);
        simpleExoPlayer.getPlaybackState();
        finish();
    }
}