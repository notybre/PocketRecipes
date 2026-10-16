package com.example.pocketrecipes.ui
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketrecipes.R
import com.google.android.material.appbar.MaterialToolbar
abstract class ScreenFragment : Fragment(R.layout.fragment_screen) {
    protected val vm: PocketViewModel by activityViewModels()
    protected val content get() = requireView().findViewById<LinearLayout>(R.id.content)
    protected val toolbar get() = requireView().findViewById<MaterialToolbar>(R.id.toolbar)
    protected fun go(id:Int,args:Bundle?=null) = findNavController().navigate(id,args)
    protected fun details(id:Long) = go(R.id.details,bundleOf("recipeId" to id))
    protected fun art(dish:String,height:Int=194) {
        content.addView(FoodArtView(requireContext()).apply { this.dish=dish },
            LinearLayout.LayoutParams(-1,requireContext().dp(height)))
        content.space()
    }
    protected fun recipeList(parent:LinearLayout=content): RecipeAdapter {
        val adapter=RecipeAdapter(::details,vm::toggleFavorite)
        parent.addView(RecyclerView(requireContext()).apply {
            layoutManager=LinearLayoutManager(context);this.adapter=adapter
            isNestedScrollingEnabled=false;itemAnimator=null
        },LinearLayout.LayoutParams(-1,-2))
        return adapter
    }
    protected fun redraw(block:()->Unit) {
        val scroll=requireView().findViewById<NestedScrollView>(R.id.scroll);val y=scroll.scrollY
        content.removeAllViews();block();scroll.post{if(view!=null)scroll.scrollTo(0,y)}
    }
    override fun onViewCreated(view:View,savedInstanceState:Bundle?) {
        toolbar.title=findNavController().currentDestination?.label
        val top=findNavController().currentDestination?.id in listOf(R.id.home,R.id.catalog,R.id.favorites,R.id.profile,R.id.onboarding)
        if(!top) {
            toolbar.setNavigationIcon(R.drawable.ic_back);toolbar.navigationContentDescription="Назад"
            toolbar.setNavigationOnClickListener{if(!findNavController().popBackStack())go(R.id.home)}
        }
        vm.event.observe(viewLifecycleOwner){e->
            if(e!=null) {
                vm.event.value=null
                Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                e.destination?.let { target ->
                    val current=findNavController().currentDestination?.id
                    if(current==R.id.add_recipe||current==R.id.edit_recipe)
                        findNavController().navigate(target,null,navOptions{popUpTo(current!!){inclusive=true};launchSingleTop=true})
                    else if(current!=target) go(target)
                }
            }
        }
        build(savedInstanceState)
    }
    abstract fun build(saved:Bundle?)
}

