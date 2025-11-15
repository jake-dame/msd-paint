package com.example.msdpaint.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.msdpaint.KtorClient
import com.example.msdpaint.MainActivity
import com.example.msdpaint.MsdPaintApplication
import com.example.msdpaint.R
import com.example.msdpaint.databinding.FragmentStudioBinding
import com.example.msdpaint.storage.Doodle
import com.example.msdpaint.viewmodels.Shape
import com.example.msdpaint.viewmodels.StorageViewModel
import com.example.msdpaint.viewmodels.StorageViewModelFactory
import com.example.msdpaint.viewmodels.StudioViewModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class StudioFragment : Fragment() {

    private lateinit var binding: FragmentStudioBinding
    private lateinit var currentDoodle: Doodle
    private val storageVM: StorageViewModel by viewModels {
        StorageViewModelFactory((requireActivity().application as MsdPaintApplication).doodleRepository)
    }
    private val studioVM: StudioViewModel by activityViewModels()
    private val client = KtorClient(MainActivity.GLOBAL_USER)
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentStudioBinding.inflate(inflater)

        setObservations()

        setOnClickListeners()

        binding.paper.setTouchFunction { x, y, action ->
            studioVM.handleTouch(x, y, action)
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.marbletime).apply {
            setVolume(1.0f, 1.0f)
            isLooping = true
        }

        studioVM.sensorSetUp(requireContext())

        return binding.root

    }

    private fun setObservations() {

        studioVM.brushColor.observe(viewLifecycleOwner) {
            binding.colorButton.setColorFilter(it, PorterDuff.Mode.SRC_IN)
        }

        studioVM.marbleOffset.observe(viewLifecycleOwner) {
            studioVM.drawMarblePath()
            binding.paper.invalidate()
        }

        studioVM.doodle.observe(viewLifecycleOwner) {
            currentDoodle = it
        }

        studioVM.bitmap.observe(viewLifecycleOwner) {
            binding.paper.updateDisplay(it)
        }
        studioVM.marbleTime.observe(viewLifecycleOwner) {
            if (studioVM.marbleTime.value == false && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }

        }

        studioVM.marbleOn.observe(viewLifecycleOwner) {
            if (it) {
                setButtonToSelectedBlue(binding.marbleButton)
            } else {
                binding.marbleButton.clearColorFilter()
            }
        }

    }

    private fun setOnClickListeners() {
        binding.backButton.setOnClickListener { findNavController().navigate(R.id.action_studioFragment_to_homeFragment) }
        binding.colorButton.setOnClickListener { showColorMenu() }
        binding.brushSizeButton.setOnClickListener { showBrushSizeMenu(it) }
        binding.marbleButton.setOnClickListener {

            // This plays music when in marble mode... disabling this until the user can be given an
            // option to turn it off themselves.
//            val audioManager =
//                requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            audioManager.setStreamVolume(
//                AudioManager.STREAM_MUSIC,
//                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
//                0
//            )
//            if (!mediaPlayer.isPlaying) {
//                mediaPlayer.start()
//            }

            studioVM.toggleMarbleTime()
            studioVM.toggleMarble()
        }
        binding.shapeButton.setOnClickListener { showShapeMenu(it) }
        binding.threeDotButton.setOnClickListener { showThreeDotMenu(it) }
    }

    private fun showColorMenu() {

        ColorPickerDialogBuilder
            .with(context)
            .setTitle("Color")
            .initialColor(Color.WHITE)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setPositiveButton(
                "OK"
            ) { _, selectedColor, _ -> studioVM.updateBrushColor(selectedColor) }
            .setNegativeButton(
                "CANCEL"
            ) { _, _ -> }
            .build()
            .show()

    }

    private fun showBrushSizeMenu(brushSizeButton: View) {

        val brushSizeMenuInflater = LayoutInflater.from(requireContext())
        val brushSizeMenu = brushSizeMenuInflater.inflate(R.layout.menu_brush_size, null)

        val popupWindow = PopupWindow(
            brushSizeMenu, // the View to pop up
            ViewGroup.LayoutParams.WRAP_CONTENT, // width of popup should just wrap view
            ViewGroup.LayoutParams.WRAP_CONTENT, // height of popup should just wrap view
            true
        )

        val seekBar = brushSizeMenu.findViewById<SeekBar>(R.id.seekBarVertical)
        val eraserButton = brushSizeMenu.findViewById<ImageView>(R.id.eraser)

        seekBar.min = StudioViewModel.BRUSH_SIZE_MIN.toInt()
        seekBar.max = StudioViewModel.BRUSH_SIZE_MAX.toInt()

        eraserButton.setOnClickListener {
            studioVM.toggleEraser()
            popupWindow.dismiss()
        }

        studioVM.eraserOn.observe(viewLifecycleOwner) {
            if (it) {
                setButtonToSelectedBlue(eraserButton)
            }
        }

        studioVM.brushSize.observe(viewLifecycleOwner) {
            seekBar.progress = it.toInt()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                studioVM.updateBrushSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                popupWindow.dismiss()
            }
        })

        val xOffset = -210 // -210 is a magic number -- eyeballed
        // Give showAsDropDown() a negative height so that it will set the origin of the dropdown
        // to be "above" the anchor view
        brushSizeMenu.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        // 50 is a magic number -- eyeballed
        val yOffset =
            -(brushSizeMenu.measuredHeight + brushSizeButton.height) - 50
        popupWindow.showAsDropDown(brushSizeButton, xOffset, yOffset)

    }

    private fun showShapeMenu(v: View) {

        val shapesMenuInflater = LayoutInflater.from(requireContext())
        val shapesMenu = shapesMenuInflater.inflate(R.layout.menu_shapes, null)

        val popupWindow = PopupWindow(
            shapesMenu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        studioVM.brushShape.observe(viewLifecycleOwner) {

            val pathButton = shapesMenu.findViewById<ImageView>(R.id.pathButton)
            val circleButton = shapesMenu.findViewById<ImageView>(R.id.circleButton)
            val squareButton = shapesMenu.findViewById<ImageView>(R.id.squareButton)

            when (it!!) {
                Shape.PATH -> setButtonToSelectedBlue(pathButton)
                Shape.CIRCLE -> setButtonToSelectedBlue(circleButton)
                Shape.SQUARE -> setButtonToSelectedBlue(squareButton)
            }

        }

        shapesMenu.findViewById<ImageView>(R.id.pathButton).setOnClickListener {
            studioVM.updateBrushShape(Shape.PATH)
            popupWindow.dismiss()
        }

        shapesMenu.findViewById<ImageView>(R.id.circleButton).setOnClickListener {
            studioVM.updateBrushShape(Shape.CIRCLE)
            popupWindow.dismiss()
        }

        shapesMenu.findViewById<ImageView>(R.id.squareButton).setOnClickListener {
            studioVM.updateBrushShape(Shape.SQUARE)
            popupWindow.dismiss()
        }

        shapesMenu.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = shapesMenu.measuredHeight

        val xOffset = 5
        val yOffset = -(popupHeight + v.height) - 50

        popupWindow.showAsDropDown(v, xOffset, yOffset)
    }

    private fun setButtonToSelectedBlue(v: View) {
        if (v is ImageView) {
            v.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.button_selected_blue),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun showThreeDotMenu(v: View) {

        val threeDotMenuInflater = LayoutInflater.from(requireContext())
        val threeDotMenu = threeDotMenuInflater.inflate(R.layout.menu_three_dot, null)
        val trashButton = threeDotMenu.findViewById<ImageView>(R.id.delete)
        val saveButton = threeDotMenu.findViewById<ImageView>(R.id.save)
        val shareButton = threeDotMenu.findViewById<ImageView>(R.id.share)

        val popupWindow = PopupWindow(
            threeDotMenu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        saveButton.setOnClickListener {
            saveDoodle()
            popupWindow.dismiss()
        }

        shareButton.setOnClickListener {
            if (!currentDoodle.isDefault()) {
                shareDoodle()
            } else {
                showUnsavedDialog()
            }
            popupWindow.dismiss()
        }

        trashButton.setOnClickListener {
            studioVM.clearBitmap()
            popupWindow.dismiss()
        }

        threeDotMenu.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = threeDotMenu.measuredHeight

        val xOffset = 5
        val yOffset = -(popupHeight + v.height) - 50

        popupWindow.showAsDropDown(v, xOffset, yOffset)

    }

    private fun showUnsavedDialog() {
        Toast.makeText(context, "You must save doodle first", Toast.LENGTH_SHORT).show()
    }

    private fun saveDoodle() {

        val inflaterr = LayoutInflater.from(requireContext())
        val view = inflaterr.inflate(R.layout.menu_save, null)
        val editText: EditText = view.findViewById(R.id.editText)

        if (currentDoodle.isDefault()) {
            editText.setText("")
        } else {
            editText.setText(currentDoodle.doodleName)
        }

        val dialogBox = AlertDialog.Builder(requireActivity())
        dialogBox.setTitle("Doodle Name")
            .setView(view)
            .setPositiveButton("SAVE") { dialog, _ ->
                val drawingName = editText.text.toString()
                val time = Date()
                time.toString()
                val path = storageVM.saveBitmapAsFile(drawingName, studioVM.bitmap.value)
                studioVM.updateDoodle(studioVM.bitmap.value!!, Doodle(drawingName, time, path))
                storageVM.addDoodle(drawingName, time, path)

                if (!MainActivity.GLOBAL_USER?.email.isNullOrEmpty()) {
                    lifecycleScope.launch() {

                        val response = client.backupLocal(
                            currentDoodle.path,
                            currentDoodle.doodleName,
                            currentDoodle.timestamp.time
                        )
                        if (response.status.value in 200..299) {
                            Toast.makeText(context, "successful response", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(context, "unsuccessful response", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ -> dialog.cancel() }

        dialogBox.create().show()

    }

    private fun shareDoodle() {

        val imageFile = File(currentDoodle.path)

        val uriToImage: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.msdpaint.fileprovider",
            imageFile
        )

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriToImage)
            type = "image/png"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        startActivity(shareIntent)

    }

    override fun onStop() {
        super.onStop()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

}
