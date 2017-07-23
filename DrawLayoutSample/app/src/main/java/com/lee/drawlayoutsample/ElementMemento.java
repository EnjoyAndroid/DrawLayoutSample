package com.lee.drawlayoutsample;

import java.util.List;

/**
 * Created by Lee on 2017/1/18.
 */

public class ElementMemento implements IElementMemento {

    private List<ViewInfo> mInfos;

    public ElementMemento(List<ViewInfo> mementos) {
        mInfos = mementos;
    }

    public List<ViewInfo> getInfos() {
        return mInfos;
    }

    @Override
    public String toString() {
        return "ElementMemento{" +
                "mInfos=" + mInfos +
                '}';
    }
}
