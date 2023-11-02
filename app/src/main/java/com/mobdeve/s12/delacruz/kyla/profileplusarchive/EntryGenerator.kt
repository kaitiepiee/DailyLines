package com.mobdeve.s12.delacruz.kyla.profileplusarchive

class EntryGenerator {
    companion object {
        fun generateData() : ArrayList<EntryModel> {
            val entryList = ArrayList<EntryModel>()

            entryList.add(
                EntryModel(
                    "kyla",
                    "Shops in Robinsons Manila",
                    "Adidas\nBench\nConverse\nCotton On\nCrocs\nDairy Queen\nFigaro Coffee\nForever 21\nH&M\nJollibee",
                    "2023-10-20"
                )
            )
            entryList.add(
                EntryModel(
                    "kyla",
                    "This is my second entry for the day",
                    "I love talking ya know /s",
                    "2023-10-20"
                )
            )
            entryList.add(
                EntryModel(
                    "kyla",
                    "Just another test",
                    "Seeing if this works for reallll weeeooopppp",
                    "2023-10-20"
                )
            )
            entryList.add(
                EntryModel(
                    "kyla",
                    "This is WILD",
                    "Today is the 21st woop woop",
                    "2023-10-21"
                )
            )
            entryList.add(
                EntryModel(
                    "kyla",
                    "HAHAHHAAH very funny",
                    "I can't think of other things to say",
                    "2023-10-1"
                )
            )
            entryList.add(
                EntryModel(
                    "kyla",
                    "lol why he kinda *limp wrist*",
                    "omg but me tho slay",
                    "2023-10-31"
                )
            )
            entryList.add(
                    EntryModel(
                        "kyla",
                        "WEEEEEEE",
                        "I wanna stop working eheheh",
                        "2023-10-15"
                    )
            )


            return entryList
        }
    }
}