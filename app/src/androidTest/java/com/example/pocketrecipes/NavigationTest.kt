package com.example.pocketrecipes

import android.os.Bundle
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.navigation.fragment.NavHostFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.pocketrecipes.data.RecipeWithDetails
import org.hamcrest.Matchers.allOf
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    private val app get()=ApplicationProvider.getApplicationContext<PocketApp>()
    private fun awaitCondition(condition:()->Boolean) {
        val deadline=SystemClock.elapsedRealtime()+15000
        while(!condition()) {
            if(SystemClock.elapsedRealtime()>deadline)fail("Condition timed out")
            SystemClock.sleep(100)
        }
    }
    @Test fun allScreensAndActivityRecreation(){
        val first=app.preferences.getBoolean("isFirstLaunch",true)
        val onboarding=app.preferences.getBoolean("showOnboarding",false)
        app.preferences.edit().putBoolean("isFirstLaunch",false).putBoolean("showOnboarding",false).commit()
        try {
            ActivityScenario.launch(MainActivity::class.java).use{scenario->
                awaitCondition{app.repository.dao.countRecipes()>=8}
                val ids=listOf(R.id.home,R.id.catalog,R.id.categories,R.id.search,R.id.details,R.id.add_recipe,
                    R.id.edit_recipe,R.id.my_recipes,R.id.favorites,R.id.shopping,R.id.profile,R.id.settings,R.id.onboarding)
                ids.forEach{id->
                    scenario.onActivity{a->
                        val nav=(a.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
                        nav.navigate(id,Bundle().apply{putLong("recipeId",1L)})
                    }
                    SystemClock.sleep(150)
                    scenario.onActivity{a->
                        val nav=(a.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
                        assertEquals(id,nav.currentDestination?.id)
                    }
                }
                scenario.recreate()
                scenario.onActivity{a->
                    val nav=(a.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
                    assertEquals(R.id.onboarding,nav.currentDestination?.id)
                }
            }
        }finally{app.preferences.edit().putBoolean("isFirstLaunch",first).putBoolean("showOnboarding",onboarding).commit()}
    }

    @Test fun createRecipeWithRealFormAndPreserveDraftOnRecreation(){
        val title="UI test ${System.nanoTime()}"
        var savedId=0L
        try {
            ActivityScenario.launch(MainActivity::class.java).use{scenario->
                scenario.onActivity{a->
                    (a.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController.navigate(R.id.add_recipe)
                }
                onView(withHint("Название рецепта *")).perform(scrollTo(),replaceText(title),closeSoftKeyboard())
                scenario.recreate()
                onView(withHint("Название рецепта *")).check(androidx.test.espresso.assertion.ViewAssertions.matches(withText(title)))
                onView(withHint("Категория *")).perform(scrollTo(),click())
                onView(allOf(withText("Завтраки"),isDisplayed())).perform(click())
                onView(withHint("Ингредиент 1 *")).perform(scrollTo(),replaceText("Творог"),closeSoftKeyboard())
                onView(withHint("Количество *")).perform(scrollTo(),replaceText("400"),closeSoftKeyboard())
                onView(withHint("Единица")).perform(scrollTo(),replaceText("г"),closeSoftKeyboard())
                onView(withHint("Шаг 1 *")).perform(scrollTo(),replaceText("Смешайте ингредиенты."),closeSoftKeyboard())
                onView(withText("Сохранить рецепт")).perform(scrollTo(),click())
                awaitCondition{
                    app.repository.db.query("SELECT id FROM recipes WHERE title=?",arrayOf(title)).use{
                        if(it.moveToFirst()){savedId=it.getLong(0);true}else false
                    }
                }
                val saved=app.repository.dao.getRecipe(savedId)
                assertEquals(title,saved.recipe.title)
                assertEquals("Завтраки",saved.recipe.category)
                assertEquals("400",saved.ingredients.single().amount)
                assertEquals("Смешайте ингредиенты.",saved.steps.single().description)
                assertTrue(saved.recipe.isUserCreated)
            }
        } finally { if(savedId>0)app.repository.dao.deleteOwnRecipe(savedId) }
    }
}

