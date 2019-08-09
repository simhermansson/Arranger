package com.simon.arranger.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task {
    private String name;
    private String time;
    private Date date;

    private enum InterpretState {
        NAME, TIME, WHEN
    }
    private InterpretState currentInterpretState;

    public Task(String input) {
        currentInterpretState = InterpretState.NAME;
        parseInput(input);
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public Date getDate() {
        return date;
    }

    private void parseInput(String input) {
        try {
            //Regex Strings
            String HH = "(0[0-9]|1[0-9]|2[0-9]|[0-9])";
            String MM = "([:.,][0-5][0-9])";
            String HHMM = "(" + HH + MM + ")";
            String AM = "(" + HH + MM + "?\\s?[Aa][Mm])";
            String PM = "(" + HH + MM + "?\\s?[Pp][Mm])";
            String TIMES = "(" + HHMM + "|" + AM + "|" + PM + "|" + HH + ")";
            String SPLITNUMALP = "[AaPp]";
            String SENTENCE = "^(.+)(at\\s+)" + TIMES + "(.*)";

            Pattern pattern = Pattern.compile(SENTENCE);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                name = matcher.group(1);
                time = matcher.group(3);
            }

            //Parse the time format
            if (time.matches(HHMM)) {
                this.time = time;
                date = new SimpleDateFormat("hh:mm").parse(time);
            } else if (time.matches(AM)) {
                this.time = time.split(SPLITNUMALP)[0];
                date = new SimpleDateFormat("hh").parse(time.split(SPLITNUMALP)[0]);
            } else if (time.matches(PM)) {
                this.time = time.split(SPLITNUMALP)[0];
                date = new SimpleDateFormat("hh").parse(time.split(SPLITNUMALP)[0]);
            } else if (time.matches(HH)) {
                this.time = time.split(SPLITNUMALP)[0];
                date = new SimpleDateFormat("hh").parse(time.split(SPLITNUMALP)[0]);
            } else {
                this.time = "Woops";
            }

        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }
}
