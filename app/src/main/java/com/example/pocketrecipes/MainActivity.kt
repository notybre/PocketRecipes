package com.example.pocketrecipes
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK != android.content.res.Configuration.UI_MODE_NIGHT_YES
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(b.left, b.top, b.right, b.bottom)
            WindowInsetsCompat.CONSUMED
        }
        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        run {
            val graph = nav.navInflater.inflate(R.navigation.nav_graph)
            val prefs = (application as PocketApp).preferences
            graph.setStartDestination(if (prefs.getBoolean("isFirstLaunch", true) || prefs.getBoolean("showOnboarding", false))
                R.id.onboarding else R.id.home)
            nav.graph = graph
        }
        val bottom = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottom.setOnItemSelectedListener { item ->
            if (nav.currentDestination?.id != item.itemId) nav.navigate(item.itemId, null, navOptions {
                launchSingleTop = true
                popUpTo(R.id.home) { inclusive = false }
            })
            true
        }
        bottom.setOnItemReselectedListener { }
        nav.addOnDestinationChangedListener { _, dest, _ ->
            val selected = when (dest.id) {
                R.id.home -> R.id.home
                R.id.catalog, R.id.categories -> R.id.catalog
                R.id.add_recipe -> R.id.add_recipe
                R.id.favorites -> R.id.favorites
                R.id.profile, R.id.my_recipes -> R.id.profile
                else -> null
            }
            bottom.visibility = if (selected == null) View.GONE else View.VISIBLE
            selected?.let { bottom.menu.findItem(it).isChecked = true }
        }
    }
}
