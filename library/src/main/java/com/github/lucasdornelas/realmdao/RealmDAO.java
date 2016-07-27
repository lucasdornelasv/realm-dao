package com.github.lucasdornelas.realmdao;

import net.jodah.typetools.TypeResolver;

import java.util.Date;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Windows 8 on 15/07/2016.
 */
@SuppressWarnings("unchecked")
public abstract class RealmDAO<T extends RealmObject> {
    protected Realm realm;
    protected RealmQuery<T> realmQuery;

    public RealmDAO(){
        this.realm = Realm.getDefaultInstance();
        this.realmQuery = realm.where(getTypeClass());
    }
    public RealmDAO(Realm realm){
        this.realm = realm;
        this.realmQuery = realm.where(getTypeClass());
    }
    protected Class<T> getTypeClass(){
        Class<?> klassAux =  TypeResolver.resolveRawArguments(RealmDAO.class, getClass())[0];
        Class<T> klass = null;
        try {
            klass = (Class<T>) Class.forName(klassAux.getName());
        } catch (ClassNotFoundException e) {e.printStackTrace();}
        return klass;
    }

    public RealmObjectSchema getRealmObjectSchema(){
        return realm.getSchema().get(getTypeClass().getSimpleName());
    }
    public String getPrimaryKeyName(){
        return getPrimaryKeyName(getRealmObjectSchema());
    }
    public String getPrimaryKeyName(RealmObjectSchema objectSchema){
        return objectSchema.hasPrimaryKey() ? objectSchema.getPrimaryKey() : "";
    }

    private RealmQuery<T> getCustomQuery(RealmQuery<T> realmQuery, Callback.CustomQuery<T> customQuery){
        RealmQuery<T> realmQueryAux = realmQuery;
        if(customQuery != null) realmQueryAux = customQuery.returnQuery(realmQuery);
        return realmQueryAux;
    }

    //Methods query
    private <E> RealmQuery<T> findByFieldQuery(final boolean not, final String field, E... values){
        RealmQuery<T> realmQuery = findQuery();
        if(field.equals("")) return realmQuery;

        class InstanceResolver{
            public RealmQuery<T> resolver(RealmQuery<T> realmQuery, E value){
                if(value instanceof String){
                    if(!not) realmQuery = realmQuery.equalTo(field, (String)value);
                    else realmQuery = realmQuery.notEqualTo(field, (String)value);
                }else if(value instanceof Long){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Long)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Long)value);
                }else if(value instanceof Integer){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Integer)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Integer)value);
                }else if(value instanceof Float){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Float)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Float)value);
                }else if(value instanceof Double){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Double)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Double)value);
                }else if(value instanceof Date){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Date)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Date)value);
                }else if(value instanceof Boolean){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Boolean)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Boolean)value);
                }else if(value instanceof Short){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Short)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Short)value);
                }else if(value instanceof Byte){
                    if(!not) realmQuery = realmQuery.equalTo(field, (Byte)value);
                    else realmQuery = realmQuery.notEqualTo(field, (Byte)value);
                }
                return realmQuery;
            }
        }
        InstanceResolver instanceResolver = new InstanceResolver();

        for(int i = 0; i < values.length; i++){
            if(i > 1) realmQuery = realmQuery.or();
            realmQuery = instanceResolver.resolver(realmQuery, values[i]);
        }
        return realmQuery;
    }
    public <E> RealmQuery<T> findByFieldQuery(String field, E... values){
        return findByFieldQuery(false, field, values);
    }
    public <E> RealmQuery<T> findNotByFieldQuery(String field, E... values){
        return findByFieldQuery(true, field, values);
    }
    public <E> RealmResults<T> findByField(String field, E... values){
        return findByFieldQuery(field, values).findAll();
    }
    public <E> RealmResults<T> findNotByField(String field, E... values){
        return findNotByFieldQuery(field, values).findAll();
    }

    public <E> T findById(E... values){
        return findByIdQuery(values).findFirst();
    }
    public <E> RealmQuery<T> findByIdQuery(E... values){
        return findByFieldQuery(false, getPrimaryKeyName(), values);
    }
    public <E> T findLessId(Object... values){
        return findLessIdQuery(values).findFirst();
    }
    public <E> RealmQuery<T> findLessIdQuery(E... values){
        return findByFieldQuery(true, getPrimaryKeyName(), values);
    }

    public RealmResults<T> find(){
        return findQuery().findAll();
    }
    public RealmQuery<T> findQuery(){
        return realmQuery;
    }

    public T findOne(){
        return findQuery().findFirst();
    }
    public T findOne(Callback.CustomQuery<T> customQuery){
        return getCustomQuery(findQuery(), customQuery).findFirst();
    }

    //Save methods
    public T save(Callback.PreSave<T> onPreSaveFunc){
        realm.beginTransaction();
        T realmObject = realm.copyToRealmOrUpdate(onPreSaveFunc.onPreSave());
        realm.commitTransaction();
        return realmObject;
    }
    public T save(T realmObject){
        realm.beginTransaction();
        realmObject = realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
        return realmObject;
    }
    public T[] save(T... realmObjects){
        realm.beginTransaction();
        for(int i = 0; i < realmObjects.length; i++){
            realmObjects[i] = realm.copyToRealmOrUpdate(realmObjects[i]);
        }
        realm.commitTransaction();
        return realmObjects;
    }
    public List<T> save(List<T> realmObjects){
        realm.beginTransaction();
        realmObjects = realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
        return realmObjects;
    }
    public RealmAsyncTask saveAsync(final Callback.PreSave<T> onPreSaveFunc,
                                    Realm.Transaction.OnSuccess onSuccess,
                                    Realm.Transaction.OnError onError){
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealmOrUpdate(onPreSaveFunc.onPreSave());
            }
        }, onSuccess, onError);
    }
    public RealmAsyncTask saveAsync(Callback.PreSave<T> onPreSaveFunc) {
        return saveAsync(onPreSaveFunc, null, null);
    }
    public RealmAsyncTask saveAsync(Callback.PreSave<T> onPreSaveFunc, Realm.Transaction.OnSuccess onSuccess) {
        return saveAsync(onPreSaveFunc, onSuccess, null);
    }
    public RealmAsyncTask saveAsync(Callback.PreSave<T> onPreSaveFunc, Realm.Transaction.OnError onError) {
        return saveAsync(onPreSaveFunc, null, onError);
    }
    public RealmAsyncTask saveAsync(final T[] realmObjects, Realm.Transaction.OnSuccess onSuccess,
                                    Realm.Transaction.OnError onError){
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                for(T realmObject : realmObjects) bgRealm.copyToRealmOrUpdate(realmObject);
            }
        }, onSuccess, onError);
    }
    public RealmAsyncTask saveAsync(final T... realmObject) {
        return saveAsync(realmObject, null, null);
    }
    public RealmAsyncTask saveAsync(final T[] realmObject, Realm.Transaction.OnSuccess onSuccess) {
        return saveAsync(realmObject, onSuccess, null);
    }
    public RealmAsyncTask saveAsync(final T[] realmObject, Realm.Transaction.OnError onError) {
        return saveAsync(realmObject, null, onError);
    }
    public RealmAsyncTask saveAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess,
                                    Realm.Transaction.OnError onError) {
        return saveAsync((T[])new Object[]{realmObject}, onSuccess, onError);
    }
    public RealmAsyncTask saveAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess) {
        return saveAsync(realmObject, onSuccess, null);
    }
    public RealmAsyncTask saveAsync(final T realmObject, Realm.Transaction.OnError onError) {
        return saveAsync(realmObject, null, onError);
    }
    public RealmAsyncTask saveAsync(final List<T> realmObjects, Realm.Transaction.OnSuccess onSuccess,
                                    Realm.Transaction.OnError onError){
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealmOrUpdate(realmObjects);
            }
        }, onSuccess, onError);
    }
    public RealmAsyncTask saveAsync(final List<T> realmObject) {
        return saveAsync(realmObject, null, null);
    }
    public RealmAsyncTask saveAsync(final List<T> realmObject, Realm.Transaction.OnSuccess onSuccess) {
        return saveAsync(realmObject, onSuccess, null);
    }
    public RealmAsyncTask saveAsync(final List<T> realmObject, Realm.Transaction.OnError onError) {
        return saveAsync(realmObject, null, onError);
    }


    //Delete methods
    public void delete(T... realmObjects){
        if(realmObjects == null) return;
        realm.beginTransaction();
        for(T realmObject : realmObjects) {
            if(realmObject != null && realmObject.isValid()) realmObject.deleteFromRealm();
        }
        realm.commitTransaction();
    }
    public void delete(T[] realmObjects, int position){
        int length;
        if(realmObjects == null || (length = realmObjects.length) < 1 || position >= length || position < 0){
            return;
        }
        delete(realmObjects[position]);
    }
    public void deleteFirst(T[] realmObjects){
        delete(realmObjects, 0);
    }
    public void deleteLast(T[] realmObjects){
        if(realmObjects != null) delete(realmObjects, realmObjects.length - 1);
    }

    public void deleteFirst(OrderedRealmCollection<T> realmObjects){
        if(realmObjects != null) realmObjects.deleteFirstFromRealm();
    }
    public void deleteLast(OrderedRealmCollection<T> realmObjects){
        if(realmObjects != null) realmObjects.deleteLastFromRealm();
    }
    public void deleteFirst(List<T> realmObjects){
        if(realmObjects != null){
            if(realmObjects instanceof OrderedRealmCollection){
                deleteFirst((OrderedRealmCollection<T>) realmObjects);
            }else{
                deleteFirst((T[]) realmObjects.toArray());
            }
        }
    }
    public void deleteLast(List<T> realmObjects){
        if(realmObjects != null){
            if(realmObjects instanceof OrderedRealmCollection){
                deleteLast((OrderedRealmCollection<T>) realmObjects);
            }else{
                deleteLast((T[]) realmObjects.toArray());
            }
        }
    }
    public void delete(final List<T> realmObjects){
        if(realmObjects != null && !realmObjects.isEmpty()){
            if(realmObjects instanceof OrderedRealmCollection){
                final OrderedRealmCollection<T> auxList = (OrderedRealmCollection<T>) realmObjects;
                if(!auxList.isValid()) return;
                realm.beginTransaction();
                auxList.deleteAllFromRealm();
                realm.commitTransaction();
            }else{
                delete((T[]) realmObjects.toArray());
            }
        }
    }

    public void delete(final OrderedRealmCollection<T> realmObjects, int position){
        if(realmObjects != null && !realmObjects.isEmpty()){
            if(!realmObjects.isValid()) return;
            realm.beginTransaction();
            realmObjects.deleteFromRealm(position);
            realm.commitTransaction();
        }
    }
    public void delete(List<T> realmObjects, int position){
        if(realmObjects != null && !realmObjects.isEmpty()){
            if(realmObjects instanceof OrderedRealmCollection){
                delete((OrderedRealmCollection<T>) realmObjects, position);
            }else{
                delete((T[]) realmObjects.toArray(), position);
            }
        }
    }


    public RealmAsyncTask deleteAsync(final T[] realmObjects, Realm.Transaction.OnSuccess onSuccess,
                            Realm.Transaction.OnError onError){
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                for(T realmObject : realmObjects) realmObject.deleteFromRealm();
            }
        }, onSuccess, onError);
    }
    public RealmAsyncTask deleteAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess,
                            Realm.Transaction.OnError onError){
        return deleteAsync((T[])new Object[]{realmObject}, onSuccess, onError);
    }
    public RealmAsyncTask deleteAsync(final T... realmObject) {
        return deleteAsync(realmObject, null, null);
    }
    public RealmAsyncTask deleteAsync(final T[] realmObject, Realm.Transaction.OnSuccess onSuccess) {
        return deleteAsync(realmObject, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final T[] realmObject, Realm.Transaction.OnError onError) {
        return deleteAsync(realmObject, null, onError);
    }
    public RealmAsyncTask deleteAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess) {
        return deleteAsync(realmObject, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final T realmObject, Realm.Transaction.OnError onError) {
        return deleteAsync(realmObject, null, onError);
    }

    public RealmAsyncTask deleteAsync(final List<T> realmObjects, Realm.Transaction.OnSuccess onSuccess,
                            Realm.Transaction.OnError onError) {
        RealmAsyncTask realmAsyncTask = null;
        if(realmObjects != null && !realmObjects.isEmpty()){
            if(realmObjects instanceof OrderedRealmCollection){
                final OrderedRealmCollection<T> auxList = (OrderedRealmCollection<T>) realmObjects;
                if(!auxList.isValid()) return null;
                realmAsyncTask = realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        auxList.deleteAllFromRealm();
                    }
                }, onSuccess, onError);
            }else{
                realmAsyncTask = deleteAsync((T[])realmObjects.toArray(), onSuccess, onError);
            }
        }
        return realmAsyncTask;
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects) {
        return deleteAsync(realmObjects, null, null);
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects, Realm.Transaction.OnSuccess onSuccess) {
        return deleteAsync(realmObjects, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects, Realm.Transaction.OnError onError) {
        return deleteAsync(realmObjects, null, onError);
    }

    public RealmAsyncTask deleteAsync(final T[] realmObjects, int position,
                                      Realm.Transaction.OnSuccess onSuccess,
                                      Realm.Transaction.OnError onError){
        int length;
        if(realmObjects == null || (length = realmObjects.length) < 1 || position >= length || position < 0){
            return null;
        }
        return deleteAsync(realmObjects[position], onSuccess, onError);
    }
    public RealmAsyncTask deleteAsync(final T[] realmObjects, int position){
        return deleteAsync(realmObjects, position, null, null);
    }
    public RealmAsyncTask deleteAsync(final T[] realmObjects, int position,
                                      Realm.Transaction.OnSuccess onSuccess){
        return deleteAsync(realmObjects, position, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final T[] realmObjects, int position,
                                      Realm.Transaction.OnError onError){
        return deleteAsync(realmObjects, position, null, onError);
    }
    public RealmAsyncTask deleteAsync(final OrderedRealmCollection<T> realmObjects, final int position,
                                      Realm.Transaction.OnSuccess onSuccess,
                                      Realm.Transaction.OnError onError) {
        if(realmObjects == null || !realmObjects.isValid() || !realmObjects.isEmpty()) return null;
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                realmObjects.deleteFromRealm(position);
            }
        }, onSuccess, onError);
    }
    public RealmAsyncTask deleteAsync(final OrderedRealmCollection<T> realmObjects, final int position) {
        return deleteAsync(realmObjects, position, null, null);
    }
    public RealmAsyncTask deleteAsync(final OrderedRealmCollection<T> realmObjects, final int position,
                                      Realm.Transaction.OnSuccess onSuccess) {
        return deleteAsync(realmObjects, position, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final OrderedRealmCollection<T> realmObjects, final int position,
                                      Realm.Transaction.OnError onError) {
        return deleteAsync(realmObjects, position, null, onError);
    }

    public RealmAsyncTask deleteAsync(final List<T> realmObjects, int position,
                                      Realm.Transaction.OnSuccess onSuccess,
                                      Realm.Transaction.OnError onError) {
        RealmAsyncTask realmAsyncTask = null;
        if(realmObjects != null && !realmObjects.isEmpty()){
            if(realmObjects instanceof OrderedRealmCollection){
                realmAsyncTask = deleteAsync((OrderedRealmCollection<T>) realmObjects, position, onSuccess, onError);
            }else{
                realmAsyncTask = deleteAsync((T[])realmObjects.toArray(), position, onSuccess, onError);
            }
        }
        return realmAsyncTask;
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects, int position) {
        return deleteAsync(realmObjects, position, null, null);
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects, int position, Realm.Transaction.OnSuccess onSuccess) {
        return deleteAsync(realmObjects, position, onSuccess, null);
    }
    public RealmAsyncTask deleteAsync(final List<T> realmObjects, int position, Realm.Transaction.OnError onError) {
        return deleteAsync(realmObjects, position, null, onError);
    }
}
