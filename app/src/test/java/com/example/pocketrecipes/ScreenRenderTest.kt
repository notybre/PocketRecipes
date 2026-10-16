package com.example.pocketrecipes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.Shadows.shadowOf
import java.io.File
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenRenderTest {
    @Test fun renderHomeFormAndSettings(){
        val app=RuntimeEnvironment.getApplication() as PocketApp
        app.preferences.edit().putBoolean("isFirstLaunch",false).commit()
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        try{
            val a=controller.get()
            val host=a.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
            val deadline=System.currentTimeMillis()+15000
            while(CompletableFuture.supplyAsync{app.repository.dao.countRecipes()}.get()<8){
                check(System.currentTimeMillis()<deadline);Thread.sleep(50);shadowOf(Looper.getMainLooper()).idle()
            }
            repeat(3){Thread.sleep(50);shadowOf(Looper.getMainLooper()).idle()}
            val output=File("build/reports/previews").apply{mkdirs()}
            for((id,name) in listOf(R.id.home to "home",R.id.add_recipe to "form",R.id.settings to "settings",R.id.favorites to "favorites-empty")){
                if(host.navController.currentDestination?.id!=id)host.navController.navigate(id)
                shadowOf(Looper.getMainLooper()).idle();host.childFragmentManager.executePendingTransactions()
                repeat(3){Thread.sleep(50);shadowOf(Looper.getMainLooper()).idle()}
                val root=a.findViewById<View>(R.id.root)
                root.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1600,View.MeasureSpec.EXACTLY))
                root.layout(0,0,720,1600)
                shadowOf(Looper.getMainLooper()).idle()
                val bmp=Bitmap.createBitmap(720,1600,Bitmap.Config.ARGB_8888)
                root.draw(Canvas(bmp))
                File(output,"$name.png").outputStream().use{assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,100,it))}
                assertEquals(720,root.width);assertEquals(1600,root.height)
                bmp.recycle()
            }
        }finally{controller.pause().stop().destroy();app.repository.db.close()}
    }
}

