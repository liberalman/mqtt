package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public interface CallStateChangeListener {
    void onCallStateChanged(CallStateChangeListener.CallState var1, CallStateChangeListener.CallError var2);

    public static enum CallError {
        ERROR_NONE("error_none"),
        ERROR_TRANSPORT("error_transport"),
        ERROR_INAVAILABLE("error_inavailable"),
        REJECTED("rejected"),
        ERROR_NORESPONSE("error_noresponse"),
        ERROR_BUSY("busy"),
        ERROR_NO_DATA("error_no_data");

        private final String error;

        private CallError(String var3) {
            this.error = var3;
        }

        public String toString() {
            return this.error;
        }
    }

    public static enum CallState {
        IDLE("idle"),
        RINGING("ringing"),
        ANSWERING("answering"),
        PAUSING("pausing"),
        CONNECTING("connecting"),
        CONNECTED("conntected"),
        ACCEPTED("accepted"),
        DISCONNNECTED("disconnected"),
        VOICE_PAUSE("voice_pause"),
        VOICE_RESUME("voice_resume"),
        VIDEO_PAUSE("video_pause"),
        VIDEO_RESUME("video_resume"),
        NETWORK_UNSTABLE("network_unstable"),
        NETWORK_NORMAL("network_normal");

        private final String state;

        private CallState(String var3) {
            this.state = var3;
        }

        public String toString() {
            return this.state;
        }
    }
}

