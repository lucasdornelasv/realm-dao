package com.github.lucasdornelas.realmdao;

import net.jodah.typetools.TypeResolver;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Windows 8 on 15/07/2016.
 */
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
    private Class<T> getTypeClass(){
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

    private RealmQuery<T> getCustomQuery(RealmQuery<T> realmQuery, CallbackDAO.CustomQuery<T> customQuery){
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
    public T findOne(CallbackDAO.CustomQuery<T> customQuery){
        return getCustomQuery(findQuery(), customQuery).findFirst();
    }

    public T save(T realmObject){
        realm.beginTransaction();
        realmObject = realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
        return realmObject;
    }
    public void saveAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess, Realm.Transaction.OnError onError){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.beginTransaction();
                bgRealm.copyToRealmOrUpdate(realmObject);
                bgRealm.commitTransaction();
            }
        }, onSuccess, onError);
    }
    public void saveAsync(final T realmObject, Realm.Transaction.OnSuccess onSuccess) {
        saveAsync(realmObject, onSuccess, null);
    }
    public void saveAsync(final T realmObject, Realm.Transaction.OnError onError) {
        saveAsync(realmObject, null, onError);
    }


}
