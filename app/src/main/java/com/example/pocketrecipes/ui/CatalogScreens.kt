package com.example.pocketrecipes.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.pocketrecipes.R
import com.example.pocketrecipes.model.categories
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class OnboardingFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        art("salad",280)
        content.label("Все любимые рецепты всегда под рукой",28,true,R.color.pr_strong)
        content.space();content.label("Находите новые блюда, сохраняйте свои рецепты и собирайте список покупок.",color=R.color.pr_muted)
        content.space(32)
        content.button("Начать") {
            vm.preferences.edit().putBoolean("isFirstLaunch",false).apply()
            findNavController().navigate(R.id.home,null,navOptions{popUpTo(R.id.onboarding){inclusive=true}})
        }
        content.label("Без регистрации. Всё хранится на устройстве.",12,color=R.color.pr_muted)
    }
}

class HomeFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        toolbar.menu.add("Список покупок").setIcon(R.drawable.ic_cart).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        toolbar.setOnMenuItemClickListener{go(R.id.shopping);true}
        content.label("Что приготовим\nсегодня?",28,true)
        content.space()
        content.field("Поиск","Найти рецепт…").editText?.apply {
            isFocusable=false;isClickable=true;setOnClickListener{go(R.id.search)}
        }
        val categoryHeading=LinearLayout(requireContext()).apply{orientation=LinearLayout.HORIZONTAL;gravity=android.view.Gravity.CENTER_VERTICAL}
        content.addView(categoryHeading)
        categoryHeading.addView(TextView(requireContext()).apply{text="Категории";textSize=18f;setTypeface(null,android.graphics.Typeface.BOLD);setTextColor(context.color(R.color.pr_text))},LinearLayout.LayoutParams(0,-2,1f))
        categoryHeading.addView(TextView(requireContext()).apply{text="Все";textSize=14f;setTextColor(context.color(R.color.pr_strong));gravity=android.view.Gravity.CENTER;isClickable=true;isFocusable=true;setOnClickListener{go(R.id.categories)}},LinearLayout.LayoutParams(requireContext().dp(48),requireContext().dp(48)))
        val scroll=HorizontalScrollView(requireContext()).apply{isHorizontalScrollBarEnabled=false}
        val row=LinearLayout(requireContext()).apply{orientation=LinearLayout.HORIZONTAL}
        scroll.addView(row);content.addView(scroll)
        categories.forEach{category->
            row.addView(Chip(requireContext()).apply{
                text=category;minHeight=context.dp(48);pocketStyle()
                setOnClickListener{go(R.id.catalog,bundleOf("category" to category))}
            },LinearLayout.LayoutParams(requireContext().dp(160),-2).apply{marginEnd=requireContext().dp(8)})
        }
        content.heading("Популярное")
        val loading=content.label("Загрузка рецептов…",14,color=R.color.pr_muted)
        val popular=recipeList()
        content.heading("Недавно добавленные")
        val recent=recipeList()
        val empty=content.label("Здесь появятся ваши новые рецепты.",14,color=R.color.pr_muted)
        content.button("Создать свой рецепт",true){go(R.id.add_recipe)}
        vm.loading.observe(viewLifecycleOwner){loading.visibility=if(it)View.VISIBLE else View.GONE}
        vm.recipes.observe(viewLifecycleOwner){list->
            popular.submitList(list.filter{!it.isUserCreated}.sortedBy{it.id}.take(1))
            val own=list.filter{it.isUserCreated}.take(3);recent.submitList(own)
            empty.visibility=if(own.isEmpty())View.VISIBLE else View.GONE
        }
    }
}

class CatalogFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        val category=arguments?.getString("category")
        if(category!=null)toolbar.title=category
        content.button("Найти рецепт…",true){go(R.id.search)}
        content.button("Фильтры и категории",true){go(R.id.search)}
        content.button("Все категории",true){go(R.id.categories)}
        val count=content.label("",12,color=R.color.pr_muted);content.space()
        val adapter=recipeList()
        vm.recipes.observe(viewLifecycleOwner){all->
            val list=all.filter{category==null||it.category==category}.sortedBy{it.id}
            count.text="Рецептов: ${list.size} · на каждый день";adapter.submitList(list)
        }
    }
}

class CategoriesFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        vm.recipes.observe(viewLifecycleOwner){list->redraw{
            content.label("Выберите настроение для своего меню",color=R.color.pr_muted);content.space()
            categories.forEach{cat->content.menuRow(cat,"Рецептов: ${list.count{it.category==cat}}"){
                go(R.id.catalog,bundleOf("category" to cat))
            }}
        }}
    }
}

class SearchFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        val input=content.field("Поиск рецептов",vm.query)
        input.editText?.inputType=android.text.InputType.TYPE_CLASS_TEXT
        content.heading("Категория")
        val cats=ChipGroup(requireContext()).apply{isSingleSelection=true;isSelectionRequired=true}
        content.addView(cats)
        val chips=mutableMapOf<String,Chip>()
        (listOf("")+categories).forEach{cat->
            val chip=Chip(requireContext()).apply{id=View.generateViewId();text=cat.ifEmpty{"Все"};isCheckable=true;minHeight=context.dp(48);pocketStyle()}
            cats.addView(chip);chips[cat]=chip;chip.isChecked=vm.category==cat
        }
        content.heading("Время приготовления")
        val times=ChipGroup(requireContext()).apply{isSingleSelection=true;isSelectionRequired=true}
        content.addView(times);val timeChips=mutableMapOf<Int,Chip>()
        listOf(0,15,30,60).forEach{time->
            val chip=Chip(requireContext()).apply{id=View.generateViewId();text=if(time==0)"Любое" else "До $time мин";isCheckable=true;minHeight=context.dp(48);pocketStyle()}
            times.addView(chip);timeChips[time]=chip;chip.isChecked=vm.maxMinutes==time
        }
        content.space()
        val summary=content.label("",14,color=R.color.pr_muted)
        content.space()
        val results=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
        content.addView(results)
        val adapter=recipeList(results)
        val empty=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
        content.addView(empty)
        fun update() {
            val list=vm.filter(vm.recipes.value.orEmpty())
            adapter.submitList(list)
            summary.text=if(vm.query.isBlank()&&vm.category.isBlank()&&vm.maxMinutes==0)
                "Начните вводить название рецепта или выберите фильтры" else "Найдено: ${list.size}"
            empty.removeAllViews()
            if(list.isEmpty()&&vm.loading.value!=true)empty.empty("Ничего не найдено","Попробуйте другое название или уберите ограничения.",R.drawable.ic_search)
        }
        // Results are live; the button also moves focus away from the keyboard.
        content.button("Применить") {
            val imm=requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(input.windowToken,0);input.clearFocus();update()
        }
        content.button("Сбросить фильтры",true) {
            vm.query="";vm.category="";vm.maxMinutes=0
            input.editText?.setText("");chips[""]?.isChecked=true;timeChips[0]?.isChecked=true;update()
        }
        input.editText?.doAfterTextChanged{vm.query=it.toString();update()}
        cats.setOnCheckedStateChangeListener{_,ids->chips.entries.find{it.value.id in ids}?.let{vm.category=it.key;update()}}
        times.setOnCheckedStateChangeListener{_,ids->timeChips.entries.find{it.value.id in ids}?.let{vm.maxMinutes=it.key;update()}}
        vm.recipes.observe(viewLifecycleOwner){update()}
        vm.loading.observe(viewLifecycleOwner){update()}
    }
}

class FavoritesFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        val holder=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL}
        content.addView(holder)
        val adapter=recipeList()
        vm.recipes.observe(viewLifecycleOwner){all->
            val list=all.filter{it.isFavorite};adapter.submitList(list);holder.removeAllViews()
            if(list.isEmpty()) {
                holder.empty("В избранном пока пусто","Нажмите на сердце возле рецепта, чтобы сохранить его здесь.",R.drawable.ic_heart)
                holder.button("Перейти в каталог"){go(R.id.catalog)}
            } else {holder.label("Сохранено для вдохновения",color=R.color.pr_muted);holder.space()}
        }
    }
}
class MyRecipesFragment : ScreenFragment() {
    override fun build(saved:Bundle?) {
        content.button("+ Создать рецепт"){go(R.id.add_recipe)}
        val holder=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL};content.addView(holder)
        val adapter=recipeList()
        vm.recipes.observe(viewLifecycleOwner){all->
            val list=all.filter{it.isUserCreated};adapter.submitList(list);holder.removeAllViews()
            if(list.isEmpty())holder.empty("Пока нет своих рецептов","Создайте первый рецепт и сохраните его в PocketRecipes.",R.drawable.ic_book)
        }
    }
}
