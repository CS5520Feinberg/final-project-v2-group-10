package edu.northeastern.numad23su_team_v2_group_10_final_project;
import android.app.Application;


public class FinalProjectApplication extends Application {
    // can add data (e.g., user id to this class)
    String userId;
    String email;
    private static FinalProjectApplication singleton;


    public static FinalProjectApplication getInstance() {
        return singleton;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
