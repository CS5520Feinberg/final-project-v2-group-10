package edu.northeastern.numad23su_team_v2_group_10_final_project.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import edu.northeastern.numad23su_team_v2_group_10_final_project.model.User;

public class AndroidUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(Intent intent, User model) {
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("name", model.getName());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("campus", model.getCampus());

    }

    public static User getUserModelFromIntent(Intent intent) {
        User userModel = new User();
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setName(intent.getStringExtra("name"));
        userModel.setEmail(intent.getStringExtra("email"));
        userModel.setCampus(intent.getStringExtra("campus"));
        return userModel;
    }
}