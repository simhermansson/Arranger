package com.simon.arranger.objects;

import com.simon.arranger.enums.Repeat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task {
    private String name;
    private Date date;
    private Repeat repeats;

    public Task(String input) {
        parseInput(input);
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

    private void parseInput(String input) {
        try {
            //Regex for time
            String hours = "(0[0-9]|1[0-9]|2[0-9]|[0-9])";
            String dividers = "[:.,]?";
            String minutes = "([0-5][0-9])?";
            String amPm = "\\s?([AaPp][Mm])?";
            String hoursAndMinutes = "(" + hours + dividers + minutes + amPm + ")?";
            //Regex for taskName
            String taskName = "(.+?(?=\\bat\\b|\\b[0-9]|$))(?:at\\s)?";
            //Regex for taskRepeat
            String taskRepeat = "\\s?(.+)?";
            //Putting the regex together so the variables below can be extracted
            String taskPattern =  taskName + hoursAndMinutes + taskRepeat;

            //Find name, time and repetitions by regex
            String time = null;
            String hour = null;
            String minute = null;
            String twelveHourTime = null;
            String repeat = null;
            Pattern pattern = Pattern.compile(taskPattern);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                //Set taskName
                name = matcher.group(1);

                //Set local variables to time and repeat matches
                time = matcher.group(2);
                hour = matcher.group(3);
                minute = matcher.group(4);
                twelveHourTime = matcher.group(5);
                repeat = matcher.group(6);
            }

            //Parse the time format
            if (time != null) {

                if (hour != null) {
                    if (minute != null) {
                        time = hour + ":" + minute;
                    } else {
                        time = hour + ":00";
                    }
                } else {
                    date = null;
                }

                if (twelveHourTime != null) {
                    date = new SimpleDateFormat("hh:mm aa").parse(time + " " + twelveHourTime);
                } else {
                    date = new SimpleDateFormat("hh:mm").parse(time);
                }

            }

            //Parse repetitions
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

    public Repeat getRepeats () {
        return repeats;
    }
}
