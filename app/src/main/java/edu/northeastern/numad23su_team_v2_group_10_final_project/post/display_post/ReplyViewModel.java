package edu.northeastern.numad23su_team_v2_group_10_final_project.post.display_post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReplyViewModel extends ViewModel {
    private final MutableLiveData<String> replyRootId = new MutableLiveData<String>();
    private final  MutableLiveData<String> replyToUserId = new MutableLiveData<String>();
    private final MutableLiveData<Boolean> trigger = new MutableLiveData<>();
    private final MutableLiveData<String> replyToName = new MutableLiveData<>();
    private final MutableLiveData<Integer> index = new MutableLiveData<>();

    public void setReplyRootId(String item) {
        replyRootId.setValue(item);
    }

    public LiveData<String> getReplyRootId() {
        return replyRootId;
    }

    public  LiveData<String> getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(String item) {
        replyToUserId.setValue(item);
    }

    public LiveData<Boolean> getTrigger() {
        return trigger;
    }

    public void setTrigger(Boolean bool) {
        trigger.setValue(bool);
    }

    public void setReplyToName(String name) {
        replyToName.setValue(name);
    }

    public LiveData<String> getReplyToName() {
        return replyToName;
    }

    public void setIndex(int val) {
        index.setValue(val);
    }

    public LiveData<Integer> getIndex() {
        return index;
    }
}
