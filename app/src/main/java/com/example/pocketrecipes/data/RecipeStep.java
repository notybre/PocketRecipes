package com.example.pocketrecipes.data;
import androidx.room.*;
import androidx.annotation.NonNull;
@Entity(tableName="steps",foreignKeys=@ForeignKey(entity=Recipe.class,parentColumns="id",childColumns="recipeId",onDelete=ForeignKey.CASCADE),indices=@Index("recipeId"))
public class RecipeStep {
 @PrimaryKey(autoGenerate=true) public long id;
 public long recipeId;
 public int stepNumber;
 @NonNull public String description="";
}
