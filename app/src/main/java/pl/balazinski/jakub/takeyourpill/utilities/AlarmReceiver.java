package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    private static Ringtone mRingtone = null;
    private OutputProvider outputProvider;
    private Context mContext;

    public AlarmReceiver() {
    }

    public AlarmReceiver(Context context) {
        this.mContext = context;
        outputProvider = new OutputProvider(context);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        mRingtone = RingtoneManager.getRingtone(context, alarmUri);
        mRingtone.play();

        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.MAIN_FROM_ALARM_KEY, Constants.MAIN_FROM_ALARM);
        i.putExtra(Constants.EXTRA_LONG_ALARM_ID, bundle.getLong(Constants.EXTRA_LONG_ALARM_ID));

        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (i.setComponent(comp)));

        setResultCode(Activity.RESULT_OK);
    }

    /**
     * Method sets repeating alarm what means that you choose hour,minute and days
     * you want alarm to repeat. If you won't choose days to repeat alarm will fire
     * in given hour next day.
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setRepeatingAlarm(Context context, Long alarmID) {

        List<Long> daysList = new ArrayList<>();
        String daysOfWeek;
        char[] daysOfWeekArray;
        Calendar calendar = Calendar.getInstance();

        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);

            daysOfWeek = alarm.getDaysRepeating();
            daysOfWeekArray = daysOfWeek.toCharArray();

            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());

            outputProvider.displayLog(TAG, "alarm.getHour = " + alarm.getHour() + ";  alarm.getMinute = " + alarm.getMinute());

            Calendar now = Calendar.getInstance();
            outputProvider.displayLog(TAG, "NOW day = " + now.get(Calendar.DAY_OF_WEEK) + "; timeinMillis = " + now.getTimeInMillis() + ";  date: " + now.getTime());

            int weekOfMonth;
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            outputProvider.displayLog(TAG, "calendar HOUR = " + now.get(Calendar.HOUR_OF_DAY) + ";  calendar MINUTE = " + now.get(Calendar.MINUTE));
            int i;
            for (i = 0; i < daysOfWeekArray.length; i++) {
                if (daysOfWeekArray[i] == '1') {
                    if (i < 6) {
                        calendar.set(Calendar.DAY_OF_WEEK, i + 2);

                        if (i + 2 == now.get(Calendar.DAY_OF_WEEK)) {
                            outputProvider.displayLog(TAG, "i + 2 == dayofweek");
                            if (alarm.getHour() == now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.gethour == now.gethourofday");
                                if (alarm.getMinute() <= now.get(Calendar.MINUTE)) {
                                    weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                    weekOfMonth = weekOfMonth + 1;
                                    calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                                    int day = i + 2;
                                    calendar.set(Calendar.DAY_OF_WEEK, day);
                                }
                            } else if (alarm.getHour() < now.get(Calendar.HOUR_OF_DAY)) {
                                weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                weekOfMonth = weekOfMonth + 1;
                                calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                                int day = i + 2;
                                calendar.set(Calendar.DAY_OF_WEEK, day);
                            }
                        } else if (i + 2 < now.get(Calendar.DAY_OF_WEEK)) {
                            weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                            weekOfMonth = weekOfMonth + 1;
                            calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                            int day = i + 2;
                            calendar.set(Calendar.DAY_OF_WEEK, day);
                        }


                        if (calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH)) {
                            month++;
                            calendar.set(Calendar.MONTH, month);
                        }
                        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR)) {
                            year++;
                            calendar.set(Calendar.YEAR, year);
                        }

                        daysList.add(calendar.getTimeInMillis());

                        outputProvider.displayLog(TAG, "i = " + i + ";  day = " + (i + 2) + "; timeInMillis = " + calendar.getTimeInMillis() +
                                ";  date: " + calendar.getTime());

                    } else if (i == 6) {
                        calendar.set(Calendar.DAY_OF_WEEK, 1);
                        int sunday = 1;
                        if (sunday == now.get(Calendar.DAY_OF_WEEK)) {
                            outputProvider.displayLog(TAG, "i  == dayofweek");
                            if (alarm.getHour() == now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.gethour == now.gethourofday");
                                if (alarm.getMinute() <= now.get(Calendar.MINUTE)) {
                                    weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                    weekOfMonth = weekOfMonth + 1;
                                    calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                                    calendar.set(Calendar.DAY_OF_WEEK, sunday);
                                }
                            } else if (alarm.getHour() < now.get(Calendar.HOUR_OF_DAY)) {
                                weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                weekOfMonth = weekOfMonth + 1;
                                calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                                calendar.set(Calendar.DAY_OF_WEEK, sunday);
                            }
                        }

                        if (calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH)) {
                            month++;
                            calendar.set(Calendar.MONTH, month);
                        }
                        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR)) {
                            year++;
                            calendar.set(Calendar.YEAR, year);
                        }

                        daysList.add(calendar.getTimeInMillis());
                        outputProvider.displayLog(TAG, "i = " + i + ";  day = " + 1 + "; timeInMillis = " + calendar.getTimeInMillis() +
                                ";  date: " + calendar.getTime().toString());

                    }

                    if (i + 2 <= now.get(Calendar.DAY_OF_WEEK)) {
                        int week = now.get(Calendar.WEEK_OF_MONTH);
                        calendar.set(Calendar.WEEK_OF_MONTH, week);
                    }
                }
            }
            outputProvider.displayLog(TAG, "collections min = " + Collections.min(daysList));


            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
            long alarmTimeInMillis = Collections.min(daysList);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
            outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));

            outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
        }
    }

    /**
     * Method sets alarm with interval. Alarm will fire from chosen date every given time (minute/hour)
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setIntervalAlarm(Context context, Long alarmID) {

        int interval, day, month, year;
        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            interval = alarm.getInterval();
            day = alarm.getDay();
            month = alarm.getMonth();
            year = alarm.getYear();

            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);

            if ((calendar.getTimeInMillis() - now.getTimeInMillis()) <= 0) {
                outputProvider.displayShortToast(context.getString(R.string.toast_add_new_date_to_interval));
                alarm.setIsActive(false);
                DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            } else {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
                long alarmTimeInMillis = calendar.getTimeInMillis();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, 1000 * 60 * interval, pendingIntent);
                outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
                outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
            }
        }
    }

    /**
     * Method sets single alarm what means it will fire only once at exact time and date.
     * You can re enable alarm by changing it's date in edit alarm option. Chosen pill will
     * stay with alarm.
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setSingleAlarm(Context context, Long alarmID) {

        int day, month, year;
        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            day = alarm.getDay();
            month = alarm.getMonth();
            year = alarm.getYear();

            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            if ((calendar.getTimeInMillis() - now.getTimeInMillis()) <= 0) {
                outputProvider.displayShortToast(context.getString(R.string.toast_add_new_date_to_interval));
                alarm.setIsActive(false);
                DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            } else {
                alarm.setIsActive(true);
                DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
                long alarmTimeInMillis = calendar.getTimeInMillis();
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
                outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
                outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
            }
        }
    }

    /**
     * Cancel every type of alarm. You can reactive alarm by setting it again with same alarm id
     *
     * @param context Activity context
     * @param id      alarm id
     */
    public void cancelAlarm(Context context, Long id) {
        Alarm alarm = DatabaseRepository.getAlarmById(context, id);
        if (alarm != null) {
            alarm.setIsActive(false);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
        }
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        outputProvider.displayLog(TAG, "cancel alarm  id == " + String.valueOf(id));

    }

    /**
     * Stops annoying ringtone from ringing!!
     */
    public static void stopRingtone() {
        if (mRingtone != null)
            mRingtone.stop();
    }

    /**
     * Converts Long value to int value.
     *
     * @param l Long value we want to transform.
     * @return Transformed int value.
     */
    private int longToInt(Long l) {
        return (int) (long) l;
    }

    private String buildString(long alarmTimeInMillis) {
        Calendar now = Calendar.getInstance();
        alarmTimeInMillis = alarmTimeInMillis - now.getTimeInMillis();
        int minutes = (int) (alarmTimeInMillis / (1000 * 60)) % 60;
        int hours = (int) ((alarmTimeInMillis / (1000 * 60 * 60)) % 24);
        StringBuilder sb = new StringBuilder();
        int days = hours / 24;
        if (days > 0) {
            sb.append(days);
            sb.append(mContext.getString(R.string.days));
            sb.append(", ");
            hours = hours % 24;
            sb.append(hours);
            sb.append(mContext.getString(R.string.hours));
            sb.append(", ");
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        } else if (hours > 0) {
            sb.append(hours);
            sb.append(mContext.getString(R.string.hours));
            sb.append(", ");
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        } else {
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        }
        return sb.toString();
    }
}
