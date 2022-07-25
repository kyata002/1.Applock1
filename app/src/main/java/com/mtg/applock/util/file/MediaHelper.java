package com.mtg.applock.util.file;


import android.text.TextUtils;

import com.mtg.applock.util.Const;

public class MediaHelper {
    public static final String[] IMAGE_SUPPORT_FORMAT = {
            ".jpg", ".jpeg",
            ".png", ".gif"
    };

    public static final String[] IMAGE_SUPPORT_FORMAT_SUB = {
            ".jpg", ".jpeg",
            ".png"
    };

    public static final String[] VIDEO_SUPPORT_FORMAT = {
            ".flac",
            ".mp4",
            ".mpeg"
    };

    public static final String[] AUDIO_SUPPORT_FORMAT = {
            ".ac3",
            ".aiff",
            ".m4a",
            ".mp3",
            ".ogg",
            ".opus",
            ".ts",
            ".wav",
    };
    public static final String[] FILES_SUPPORT_FORMAT = {
            ".doc", ".txt", ".xls", ".docx", ".xlsx", ".pdf", ".ppt", ".html"
    };
    public static final String[] FILES_SUPPORT_FORMAT_NAME = {
            "Other", "Txt", "Other", "Docx", "Xlsx", "Pdf", "Ppt", "Html"
    };

    public static final String[] FILES_SUPPORT_FORMAT_OTHER = {".doc", ".xls"};

    public static final String EXTENSION_OTHER_SUPPORT_FORMAT = ".other";

    public static final String EXTENSION_TXT_SUPPORT_FORMAT = ".txt";

    public static boolean isSupportImage(String path) {
        return isSupport(path, IMAGE_SUPPORT_FORMAT);
    }

    public static boolean isSupportVideo(String path) {
        return isSupport(path, VIDEO_SUPPORT_FORMAT);
    }

    public static boolean isSupportAudio(String path) {
        return isSupport(path, AUDIO_SUPPORT_FORMAT);
    }

    public static boolean isSupportFiles(String path) {
        return isSupport(path, FILES_SUPPORT_FORMAT);
    }

    public static boolean isSupport(String path, int type) {
        if (type == Const.TYPE_IMAGES) {
            return isSupportImage(path);
        } else if (type == Const.TYPE_VIDEOS) {
            return isSupportVideo(path);
        } else if (type == Const.TYPE_AUDIOS) {
            return isSupportAudio(path);
        } else if (type == Const.TYPE_FILES) {
            return isSupportFiles(path);
        } else return false;
    }

    public static boolean isSupport(String path, String[] extensions) {
        path = path.toLowerCase();
        for (String extension : extensions) {
            if (path.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsExtensions(String path1, String path2) {
        String extension1 = FilePathHelper.INSTANCE.getExtension(path1.toLowerCase());
        String extension2 = FilePathHelper.INSTANCE.getExtension(path2.toLowerCase());
        if (TextUtils.isEmpty(extension1)) return false;
        if (TextUtils.isEmpty(extension2)) return false;
        return TextUtils.equals(extension1, extension2);
    }
}
