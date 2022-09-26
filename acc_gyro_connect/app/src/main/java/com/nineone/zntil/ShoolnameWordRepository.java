package com.nineone.zntil;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;


import java.util.List;

public class ShoolnameWordRepository {
    private ShoolnameDao myDao;  //Dao의 멤버변수와 word를 넣을 list변수를 만들어준다
    private LiveData<List<ShoolnameList>> mAll_list;
    private ShoolnameDatabase myDatabase;
    ShoolnameWordRepository(Application application) {
        ShoolnameDatabase db = ShoolnameDatabase.getDatabase(application);
        myDao = db.myDao();  //RoomDatabase에 있는 Dao를 가져온다.
        mAll_list = myDao.getMyData(); //Dao의 쿼리를 이용하여 저장되어있는 모든 list를 가져온다.
    }

    LiveData<List<ShoolnameList>> getAllWords() {
        return mAll_list;
    }

    public void insert (ShoolnameList word) {
        new ShoolnameWordRepository.insertAsyncTask(myDao).execute(word);
    }

    private static class insertAsyncTask extends AsyncTask<ShoolnameList, Void, Void> {

        private ShoolnameDao mAsyncTaskDao;

        insertAsyncTask(ShoolnameDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ShoolnameList... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void updateItem(ShoolnameList dataItem) {
        new ShoolnameWordRepository.updateAsyncTask(myDao).execute(dataItem);
    }

    private static class updateAsyncTask extends AsyncTask<ShoolnameList, Void, Void> {
        private ShoolnameDao mAsyncTaskDao;

        updateAsyncTask(ShoolnameDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ShoolnameList... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
    private static class deleteAsyncTask extends AsyncTask<ShoolnameList, Void, Void> {
        private ShoolnameDao mAsyncTaskDao;
        deleteAsyncTask(ShoolnameDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ShoolnameList... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }




}
