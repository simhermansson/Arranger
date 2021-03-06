package com.arrangerapp.arranger.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.arrangerapp.arranger.enums.Repeat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task implements Parcelable {
    private String name;
    private Date date;
    private Repeat repeats;
    private boolean hasDate;
    private int id;

    public Task(String input) {
        hasDate = false;
        parseInput(input);
        // Create unique id
        id = (int) System.currentTimeMillis();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(repeats.ordinal());
        dest.writeByte((byte) (hasDate ? 1 : 0));
        if (hasDate) {
            dest.writeLong(date.getTime());
        }
        dest.writeInt(id);
    }

    public static final Parcelable.Creator<Task> CREATOR
            = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    private Task(Parcel in) {
        name = in.readString();
        repeats = Repeat.values()[in.readInt()];
        hasDate = in.readByte() != 0;
        if (hasDate) {
            date = new Date(in.readLong());
        }
        id = in.readInt();
    }

    /**
     * Sets private fields of task using regex.
     * @param input Input sentence
     */
    private void parseInput(String input) {
        try {
            // Regex for time
            String hours = "(0[0-9]|1[0-9]|2[0-9]|[0-9])";
            String dividers = "[:.,]?";
            String minutes = "([0-5][0-9])?";
            String amPm = "\\s?([AaPp][Mm])?";
            String hoursAndMinutes = "(" + hours + dividers + minutes + amPm + ")?";
            // Regex for taskName
            String taskName = "(.+?(?=\\bat\\b|\\b[0-9]|$))(?:at\\s)?";
            // Regex for taskRepeat
            String taskRepeat = "\\s?(.+)?";
            // Putting the regex together so the variables below can be extracted
            String taskPattern =  taskName + hoursAndMinutes + taskRepeat;

            // Find name, time and repetitions by regex
            String time = null;
            String hour = null;
            String minute = null;
            String twelveHourTime = null;
            String repeat = null;
            Pattern pattern = Pattern.compile(taskPattern);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                // Set taskName
                name = matcher.group(1);

                //Set local variables to time and repeat matches
                time = matcher.group(2);
                hour = matcher.group(3);
                minute = matcher.group(4);
                twelveHourTime = matcher.group(5);
                repeat = matcher.group(6);
            }

            // Parse the time format
            if (time != null) {

                if (hour != null) {
                    if (minute != null) {
                        time = hour + ":" + minute;
                    } else {
                        time = hour + ":00";
                    }

                    if (twelveHourTime != null) {
                        date = new SimpleDateFormat("hh:mm aa").parse(time + " " + twelveHourTime);
                    } else {
                        date = new SimpleDateFormat("HH:mm").parse(time);
                    }
                    hasDate = true;
                }
            }

            // Parse repetitions
            if (repeat != null) {
                switch (repeat) {
                    case "every day":
                        repeats = Repeat.DAILY;
                        break;
                    case "every monday":
                        repeats = Repeat.MONDAY;
                        break;
                    case "every tuesday":
                        repeats = Repeat.TUESDAY;
                        break;
                    case "every wednesday":
                        repeats = Repeat.WEDNESDAY;
                        break;
                    case "every thursday":
                        repeats = Repeat.THURSDAY;
                        break;
                    case "every friday":
                        repeats = Repeat.FRIDAY;
                        break;
                    case "every saturday":
                        repeats = Repeat.SATURDAY;
                        break;
                    case "every sunday":
                        repeats = Repeat.SUNDAY;
                        break;
                    default:
                        repeats = Repeat.TODAY;
                        break;
                }
            } else {
                repeats = Repeat.TODAY;
            }

        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        if (date != null) {
            return SimpleDateFormat.getTimeInstance(SimpleDateFormat.DATE_FIELD).format(date);
        }
        return "Anytime";
    }

    public Date getDate() {
        return date;
    }

    public boolean hasDate() {
        return hasDate;
    }

    public Repeat getRepeats () {
        return repeats;
    }
}
