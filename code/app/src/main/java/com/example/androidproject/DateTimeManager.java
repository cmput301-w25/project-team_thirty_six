package com.example.androidproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Manages date and time operations for mood editing
 */
public class DateTimeManager {
    private Context context;
    private TextView dateTextView;
    private TextView timeTextView;
    private Calendar calendar;
    private OnDateTimeChangedListener listener;

    public DateTimeManager(Context context, TextView dateTextView, TextView timeTextView) {
        this.context = context;
        this.dateTextView = dateTextView;
        this.timeTextView = timeTextView;
        this.calendar = Calendar.getInstance();
        updateDisplay();
    }

    public void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                new ContextThemeWrapper(context, R.style.CustomDateTimePickerTheme),
                (view, year, month, dayOfMonth) -> {
                    // Update calendar with selected date
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Update display
                    updateDisplay();

                    // Notify listener if set
                    if (listener != null) {
                        listener.onDateTimeChanged(calendar);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    public void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                new ContextThemeWrapper(context, R.style.CustomDateTimePickerTheme),
                (view, hourOfDay, minute) -> {
                    // Update calendar with selected time
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    // Update display
                    updateDisplay();

                    // Notify listener if set
                    if (listener != null) {
                        listener.onDateTimeChanged(calendar);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false  // 12-hour format
        );
        timePickerDialog.show();
    }

    public void updateDisplay() {
        // Date formatting
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(calendar.getTime()));

        // Time formatting
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeTextView.setText(timeFormat.format(calendar.getTime()));
    }

    public void setCalendarFromTimestamp(long timestamp) {
        calendar.setTimeInMillis(timestamp);
        updateDisplay();
    }

    public void setCalendar(Calendar newCalendar) {
        calendar = newCalendar;
        updateDisplay();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    // Optional listener for date/time changes
    public void setOnDateTimeChangedListener(OnDateTimeChangedListener listener) {
        this.listener = listener;
    }

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(Calendar calendar);
    }
}