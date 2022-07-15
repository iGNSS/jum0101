package com.example.picturemap;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class WordViewModel extends AndroidViewModel {//ViewModel의 역할은 view에서 필요한 데이터를 model에게 요청하며,
                                                     // 응답받은 데이터를 가공하여 view에 다시 보내주는 역할을 합니다.
                                                    // LiveData를 사용하기위해 필요합니다.
    private WordRepository mRepository;

    private LiveData<List<MyDataList>> mAllWords;

    public WordViewModel (Application application) {
        super(application);
        mRepository = new WordRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    LiveData<List<MyDataList>> getAllWords() {
        return mAllWords;
    }

    public void insert(MyDataList word) {
        mRepository.insert(word);
    }
    public void delete(MyDataList word) {
        mRepository.deleteItem(word);
    }
    public void deleteByItemId(Long idItem) {
        mRepository.deleteItemById(idItem);
    }
}