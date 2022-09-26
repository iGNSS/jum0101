package com.nineone.zntil;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


import java.util.List;

public class ShoolnameViewModel extends AndroidViewModel {//ViewModel의 역할은 view에서 필요한 데이터를 model에게 요청하며,
    // 응답받은 데이터를 가공하여 view에 다시 보내주는 역할을 합니다.
    // LiveData를 사용하기위해 필요합니다.
    private ShoolnameWordRepository mRepository;
    private ShoolnameDao myDao;
    private LiveData<List<ShoolnameList>> mAllWords;

    public ShoolnameViewModel (Application application) {
        super(application);
        mRepository = new ShoolnameWordRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    LiveData<List<ShoolnameList>> getAllWords() {
        return mAllWords;
    }

    public void insert(ShoolnameList word) {
        mRepository.insert(word);
    }
    public void update(ShoolnameList word) {
        mRepository.updateItem(word);
    }
    //public void update(UserMyDataList word) {mRepository.updateItem(word); }
 /*   public LiveData<UserMyDataList> getdatelist(String Ids){
        return myDao.updateByItemId(Ids);
    }*/
}
