package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 5/15/2018.
 */

public class GroupMeetingMembers {

    private Recipient[] others;
    private Recipient creator1;
    private Recipient creator2;

    public Recipient[] getOthers() {
        return others;
    }

    public void setOthers(Recipient[] others) {
        this.others = others;
    }

    public Recipient getCreator1() {
        return creator1;
    }

    public void setCreator1(Recipient creator1) {
        this.creator1 = creator1;
    }

    public Recipient getCreator2() {
        return creator2;
    }

    public void setCreator2(Recipient creator2) {
        this.creator2 = creator2;
    }
}
