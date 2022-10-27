package com.example.foody.util

import androidx.recyclerview.widget.DiffUtil
import com.example.foody.models.Result

    //二つのリストを比較する
class RecipesDiffUtil<T>(private val oldList: List<T>, private val newList: List<T>): DiffUtil.Callback() {

    //古いリストのサイズを返します。
    override fun getOldListSize(): Int  = oldList.size
    //新しいリストのサイズを返します。
    override fun getNewListSize(): Int = newList.size

    //2 つのオブジェクトが同じアイテムを表しているかどうかを判断するために、DiffUtil によって呼び出されます。
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]    //=== →　参照比較
    }

    //2 つの項目が同じデータを持っているかどうかを確認する場合に、DiffUtil によって呼び出されます。
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}