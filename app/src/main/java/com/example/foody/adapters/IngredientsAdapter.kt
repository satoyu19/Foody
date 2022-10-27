package com.example.foody.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.foody.R
import com.example.foody.databinding.IngredientsRowLayoutBinding
import com.example.foody.models.ExtendedIngredient
import com.example.foody.util.Constants.Companion.BASE_IMAGE_URL
import com.example.foody.util.RecipesDiffUtil
import java.util.*

class IngredientsAdapter: RecyclerView.Adapter<IngredientsAdapter.MyViewHolder>() {

    private var ingredientsList = emptyList<ExtendedIngredient>()

    class MyViewHolder(val binding: IngredientsRowLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(IngredientsRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.ingredientImageView.load(BASE_IMAGE_URL + ingredientsList[position].image) {         //画像の読み込み
            crossfade(600)  //クロスフェードアニメーション
            error(R.drawable.ic_error_placeholder)  //エラーの場合の画像
        }
        holder.binding.ingredientName.text = ingredientsList[position].name.replaceFirstChar {
            if (it.isLowerCase()) {     //小文字の場合
                it.titlecase(Locale.ROOT)   //タイトルケースにする
            } else {
                it.toString()
            }
        }
        holder.binding.ingredientAmount.text = ingredientsList[position].amount.toString()
        holder.binding.ingredientUnit.text = ingredientsList[position].unit
        holder.binding.ingredientConsistency.text = ingredientsList[position].consistency
        holder.binding.ingredientOriginal.text = ingredientsList[position].original

    }

    override fun getItemCount(): Int = ingredientsList.size

    //APIから新しいデータを取得するたびに追加する
    fun setData(newIngredients: List<ExtendedIngredient>){
        //古いリストと新しいリストの差分を計算する
        val ingredientsDiffUtil = RecipesDiffUtil(ingredientsList, newIngredients)
        val diffUtilResult = DiffUtil.calculateDiff(ingredientsDiffUtil)    //1 つのリストを別のリストに変換できる更新操作のリストを計算します。
        ingredientsList = newIngredients
        //DiffUtil.DiffResult →　呼び出しの結果に関する情報を保持する
        diffUtilResult.dispatchUpdatesTo(this)  //更新操作を受け取るコールバック。すべての更新を RecyclerView にディスパッチ
//        notifyDataSetChanged()      //データセットが変更されたことを登録済みオブザーバーに通知
    }
}