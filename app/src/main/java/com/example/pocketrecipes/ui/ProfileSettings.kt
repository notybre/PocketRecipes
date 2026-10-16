package com.example.pocketrecipes.ui
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.example.pocketrecipes.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProfileFragment:ScreenFragment(){
    override fun build(saved:Bundle?){
        vm.recipes.observe(viewLifecycleOwner){list->redraw{
            content.addView(ImageView(requireContext()).apply{setImageResource(R.drawable.ic_user);imageTintList=android.content.res.ColorStateList.valueOf(context.color(R.color.pr_strong))},LinearLayout.LayoutParams(requireContext().dp(48),requireContext().dp(48)))
            content.space(12)
            val nameLabel=content.label(vm.preferences.getString("userName","Алекс")?:"Алекс",28,true,R.color.pr_strong)
            content.space(8);content.label("Ваша домашняя книга рецептов",color=R.color.pr_muted)
            content.space()
            content.button("Изменить имя",true){
                val input=EditText(requireContext()).apply{setSingleLine();setText(vm.preferences.getString("userName","Алекс"))}
                val dialog=MaterialAlertDialogBuilder(requireContext()).setTitle("Ваше имя").setView(input)
                    .setNegativeButton("Отмена",null).setPositiveButton("Сохранить",null).create()
                dialog.setOnShowListener{dialog.getButton(-1).setOnClickListener{
                    val name=input.text.toString().trim()
                    if(name.isBlank())input.error="Введите имя" else{
                        vm.preferences.edit().putString("userName",name).apply();dialog.dismiss()
                        nameLabel.text=name
                    }
                }};dialog.show()
            }
            val row=LinearLayout(requireContext()).apply{orientation=LinearLayout.HORIZONTAL};content.addView(row)
            listOf(list.count{it.isUserCreated} to "Своих",list.count{it.isFavorite} to "Избранное",list.size to "В каталоге").forEach{(n,title)->
                val box=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL;setPadding(context.dp(8),context.dp(16),context.dp(8),context.dp(16));roundBackground(context.color(R.color.pr_soft))}
                row.addView(box,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=requireContext().dp(4)})
                box.label(n.toString(),24,true,R.color.pr_strong);box.label(title,12)
            }
            content.space(24)
            content.menuRow("Мои рецепты","Ваши блюда и идеи"){go(R.id.my_recipes)}
            content.menuRow("Список покупок","Продукты для следующего блюда"){go(R.id.shopping)}
            content.menuRow("Настройки"){go(R.id.settings)}
            content.menuRow("О приложении"){
                MaterialAlertDialogBuilder(requireContext()).setTitle("PocketRecipes · 1.0")
                    .setMessage("Книга рецептов с поиском, избранным и списком покупок.\n\nВсе пользовательские данные хранятся на устройстве. Интернет и регистрация не нужны.")
                    .setPositiveButton("Понятно",null).show()
            }
            content.label("Данные доступны без интернета",12,color=R.color.pr_muted)
        }}
    }
}
class SettingsFragment:ScreenFragment(){
    override fun build(saved:Bundle?){
        fun toggle(title:String,key:String,change:(Boolean)->Unit){
            val row=LinearLayout(requireContext()).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(context.dp(12),context.dp(12),context.dp(12),context.dp(12));roundBackground(context.color(R.color.pr_surface))}
            row.addView(TextView(requireContext()).apply{text=title;textSize=16f;setTextColor(context.color(R.color.pr_text))},LinearLayout.LayoutParams(0,-2,1f))
            val switch=SwitchMaterial(requireContext()).apply{isChecked=vm.preferences.getBoolean(key,false);contentDescription=title}
            row.addView(switch);content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=requireContext().dp(16)})
            switch.setOnCheckedChangeListener{_,value->vm.preferences.edit().putBoolean(key,value).apply();change(value)}
        }
        toggle("Тёмная тема","darkTheme"){dark->AppCompatDelegate.setDefaultNightMode(if(dark)AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)}
        toggle("Приветствие при запуске","showOnboarding"){}
        content.heading("Порций по умолчанию")
        val group=ChipGroup(requireContext()).apply{isSingleSelection=true;isSelectionRequired=true};content.addView(group)
        listOf(2,4,6).forEach{n->
            group.addView(Chip(requireContext()).apply{
                id=android.view.View.generateViewId();text=n.toString();isCheckable=true;minHeight=context.dp(48);minWidth=context.dp(96);pocketStyle()
                isChecked=vm.preferences.getInt("defaultServings",2)==n
                setOnCheckedChangeListener{_,checked->if(checked)vm.preferences.edit().putInt("defaultServings",n).apply()}
            })
        }
        content.heading("Данные")
        content.menuRow("Очистить список покупок"){requireContext().confirm("Очистить список покупок?"){
            vm.change({vm.repo.dao.clearShopping()},"Список покупок очищен")
        }}
        content.menuRow("Удалить мои рецепты"){requireContext().confirm("Удалить все мои рецепты?"){
            vm.change({vm.repo.dao.deleteOwnRecipes()},"Собственные рецепты удалены")
        }}
        content.heading("О приложении");content.label("PocketRecipes\nВерсия 1.0",color=R.color.pr_muted)
    }
}
