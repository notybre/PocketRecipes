package com.example.pocketrecipes
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.pocketrecipes.data.RecipeRepository
class PocketApp : Application() {
    val preferences by lazy { getSharedPreferences("pocket_settings", MODE_PRIVATE) }
    val repository by lazy { RecipeRepository(this) }
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(if (preferences.getBoolean("darkTheme", false))
            AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }
}

