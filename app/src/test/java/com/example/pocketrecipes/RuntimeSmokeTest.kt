package com.example.pocketrecipes

import android.os.Bundle
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.room.Room
import com.example.pocketrecipes.data.*
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class RuntimeSmokeTest {
    private fun nodes(v:android.view.View):Sequence<android.view.View> = sequence {
        yield(v)
        if(v is android.view.ViewGroup)for(i in 0 until v.childCount)yieldAll(nodes(v.getChildAt(i)))
    }
    @Test fun recipeFormValidatesSavesAndEdits(){
        val app=RuntimeEnvironment.getApplication() as PocketApp
        app.preferences.edit().putBoolean("isFirstLaunch",false).commit()
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        fun idle(){shadowOf(Looper.getMainLooper()).idle()}
        fun field(hint:String)=nodes(controller.get().findViewById(R.id.root))
            .filterIsInstance<com.google.android.material.textfield.TextInputLayout>().first{it.hint.toString()==hint}
        fun button(text:String)=nodes(controller.get().findViewById(R.id.root))
            .filterIsInstance<com.google.android.material.button.MaterialButton>().first{it.text.toString()==text}
        fun waitFor(condition:()->Boolean){
            val end=System.currentTimeMillis()+15000
            while(!condition()){check(System.currentTimeMillis()<end){"Timed out"};Thread.sleep(30);idle()}
        }
        try{
            val host=controller.get().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            host.navController.navigate(R.id.add_recipe);idle();host.childFragmentManager.executePendingTransactions();idle()
            button("Сохранить рецепт").performClick()
            assertNotNull(field("Название рецепта *").error)
            field("Название рецепта *").editText!!.setText("Runtime сырники")
            controller.recreate();idle()
            assertEquals("Runtime сырники",field("Название рецепта *").editText!!.text.toString())
            field("Категория *").editText!!.performClick()
            val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog() as androidx.appcompat.app.AlertDialog
            dialog.listView.performItemClick(null,0,0)
            field("Ингредиент 1 *").editText!!.setText("Творог")
            field("Количество *").editText!!.setText("400")
            field("Единица").editText!!.setText("г")
            field("Шаг 1 *").editText!!.setText("Смешать и обжарить")
            button("Сохранить рецепт").performClick()
            var id=0L
            waitFor{CompletableFuture.supplyAsync{
                app.repository.db.query("SELECT id FROM recipes WHERE title='Runtime сырники'",emptyArray()).use{
                    if(it.moveToFirst()){id=it.getLong(0);true}else false
                }
            }.get()}
            idle()
            val saved=CompletableFuture.supplyAsync{app.repository.dao.getRecipe(id)}.get()
            assertEquals("Завтраки",saved.recipe.category)
            assertEquals("400",saved.ingredients.single().amount)
            val nav=(controller.get().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
            nav.navigate(R.id.edit_recipe,Bundle().apply{putLong("recipeId",id)});idle()
            waitFor{nodes(controller.get().findViewById(R.id.root)).filterIsInstance<com.google.android.material.textfield.TextInputLayout>().any{it.hint.toString()=="Название рецепта *"}}
            field("Название рецепта *").editText!!.setText("Runtime сырники обновлены")
            button("Сохранить изменения").performClick()
            waitFor{CompletableFuture.supplyAsync{app.repository.dao.getRecipe(id).recipe.title=="Runtime сырники обновлены"}.get()}
            idle()
            assertEquals(R.id.my_recipes,nav.currentDestination?.id)
        }finally{controller.pause().stop().destroy();app.repository.db.close()}
    }
    @Test fun launchAllScreensAndRestoreActivity() {
        val app=RuntimeEnvironment.getApplication() as PocketApp
        app.preferences.edit().putBoolean("isFirstLaunch",false).commit()
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        try {
            val activity=controller.get()
            val host=activity.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            val nav=host.navController
            val deadline=System.currentTimeMillis()+15000
            while(CompletableFuture.supplyAsync{app.repository.dao.countRecipes()}.get()<8) {
                check(System.currentTimeMillis()<deadline)
                Thread.sleep(30);shadowOf(Looper.getMainLooper()).idle()
            }
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(R.id.home,nav.currentDestination?.id)
            for(id in listOf(R.id.catalog,R.id.categories,R.id.search,R.id.details,R.id.add_recipe,
                R.id.edit_recipe,R.id.my_recipes,R.id.favorites,R.id.shopping,R.id.profile,R.id.settings,R.id.onboarding)) {
                nav.navigate(id,Bundle().apply{putLong("recipeId",1L)})
                shadowOf(Looper.getMainLooper()).idle()
                host.childFragmentManager.executePendingTransactions()
                shadowOf(Looper.getMainLooper()).idle()
                assertEquals(id,nav.currentDestination?.id)
                assertNotNull(activity.findViewById<LinearLayout>(R.id.content))
            }
            controller.recreate()
            shadowOf(Looper.getMainLooper()).idle()
            val restored=controller.get().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            assertEquals(R.id.onboarding,restored.navController.currentDestination?.id)
        } finally { controller.pause().stop().destroy();app.repository.db.close() }
    }
    @Test fun roomRetainsRelationsAndFavoriteAfterReopen(){
        val app=RuntimeEnvironment.getApplication()
        val name="runtime-persistence.db"
        var db=Room.databaseBuilder(app,RecipeDatabase::class.java,name).allowMainThreadQueries().build()
        try{
            val id=db.dao().insertRecipe(Recipe().apply{title="Сырники";isUserCreated=true;category="Завтраки";prepTime=25;servings=3})
            db.dao().insertIngredients(listOf(Ingredient().apply{recipeId=id;this.name="Творог";amount="400";unit="г"}))
            db.dao().insertSteps(listOf(RecipeStep().apply{recipeId=id;stepNumber=1;description="Смешать и обжарить"}))
            db.dao().toggleFavorite(id)
            db.dao().insertShopping(listOf(ShoppingItem().apply{this.name="Творог";amount="400";isPurchased=true}))
            db.close()
            db=Room.databaseBuilder(app,RecipeDatabase::class.java,name).allowMainThreadQueries().build()
            val saved=db.dao().getRecipe(id)
            assertTrue(saved.recipe.isFavorite);assertEquals("Творог",saved.ingredients.single().name)
            assertEquals("Смешать и обжарить",saved.steps.single().description)
            db.query("SELECT COUNT(*) FROM shopping WHERE isPurchased=1",emptyArray()).use{it.moveToFirst();assertEquals(1,it.getInt(0))}
            db.dao().deleteOwnRecipe(id)
            db.query("SELECT COUNT(*) FROM ingredients",emptyArray()).use{it.moveToFirst();assertEquals(0,it.getInt(0))}
        }finally{db.close();app.deleteDatabase(name)}
    }
}
