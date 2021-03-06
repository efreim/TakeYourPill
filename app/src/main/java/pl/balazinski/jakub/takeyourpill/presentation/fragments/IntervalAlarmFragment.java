package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.Constants;
import pl.balazinski.jakub.takeyourpill.data.model.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.model.Pill;
import pl.balazinski.jakub.takeyourpill.data.model.PillToAlarm;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;

public class IntervalAlarmFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    @Bind(R.id.inside_horizontal)
    GridLayout linearInsideHorizontal;
    @Bind(R.id.change_time_button)
    EditText changeTimeButton;
    @Bind(R.id.change_day_button)
    EditText changeDayButton;
    @Bind(R.id.interval_time)
    EditText intervalTimeEditText;
    @Bind(R.id.number_of_usage)
    EditText numberOfUsageEditText;
    @Bind(R.id.interval_dummy)
    LinearLayout linearLayoutDummy;
    @Bind(R.id.number_of_usage_checkbox)
    CheckBox numberOfUsageCheckbox;

    private List<HorizontalScrollViewItem> mPillViewList;
    private Alarm mAlarm;
    private OutputProvider mOutputProvider;
    private Context mContext;
    private int mMinute = 0;
    private int mHour = 0;
    private int mDay = 0;
    private int mMonth = 0;
    private int mYear = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_interval_alarm, container, false);
        mContext = getContext();
        mOutputProvider = new OutputProvider(mContext);
        ButterKnife.bind(this, scrollView);

        Bundle bundle = getArguments();
        setupContent(bundle);

        return scrollView;
    }

    private void setupContent(Bundle bundle) {
        AlarmActivity.State state;
        if (bundle == null) {
            state = AlarmActivity.State.NEW;
            setupView(state);
        } else {
            state = AlarmActivity.State.EDIT;
            Long alarmId = bundle.getLong(Constants.EXTRA_LONG_ID);

            mAlarm = DatabaseRepository.getAlarmById(mContext, alarmId);
            if (mAlarm == null)
                mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));
            else {
                setupView(state);
            }
        }
    }

    private void setupView(AlarmActivity.State state) {
        linearLayoutDummy.requestFocus();
        mPillViewList = new ArrayList<>();
        List<Pill> pills = DatabaseRepository.getAllPills(mContext);

        if (pills != null) {
            for (Pill p : pills) {
                HorizontalScrollViewItem item = new HorizontalScrollViewItem(mContext, p.getPhoto(), p.getName(), p.getId());
                linearInsideHorizontal.addView(item);
                mPillViewList.add(item);
            }
        } else
            mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));


        if (state == AlarmActivity.State.NEW) {
            Calendar calendar = Calendar.getInstance();
            mMinute = calendar.get(Calendar.MINUTE);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mMonth = calendar.get(Calendar.MONTH);
            mYear = calendar.get(Calendar.YEAR);
            numberOfUsageEditText.setVisibility(View.GONE);
            changeTimeButton.setText(buildString(mMinute, mHour));
            changeTimeButton.setText(buildString(mMinute, mHour));
            changeDayButton.setText(buildString(mDay, mMonth, mYear));
        } else {
            mMinute = mAlarm.getMinute();
            mHour = mAlarm.getHour();
            mDay = mAlarm.getDay();
            mMonth = mAlarm.getMonth();
            mYear = mAlarm.getYear();
            int interval = mAlarm.getIntervalTime();
            int numberOfAlarms = mAlarm.getUsageNumber();
            changeTimeButton.setText(buildString(mMinute, mHour));
            changeDayButton.setText(buildString(mDay, mMonth, mYear));


            if (numberOfAlarms != -1) {
                numberOfUsageCheckbox.setChecked(true);
                numberOfUsageEditText.setText(String.valueOf(numberOfAlarms));
            } else {
                numberOfUsageCheckbox.setChecked(false);
                numberOfUsageEditText.setVisibility(View.GONE);
            }

            if (interval != -1) {
                intervalTimeEditText.setText(String.valueOf(interval));
            }

            List<Long> pillIds = DatabaseRepository.getPillsByAlarm(mContext, mAlarm.getId());
            for (Long id : pillIds) {
                getViewItem(id);
            }
        }
    }

    @OnCheckedChanged(R.id.number_of_usage_checkbox)
    public void onCheckedChanged(boolean isChecked) {
        if (isChecked) {
            numberOfUsageEditText.setVisibility(View.VISIBLE);
        } else
            numberOfUsageEditText.setVisibility(View.GONE);
    }

    @OnClick(R.id.change_time_button)
    public void onChangeTimeClick(View v) {
        Calendar mCurrentTime = Calendar.getInstance();
        //int hour, minute;
        if (mAlarm != null) {
            mHour = mAlarm.getHour();
            mMinute = mAlarm.getMinute();
        } else {
            mHour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
            mMinute = mCurrentTime.get(Calendar.MINUTE);
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                changeTimeButton.setText(buildString(selectedMinute, selectedHour));
                mMinute = selectedMinute;
                mHour = selectedHour;
            }
        }, mHour, mMinute, true);
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();

    }

    @OnClick(R.id.change_day_button)
    public void onDayTimeClick(View v) {
        Calendar mCurrentTime = Calendar.getInstance();

        if (mAlarm != null) {
            mDay = mAlarm.getDay();
            mMonth = mAlarm.getMonth();
            mYear = mAlarm.getYear();
        } else {
            mDay = mCurrentTime.get(Calendar.DAY_OF_MONTH);
            mMonth = mCurrentTime.get(Calendar.MONTH);
            mYear = mCurrentTime.get(Calendar.YEAR);
        }
        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(mContext,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                changeDayButton.setText(buildString(dayOfMonth, monthOfYear, year));
                mDay = dayOfMonth;
                mMonth = monthOfYear;
                mYear = year;
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle(getString(R.string.select_starting_date));
        mDatePicker.show();
    }


    public boolean addAlarm(AlarmActivity.State state) {
        AlarmReceiver alarmReceiver = new AlarmReceiver(mContext);
        String numberOfUsageString = numberOfUsageEditText.getText().toString();
        String intervalString = intervalTimeEditText.getText().toString();
        int numberOfUsage, interval;

        if (numberOfUsageString.equals("") && !intervalString.equals("") && !intervalString.startsWith(".")) {
            numberOfUsage = -1;
            interval = Integer.parseInt(intervalString);
        } else if (!numberOfUsageString.equals("") && !numberOfUsageString.startsWith(".") && !intervalString.equals("") && !intervalString.startsWith(".")) {
            numberOfUsage = Integer.parseInt(numberOfUsageString);
            interval = Integer.parseInt(intervalString);
        } else {
            intervalTimeEditText.setError(getString(R.string.error_set_interval));
            return false;
        }

        if (state == AlarmActivity.State.NEW) {

            if (changeTimeButton.getText().toString().equals("")) {
                changeTimeButton.setError(getString(R.string.error_choose_alarm_time));
                return false;
            } else
                changeTimeButton.setError(null);

            if (changeDayButton.getText().toString().equals("")) {
                changeDayButton.setError(getString(R.string.error_choose_alarm_date));
                return false;
            } else
                changeTimeButton.setError(null);


            mAlarm = new Alarm(mHour, mMinute, interval, numberOfUsage, mDay, mMonth, mYear, true, false, true, false, "");
            DatabaseRepository.addAlarm(mContext, mAlarm);
            alarmReceiver.setIntervalAlarm(mContext, mAlarm.getId());

        } else {
            mAlarm.setIntervalTime(interval);
            mAlarm.setUsageNumber(numberOfUsage);
            mAlarm.setDay(mDay);
            mAlarm.setMonth(mMonth);
            mAlarm.setYear(mYear);
            mAlarm.setMinute(mMinute);
            mAlarm.setHour(mHour);
            mAlarm.setActive(true);
            DatabaseHelper.getInstance(mContext).getAlarmDao().update(mAlarm);
            DatabaseRepository.deleteAlarmToPill(mContext, mAlarm.getId());
            alarmReceiver.setIntervalAlarm(mContext, mAlarm.getId());
        }

        for (HorizontalScrollViewItem item : mPillViewList) {
            if (item.isChecked()) {
                DatabaseRepository.addPillToAlarm(getContext(), new PillToAlarm(mAlarm.getId(), item.getPillId()));
            }
        }
        return true;
    }

    private void getViewItem(Long id) {
        for (HorizontalScrollViewItem item : mPillViewList) {
            if (item.getPillId().equals(id))
                item.setClick();
        }
    }


    /**
     * Builds string
     *
     * @param minute alarm minute
     * @param hour   alarm hour
     * @return returns built string
     */
    private String buildString(int minute, int hour) {
        StringBuilder stringBuilder = new StringBuilder();
        if (hour < 10)
            stringBuilder.append("0");
        stringBuilder.append(String.valueOf(hour));
        String s = " : ";
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));
        return stringBuilder.toString();
    }

    /**
     * Builds string
     * @param day   alarm day
     * @param month alarm month
     * @param year  alarm year
     * @return returns built string
     */
    private String buildString(int day, int month, int year) {
        month++;
        StringBuilder stringBuilder = new StringBuilder();
        String s = "/";
        if (day < 10)
            stringBuilder.append("0");
        stringBuilder.append(day);
        stringBuilder.append(s);
        if (month < 10)
            stringBuilder.append("0");
        stringBuilder.append(month);
        stringBuilder.append(s);
        stringBuilder.append(year);
        return stringBuilder.toString();
    }

}
