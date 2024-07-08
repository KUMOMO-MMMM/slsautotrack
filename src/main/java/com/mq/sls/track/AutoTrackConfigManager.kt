package com.mq.sls.track

class AutoTrackConfigManager {
    companion object {
        private val targetFragmentClass: MutableSet<String> by lazy {
            mutableSetOf<String>().apply {
                /**
                 * For Android App Fragment
                 */
                add("android/app/Fragment")
                add("android/app/ListFragment")
                add("android/app/DialogFragment")

                /**
                 * For Support V4 Fragment
                 */
                add("android/support/v4/app/Fragment")
                add("android/support/v4/app/ListFragment")
                add("android/support/v4/app/DialogFragment")

                /**
                 * For AndroidX Fragment
                 */
                add("androidx/fragment/app/Fragment")
                add("androidx/fragment/app/ListFragment")
                add("androidx/fragment/app/DialogFragment")
                add("androidx/appcompat/app/AppCompatDialogFragment")

                add("com/google/android/material/bottomsheet/BottomSheetDialogFragment")
            }
        }

        fun isInstanceOfFragment(superName: String?): Boolean {
            return targetFragmentClass.contains(superName)
        }
    }
}

