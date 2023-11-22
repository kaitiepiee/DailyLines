package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import MyViewHolder
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ArchiveEntryLayoutBinding


class MyAdapter(private val entryList: List<EntryModel>, private val viewNoteLauncher: ActivityResultLauncher<Intent>) : Adapter<MyViewHolder>() {
    companion object {
        const val titleKey : String = "TITLE_KEY"
        const val bodyKey : String = "BODY_KEY"
        const val dateKey : String = "DATE_KEY"
        const val imageKey : String = "IMAGE_KEY"
//        const val positionKey : String = "POSITION_KEY"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val archiveEntryBinding: ArchiveEntryLayoutBinding = ArchiveEntryLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(archiveEntryBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(entryList[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewNoteActivity::class.java)
//            intent.putExtra(positionKey, position)
            intent.putExtra(titleKey, entryList[position].title)
            intent.putExtra(bodyKey, entryList[position].body)
            intent.putExtra(dateKey, entryList[position].dateString)
            intent.putExtra(imageKey, entryList[position].image)
            viewNoteLauncher.launch(intent)
        }
    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}