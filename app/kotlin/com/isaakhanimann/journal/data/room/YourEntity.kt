@androidx.room.Entity(tableName = "your_table_name")
data class YourEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Add the defaultValue here
    @androidx.room.ColumnInfo(name = "isHiddenFromCalendar", defaultValue = "0")
    val isHiddenFromCalendar: Boolean = false
)