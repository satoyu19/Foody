package com.example.foody.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.navArgs
import com.example.foody.R
import com.example.foody.adapters.PagerAdapter
import com.example.foody.data.database.entities.FavoritesEntity
import com.example.foody.databinding.ActivityDetailBinding
import com.example.foody.ui.fragments.ingredients.IngredientsFragment
import com.example.foody.ui.fragments.instructions.InstructionsFragment
import com.example.foody.ui.fragments.overview.OverviewFragment
import com.example.foody.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private val args by navArgs<DetailActivityArgs>()
    private val mainViewModel: MainViewModel by viewModels()

    private var recipeSaved = false //テーブルに保存されているか
    private var savedRecipeId = 0   //レシピid,削除時使用

    //Activityはリソースの解放をしなくても大丈夫？
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))  //R.color.whiteはIntであるため、そのまま指定しない。
        supportActionBar?.setDisplayHomeAsUpEnabled(true)     //ツールバーにバックボタンを追加

        val fragments = ArrayList<Fragment>()
        fragments.add(OverviewFragment())
        fragments.add(IngredientsFragment())
        fragments.add(InstructionsFragment())

        val titles = ArrayList<String>()
        titles.add("Overview")
        titles.add("Ingredients")
        titles.add("Instructions")

        val resultBundle = Bundle() //文字列キーからさまざまなParcelable値へのマッピング。

        resultBundle.putParcelable("recipeBundle", args.result)

        val pagerAdapter = PagerAdapter(
            resultBundle,
            fragments,
            this
        )

        binding.viewPager2.isUserInputEnabled = false    //ユーザーによるスクロールを無効化
        binding.viewPager2.apply { adapter = pagerAdapter }

            //TabLayout と ViewPager2 をリンク
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = titles[position]
        }.attach()

    }

        //オプションメニューの作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        val menuItem = menu?.findItem(R.id.save_to_favorites_menu)
        checkSaveRecipes(menuItem!!)
        return true
    }

    //ツールバーの押されたアイテムで挙動を決める
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.save_to_favorites_menu && !recipeSaved) {
            saveToFavorites(item)
        } else if (item.itemId == R.id.save_to_favorites_menu && recipeSaved) {
            removeFromFavorites(item)
        }

        return super.onOptionsItemSelected(item)
    }

        //お気に入りレシピ一覧のidと詳細画面のレシピのidを比較し、登録済みかを検証し、スターアイコンの配色を決める
    private fun checkSaveRecipes(menuItem: MenuItem) {
            changeMenuItemColor(menuItem, R.color.white)
            mainViewModel.readFavoriteRecipes.observe(this) { favoritesEntity ->
            try {
                for (saveRecipe in favoritesEntity) {
                    if (saveRecipe.result.recipeId == args.result.recipeId) {
                        changeMenuItemColor(menuItem, R.color.yellow)
                        savedRecipeId = saveRecipe.id   //テーブルにあればそのレシピのidを保持
                        recipeSaved = true  //保存状態に変更
                    }
                }
            } catch (e: Exception) {
                Log.d("DetailsActivity", e.message.toString())
            }
        }
    }

        //エンティティ挿入
    private fun saveToFavorites(item: MenuItem) {
        val favoritesEntity = FavoritesEntity(0, args.result)   //挿入データ,0を開始として勝手にインクリメントしてくれる？
        mainViewModel.insertFavoriteRecipes(favoritesEntity)
        changeMenuItemColor(item, R.color.yellow)
        showSnackBar("Recipe saved.")
            recipeSaved = true
    }

        //お気に入り削除
    private fun removeFromFavorites(item: MenuItem) {
        val favoriteEntity = FavoritesEntity(savedRecipeId, args.result)
        mainViewModel.deleteFavoriteRecipes(favoriteEntity)
        changeMenuItemColor(item, R.color.white)
            showSnackBar("Removed from Favorites.")
            recipeSaved = false
    }

    //スナックバーの表示
    private fun showSnackBar(message: String) {
        Snackbar.make(binding.detailsLayout, message, Snackbar.LENGTH_SHORT).setAction("Okay"){}.show()
    }

    //スターアイコン色変更
    private fun changeMenuItemColor(item: MenuItem, color: Int) {
        item.icon.setTint(ContextCompat.getColor(this, color))

    }
}