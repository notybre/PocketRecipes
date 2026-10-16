package com.example.pocketrecipes.data;
import androidx.room.*;
import androidx.annotation.NonNull;
@Entity(tableName="shopping")
public class ShoppingItem {
 @PrimaryKey(autoGenerate=true) public long id;
 @NonNull public String name="";
 @NonNull public String amount="";
 @NonNull public String unit="";
 public boolean isPurchased;
}
