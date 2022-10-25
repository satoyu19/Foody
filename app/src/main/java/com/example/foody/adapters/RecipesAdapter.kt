package com.example.foody.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.databinding.RecipesRowLayoutBinding
import com.example.foody.models.FoodRecipe
import com.example.foody.models.Result
import com.example.foody.util.RecipesDiffUtil

//viewHolder →　1行分のView（ウィジェット）の参照を保持するもの
//adapter →  1行分のデータを1行分のViewに設定して生成するもの
class RecipesAdapter: RecyclerView.Adapter<RecipesAdapter.MyViewHolder>() {

    private var recipes = emptyList<Result>()

    class MyViewHolder(private val binding: RecipesRowLayoutBinding): RecyclerView.ViewHolder(binding.root) {
            fun bind(result: Result){
                binding.result = result
                binding.executePendingBindings()    //RecycleViewの時は必要？即時バインディング
            }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    //RecyclerView は、新しい ViewHolder を作成する必要があるときはいつでもこのメソッドを呼び出します。
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //親を利用し、インフレートする
        return MyViewHolder.from(parent)
    }

    //RecyclerViewは、ViewHolderをデータに関連付けるために、このメソッドを呼び出します
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {    //holderはonCreateViewHolderの戻り値？
        val currentRecipe = recipes[position]   //一つ分のレシピ情報 (models/Result)
        holder.bind(currentRecipe)
    }

    //RecyclerViewはデータセットのサイズを得るために、このメソッドを呼び出します
    override fun getItemCount(): Int = recipes.size


    //APIから新しいデータを取得するたびに追加する
    fun setData(newData: FoodRecipe){
        //古いリストと新しいリストの差分を計算する
        val recipesDiffUtil = RecipesDiffUtil(recipes, newData.results)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)    //1 つのリストを別のリストに変換できる更新操作のリストを計算します。
        recipes = newData.results
        //DiffUtil.DiffResult →　呼び出しの結果に関する情報を保持する
        diffUtilResult.dispatchUpdatesTo(this)  //更新操作を受け取るコールバック。すべての更新を RecyclerView にディスパッチ
//        notifyDataSetChanged()      //データセットが変更されたことを登録済みオブザーバーに通知
    }
}