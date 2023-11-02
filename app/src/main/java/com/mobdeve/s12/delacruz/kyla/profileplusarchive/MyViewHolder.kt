import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.EntryModel
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ArchiveEntryLayoutBinding

class MyViewHolder(private val archiveEntryBinding: ArchiveEntryLayoutBinding) : ViewHolder(archiveEntryBinding.root) {
    fun bindData(entryData: EntryModel) {
        archiveEntryBinding.entryTitleTv.text = entryData.title
        archiveEntryBinding.entryBodyTv.text = entryData.body.replace("\n", " ")
    }
}