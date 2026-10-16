package com.example.pocketrecipes.data;
import androidx.room.*;
import androidx.lifecycle.LiveData;
import java.util.List;
@Dao
public interface RecipeDao {
 @Query("SELECT * FROM recipes ORDER BY createdAt DESC, id DESC") LiveData<List<Recipe>> observeRecipes();
 @Query("SELECT * FROM shopping ORDER BY isPurchased ASC, id ASC") LiveData<List<ShoppingItem>> observeShopping();
 @Transaction @Query("SELECT * FROM recipes WHERE id=:id") LiveData<RecipeWithDetails> observeRecipe(long id);
 @Transaction @Query("SELECT * FROM recipes WHERE id=:id") RecipeWithDetails getRecipe(long id);
 @Query("SELECT COUNT(*) FROM recipes") int countRecipes();
 @Insert long insertRecipe(Recipe recipe);
 @Update void updateRecipe(Recipe recipe);
 @Insert void insertIngredients(List<Ingredient> ingredients);
 @Insert void insertSteps(List<RecipeStep> steps);
 @Insert void insertShopping(List<ShoppingItem> items);
 @Query("DELETE FROM ingredients WHERE recipeId=:id") void deleteIngredients(long id);
 @Query("DELETE FROM steps WHERE recipeId=:id") void deleteSteps(long id);
 @Query("UPDATE recipes SET isFavorite=NOT isFavorite WHERE id=:id") void toggleFavorite(long id);
 @Query("DELETE FROM recipes WHERE id=:id AND isUserCreated=1") void deleteOwnRecipe(long id);
 @Query("DELETE FROM recipes WHERE isUserCreated=1") void deleteOwnRecipes();
 @Query("UPDATE shopping SET isPurchased=:checked WHERE id=:id") void setPurchased(long id, boolean checked);
 @Query("DELETE FROM shopping WHERE id=:id") void deleteShopping(long id);
 @Query("DELETE FROM shopping WHERE isPurchased=1") void deletePurchased();
 @Query("DELETE FROM shopping") void clearShopping();
}
