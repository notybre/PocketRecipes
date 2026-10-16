package com.example.pocketrecipes.ui
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketrecipes.R
import com.example.pocketrecipes.data.Recipe
class RecipeAdapter(private val open: (Long)->Unit, private val favorite: (Long)->Unit) :
    ListAdapter<Recipe, RecipeAdapter.Holder>(object: DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(a:Recipe,b:Recipe)=a.id==b.id
        override fun areContentsTheSame(a:Recipe,b:Recipe)=a.title==b.title && a.isFavorite==b.isFavorite &&
            a.prepTime==b.prepTime && a.servings==b.servings && a.category==b.category && a.image==b.image
    }) {
    class Holder(v:View):RecyclerView.ViewHolder(v)
    override fun onCreateViewHolder(parent:ViewGroup,type:Int)=Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_recipe,parent,false))
    override fun onBindViewHolder(h:Holder,p:Int) {
        val r=getItem(p);val v=h.itemView
        v.findViewById<TextView>(R.id.recipe_title).text=r.title
        v.findViewById<TextView>(R.id.recipe_category).text=r.category
        v.findViewById<TextView>(R.id.recipe_meta).text="${r.prepTime} мин · ${r.servings} порц."
        v.findViewById<FoodArtView>(R.id.art).dish=r.image
        v.findViewById<ImageButton>(R.id.favorite).apply {
            setImageResource(if(r.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imageTintList=ColorStateList.valueOf(context.color(if(r.isFavorite) R.color.pr_error else R.color.pr_muted))
            contentDescription=(if(r.isFavorite) "Удалить из избранного: " else "Добавить в избранное: ")+r.title
            setOnClickListener{favorite(r.id)}
        }
        v.setOnClickListener{open(r.id)}
    }
}

