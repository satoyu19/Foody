package com.example.foody.ui.fragments.instructions

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.foody.R
import com.example.foody.databinding.FragmentInstructionsBinding
import com.example.foody.models.Result
import com.example.foody.util.Constants

class InstructionsFragment : Fragment() {

    private var _binding: FragmentInstructionsBinding? = null
    private val binding: FragmentInstructionsBinding
    get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstructionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //レシピ（Bundle）
        val args = arguments
        val myBundle: Result? = args?.getParcelable(Constants.RECIPES_RESULT)

        //HTTP接続を許可する必要あり。　→　ManifestのusesCleartextTraffic
        binding.instructionsWeb.webViewClient = object : WebViewClient() {
            //ローディング開始時に呼ばれる
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                binding.progress.visibility = View.VISIBLE
            }

            //ローディング終了時に呼ばれる
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                binding.progress.visibility = View.GONE
            }
        }
        val websiteUrl: String = myBundle!!.sourceUrl
        binding.instructionsWeb.loadUrl(websiteUrl) //指定されたURLを読み込む

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}