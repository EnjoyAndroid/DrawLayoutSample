package com.lee.drawlayoutsample;

import android.widget.Toast;

import com.lee.drawlayoutsample.utils.BaseUtils;
import com.lee.drawlayoutsample.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 2017/1/18.
 */

public class MementoCreataker {

//    private MementoOriginator mOriginator;
    private List<ElementMemento> mMementos = new ArrayList<>();
    private List<ElementMemento> mBackups = new ArrayList<>();

    public void createMemento(ElementMemento memento) {
//        ElementMemento memento = mOriginator.createMemento();
        mMementos.add(memento);
    }

    public void createMemento(ElementMemento memento, int index) {

        int lastIndex = mMementos.size() - 1;
        LogUtils.i("lastIndex: " + lastIndex);
        LogUtils.d("start createMemento ,mMementos size:" + mMementos.size()+"\n     #####"+mMementos.toString());
        if (index > lastIndex) {//直接加在最后一个节点位置
            LogUtils.d("插入到最后一个位置：" + index);
            createMemento(memento);
        } else if (index >= 0 && index <= lastIndex) {
            LogUtils.d("插入到指定位置：" + index);
            for (int i = mMementos.size() - 1; i >= index; i--) {
                mMementos.remove(i);
            }
            createMemento(memento);
        } else {
            throw new IndexOutOfBoundsException();
        }
        LogUtils.i("end createMemento ,mMementos size:" + mMementos.size()+"\n     #####"+mMementos.toString());
    }

    public ElementMemento restoreMemento(int index) {
        if (index < 0 || index >= mMementos.size()) {
            if (index < 0) {
                Toast.makeText(BaseUtils.getContext(), "没有上一步了", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BaseUtils.getContext(), "没有下一步了", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
//        ElementMemento elementMemento = mMementos.get(index);
//        mOriginator.restoreMemento(elementMemento);
        return mMementos.get(index);
    }

}
