package com.example.pocketrecipes
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pocketrecipes.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class StorageTest {
    @Test fun persistenceAndCascadingDelete() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val name="storage-test-${System.nanoTime()}.db"
        var db=Room.databaseBuilder(context,RecipeDatabase::class.java,name).build()
        try {
            val recipe=Recipe().apply{title="Тестовый рецепт";category="Завтраки";prepTime=25;servings=2;isUserCreated=true}
            var id=0L
            db.runInTransaction {
                id=db.dao().insertRecipe(recipe)
                db.dao().insertIngredients(listOf(Ingredient().apply{recipeId=id;this.name="Творог";amount="400";unit="г"}))
                db.dao().insertSteps(listOf(RecipeStep().apply{recipeId=id;stepNumber=1;description="Смешать ингредиенты"}))
                db.dao().toggleFavorite(id)
                db.dao().insertShopping(listOf(ShoppingItem().apply{this.name="Творог";amount="400";unit="г";isPurchased=true}))
            }
            db.close()
            db=Room.databaseBuilder(context,RecipeDatabase::class.java,name).build()
            val saved=db.dao().getRecipe(id)
            assertEquals("Тестовый рецепт",saved.recipe.title)
            assertTrue(saved.recipe.isFavorite)
            assertEquals("400",saved.ingredients.single().amount)
            assertEquals(1,saved.steps.single().stepNumber)
            db.query("SELECT name,isPurchased FROM shopping",emptyArray()).use{cursor->
                assertTrue(cursor.moveToFirst());assertEquals("Творог",cursor.getString(0));assertEquals(1,cursor.getInt(1))
            }
            db.dao().deleteOwnRecipe(id)
            assertNull(db.dao().getRecipe(id))
            db.query("SELECT COUNT(*) FROM ingredients",emptyArray()).use{it.moveToFirst();assertEquals(0,it.getInt(0))}
            db.query("SELECT COUNT(*) FROM steps",emptyArray()).use{it.moveToFirst();assertEquals(0,it.getInt(0))}
            db.dao().deletePurchased()
            db.query("SELECT COUNT(*) FROM shopping",emptyArray()).use{it.moveToFirst();assertEquals(0,it.getInt(0))}
        } finally {db.close();context.deleteDatabase(name)}
    }
    @Test fun deletingOwnRecipesKeepsStarterCatalog(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        val db=Room.inMemoryDatabaseBuilder(context,RecipeDatabase::class.java).build()
        try{
            db.dao().insertRecipe(Recipe().apply{title="Каталог";isUserCreated=false})
            db.dao().insertRecipe(Recipe().apply{title="Мой";isUserCreated=true})
            db.dao().deleteOwnRecipes()
            assertEquals(1,db.dao().countRecipes())
        }finally{db.close()}
    }
}
