package com.example.foody.util

import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.foody.models.FoodRecipe

        /** データを一度だけ受け取る　**/
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer : Observer<T>) {
            //オーナーの寿命内でイベントをメインスレッドでディスパッチする。
    observe(lifecycleOwner, object: Observer<T> {

        override fun onChanged(t: T) {  //Observe　→　関数型インターフェース 値が変更した際に呼ばれる
            removeObserver(this)    //指定されたオブザーバーの削除
            observer.onChanged(t)
        }

    })
}

