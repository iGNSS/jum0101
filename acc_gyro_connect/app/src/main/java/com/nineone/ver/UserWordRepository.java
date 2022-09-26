package com.nineone.ver;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class UserWordRepository {//  여러 데이터에 대한 접근을 할 수 있는 클래스
    //Repository는 데이터(Dao, Firestore, Retrofit등)를 가져오는 클래스들과 1:n 매칭이 가능한 유일한 클래스입니다.

    private UserMyDao myDao;  //Dao의 멤버변수와 word를 넣을 list변수를 만들어준다
    private LiveData<List<UserMyDataList>> mAll_list;
    private UserMyDatabase myDatabase;
    UserWordRepository(Application application) {
        UserMyDatabase db = UserMyDatabase.getDatabase(application);
        myDao = db.myDao();  //RoomDatabase에 있는 Dao를 가져온다.
        mAll_list = myDao.getMyData(); //Dao의 쿼리를 이용하여 저장되어있는 모든 list를 가져온다.
    }

    LiveData<List<UserMyDataList>> getAllWords() {
        return mAll_list;
    }

    public void insert (UserMyDataList word) {
        new insertAsyncTask(myDao).execute(word);
    }

    private static class insertAsyncTask extends AsyncTask<UserMyDataList, Void, Void> {

        private UserMyDao mAsyncTaskDao;

        insertAsyncTask(UserMyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserMyDataList... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void updateItem(UserMyDataList dataItem) {
        new updateAsyncTask(myDao).execute(dataItem);
    }

    private static class updateAsyncTask extends AsyncTask<UserMyDataList, Void, Void> {
        private UserMyDao mAsyncTaskDao;

        updateAsyncTask(UserMyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserMyDataList... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    public void deleteItem(UserMyDataList dataItem) {
        new deleteAsyncTask(myDao).execute(dataItem);
    }
    private static class deleteAsyncTask extends AsyncTask<UserMyDataList, Void, Void> {
        private UserMyDao mAsyncTaskDao;
        deleteAsyncTask(UserMyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserMyDataList... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    public void deleteItemById(Long idItem) {
        new deleteByIdAsyncTask(myDao).execute(idItem);
    }

    private static class deleteByIdAsyncTask extends AsyncTask<Long, Void, Void> {
        private UserMyDao mAsyncTaskDao;
        deleteByIdAsyncTask(UserMyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... params) {
            mAsyncTaskDao.deleteByItemId(params[0]);
            return null;
        }
    }
}