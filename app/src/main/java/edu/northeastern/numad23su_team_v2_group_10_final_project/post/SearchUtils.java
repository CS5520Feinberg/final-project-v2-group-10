package edu.northeastern.numad23su_team_v2_group_10_final_project.post;

import com.google.firebase.firestore.Filter;

import java.util.HashMap;
import java.util.List;

public class SearchUtils {

    private static int LIMIT = 500;

    // search without dependency ref:
    // https://levelup.gitconnected.com/firestore-full-text-search-at-no-extra-cost-ee148856685
    public static HashMap<String, Object> triGram(String s) {
        HashMap<String, Object> ret = new HashMap<>();
        int cap = Math.min(LIMIT, s.length());
        for (int i = 0; i <= cap - 3; i++) {
            String token = s.substring(i, i + 3);
            ret.put(token, true);
        }
        return ret;
    }

    public static Filter[] generateFilterArr(String s) {
        LIMIT = 30;
        HashMap<String, Object> rec = triGram(s);
        Filter ret[] = new Filter[rec.size()];
        int ptr = 0;
        for (String k : rec.keySet()) {
            ret[ptr++] = Filter.equalTo(k, true);
        }
        LIMIT = 500;
        return ret;
    }
}

