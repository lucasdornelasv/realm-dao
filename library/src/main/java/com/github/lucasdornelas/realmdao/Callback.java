package com.github.lucasdornelas.realmdao;

import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by Windows 8 on 15/07/2016.
 */
public class Callback {
    public interface CustomQuery<T extends RealmObject>{
        RealmQuery<T> returnQuery(RealmQuery<T> realmQuery);
    }
    public interface PreSave<T extends RealmObject>{
        T onPreSave();
    }
}
