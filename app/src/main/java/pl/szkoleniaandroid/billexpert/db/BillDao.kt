package pl.szkoleniaandroid.billexpert.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(billDto: BillDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(billDtos: List<BillDto>)

    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY date DESC, name ASC")
    fun getBills(userId: String): LiveData<List<BillDto>>

}