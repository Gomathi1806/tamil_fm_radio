package com.ibitway.fmradio.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.ibitway.fmradio.BuildConfig;
import com.ibitway.fmradio.Config;
import com.ibitway.fmradio.R;
import com.ibitway.fmradio.activities.MainActivity;
import com.ibitway.fmradio.activities.WebviewActivity;
import com.ibitway.fmradio.models.RadioResponse;
import com.ibitway.fmradio.services.MediaNotificationManager;
import com.ibitway.fmradio.services.PlaybackStatus;
import com.ibitway.fmradio.services.RadioManager;

import com.ibitway.fmradio.services.metadata.Metadata;
import com.ibitway.fmradio.services.parser.UrlParser;
import com.ibitway.fmradio.utilities.CollapseControllingFragment;
import com.ibitway.fmradio.utilities.Log;
import com.ibitway.fmradio.utilities.PermissionsFragment;
import com.ibitway.fmradio.utilities.SharedPref;
import com.ibitway.fmradio.utilities.SleepTimeReceiver;
import com.ibitway.fmradio.utilities.Tools;
import com.ibitway.fmradio.utilities.Tools.EventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import es.claucookie.miniequalizerlibrary.EqualizerView;

import static android.content.Context.ALARM_SERVICE;

/**
 * This fragment is used to listen to a radio station
 */
public class FragmentRadio extends Fragment implements OnClickListener, PermissionsFragment, CollapseControllingFragment, EventListener {

    private RadioManager radioManager;
    private String urlToPlay = Config.RADIO_STREAM_URL;
    private Activity activity;
    private RoundedImageView albumArtView;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private ImageView buttonPlayPause;
    private Toolbar toolbar;
    private MainActivity mainActivity;
    private ImageView img_volume_bar;
    private ImageView aboutIv;
    private ImageView fbIv,img_menu;
    private int counter = 1;
    Handler handler = new Handler();
    SharedPref sharedPref;
    EqualizerView equalizerView;
    Tools tools;
    TextView artistView;
    //CircleBarVisualizer circleVisualizer;
    ImageView adView;
    List<DocumentSnapshot> documentSnapshot;
    private  int adIndex = 0;
    Handler adHandler = new Handler();
    Runnable runnable  = null;
    int delay = 10 * 1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    //private Callback<RadioResponse> callback;

    String nextShow = "";

    private MediaNotificationManager notificationManager;
    Metadata metadata;
    Bitmap albumBitmap;

    public FragmentRadio() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        relativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_radio, container, false);

        toolbar = relativeLayout.findViewById(R.id.toolbar);
        //circleVisualizer = relativeLayout.findViewById(R.id.visualizer);

        adView = relativeLayout.findViewById(R.id.adView);

        setupToolbar();

        setHasOptionsMenu(true);

        sharedPref = new SharedPref(getActivity());
        sharedPref.setCheckSleepTime();
        tools = new Tools(getActivity());

        initializeUIElements();

        if (Config.ENABLE_AUTO_PLAY) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonPlayPause.performClick();
                }
            }, 1000);
        }

        //Initialize visualizer or imageview for album art
        if (Config.ENABLE_ALBUM_ART) {
            albumArtView.setVisibility(View.VISIBLE);
        } else {
            albumArtView.setVisibility(View.GONE);
        }

        albumArtView.setImageBitmap(Tools.BACKGROUND_IMAGE_ID);

        //loadInterstitialAd();

        onBackPressed();

        return relativeLayout;
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        mainActivity.setSupportActionBar(toolbar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
        activity = getActivity();

        //fetchAds();

        Tools.isOnlineShowDialog(activity);
        // set custom color to the line.
        //circleVisualizer.setColor(ContextCompat.getColor(getContext(), R.color.white));

        // Customize the size of the circle. by defalut multipliers is 1.
        //circleVisualizer.setRadiusMultiplier(2f);

        // set the line with for the visualizer between 1-10 default 1.
        //circleVisualizer.setStrokeWidth(1);

        //Get the radioManager
        radioManager = RadioManager.with();
        //notificationManager = new MediaNotificationManager(RadioManager.getService());

        progressBar.setVisibility(View.VISIBLE);
        //Obtain the actual radio url
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                urlToPlay = (UrlParser.getUrl(urlToPlay));
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        updateButtons();
                    }
                });
            }

        });

        if (isPlaying()) {
            onAudioSessionId(RadioManager.getService().getAudioSessionId());
        }

        fetchAds();
    }

    public void populateData(RadioResponse radioResponse){

        TextView nowPlaying = relativeLayout.findViewById(R.id.now_playing);
        nowPlaying.setText(R.string.app_name);

        if (radioResponse != null){

            if (radioResponse.getBroadcast() != null){

                if (radioResponse.getBroadcast().getCurrentShow() != null){

                    if (radioResponse.getBroadcast().getCurrentShow().getShow() != null){

                        metadata = new Metadata(radioResponse.getBroadcast().getCurrentShow().getTime(), radioResponse.getBroadcast().getCurrentShow().getShow().getName());

                        nowPlaying.setText(radioResponse.getBroadcast().getCurrentShow().getShow().getName()+ "\n" +
                                radioResponse.getBroadcast().getCurrentShow().getTime());

                        if (radioResponse.getBroadcast().getCurrentShow().getShow().getAvatarUrl() != null){

                            Picasso.with(getContext()).load(radioResponse.getBroadcast().getCurrentShow().getShow().getAvatarUrl())
                                    .fit().centerCrop()
                                    .into(albumArtView);

                            Picasso.with(getContext())
                                    .load(radioResponse.getBroadcast().getCurrentShow().getShow().getAvatarUrl())
                                    .placeholder(R.drawable.bg_album_art)
                                    .into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                            albumBitmap = bitmap;
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {
                                            albumBitmap = null;
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    });
                        }

                        radioManager.getRadioService().getNotificationManager().startNotify(albumBitmap,metadata);
                    }
                }

                if (radioResponse.getBroadcast().getNextShow() != null){

                    if (radioResponse.getBroadcast().getNextShow().getShow() != null){

                        //nextShow = "Up next: "+radioResponse.getBroadcast().getNextShow().getShow().getName();

                        String givenDateString = radioResponse.getBroadcast().getCurrentShow().getEnd();

                        long min = Integer.parseInt(givenDateString.split(":")[0]) * 60000 ;
                        long sec = Integer.parseInt(givenDateString.split(":")[1]) * 1000;

                        long finalTime = min + sec;

                        new CountDownTimer(finalTime, 1000) {

                            public void onTick(long millisUntilFinished) {
                                long second = (millisUntilFinished / 1000) % 60;
                                long minutes = (millisUntilFinished/(1000*60)) % 60;
                                //artistView.setText(nextShow+ "\nRemaining Time: "+minutes+":" +second);
                            }

                            public void onFinish() {
                                //artistView.setText(nextShow+"\nRemaining Time: 00:00");
                            }

                        }.start();
                    }
                }

            }
        }

    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d", m,s);
    }

    @Override
    public void onEvent(String status) {

        switch (status) {
            case PlaybackStatus.LOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;

            case PlaybackStatus.ERROR:
                makeSnackBar(R.string.error_retry);
                break;
        }

        if (!status.equals(PlaybackStatus.LOADING))
            progressBar.setVisibility(View.INVISIBLE);

        updateButtons();

        //TODO Updating the button
        //trigger.setImageResource(status.equals(PlaybackStatus.PLAYING)
        //        ? R.drawable.ic_pause_black
        //        : R.drawable.ic_play_arrow_black);

    }

    @Override
    public void onAudioSessionId(Integer i) {
    }

    @Override
    public void onStart() {
        super.onStart();
        Tools.registerAsListener(this);
    }

    @Override
    public void onStop() {
        Tools.unregisterAsListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (!radioManager.isPlaying())
            radioManager.unbind(getContext());
        Tools.unregisterAsListener(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateButtons();
        radioManager.bind(getContext());

        notificationManager = new MediaNotificationManager(RadioManager.getService(),getResources());

        if (Config.FORCE_UPDATE_METADATA_ON_RESUME) {
            if (isPlaying()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startResume();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startResume();
                            }
                        }, 10);
                    }
                }, 10);
            }
        }

    }

    private void initializeUIElements() {
        progressBar = relativeLayout.findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);

        equalizerView = relativeLayout.findViewById(R.id.equalizer_view);
        artistView = relativeLayout.findViewById(R.id.now_playing2);

        albumArtView = relativeLayout.findViewById(R.id.albumArt);
        albumArtView.setCornerRadius((float) Config.ALBUM_ART_CORNER_RADIUS);
        //albumArtView.setBorderWidth((float) Config.ALBUM_ART_BORDER_WIDTH);

        if (Config.ENABLE_CIRCULAR_IMAGE_ALBUM_ART) {
            albumArtView.setOval(true);
        } else {
            albumArtView.setOval(false);
        }

        img_menu = relativeLayout.findViewById(R.id.menu_toggle);
        img_menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity != null) {
                    mainActivity.toggleMenu();
                }
            }
        });

        aboutIv = relativeLayout.findViewById(R.id.about_iv);
        aboutIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        fbIv = relativeLayout.findViewById(R.id.fb_iv);
        fbIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = Config.EMAIL;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, email);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } else {
                    Toast.makeText(
                            getContext(),
                            "Email app not installed in your phone",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                //openTimeSelectDialog();
            }
        });

        SeekBar seekBar = relativeLayout.findViewById(R.id.seekBar);
        //seekBar.getThumb().setColorFilter(sharedPref.getFirstColor(), PorterDuff.Mode.SRC_IN);
        //seekBar.getProgressDrawable().setColorFilter(sharedPref.getSecondColor(), PorterDuff.Mode.SRC_IN);

        final AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setProgress(volume_level);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonPlayPause = relativeLayout.findViewById(R.id.btn_play_pause);
        buttonPlayPause.setOnClickListener(this);

        equalizerView.stopBars();
        updateButtons();

    }

    private void updateButtons() {
        if (isPlaying() || progressBar.getVisibility() == View.VISIBLE) {
            //If another stream is playing, show this in the layout
            if (RadioManager.getService() != null && urlToPlay != null && !urlToPlay.equals(RadioManager.getService().getStreamUrl())) {
                buttonPlayPause.setImageResource(R.drawable.ic_play_white);
                relativeLayout.findViewById(R.id.already_playing_tooltip).setVisibility(View.VISIBLE);

                //If this stream is playing, adjust the buttons accordingly
            } else {
                buttonPlayPause.setImageResource(R.drawable.ic_pause_white);
                relativeLayout.findViewById(R.id.already_playing_tooltip).setVisibility(View.GONE);
            }
        } else {
            //If this stream is paused, adjust the buttons accordingly
            buttonPlayPause.setImageResource(R.drawable.ic_play_white);
            relativeLayout.findViewById(R.id.already_playing_tooltip).setVisibility(View.GONE);

            updateMediaInfoFromBackground(null, null);
        }

        if (isPlaying()) {
            equalizerView.animateBars();
            // Set your media player to the visualizer.
            //circleVisualizer.setVisibility(View.VISIBLE);
            //circleVisualizer.setPlayer(RadioManager.getService().getAudioSessionId());
        } else {
            equalizerView.stopBars();
            //circleVisualizer.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        requestStoragePermission();
    }

    private void startStopPlaying() {
        //Start the radio playing
        radioManager.playOrPause(urlToPlay);
        //Update the UI
        updateButtons();
    }

    private void startResume() {
        //Start the radio playing
        radioManager.playResume(urlToPlay);
        //Update the UI
        updateButtons();
    }

    private void stopService() {
        radioManager.stopServices();
        Tools.unregisterAsListener(this);
    }

    //@param info - the text to be updated. Giving a null string will hide the info.
    public void updateMediaInfoFromBackground(String info, Bitmap image) {
        TextView nowPlayingTitle = relativeLayout.findViewById(R.id.title);
        TextView nowPlaying = relativeLayout.findViewById(R.id.now_playing);
        nowPlaying.setText(R.string.app_name);

        if (info != null)
            nowPlaying.setText(info);

        if (info != null ) {
            nowPlaying.setVisibility(View.VISIBLE);
        } else if (info == null) {
            nowPlaying.setVisibility(View.VISIBLE);
            nowPlaying.setText(R.string.app_name);
        }

        if (image != null) {
            albumArtView.setImageBitmap(image);
        } else {
            albumArtView.setImageBitmap(Tools.BACKGROUND_IMAGE_ID);
        }

    }

    @Override
    public String[] requiredPermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO};
    }

    @Override
    public void onMetaDataReceived(Metadata meta, Bitmap image) {
        //Update the mediainfo shown above the controls
        String artistAndSong = null;
        if (meta != null && meta.getArtist() != null) {
            artistAndSong = meta.getArtist() + " - " + meta.getSong();
            artistView.setText(meta.getArtist());
        }
        updateMediaInfoFromBackground(artistAndSong, image);

    }

    private boolean isPlaying() {
        return (null != radioManager && null != RadioManager.getService() && RadioManager.getService().isPlaying());
    }

    @Override
    public boolean supportsCollapse() {
        return false;
    }

    private void makeSnackBar(int text) {
        Snackbar bar = Snackbar.make(buttonPlayPause, text, Snackbar.LENGTH_SHORT);
        bar.show();
        ((TextView) bar.getView().findViewById(R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.white));
    }

    public void onBackPressed() {
        ((MainActivity) getActivity()).setOnBackClickListener(new MainActivity.OnBackClickListener() {
            @Override
            public boolean onBackClick() {
                exitDialog();
                return true;
            }
        });
    }

    public void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getResources().getString(R.string.message));
        dialog.setPositiveButton(getResources().getString(R.string.quit), (dialogInterface, i) -> {
            stopService();
            getActivity().finish();
        });

        dialog.setNegativeButton(getResources().getString(R.string.minimize), (dialogInterface, i) -> minimizeApp());

        dialog.setNeutralButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {

        });
        dialog.show();
    }

    public void minimizeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestStoragePermission() {
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (!isPlaying()) {
                                if (urlToPlay != null) {

                                    startStopPlaying();
                                    //showInterstitialAd();

                                    //Check the sound level
                                    AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                                    int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    if (volume_level < 2) {
                                        makeSnackBar(R.string.volume_low);
                                    }

                                } else {
                                    //The loading of urlToPlay should happen almost instantly, so this code should never be reached
                                    makeSnackBar(R.string.error_retry_later);
                                }
                            } else {
                                startStopPlaying();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getActivity(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void changeVolume() {
        final RelativePopupWindow popupWindow = new RelativePopupWindow(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.lyt_volume, null);
        ImageView imageView1 = view.findViewById(R.id.img_volume_max);
        ImageView imageView2 = view.findViewById(R.id.img_volume_min);
        imageView1.setColorFilter(Color.BLACK);
        imageView2.setColorFilter(Color.BLACK);

        VerticalSeekBar seekBar = view.findViewById(R.id.seek_bar_volume);
        seekBar.getThumb().setColorFilter(sharedPref.getFirstColor(), PorterDuff.Mode.SRC_IN);
        seekBar.getProgressDrawable().setColorFilter(sharedPref.getSecondColor(), PorterDuff.Mode.SRC_IN);

        final AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setProgress(volume_level);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setContentView(view);
        popupWindow.showOnAnchor(img_volume_bar, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
    }

    public void openTimeSelectDialog() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
        alt_bld.setTitle(getString(R.string.sleep_time));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.lyt_dialog_select_time, null);
        alt_bld.setView(dialogView);

        final TextView tv_min = dialogView.findViewById(R.id.txt_minutes);
        tv_min.setText("1 " + getString(R.string.min));
        FrameLayout frameLayout = dialogView.findViewById(R.id.frameLayout);

        final IndicatorSeekBar seekbar = IndicatorSeekBar
                .with(getActivity())
                .min(1)
                .max(120)
                .progress(1)
                .thumbColor(sharedPref.getSecondColor())
                .indicatorColor(sharedPref.getFirstColor())
                .trackProgressColor(sharedPref.getFirstColor())
                .build();

        seekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                tv_min.setText(seekParams.progress + " " + getString(R.string.min));
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        frameLayout.addView(seekbar);

        alt_bld.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String hours = String.valueOf(seekbar.getProgress() / 60);
                String minute = String.valueOf(seekbar.getProgress() % 60);

                if (hours.length() == 1) {
                    hours = "0" + hours;
                }

                if (minute.length() == 1) {
                    minute = "0" + minute;
                }

                String totalTime = hours + ":" + minute;
                long total_timer = tools.convertToMilliSeconds(totalTime) + System.currentTimeMillis();

                Random random = new Random();
                int id = random.nextInt(100);

                sharedPref.setSleepTime(true, total_timer, id);

                Intent intent = new Intent(getActivity(), SleepTimeReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, total_timer, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, total_timer, pendingIntent);
                }
            }
        });
        alt_bld.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    public void openTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.sleep_time));
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.lyt_dialog_time, null);
        builder.setView(dialogView);

        TextView textView = dialogView.findViewById(R.id.txt_time);

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getActivity(), SleepTimeReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), sharedPref.getSleepID(), i, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                pendingIntent.cancel();
                alarmManager.cancel(pendingIntent);
                sharedPref.setSleepTime(false, 0, 0);
            }
        });

        updateTimer(textView, sharedPref.getSleepTime());

        builder.show();
    }

    private void updateTimer(final TextView textView, long time) {
        long timeleft = time - System.currentTimeMillis();
        if (timeleft > 0) {
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeleft),
                    TimeUnit.MILLISECONDS.toMinutes(timeleft) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeleft) % TimeUnit.MINUTES.toSeconds(1));

            textView.setText(hms);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sharedPref.getIsSleepTimeOn()) {
                        updateTimer(textView, sharedPref.getSleepTime());
                    }
                }
            }, 1000);
        }
    }

    private void fetchAds() {

        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("customAds").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()){

                    QuerySnapshot snapshot = task.getResult();
                    if (!snapshot.isEmpty()) {
                        documentSnapshot = snapshot.getDocuments();
                        adView.setVisibility(View.VISIBLE);
                        loadAd();
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("Error", e.getMessage());
                    }
                });
    }


    private void loadAd(){

        adHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (documentSnapshot != null) {
                                if (!documentSnapshot.isEmpty()) {
                                    refreshAdView();
                                }
                            }
                        }
                    });
                }

                runnable = this;
                adHandler.postDelayed(runnable,delay);
            }
        }, delay);
    }

    private void refreshAdView() {

        adIndex = Tools.randInt(0,documentSnapshot.size());

        if (adIndex >= documentSnapshot.size()){
            adIndex = adIndex - 1;
        }

        if (documentSnapshot.get(adIndex).getString("image") != null) {
            Picasso.with(getContext()).load(documentSnapshot.get(adIndex).getString("image"))
                    .fit()
                    .into(adView);


            adView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(documentSnapshot.get(adIndex).getString("url"))
                            )
                    );
                }
            });
        }
    }
}