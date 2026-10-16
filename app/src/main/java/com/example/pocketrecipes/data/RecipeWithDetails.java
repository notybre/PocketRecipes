package com.example.pocketrecipes.data;
import androidx.room.*;
import java.util.List;
public class RecipeWithDetails {
 @Embedded public Recipe recipe;
 @Relation(parentColumn="id",entityColumn="recipeId") public List<Ingredient> ingredients;
 @Relation(parentColumn="id",entityColumn="recipeId") public List<RecipeStep> steps;
}
