package com.nineone.ver;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface UserMyDao {
    @Query("select * from mydatalist")
    LiveData<List<UserMyDataList>> getMyData();//데이터베이스에서 읽기/쓰기 작업을 실행할 수(데이터 저장을 위해)
    //LiveData는 ViewModel에서 이용되며, Repository가 Room Database에서 가져온 데이터를 객체형식으로 보유하고 있습니다.
    @Insert(onConflict = OnConflictStrategy.IGNORE)//중복시 항복 덮어쓰기
    Long insert(UserMyDataList mydatalist);//파라미터로 넘겨받은 데이터를 테이블에 저장

    @Delete
    void delete(UserMyDataList mydatalist);//파라미터로 넘겨 받은 데이터를 테이블에서 삭제

    @Query("DELETE FROM mydatalist WHERE id = :itemId")
    void deleteByItemId(long itemId);//데이터베이스에서 읽기/쓰기 작업을 실행할 수(데이터 삭제를 위해)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UserMyDataList mydatalist);


    @Query("select * from mydatalist WHERE id=:ids")
    LiveData<UserMyDataList> updateByItemId(String ids);//

    @Query("DELETE FROM mydatalist")
    void deleteAll();
    //Delete one item by id

}