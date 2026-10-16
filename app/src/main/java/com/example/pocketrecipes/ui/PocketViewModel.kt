package com.example.pocketrecipes.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.pocketrecipes.PocketApp
import com.example.pocketrecipes.data.*
import com.example.pocketrecipes.model.*
import org.json.JSONArray
import org.json.JSONObject
data class UiEvent(val message: String, val destination: Int? = null, val recipeId: Long = 0)
class PocketViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    private val app = application as PocketApp
    val repo = app.repository
    val preferences = app.preferences
    val recipes = repo.recipes
    val shopping = repo.shopping
    val event = MutableLiveData<UiEvent?>()
    val loading = MutableLiveData(true)
    val saving = MutableLiveData(false)
    var query: String
        get() = state["query"] ?: ""
        set(v) { state["query"] = v }
    var category: String
        get() = state["category"] ?: ""
        set(v) { state["category"] = v }
    var maxMinutes: Int
        get() = state["maxMinutes"] ?: 0
        set(v) { state["maxMinutes"] = v }
    init { repo.seed { result ->
        loading.value = false
        result.onFailure { event.value = UiEvent("Не удалось открыть каталог. Повторите запуск приложения.") }
    } }
    fun filter(list: List<Recipe>, categoryOverride: String? = null): List<Recipe> = list.filter {
        it.title.contains(query.trim(), ignoreCase = true) &&
            ((categoryOverride ?: category).isBlank() || it.category == (categoryOverride ?: category)) &&
            (maxMinutes == 0 || it.prepTime <= maxMinutes)
    }
    fun change(block: () -> Unit, message: String? = null, destination: Int? = null) {
        repo.execute(block) { result ->
            result.fold({ if (message != null) event.value = UiEvent(message, destination) },
                { event.value = UiEvent("Не удалось сохранить изменение. Попробуйте ещё раз.") })
        }
    }
    fun toggleFavorite(id: Long) = change({ repo.dao.toggleFavorite(id) })
    fun addIngredients(id: Long) = repo.addIngredients(id) { result ->
        result.fold({ event.value = UiEvent("Ингредиенты добавлены", com.example.pocketrecipes.R.id.shopping) },
            { event.value = UiEvent("Не удалось добавить ингредиенты") })
    }
    fun save(draft: RecipeDraft) {
        if (saving.value == true) return
        saving.value = true
        repo.save(draft) { result ->
            saving.value = false
            result.fold({
                state.remove<String>("draft_${draft.id}")
                event.value = UiEvent(if (draft.id == 0L) "Рецепт успешно добавлен" else "Изменения сохранены",
                    com.example.pocketrecipes.R.id.my_recipes, it)
            }, { event.value = UiEvent(it.message ?: "Не удалось сохранить рецепт") })
        }
    }
    fun saveDraft(d: RecipeDraft) {
        val json = JSONObject().put("id", d.id).put("title", d.title).put("description", d.description)
            .put("category", d.category).put("minutes", d.minutes).put("servings", d.servings).put("image", d.image)
        json.put("ingredients", JSONArray().apply { d.ingredients.forEach { v ->
            put(JSONObject().put("name", v.name).put("amount", v.amount).put("unit", v.unit))
        } })
        json.put("steps", JSONArray(d.steps))
        state["draft_${d.id}"] = json.toString()
    }
    fun draft(id: Long): RecipeDraft? = state.get<String>("draft_$id")?.let {
        runCatching {
            val j = JSONObject(it)
            RecipeDraft(id, j.getString("title"), j.getString("description"), j.getString("category"),
                j.getString("minutes"), j.getString("servings"), j.getString("image"),
                j.getJSONArray("ingredients").let { a -> (0 until a.length()).map { n -> a.getJSONObject(n).let { v ->
                    IngredientInput(v.getString("name"), v.getString("amount"), v.getString("unit"))
                } }.toMutableList() },
                j.getJSONArray("steps").let { a -> (0 until a.length()).map { n -> a.getString(n) }.toMutableList() })
        }.getOrNull()
    }
    fun toDraft(d: RecipeWithDetails) = RecipeDraft(d.recipe.id, d.recipe.title, d.recipe.description, d.recipe.category,
        d.recipe.prepTime.toString(), d.recipe.servings.toString(), d.recipe.image,
        d.ingredients.sortedBy { it.position }.map { IngredientInput(it.name, it.amount, it.unit) }.toMutableList(),
        d.steps.sortedBy { it.stepNumber }.map { it.description }.toMutableList())
}

