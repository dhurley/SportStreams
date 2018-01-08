package net.djhurley.sportstreams.activities

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import net.djhurley.sportstreams.fragments.AceStreamPlayerFragment

/**
 * Created by DJHURLEY on 07/01/2018.
 */
class AceStreamPlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, AceStreamPlayerFragment())
                    .commit()
        }
    }
}