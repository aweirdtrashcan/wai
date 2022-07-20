package com.aweirdtrashcan.wai

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class FragmentFactory(private val latitude: Double, private val longitude: Double): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            MapsFragment::class.java -> {
                MapsFragment(latitude = latitude, longitude = longitude)
            } else -> {
                super.instantiate(classLoader, className)
            }
        }
    }
}