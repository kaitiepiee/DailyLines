import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.AppPreferences
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.EntryModel
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.R
import com.mobdeve.s12.delacruz.kyla.profileplusarchive.databinding.ArchiveEntryLayoutBinding

/**
 * This class handles the view binding of the Archive screen.
 */
class MyViewHolder(val archiveEntryBinding: ArchiveEntryLayoutBinding) : ViewHolder(archiveEntryBinding.root) {

    fun bindData(entryData: EntryModel) {
        archiveEntryBinding.entryTitleTv.text = entryData.title
        archiveEntryBinding.entryBodyTv.text = entryData.body.replace("\n", " ")
    }
}