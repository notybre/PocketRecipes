package com.example.pocketrecipes.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import com.example.pocketrecipes.model.*
import java.util.concurrent.Executors

class RecipeRepository(context: Context) {
    val db = Room.databaseBuilder(context.applicationContext, RecipeDatabase::class.java, "pocketrecipes.db").build()
    val dao = db.dao()
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    val recipes = dao.observeRecipes()
    val shopping = dao.observeShopping()

    fun <T> execute(block: () -> T, done: (Result<T>) -> Unit) {
        worker.execute {
            val result = runCatching(block)
            main.post { done(result) }
        }
    }

    fun seed(done: (Result<Unit>) -> Unit) = execute({
        db.runInTransaction {
            if (dao.countRecipes() == 0) SeedData.recipes().forEach { insert(it, false) }
        }
    }, done)

    private fun insert(d: RecipeDraft, own: Boolean): Long {
        val old = if (d.id > 0) dao.getRecipe(d.id)?.recipe else null
        check(d.id == 0L || old?.isUserCreated == true) { "Рецепт недоступен для редактирования" }
        val recipe = Recipe().apply {
            id = d.id; title = d.title.trim(); description = d.description.trim()
            category = d.category; prepTime = d.minutes.toInt(); servings = d.servings.toInt()
            image = d.image; isUserCreated = own; isFavorite = old?.isFavorite ?: false
            createdAt = old?.createdAt ?: System.currentTimeMillis()
        }
        val id = if (d.id == 0L) dao.insertRecipe(recipe) else {
            dao.updateRecipe(recipe); dao.deleteIngredients(d.id); dao.deleteSteps(d.id); d.id
        }
        dao.insertIngredients(d.ingredients.mapIndexed { i, v -> Ingredient().apply {
            recipeId = id; position = i; name = v.name.trim(); amount = v.amount.trim(); unit = v.unit.trim()
        } })
        dao.insertSteps(d.steps.mapIndexed { i, s -> RecipeStep().apply {
            recipeId = id; stepNumber = i + 1; description = s.trim()
        } })
        return id
    }

    fun save(d: RecipeDraft, done: (Result<Long>) -> Unit) = execute({
        require(DraftValidator.validate(d).isEmpty()) { "Проверьте обязательные поля" }
        var id = 0L
        db.runInTransaction { id = insert(d, true) }
        id
    }, done)

    fun addIngredients(id: Long, done: (Result<Unit>) -> Unit) = execute({
        db.runInTransaction {
            val details = checkNotNull(dao.getRecipe(id)) { "Рецепт удалён" }
            dao.insertShopping(details.ingredients.sortedBy { it.position }.map { v -> ShoppingItem().apply {
                name = v.name; amount = v.amount; unit = v.unit
            } })
        }
    }, done)
}

