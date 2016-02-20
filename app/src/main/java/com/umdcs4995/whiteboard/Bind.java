package com.umdcs4995.whiteboard;

/**
 * Created by LauraKrebs on 2/20/16.
 */



public interface Bind<T> {

        void bind(butterknife.ButterKnife.Finder finder, T target, Object source);
}
