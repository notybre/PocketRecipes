package com.example.pocketrecipes.model

data class IngredientInput(var name: String = "", var amount: String = "", var unit: String = "")
data class RecipeDraft(
    var id: Long = 0, var title: String = "", var description: String = "",
    var category: String = "", var minutes: String = "30", var servings: String = "2",
    var image: String = "cakes",
    var ingredients: MutableList<IngredientInput> = mutableListOf(IngredientInput()),
    var steps: MutableList<String> = mutableListOf("")
)
val categories = listOf("Завтраки", "Супы", "Салаты", "Основные блюда", "Десерты", "Напитки")

object DraftValidator {
    fun validate(draft: RecipeDraft): Map<String, String> = buildMap {
        if (draft.title.isBlank()) put("title", "Введите название рецепта")
        if (draft.category !in categories) put("category", "Выберите категорию")
        if (draft.minutes.toIntOrNull()?.let { it > 0 } != true) put("minutes", "Введите время больше нуля")
        if (draft.servings.toIntOrNull()?.let { it > 0 } != true) put("servings", "Введите количество порций больше нуля")
        if (draft.ingredients.isEmpty()) put("ingredients", "Добавьте хотя бы один ингредиент")
        draft.ingredients.forEachIndexed { i, v ->
            if (v.name.isBlank()) put("ingredient_${i}_name", "Введите название ингредиента")
            if (v.amount.isBlank()) put("ingredient_${i}_amount", "Укажите количество")
            val number = v.amount.replace(',', '.').toDoubleOrNull()
            if (number != null && (!number.isFinite() || number <= 0)) put("ingredient_${i}_amount", "Количество должно быть больше нуля")
        }
        if (draft.steps.isEmpty()) put("steps", "Добавьте хотя бы один этап приготовления")
        draft.steps.forEachIndexed { i, v -> if (v.isBlank()) put("step_$i", "Опишите этап приготовления") }
    }
}

