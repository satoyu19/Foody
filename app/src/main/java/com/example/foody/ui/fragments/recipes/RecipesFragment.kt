package com.example.foody.ui.fragments.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foody.R
import com.example.foody.adapters.RecipesAdapter
import com.example.foody.databinding.FragmentRecipesBinding
import com.example.foody.databinding.RecipesBottomSheetBinding
import com.example.foody.models.FoodRecipe
import com.example.foody.util.NetworkResult
import com.example.foody.util.observeOnce
import com.example.foody.viewmodels.MainViewModel
import com.example.foody.viewmodels.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    private val args by navArgs<RecipesFragmentArgs>()

    //todo: 「private lateinit var~」ではなく、こうする理由とは？
//    private lateinit var binding: FragmentRecipesBinding
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

//    private lateinit var mainViewModel: MainViewModel
    private val mainViewModel: MainViewModel by viewModels()
    private val recipeViewModel by viewModels<RecipesViewModel>()

    private val mAdapter by lazy { RecipesAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

//        _binding = DataBindingUtil.inflate<FragmentRecipesBinding>(inflater, R.layout.recipes_bottom_sheet, container, false) エラーで落ちる。
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycleView()      //recycleViewをセットアップし、APIデータが取得されるまでShimmer効果をアクティブにする
        readDatabase()

        binding.recipesFab.setOnClickListener{
            findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null //メモリリークを回避
    }

    //RecycleViewセットアップ
    private fun setupRecycleView(){
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()
    }

    //データベースまたはリモートからレシピ情報を取得する
    private fun readDatabase() {
       lifecycleScope.launch {
           /** 監視期間を限定しているLiveDateの拡張関数を利用*/
           mainViewModel.readRecipes.observeOnce(viewLifecycleOwner) {  database ->
                ////データベースがからではなく、ボトムシートからのバックがfalse(デフォルト値はfalse、つまりボトムシートから戻っていないことを指す)であれば、データベースから読み取る
               if (database.isNotEmpty() && !args.backFromBottomSheet){
                   Log.d("RecipesFragment", "readDatabase called!!")

                   mAdapter.setData(database[0].foodRecipes)
                   hideShimmerEffect()
               } else {     //ボトムシートから戻っていれば、データストアに保存された新しいクエリを利用してAPI通信を行う
                   requestApiData()
               }
           }
       }
    }

    //Shimmer効果
    private fun showShimmerEffect() {
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.recyclerview.visibility = View.GONE
    }

    //Shimmer無効
     fun hideShimmerEffect() {
        binding.shimmerFrameLayout.stopShimmer()
        binding.shimmerFrameLayout.visibility = View.GONE
        binding.recyclerview.visibility = View.VISIBLE
    }

    /** Flowをactivityやfragmentで安全にcollectするためには、lifecycle scopeを使う必要*/
    private fun loadDataFromCache() {
        lifecycleScope.launch {
        mainViewModel.readRecipes.observe(viewLifecycleOwner) { database ->
            if (database.isNotEmpty()) {
                mAdapter.setData(database[0].foodRecipes)
            }
        }
        }
    }

    //APIリクエスト、データベースが空の場合に呼び出す
    private fun requestApiData(){
        mainViewModel.getRecipes(recipeViewModel.applyQueries())
        Log.d("RecipesFragment", "requestApiData called!!")
        //内部クラスを利用
        mainViewModel.recipesResponse.observe(viewLifecycleOwner, MyObserver())
    }

    /** なんとなくObserverの実装を内部クラスで行ってみた*/
    inner class MyObserver: Observer<NetworkResult<FoodRecipe>> {
        override fun onChanged(response: NetworkResult<FoodRecipe>?) {
            when(response){
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    //.dataはnullの可能性あり。
                    response.data?.let {
                        mAdapter.setData(it)
                    }
                }
                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }

                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    /** エラーを受け取った場合、古い情報を表示する*/
                    loadDataFromCache()
                    Toast.makeText(requireContext(), response.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}
