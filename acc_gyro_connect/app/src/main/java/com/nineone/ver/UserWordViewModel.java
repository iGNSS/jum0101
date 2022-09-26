package com.nineone.ver;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UserWordViewModel extends AndroidViewModel {//ViewModel의 역할은 view에서 필요한 데이터를 model에게 요청하며,
    // 응답받은 데이터를 가공하여 view에 다시 보내주는 역할을 합니다.
    // LiveData를 사용하기위해 필요합니다.
    private UserWordRepository mRepository;
    private UserMyDao myDao;
    private LiveData<List<UserMyDataList>> mAllWords;

    public UserWordViewModel (Application application) {
        super(application);
        mRepository = new UserWordRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    LiveData<List<UserMyDataList>> getAllWords() {
        return mAllWords;
    }

    public void insert(UserMyDataList word) {
        mRepository.insert(word);
    }
    public void update(UserMyDataList word) {
        mRepository.updateItem(word);
    }
    //public void update(UserMyDataList word) {mRepository.updateItem(word); }
    public void delete(UserMyDataList word) {
        mRepository.deleteItem(word);
    }
    public void deleteByItemId(Long idItem) {
        mRepository.deleteItemById(idItem);
    }
 /*   public LiveData<UserMyDataList> getdatelist(String Ids){
        return myDao.updateByItemId(Ids);
    }*/
}
