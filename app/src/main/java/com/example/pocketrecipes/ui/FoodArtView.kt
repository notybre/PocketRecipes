package com.example.pocketrecipes.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.pocketrecipes.R

data class DishPhoto(val key: String, val title: String, val resource: Int)

// Preserve existing database keys when replacing illustrations with photos.
val dishPhotos = listOf(
    DishPhoto("pasta", "Паста карбонара", R.drawable.carbonara),
    DishPhoto("cakes", "Сырники", R.drawable.syrniki),
    DishPhoto("soup", "Куриный суп", R.drawable.chicken_soup),
    DishPhoto("salad", "Салат Цезарь", R.drawable.cezar),
    DishPhoto("brownie", "Брауни", R.drawable.brownie),
    DishPhoto("oat", "Каша с ягодами", R.drawable.porridge_with_berries),
    DishPhoto("pizza", "Домашняя пицца", R.drawable.homemade_pizza),
    DishPhoto("lemonade", "Лимонад", R.drawable.lemonade)
)

class FoodArtView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {
    var dish = "pasta"
        set(value) { field = value; showPhoto() }
    init { scaleType = ScaleType.CENTER_CROP; showPhoto() }
    private fun showPhoto() {
        val photo = dishPhotos.firstOrNull { it.key == dish } ?: dishPhotos.first()
        setImageResource(photo.resource)
        contentDescription = photo.title
    }
}
