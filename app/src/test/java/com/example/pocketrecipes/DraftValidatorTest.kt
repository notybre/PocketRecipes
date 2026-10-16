package com.example.pocketrecipes
import com.example.pocketrecipes.model.*
import org.junit.Assert.*
import org.junit.Test
class DraftValidatorTest {
    private fun valid()=RecipeDraft(title="Сырники",category="Завтраки",minutes="25",servings="3",
        ingredients=mutableListOf(IngredientInput("Творог","400","г")),steps=mutableListOf("Смешать и обжарить."))
    @Test fun acceptsCompleteRecipe(){assertTrue(DraftValidator.validate(valid()).isEmpty())}
    @Test fun rejectsMissingRequiredData(){
        val errors=DraftValidator.validate(RecipeDraft(title="  ",category="",minutes="0",servings="-2",
            ingredients=mutableListOf(),steps=mutableListOf()))
        assertEquals(setOf("title","category","minutes","servings","ingredients","steps"),errors.keys)
    }
    @Test fun validatesEachIngredientAndStep(){
        val d=valid().apply{ingredients.add(IngredientInput("","0","г"));steps.add(" ")}
        val e=DraftValidator.validate(d)
        assertTrue(e.containsKey("ingredient_1_name"));assertTrue(e.containsKey("ingredient_1_amount"));assertTrue(e.containsKey("step_1"))
    }
    @Test fun acceptsFractionalAndTasteAmounts(){
        val d=valid().apply{ingredients.add(IngredientInput("Вода","1,5","л"));ingredients.add(IngredientInput("Соль","по вкусу",""))}
        assertTrue(DraftValidator.validate(d).isEmpty())
    }
    @Test fun rejectsMalformedTimeAndUnknownCategory(){
        val d=valid().apply{minutes="99999999999999999";category="Неизвестно";servings="abc"}
        assertEquals(setOf("category","minutes","servings"),DraftValidator.validate(d).keys)
    }
}

