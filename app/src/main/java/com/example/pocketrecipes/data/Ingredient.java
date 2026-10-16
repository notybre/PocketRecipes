package com.example.pocketrecipes.data;
import androidx.room.*;
import androidx.annotation.NonNull;
@Entity(tableName="ingredients", foreignKeys=@ForeignKey(entity=Recipe.class,parentColumns="id",childColumns="recipeId",onDelete=ForeignKey.CASCADE),indices=@Index("recipeId"))
public class Ingredient {
 @PrimaryKey(autoGenerate=true) public long id;
 public long recipeId;
 public int position;
 @NonNull public String name="";
 @NonNull public String amount="";
 @NonNull public String unit="";
}
