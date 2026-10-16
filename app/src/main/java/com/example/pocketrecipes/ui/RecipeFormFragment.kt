package com.example.pocketrecipes.ui
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.NestedScrollView
import com.example.pocketrecipes.R
import com.example.pocketrecipes.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class AddRecipeFragment:RecipeFormFragment()
class EditRecipeFragment:RecipeFormFragment()
open class RecipeFormFragment:ScreenFragment() {
    private var draft:RecipeDraft?=null
    private val fields=linkedMapOf<String,TextInputLayout>()
    override fun build(saved:Bundle?) {
        val id=arguments?.getLong("recipeId")?:0L
        val restored=vm.draft(id)
        if(restored!=null){draft=restored;render();return}
        if(id==0L){
            draft=RecipeDraft(servings=vm.preferences.getInt("defaultServings",2).toString());render()
        }else{
            content.label("Загрузка рецепта…")
            vm.repo.execute({vm.repo.dao.getRecipe(id)}){result->
                if(view==null)return@execute
                val d=result.getOrNull()
                if(d==null||!d.recipe.isUserCreated){
                    content.removeAllViews();content.label("Рецепт недоступен для редактирования",color=R.color.pr_error)
                }else{draft=vm.toDraft(d);render()}
            }
        }
    }
    private fun persist(){draft?.let(vm::saveDraft)}
    private fun bind(parent:LinearLayout,key:String,label:String,value:String,numeric:Boolean=false,multiline:Boolean=false,set:(String)->Unit):TextInputLayout{
        val field=parent.field(label,value,numeric,multiline);fields[key]=field
        field.editText?.doAfterTextChanged{set(it.toString());field.error=null;persist()}
        return field
    }
    private fun render(){
        val d=draft?:return
        content.removeAllViews();fields.clear()
        val preview=FoodArtView(requireContext()).apply{dish=d.image}
        content.addView(preview,LinearLayout.LayoutParams(-1,requireContext().dp(140)));content.space(12)
        content.button("Выбрать фото",true){
            val adapter = object : BaseAdapter() {
                override fun getCount() = dishPhotos.size
                override fun getItem(position: Int) = dishPhotos[position]
                override fun getItemId(position: Int) = position.toLong()
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                    val photo = getItem(position)
                    return LinearLayout(parent.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        setPadding(context.dp(16), context.dp(8), context.dp(16), context.dp(8))
                        addView(FoodArtView(context).apply {
                            dish = photo.key
                            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                        }, LinearLayout.LayoutParams(context.dp(88), context.dp(64)))
                        addView(TextView(context).apply {
                            text = photo.title + if (photo.key == d.image) " ✓" else ""
                            textSize = 16f
                            setTextColor(context.color(R.color.pr_text))
                            setPadding(context.dp(16), 0, 0, 0)
                        }, LinearLayout.LayoutParams(0, -2, 1f))
                    }
                }
            }
            MaterialAlertDialogBuilder(requireContext()).setTitle("Фото блюда").setAdapter(adapter){_,i->
                d.image=dishPhotos[i].key;preview.dish=d.image;persist()
            }.setNegativeButton("Отмена",null).show()
        }
        bind(content,"title","Название рецепта *",d.title){d.title=it}
        bind(content,"description","Описание",d.description,multiline=true){d.description=it}
        val category=content.field("Категория *",d.category)
        fields["category"]=category
        category.editText?.apply{
            isFocusable=false;isClickable=true;setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_chevron,0)
            setOnClickListener{
                MaterialAlertDialogBuilder(requireContext()).setTitle("Категория")
                    .setSingleChoiceItems(categories.toTypedArray(),categories.indexOf(d.category)){dialog,index->
                        d.category=categories[index];setText(d.category);category.error=null;persist();dialog.dismiss()
                    }.setNegativeButton("Отмена",null).show()
            }
        }
        bind(content,"minutes","Время приготовления, мин *",d.minutes,true){d.minutes=it}
        bind(content,"servings","Количество порций *",d.servings,true){d.servings=it}
        content.heading("Ингредиенты")
        val ingredientError=content.label("",12,color=R.color.pr_error)
        val ingredientRows=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
        content.addView(ingredientRows)
        fun renderIngredients(){
            ingredientRows.removeAllViews();fields.keys.filter{it.startsWith("ingredient_")}.toList().forEach(fields::remove)
            d.ingredients.forEachIndexed{i,v->
                val group=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
                ingredientRows.addView(group)
                bind(group,"ingredient_${i}_name","Ингредиент ${i+1} *",v.name){v.name=it}
                val row=LinearLayout(requireContext()).apply{orientation=LinearLayout.HORIZONTAL}
                group.addView(row)
                val amount=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
                val unit=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
                row.addView(amount,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=requireContext().dp(8)})
                row.addView(unit,LinearLayout.LayoutParams(0,-2,1f))
                bind(amount,"ingredient_${i}_amount","Количество *",v.amount){v.amount=it}
                bind(unit,"ingredient_${i}_unit","Единица",v.unit){v.unit=it}
                group.button("Удалить ингредиент ${i+1}",true){d.ingredients.removeAt(i);persist();renderIngredients()}
            }
        }
        renderIngredients()
        content.button("+ Добавить ингредиент",true){d.ingredients.add(IngredientInput());ingredientError.text="";persist();renderIngredients()}
        content.heading("Приготовление")
        val stepsError=content.label("",12,color=R.color.pr_error)
        val stepRows=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL};content.addView(stepRows)
        fun renderSteps(){
            stepRows.removeAllViews();fields.keys.filter{it.startsWith("step_")}.toList().forEach(fields::remove)
            d.steps.forEachIndexed{i,s->
                bind(stepRows,"step_$i","Шаг ${i+1} *",s,multiline=true){d.steps[i]=it}
                stepRows.button("Удалить шаг ${i+1}",true){d.steps.removeAt(i);persist();renderSteps()}
            }
        }
        renderSteps()
        content.button("+ Добавить шаг",true){d.steps.add("");stepsError.text="";persist();renderSteps()}
        val save=content.button(if(d.id==0L)"Сохранить рецепт" else "Сохранить изменения"){
            val errors=DraftValidator.validate(d)
            fields.forEach{(key,field)->field.error=errors[key]}
            ingredientError.text=errors["ingredients"].orEmpty();stepsError.text=errors["steps"].orEmpty()
            if(errors.isEmpty()){
                val imm=requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken,0)
                vm.save(d.copy(ingredients=d.ingredients.map{it.copy()}.toMutableList(),steps=d.steps.toMutableList()))
            }else{
                val target=fields.entries.firstOrNull{errors.containsKey(it.key)}?.value
                target?.editText?.requestFocus()
                target?.post{target.requestRectangleOnScreen(android.graphics.Rect(0,0,target.width,target.height),false)}
            }
        }
        vm.saving.observe(viewLifecycleOwner){saving->
            save.isEnabled=!saving;save.alpha=if(saving).5f else 1f
            save.text=if(saving)"Сохранение…" else if(d.id==0L)"Сохранить рецепт" else "Сохранить изменения"
        }
        if(d.id>0)content.button("Удалить рецепт",true){requireContext().confirm("Удалить рецепт?"){
            vm.change({vm.repo.dao.deleteOwnRecipe(d.id)},"Рецепт удалён",R.id.my_recipes)
        }}
        persist()
    }
}
