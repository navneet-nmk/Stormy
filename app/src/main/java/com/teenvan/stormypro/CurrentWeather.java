package com.teenvan.stormypro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by navneet on 09/08/15.
 */
public class CurrentWeather {

    // Declaration of member variables
    private String mIcon;
    private String mSummary;
    private long mTime;
    private int mNearestStormDistance;
    private int nearestStormBearing;
    private int mPrecipIntensity;
    private int mPrecipProbability;
    private Double mTemperature;
    private Double mApparentTemperature;
    private Double mDewPoint;
    private Double mHumidity;
    private Double mWindSpeed;
    private int mWindBearing;
    private Double mVisibility;
    private Double mCloudCover;
    private Double mPressure;
    private Double mOzone;
    private String mTimeZone;

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public int getNearestStormDistance() {
        return mNearestStormDistance;
    }

    public void setNearestStormDistance(int nearestStormDistance) {
        mNearestStormDistance = nearestStormDistance;
    }

    public int getNearestStormBearing() {
        return nearestStormBearing;
    }

    public void setNearestStormBearing(int nearestStormBearing) {
        this.nearestStormBearing = nearestStormBearing;
    }

    public int getPrecipIntensity() {
        return mPrecipIntensity;
    }

    public void setPrecipIntensity(int precipIntensity) {
        mPrecipIntensity = precipIntensity;
    }

    public int getPrecipProbability() {
        return mPrecipProbability;
    }

    public void setPrecipProbability(int precipProbability) {
        mPrecipProbability = precipProbability;
    }

    public Double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(Double temperature) {
        mTemperature = temperature;
    }

    public Double getApparentTemperature() {
        return mApparentTemperature;
    }

    public void setApparentTemperature(Double apparentTemperature) {
        mApparentTemperature = apparentTemperature;
    }

    public Double getDewPoint() {
        return mDewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        mDewPoint = dewPoint;
    }

    public Double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(Double humidity) {
        mHumidity = humidity;
    }

    public Double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        mWindSpeed = windSpeed;
    }

    public int getWindBearing() {
        return mWindBearing;
    }

    public void setWindBearing(int windBearing) {
        mWindBearing = windBearing;
    }

    public Double getVisibility() {
        return mVisibility;
    }

    public void setVisibility(Double visibility) {
        mVisibility = visibility;
    }

    public Double getCloudCover() {
        return mCloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        mCloudCover = cloudCover;
    }

    public Double getPressure() {
        return mPressure;
    }

    public void setPressure(Double pressure) {
        mPressure = pressure;
    }

    public Double getOzone() {
        return mOzone;
    }

    public void setOzone(Double ozone) {
        mOzone = ozone;
    }

    public String getFormattedTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime() * 1000);
        String timeString = formatter.format(dateTime);
        return timeString;
    }

    public String getDayOfTheWeek(long time){
        GregorianCalendar cal =new GregorianCalendar();
        cal.setTime(new Date(getTime()*1000));
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
        }
        return "unknown";
    }
}
