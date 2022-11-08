package com.example.foody.adapters

import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.R
import com.example.foody.data.database.entities.FavoritesEntity
import com.example.foody.databinding.FavoriteRecipesRowLayoutBinding
import com.example.foody.ui.fragments.favorites.FavoriteRecipesFragmentDirections
import com.example.foody.util.RecipesDiffUtil
import com.example.foody.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar

/** ActionMode.Callback →　ユーザーがアクション ボタンをクリックしたことを報告するために呼び出される。**/
class FavoriteRecipesAdapter constructor(private val requireActivity: FragmentActivity, private val mainViewModel: MainViewModel) :
    RecyclerView.Adapter<FavoriteRecipesAdapter.MyViewHolder>(), ActionMode.Callback {

    private var favoriteRecipes = emptyList<FavoritesEntity>()

    private lateinit var mActionMode: ActionMode
    private lateinit var rootView: View

    private var multiSelection = false
    private var selectedRecipes = arrayListOf<FavoritesEntity>()  //選択されたレシピ群
    private var myViewHolders = arrayListOf<MyViewHolder>()


    class MyViewHolder(val binding: FavoriteRecipesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //onBindViewHolderで呼び出される
        fun bind(favoritesEntity: FavoritesEntity) {
            binding.favoritesEntity = favoritesEntity
            binding.executePendingBindings()    //RecycleViewの時は必要？即時バインディング
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FavoriteRecipesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        myViewHolders.add(holder)
        rootView = holder.itemView.rootView

        val currentRecipe = favoriteRecipes[position]
        holder.bind(currentRecipe)

        /** クリック時、詳細画面への遷移 **/
        holder.binding.favoriteRecipesRowLayout.setOnClickListener {
            if (multiSelection) {   //アクションモードだったら
                applySelection(holder, currentRecipe)   //クリックされたホルダーとレシピを引数に処理
            } else {
                val action = FavoriteRecipesFragmentDirections.actionFavoriteRecipesFragmentToDetailActivity(currentRecipe.result)
                holder.itemView.findNavController().navigate(action)
            }
        }

        /** 長押し **/
        holder.binding.favoriteRecipesRowLayout.setOnLongClickListener() {
            if (!multiSelection) {
                multiSelection = true
                requireActivity.startActionMode(this)
                applySelection(holder, currentRecipe)
                true
            } else {
                applySelection(holder, currentRecipe)
                true
            }
        }
    }

    override fun getItemCount(): Int = favoriteRecipes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    //アクションモードで表示するメニューを最初に作成するときに呼び出される
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.favorites_contextual_menu, menu)
        mActionMode = mode!!
        applyStatusBarColor(R.color.contextualStatusBarColor)
        return true
    }

    //アクションモードが無効になるたびに、アクションモードのメニュー表示を更新するときに呼び出される。
    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    //アクションモードに表示されたメニューをクリックしたときに呼び出される。(お気に入りレシピの削除を行う)
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//        if (item?.itemId == R.id.delete_favorite_recipe_menu) {
//            if (selectedRecipes.size == myViewHolders.size) {
//                mainViewModel.deleteAllFavoriteRecipes()
//            } else {
//                selectedRecipes.forEach { recipe ->
//                    mainViewModel.deleteFavoriteRecipes(recipe)
//                }
//            }
//        }

        if (item?.itemId ==R.id.delete_favorite_recipe_menu) {
            selectedRecipes.forEach {
                mainViewModel.deleteFavoriteRecipes(it)
            }
            showSnackBar("${selectedRecipes.size} Recipe/s removed.")
        }
        multiSelection = false
        selectedRecipes.clear()
        mode?.finish()
        return true
    }

    // アクションモードから抜けるとき、または破棄するときに呼び出される。
    override fun onDestroyActionMode(mode: ActionMode?) {
        myViewHolders.forEach { holder ->
            changeRecipeStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        }
        multiSelection = false
        selectedRecipes.clear()
        applyStatusBarColor(R.color.statusBarColor)
    }

    private fun applyStatusBarColor(color: Int) {
        requireActivity.window.statusBarColor = ContextCompat.getColor(requireActivity, color)
    }

        //アクションモードで選択されたレシピの追加・削除
        private fun applySelection(holder: MyViewHolder, currentRecipe: FavoritesEntity) {
            if (selectedRecipes.contains(currentRecipe)) {
                selectedRecipes.remove(currentRecipe)
                changeRecipeStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
                applyActionModeTitle()
            } else {
                selectedRecipes.add(currentRecipe)
                changeRecipeStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
                applyActionModeTitle()
            }
        }

        //ActionModeの無効化とTitleの変更
    private fun applyActionModeTitle() {
        when(selectedRecipes.size) {
            0 -> { mActionMode.finish() }   //選択しているレシピがなくなったらActionModeを終了する
            1 -> { mActionMode.title = "${selectedRecipes.size} item selected" }
            else -> { mActionMode.title = "${selectedRecipes.size} items selected" }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).setAction("Okay"){}.show()
    }

    //アクションモード時に選択されたレシピのstyleを変更するメソッド
    private fun changeRecipeStyle(holder: MyViewHolder, backgroundColor: Int, strokeColor: Int) {
        holder.binding.favoriteRecipesRowLayout.setBackgroundColor(
            ContextCompat.getColor(requireActivity, backgroundColor)
        )
        holder.binding.favoriteRowCardView.strokeColor =
            ContextCompat.getColor(requireActivity, strokeColor)
    }

    //todo: 呼び出される場所に注意
    //RecyclerViewのデータ更新を処理する
    fun setData(newFavoriteRecipes: List<FavoritesEntity>) {
        val recipesDiffUtil = RecipesDiffUtil(favoriteRecipes, newFavoriteRecipes)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
        favoriteRecipes = newFavoriteRecipes
        diffUtilResult.dispatchUpdatesTo(this)
    }

        //todo: コードの意味は？
    fun clearContextualActionMode() {
            /**
             * isInitialized →　あるクラスのlateinit変数が初期化済みかどうかを調べる(プロパティ参照の仕組み(kotlinのリフレクション？))
             * **/
        if (this::mActionMode.isInitialized) {
            mActionMode.finish()
        }
    }
}

