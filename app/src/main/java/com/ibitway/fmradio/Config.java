package com.ibitway.fmradio;

import com.ibitway.fmradio.models.SocialObject;
import com.ibitway.fmradio.utilities.Tools;

import java.util.ArrayList;

public class Config {

    //[9:28 pm, 03/09/2021] Singda Fiverr: Email: ilctamilradio@gmail.com
    //Live program Line: 0044 2032 864 959
    //Facebook: ILC Tamil Radio
    //Viber : 0044 7402 078 768
    //Skype: ilctamililctamil
    //Customer Service: 0044 7448 360 021
    //Join via Zoom- https://us02web.zoom.us/j/2032864959
    //Zoom ID 203 286 4959 (No Passcode)
    //[9:30 pm, 03/09/2021] Singda Fiverr: Youtube LIVE: http://ilctamil.com/index.php/en/live-youtube
    //Youtube CHANNEL : https://www.youtube.com/user/ILCTamilRadio

    public static String RADIO_STREAM_URL = "http://station.voscast.com/584482a378191";
    public static String CURRENT_STATION = "ILC Tamil Radio";

    //set true to enable admin panel or set false to disable
    public static final boolean ENABLE_ADMIN_PANEL = false;
    //if admin panel enabled, put your admin panel url here
    public static final String ADMIN_PANEL_URL = "http://10.0.2.2/your_single_radio";

    public static String SOCIAL_URL = "http://www.google.com";
    public static String REQUEST = "abc@gmail.com";
    public static String CONTACT = "http://www.google.com";
    public static Boolean isAutoPlay = true;

    public static String ABOUT_DATA = "About Us";

    public static ArrayList<SocialObject> socialList = new ArrayList<>();
    public static ArrayList<SocialObject> banners = new ArrayList<>();

    //ads configuration
    public static final boolean ENABLE_ADMOB_BANNER_ADS = true;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_LOAD = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_DRAWER_SELECTION = true;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ON_PLAY = false;
    public static final int ADMOB_INTERSTITIAL_ON_PLAY_INTERVAL = 3;

    //auto play function
    public static final boolean ENABLE_AUTO_PLAY = true;

    //layout customization
    public static final boolean ENABLE_SOCIAL_MENU = true;

    //album art configuration
    public static final boolean ENABLE_ALBUM_ART = true;
    public static final boolean ENABLE_CIRCULAR_IMAGE_ALBUM_ART = true;
    public static final int ALBUM_ART_BORDER_WIDTH = 8;
    public static final int ALBUM_ART_CORNER_RADIUS = 30;

    //when it enabled, the radio will be reloaded and the metadata will immediately be updated
    //there may be a slight delay on play audio streaming when the metadata is updated
    public static final boolean FORCE_UPDATE_METADATA_ON_RESUME = true;

    //splash screen duration in millisecond
    public static final int SPLASH_SCREEN_DURATION = 3000;

    public static String EMAIL = "abc@gmail.com";
}