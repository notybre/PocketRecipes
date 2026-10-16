package com.example.pocketrecipes.ui
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketrecipes.R
import com.example.pocketrecipes.data.ShoppingItem

class ShoppingListFragment:ScreenFragment() {
    override fun build(saved:Bundle?) {
        val summary=content.label("",14,true,R.color.pr_strong);content.space()
        val empty=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL};content.addView(empty)
        val adapter=ShoppingAdapter(
            {id,checked->vm.change({vm.repo.dao.setPurchased(id,checked)})},
            {id->vm.change({vm.repo.dao.deleteShopping(id)})})
        content.addView(RecyclerView(requireContext()).apply{
            layoutManager=LinearLayoutManager(context);this.adapter=adapter;isNestedScrollingEnabled=false;itemAnimator=null
        },LinearLayout.LayoutParams(-1,-2))
        content.heading("Добавить продукт")
        val name=content.field("Название продукта",saved?.getString("productName").orEmpty())
        val amount=content.field("Количество и единица",saved?.getString("productAmount").orEmpty())
        name.tag="productName";amount.tag="productAmount"
        content.button("Добавить продукт"){
            val title=name.editText?.text.toString().trim()
            val quantity=amount.editText?.text.toString().trim()
            name.error=if(title.isBlank())"Введите название продукта" else null
            amount.error=if(quantity.isBlank())"Укажите количество" else null
            if(title.isNotBlank()&&quantity.isNotBlank()){
                vm.repo.execute({vm.repo.dao.insertShopping(listOf(ShoppingItem().apply{this.name=title;this.amount=quantity}))}){result->
                    if(view!=null)result.fold({
                        name.editText?.setText("");amount.editText?.setText("")
                        Toast.makeText(requireContext(),"Продукт добавлен",Toast.LENGTH_SHORT).show()
                    },{Toast.makeText(requireContext(),"Не удалось добавить продукт",Toast.LENGTH_SHORT).show()})
                }
            }
        }
        val delete=content.button("Удалить купленные",true){vm.change({vm.repo.dao.deletePurchased()},"Купленные продукты удалены")}
        content.button("Добавить из рецепта",true){go(R.id.catalog)}
        vm.shopping.observe(viewLifecycleOwner){list->
            adapter.items=list
            summary.text="Куплено ${list.count{it.isPurchased}} из ${list.size}"
            delete.isEnabled=list.any{it.isPurchased}
            empty.removeAllViews()
            if(list.isEmpty())empty.empty("Список покупок пуст","Добавьте продукты вручную или из выбранного рецепта.",R.drawable.ic_cart)
        }
    }
    override fun onSaveInstanceState(outState:Bundle) {
        view?.findViewWithTag<com.google.android.material.textfield.TextInputLayout>("productName")?.let{outState.putString("productName",it.editText?.text.toString())}
        view?.findViewWithTag<com.google.android.material.textfield.TextInputLayout>("productAmount")?.let{outState.putString("productAmount",it.editText?.text.toString())}
        super.onSaveInstanceState(outState)
    }
}
private class ShoppingAdapter(val check:(Long,Boolean)->Unit,val delete:(Long)->Unit):RecyclerView.Adapter<ShoppingAdapter.Holder>(){
    var items:List<ShoppingItem> = emptyList()
        set(value){field=value;notifyDataSetChanged()}
    class Holder(val row:LinearLayout,val checkbox:CheckBox,val text:TextView,val remove:ImageButton):RecyclerView.ViewHolder(row)
    override fun getItemCount()=items.size
    override fun onCreateViewHolder(parent:ViewGroup,type:Int):Holder {
        val c=parent.context
        val row=LinearLayout(c).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            setPadding(c.dp(8),c.dp(8),c.dp(4),c.dp(8));roundBackground(c.color(R.color.pr_surface))
            layoutParams=RecyclerView.LayoutParams(-1,-2).apply{bottomMargin=c.dp(8)}}
        val check=CheckBox(c).apply{buttonTintList=ColorStateList.valueOf(c.color(R.color.pr_strong))}
        row.addView(check,LinearLayout.LayoutParams(c.dp(48),c.dp(48)))
        val text=TextView(c).apply{textSize=16f;setTextColor(c.color(R.color.pr_text))}
        row.addView(text,LinearLayout.LayoutParams(0,-2,1f))
        val remove=ImageButton(c).apply{setImageResource(R.drawable.ic_trash);setBackgroundColor(android.graphics.Color.TRANSPARENT)
            imageTintList=ColorStateList.valueOf(c.color(R.color.pr_muted));setPadding(c.dp(12),c.dp(12),c.dp(12),c.dp(12))}
        row.addView(remove,LinearLayout.LayoutParams(c.dp(48),c.dp(48)))
        return Holder(row,check,text,remove)
    }
    override fun onBindViewHolder(h:Holder,pos:Int){
        val i=items[pos]
        h.text.text="${i.name}\n${i.amount} ${i.unit}".trim()
        h.text.paintFlags=if(i.isPurchased)h.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else h.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        h.text.alpha=if(i.isPurchased).6f else 1f
        h.checkbox.setOnCheckedChangeListener(null);h.checkbox.isChecked=i.isPurchased
        h.checkbox.contentDescription="Куплено: ${i.name}"
        h.checkbox.setOnCheckedChangeListener{_,checked->check(i.id,checked)}
        h.remove.contentDescription="Удалить продукт: ${i.name}";h.remove.setOnClickListener{delete(i.id)}
    }
}

