package pl.szkoleniaandroid.billexpert.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import pl.szkoleniaandroid.billexpert.api.Category
import java.util.*

@Entity(tableName = "bills")
@TypeConverters(Converters::class)
class BillDto {
    @PrimaryKey
    var objectId: String = ""
    var userId: String = ""
    var date: Date = Date()
    var name: String = ""
    var amount: Double = 0.0
    var category: Category = Category.OTHER
    var comment: String = ""
}

class Converters {

    @TypeConverter
    fun convert(category: Category) : Int = category.ordinal

    @TypeConverter
    fun convert(categoryIndex: Int) : Category = Category.values()[categoryIndex]

    @TypeConverter
    fun convert(date: Date) : Long = date.time

    @TypeConverter
    fun convert(timestamp: Long): Date = Date(timestamp)
}