package com.example.pocketrecipes.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
@Entity(tableName="recipes")
public class Recipe {
 @PrimaryKey(autoGenerate=true) public long id;
 @NonNull public String title="";
 @NonNull public String description="";
 @NonNull public String category="";
 public int prepTime;
 public int servings;
 @NonNull public String image="pasta";
 public boolean isFavorite;
 public boolean isUserCreated;
 public long createdAt;
}
