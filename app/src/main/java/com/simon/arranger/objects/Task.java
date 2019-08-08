package com.simon.arranger.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        //StringBuilders for name, time and when
        StringBuilder name = new StringBuilder();
        StringBuilder when = new StringBuilder();

        for (String word : input.split(" ")) {
            switch (currentInterpretState) {
                case NAME:
                    if (!"at".equals(word)) {
                        name.append(word);
                    } else {
                        this.name = name.toString();
                        currentInterpretState = InterpretState.TIME;
                    }
                    break;
                case TIME:
                    try {
                        //Regex Strings
                        String HHMM = "^(0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]$";
                        String AM = "^(0[0-9]|1[0-9]|[0-9])[Aa][Mm]$";
                        String PM = "^(0[0-9]|1[0-9]|[0-9])[Pp][Mm]$";
                        String HOUR = "^(0[0-9]|1[0-9]|[0-9])$";
                        String SPLITNUMALP = "[AaPp]";

                        //Check if the word matches any time format
                        if (word.matches(HHMM)) {
                            this.time = word;
                            date = new SimpleDateFormat("hh:mm").parse(word);
                        } else if (word.matches(AM)) {
                            this.time = word.split(SPLITNUMALP)[0];
                            date = new SimpleDateFormat("hh").parse(word.split(SPLITNUMALP)[0]);
                        } else if (word.matches(PM)) {
                            this.time = word.split(SPLITNUMALP)[0];
                            date = new SimpleDateFormat("hh").parse(word.split(SPLITNUMALP)[0]);
                        } else if (word.matches(HOUR)) {
                            this.time = word.split(SPLITNUMALP)[0];
                            date = new SimpleDateFormat("hh").parse(word.split(SPLITNUMALP)[0]);
                        } else {
                            this.time = "Woops";
                        }

                    } catch (ParseException e) {
                        System.out.println(e.toString());
                    }
                    currentInterpretState = InterpretState.WHEN;
                    break;
                case WHEN:
                    break;
            }
        }
    }
}
