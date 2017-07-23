package com.lee.drawlayoutsample;


import com.lee.drawlayoutsample.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 2017/1/18.
 */

public class MementoOriginator {

    private List<ViewInfo> mInfos;

    public MementoOriginator() {
        mInfos = new ArrayList<>();
    }

    public  List<ViewInfo> restoreMemento(ElementMemento memento) {
        mInfos = memento.getInfos();
        return mInfos;
    }

    public ElementMemento createMemento() {
        return new ElementMemento(mInfos);
    }

    public void setInfos(List<ViewInfo> infos) {
        mInfos = infos;
    }

    public void printMementos() {
        LogUtils.d("mInfos size: " + mInfos.size() + "  $$$　mInfos: " + mInfos.toString());
    }

}
