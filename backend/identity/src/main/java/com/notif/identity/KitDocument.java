package com.notif.identity;

import java.util.ArrayList;
import java.util.List;

public class KitDocument {
    private int version = 1;
    private Preferences preferences = new Preferences();
    private List<Interest> interests = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests;
    }

    public static class Preferences {
        private Channels channels = new Channels();
        private String email = "";
        private Slack slack = new Slack();
        private String pushoverUserKey = "";

        public Channels getChannels() {
            return channels;
        }

        public void setChannels(Channels channels) {
            this.channels = channels;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Slack getSlack() {
            return slack;
        }

        public void setSlack(Slack slack) {
            this.slack = slack;
        }

        public String getPushoverUserKey() {
            return pushoverUserKey;
        }

        public void setPushoverUserKey(String pushoverUserKey) {
            this.pushoverUserKey = pushoverUserKey;
        }
    }

    public static class Channels {
        private boolean email = true;
        private boolean slack = false;
        private boolean pushover = false;

        public boolean isEmail() {
            return email;
        }

        public void setEmail(boolean email) {
            this.email = email;
        }

        public boolean isSlack() {
            return slack;
        }

        public void setSlack(boolean slack) {
            this.slack = slack;
        }

        public boolean isPushover() {
            return pushover;
        }

        public void setPushover(boolean pushover) {
            this.pushover = pushover;
        }
    }

    public static class Slack {
        private String mode = "chatbot";
        private String userId = "";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    public static class Interest {
        private String type;
        private String kind;
        private Integer minMagnitude;
        private String instrument;
        private Integer movePercent;
        private List<String> topics;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public Integer getMinMagnitude() {
            return minMagnitude;
        }

        public void setMinMagnitude(Integer minMagnitude) {
            this.minMagnitude = minMagnitude;
        }

        public String getInstrument() {
            return instrument;
        }

        public void setInstrument(String instrument) {
            this.instrument = instrument;
        }

        public Integer getMovePercent() {
            return movePercent;
        }

        public void setMovePercent(Integer movePercent) {
            this.movePercent = movePercent;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }
    }
}
