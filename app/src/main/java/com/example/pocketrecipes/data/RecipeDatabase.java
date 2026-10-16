package com.example.pocketrecipes.data;
import androidx.room.*;
@Database(entities={Recipe.class,Ingredient.class,RecipeStep.class,ShoppingItem.class},version=1,exportSchema=false)
public abstract class RecipeDatabase extends RoomDatabase { public abstract RecipeDao dao(); }
