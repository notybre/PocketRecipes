package com.example.pocketrecipes.ui
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.example.pocketrecipes.R
class RecipeDetailsFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        val id=arguments?.getLong("recipeId")?:0
        content.label("Загрузка рецепта…",14,color=R.color.pr_muted)
        vm.repo.dao.observeRecipe(id).observe(viewLifecycleOwner){d->
            redraw{
                if(d==null) {
                    content.empty("Рецепт недоступен","Возможно, он был удалён.",R.drawable.ic_book)
                    content.button("Перейти в каталог"){go(R.id.catalog)}
                } else {
                    val r=d.recipe;toolbar.title=if(r.isUserCreated)"Мой рецепт" else "Рецепт"
                    art(r.image)
                    content.label(r.title,28,true,R.color.pr_strong);content.space(8)
                    content.label("${r.category} · ${r.prepTime} мин · ${r.servings} порц.",14,color=R.color.pr_muted)
                    content.space()
                    content.button(if(r.isFavorite)"♥ В избранном" else "♡ Добавить в избранное",true){vm.toggleFavorite(id)}
                    if(r.description.isNotBlank()){content.label(r.description,color=R.color.pr_muted);content.space()}
                    content.heading("Ингредиенты")
                    d.ingredients.sortedBy{it.position}.forEach{v->
                        content.label("${v.name} — ${v.amount} ${v.unit}");content.space(12)
                    }
                    content.button("Ингредиенты в список покупок"){vm.addIngredients(id)}
                    content.heading("Приготовление")
                    d.steps.sortedBy{it.stepNumber}.forEach{s->
                        content.label("Шаг ${s.stepNumber}",14,true,R.color.pr_strong)
                        content.space(8);content.label(s.description);content.space(16)
                    }
                    if(r.isUserCreated) {
                        content.button("Редактировать"){go(R.id.edit_recipe,bundleOf("recipeId" to id))}
                        content.button("Удалить рецепт",true){requireContext().confirm("Удалить рецепт?"){
                            vm.change({vm.repo.dao.deleteOwnRecipe(id)},"Рецепт удалён",R.id.my_recipes)
                        }}
                    }
                }
            }
        }
    }
}

