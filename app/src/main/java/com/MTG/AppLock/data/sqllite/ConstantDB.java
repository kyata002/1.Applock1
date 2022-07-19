package com.MTG.AppLock.data.sqllite;

public class ConstantDB {
    protected static final int DB_VERSION = 1;
    protected static final String DB_NAME = "hn_applock.db";
    // table intruder
    protected static final String TABLE_INTRUDER = "intruder";
    protected static final String INTRUDER_ID = "intruder_id";
    protected static final String INTRUDER_PACKAGE = "intruder_package";
    protected static final String INTRUDER_PATH = "intruder_path";
    protected static final String INTRUDER_TIME = "intruder_time";
    // table configuration
    protected static final String TABLE_CONFIGURATION = "configuration";
    protected static final String CONFIGURATION_ID = "configuration_id";
    protected static final String CONFIGURATION_NAME = "configuration_name";
    protected static final String CONFIGURATION_PACKAGE_APP_LOOK = "configuration_package_app_lock";
    protected static final String CONFIGURATION_PACKAGE_APP_LOOK_TEMP = "configuration_package_app_lock_temp";
    protected static final String CONFIGURATION_IS_DEFAULT = "configuration_is_default";
    protected static final String CONFIGURATION_HOUR_START = "configuration_hour_start";
    protected static final String CONFIGURATION_MINUTE_START = "configuration_minute_start";
    protected static final String CONFIGURATION_HOUR_END = "configuration_hour_end";
    protected static final String CONFIGURATION_MINUTE_END = "configuration_minute_end";
    protected static final String CONFIGURATION_EVERY = "configuration_every";
    protected static final String CONFIGURATION_IS_ACTIVE = "configuration_is_active";
    //
    public static final int CONFIGURATION_ID_CREATE = -1;
    public static final int CONFIGURATION_ID_DEFAULT = 1; // configuration custom
    // table theme
    protected static final String TABLE_THEME = "theme";
    protected static final String THEME_ID = "theme_id";
    protected static final String THEME_TYPE = "theme_type";
    // table scan file
    protected static final String TABLE_SCAN_FILE = "scan_file";
    protected static final String SCAN_FILE_ID = "scan_file_id";
    protected static final String SCAN_FILE_PATH = "scan_file_path";
    // offline
    protected static final String THEME_BACKGROUND_RES_ID = "theme_background_res_id"; // pin, pattern, background
    protected static final String THEME_THUMBNAIL_RES_ID = "theme_thumbnail_res_id"; // pin, pattern, background
    protected static final String THEME_BUTTON_RES_LIST = "theme_button_res_list"; // pin
    protected static final String THEME_SELECTED_RES_ID = "theme_selected_res_id"; // pattern
    protected static final String THEME_UNSELECTED_RES_ID = "theme_unselected_res_id"; // pattern
    protected static final String THEME_SELECTED_RES_ID_LIST = "theme_selected_res_id_list"; // pattern
    protected static final String THEME_UNSELECTED_RES_ID_LIST = "theme_unselected_res_id_list"; // pattern
    protected static final String THEME_LINE_COLOR_RES_ID = "theme_color_line_res_id"; // pattern
    protected static final String THEME_TEXT_COLOR_MESSAGE_RES_ID = "theme_color_text_message_res_id"; // pin, pattern
    protected static final String THEME_BUTTON_COLOR_RES_ID = "theme_color_button_res_id"; // pin, pattern
    protected static final String THEME_SELECTOR_CHECKBOX_COLOR_RES_ID = "theme_selector_checkbox_color_res_id"; // pin
    protected static final String THEME_IS_DELETE_PADDING = "theme_is_delete_padding";
    protected static final String THEME_IS_NUMBER_PADDING = "theme_is_number_padding";
    protected static final String THEME_DELETE_PADDING = "theme_delete_padding";
    // online
    protected static final String THEME_BACKGROUND_URL = "theme_background_url"; // pin, pattern, background
    protected static final String THEME_THUMBNAIL_URL = "theme_thumbnail_url"; // pin, pattern, background
    protected static final String THEME_BUTTON_URL_LIST = "theme_button_url_list"; // pin
    protected static final String THEME_SELECTED_URL = "theme_selected_url"; // pattern
    protected static final String THEME_UNSELECTED_URL = "theme_unselected_url"; // pattern
    protected static final String THEME_LINE_COLOR = "theme_line_color"; // pattern
    protected static final String THEME_TEXT_COLOR_MESSAGE = "theme_color_text_message"; // pin, pattern
    protected static final String THEME_SELECTOR_CHECKBOX_COLOR = "theme_selector_checkbox_color"; // pin
    // download
    protected static final String THEME_BACKGROUND_DOWNLOAD = "theme_background_download"; // pin, pattern, background
    protected static final String THEME_THUMBNAIL_DOWNLOAD = "theme_thumbnail_download"; // pin, pattern, background
    protected static final String THEME_BUTTON_DOWNLOAD_LIST = "theme_button_download_list"; // pin
    protected static final String THEME_SELECTED_DOWNLOAD = "theme_selected_download"; // pattern
    protected static final String THEME_UNSELECTED_DOWNLOAD = "theme_unselected_download"; // pattern
    //
    protected static final String THEME_CONFIG = "theme_config"; // pin, pattern
    protected static final String THEME_POINT_NUMBER = "theme_point_number";
    protected static final String THEME_IS_SHOW_DELETE = "theme_is_show_delete";
    //
    protected static final String THEME_LAST_UPDATE = "theme_last_update";

    protected static final int THEME_DEFAULT_ID = 1;
}
