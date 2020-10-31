package me.wierdest.affirmative.title


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import me.wierdest.affirmative.Configuration.Companion.TEXT_TO_SPEECH_REQUEST_CODE
import me.wierdest.affirmative.R
import me.wierdest.affirmative.database.MyViewModel
import me.wierdest.affirmative.databinding.FragmentTitleBinding
import me.wierdest.affirmative.utilities.TypewriterTextView
import me.wierdest.affirmative.utilities.correctCEFRString
import me.wierdest.affirmative.utilities.setSupportBarTitle
import me.wierdest.affirmative.utilities.translateCEFRDescriptor
import java.lang.IllegalStateException
import java.util.*

class TitleFragment : Fragment(), TextToSpeech.OnInitListener {
    val TAG = javaClass.name
    private lateinit var b: FragmentTitleBinding
    private lateinit var viewModel: MyViewModel
    private lateinit var preferences: SharedPreferences
    private var assets: TypedArray? = null
    private var canNavigate: Boolean = false

    private lateinit var textToSpeech: TextToSpeech


    @Suppress("InlinedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * Binding and viewModel
         */
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_title, container,false)
        b.lifecycleOwner = this


        viewModel = ViewModelProvider(this.requireActivity()).get(MyViewModel::class.java)
        b.myViewModel = viewModel

        ViewCompat.setTransitionName(b.startButton, "shared_element_start_fab")

        /**
         * Text-To-Speech initialization
         */
        val checkTTSIntent = Intent().setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        startActivityForResult(checkTTSIntent, TEXT_TO_SPEECH_REQUEST_CODE)



        b.startButton.setOnClickListener {
            if (canNavigate) {
                b.settingsButton.hide()
                val extras = FragmentNavigatorExtras(b.startButton to "shared_element_end_fab")

//                val extras = ActivityNavigatorExtras(options)
                findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToReaderFragment(), extras)
            }

        }
        exitTransition = MaterialElevationScale(/* growing= */ false).apply {
            duration = 500
        }
//        exitTransition = Hold().apply {
//            duration = 1000
//        }
//        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
//            duration = 500
//        }

//        reenterTransition = MaterialElevationScale(/* growing= */ true)

        enterTransition = Hold().apply {
            duration = 1000
        }

        /**
         * Text Switchers
         */
        b.titleSwitcher.apply {
            outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
            setFactory {
                val textView = TypewriterTextView(context).apply {
                    textSize = 36F
                    gravity = Gravity.CENTER
                    typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.colorWhite, null))
                }
                textView
            }
        }


        /**
         * SeekBar Change Listener
         */

        b.levelSlider.setOnSeekBarChangeListener(LevelSlider(b))

        /**
         * Build library from assets if necessary
         */

        viewModel.snippetHandler.allSnippetsWithSnipsLive.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                Log.i(TAG, "read file asset")
                assets = resources.obtainTypedArray(R.array.text_assets)
                for (i in 0 until assets!!.length()) {
                    val file = assets!!.getString(i)?.let { f -> requireContext().assets.open("$f.txt") }?.bufferedReader().use { t ->
                        t?.readText()
                    }
                    file?.let { contents -> viewModel.snippetHandler.createSnippetFromAssetFile(contents, i == assets!!.length() - 1); Log.i(TAG, "read file asset") }
                }
            } else {
                if (!(it.any { snippet -> snippet.snips.isNullOrEmpty() })) {
                    Log.i(TAG, "processing assets has been completed!")
                    if (viewModel.snippetHandler.snippetToRead.value == null) {
                        setSupportBarTitle("Self affirmation helps learning.")
                        val textView = b.titleSwitcher.currentView as TypewriterTextView
                        textView.animateText(getString(R.string.frag_title_choose_instruction)) {
                            b.levelSlider.apply { progress = 0; visibility = View.VISIBLE }
                        }
                    }
                }
            }
        })

        viewModel.snippetHandler.readyToMakeSnips.observe(viewLifecycleOwner, Observer {
            it?.also {
                if (it) {
                    viewModel.snippetHandler.makeAllSnips()
                }
                viewModel.snippetHandler.resetReadyToMakeSnips()
            }
        })


        viewModel.snippetHandler.snippetToRead.observe(viewLifecycleOwner, Observer {
            canNavigate = it != null
            it?.apply {
                Log.i(TAG, "We've got a snippet to read: ${it.snippet.level} / ${it.snippet.name} / canNavigate: $canNavigate ")
                setSupportBarTitle("To practice:")

                b.apply {

                    icon.setImageResource(snippet.icon)

                    level.text = if (snippet.level.length != 2) snippet.level.correctCEFRString() else snippet.level

                    val textView = titleSwitcher.currentView as TypewriterTextView
                    textView.animateText(snippet.name) { titleLayout.transitionToEnd() }

                    keywords.setOnClickListener {
                        createKeywordsDialog(snippet.keywords.split(","))
                    }


                }
            }
        })

        b.level.setOnClickListener {
            createLevelDialog(b.level.text.toString().toUpperCase(Locale.ROOT))
        }



        return b.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TEXT_TO_SPEECH_REQUEST_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // User has the necessary data. Create TTS:
                textToSpeech = TextToSpeech(requireContext(), this)
            } else {
                // No data available. Install it
                val installTextToSpeech = Intent()
                installTextToSpeech.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(installTextToSpeech)

            }
        }
    }

    /**
     *  Setup TextToSpeech
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            if (textToSpeech.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
                textToSpeech.language = Locale.ENGLISH
            }
        } else {
            if (status == TextToSpeech.ERROR) {
                Toast.makeText(requireContext(), "Failed to Init Text-To-Speech!", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        assets?.recycle()
        textToSpeech.shutdown()
        viewModel.snippetHandler.resetSnippetToRead()
    }
    @Suppress("InlinedApi")
    private fun createLevelDialog(level: String) {
        val customLayout = View.inflate(context, R.layout.typewriter_dialog, null) as ConstraintLayout
        val switcher = customLayout.getViewById(R.id.levelDialogSwitcher) as TextSwitcher
        switcher.setFactory {
            val textView = TypewriterTextView(requireContext()).apply {
                textSize = 36F
                gravity = Gravity.CENTER
                typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
                setTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
            }
            textView

        }
        val alertDialog: AlertDialog? = activity?.let { val builder: AlertDialog.Builder = AlertDialog.Builder(it)
            builder.apply {
                setView(customLayout)
                setPositiveButton(R.string.dialog_button_ok) { _, _ -> }
                setNegativeButton(R.string.dialog_button_more) { _, _ -> }

            }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
        alertDialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, android.R.color.transparent, null)))
            setOnShowListener {
                val textView = switcher.currentView as TypewriterTextView
                val string = getString(R.string.dialog_level_basic_info, level, level.translateCEFRDescriptor())
                val styled = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY)
                textView.animateText(styled) { }
            }
        }
        alertDialog?.show()
    }


    private fun createKeywordsDialog(words: List<String>) {
        val customLayout = View.inflate(context, R.layout.snippet_keyword_dialog, null) as ConstraintLayout
        val chipGroup = customLayout.getViewById(R.id.chips) as ChipGroup
        words.forEach { keyword ->
            val chip = View.inflate(context, R.layout.snippet_keyword_chip, null) as Chip
            chip.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(keyword, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech.speak(keyword, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
            chip.text = keyword
            chipGroup.addView(chip)
        }
        val alertDialog: AlertDialog? = activity?.let { val builder: AlertDialog.Builder = AlertDialog.Builder(it)
            builder.apply {
                setView(customLayout)

            }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, android.R.color.transparent, null)))
        alertDialog?.show()
    }



}