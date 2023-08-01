package edu.northeastern.numad23su_team_v2_group_10_final_project.post;

import android.util.Log;

import com.google.firebase.firestore.Filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SearchUtils {

    private static int LIMIT = 500;
    private  static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    // search without dependency ref:
    // https://levelup.gitconnected.com/firestore-full-text-search-at-no-extra-cost-ee148856685
    public static HashMap<String, Object> triGram(String s) {
        s = s.toLowerCase(Locale.ROOT);
        HashMap<String, Object> ret = new HashMap<>();
        int cap = Math.min(LIMIT, s.length());
        for (int i = 0; i <= cap - 3; i++) {
            String token = s.substring(i, i + 3);
            ret.put(token, true);
        }
        return ret;
    }

    public static Filter[] generateFilterArr(String s) {
        LIMIT = 24;
        HashMap<String, Object> rec = triGram(s);
        Filter ret[] = new Filter[rec.size() + 1];
        int ptr = 0;
        for (String k : rec.keySet()) {
            ret[ptr++] = Filter.equalTo(k, true);
        }
        ret[ptr] = Filter.equalTo("isActive", true);
        LIMIT = 500;
        return ret;
    }

    // generate key in reverse date order
    public static String generateKey() {
        Random random = new Random();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateString = "01-01-5000 12:00:00";
        long m1 = 0;
        try {
            Date date = sdf.parse(dateString);
            m1 = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date();
        long cur = date.getTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String part = String.format("%016x", m1 - cur);
        return part + sb.toString();
    }
}

