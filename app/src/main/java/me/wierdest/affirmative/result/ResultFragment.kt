package me.wierdest.affirmative.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.wierdest.affirmative.R
import me.wierdest.affirmative.database.MyViewModel
import me.wierdest.affirmative.databinding.FragmentResultBinding
import me.wierdest.affirmative.utilities.hideSupportBar

class ResultFragment : Fragment() {
    val TAG = javaClass.name
    private lateinit var b: FragmentResultBinding
    private lateinit var viewModel: MyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * Binding and viewModel
         */
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_reader, container, false)
        b.lifecycleOwner = this
        viewModel = ViewModelProvider(this.requireActivity()).get(MyViewModel::class.java)


        return b.root

    }

    override fun onResume() {
        super.onResume()
        hideSupportBar(true)
    }

    override fun onStop() {
        super.onStop()
        hideSupportBar(false)
    }
}