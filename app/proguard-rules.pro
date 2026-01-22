# Add project specific ProGuard rules here.
-keep class com.dotmatrix.calendar.data.model.** { *; }
-keep class com.dotmatrix.calendar.data.db.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Billing
-keep class com.android.vending.billing.**
