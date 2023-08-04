package edu.northeastern.numad23su_team_v2_group_10_final_project;

import android.content.ClipData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<String> selectedItem = new MutableLiveData<String>();
    private final  MutableLiveData<Boolean> trigger = new MutableLiveData<Boolean>();

    public void setUser(String item) {
        selectedItem.setValue(item);
    }

    public LiveData<String> getUser() {
        return selectedItem;
    }
}

