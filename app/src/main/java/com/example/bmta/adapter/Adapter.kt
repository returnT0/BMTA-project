package com.example.bmta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bmta.databinding.ItemBinding
import com.example.bmta.model.Note
import com.example.bmta.utils.TextFormatter
import com.example.bmta.utils.INote

class Adapter(private val itemList : ArrayList<Note>, private val listener: INote) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(val binding : ItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(itemList[position]){
                binding.textView.text = this.text
                binding.tvDate.text = TextFormatter.fromDate(date)
                binding.root.setOnClickListener {
                    listener.onClick(this, position, binding)
                }
                binding.root.setOnLongClickListener {
                    listener.onLongClick(this, position, binding)
                    true
                }
            }
        }
    }
    override fun getItemCount() = itemList.size

    fun addAll(it : List<Note>) = itemList.addAll(it)
}