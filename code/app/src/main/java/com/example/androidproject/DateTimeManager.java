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

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Shows date picker dialog
     */
    public void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                new ContextThemeWrapper(context, R.style.CustomDateTimePickerTheme),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDisplay();

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

    /**
     * Shows time picker dialog
     */
    public void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                new ContextThemeWrapper(context, R.style.CustomDateTimePickerTheme),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDisplay();

                    if (listener != null) {
                        listener.onDateTimeChanged(calendar);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    /**
     * Updates the date and time display
     */
    public void updateDisplay() {
        // Update date display
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(calendar.getTime()));

        // Update time display
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeTextView.setText(timeFormat.format(calendar.getTime()));
    }

    /**
     * Sets the calendar to a specific time
     */
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        updateDisplay();
    }

    /**
     * Sets the calendar from a timestamp
     */
    public void setCalendarFromTimestamp(long timestamp) {
        calendar.setTimeInMillis(timestamp);
        updateDisplay();
    }

    /**
     * Returns the current calendar
     */
    public Calendar getCalendar() {
        return calendar;
    }

    // Interface for callbacks
    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(Calendar calendar);
    }
}