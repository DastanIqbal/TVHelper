package com.dastanapps.poweroff.wifi

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.databinding.DpadBinding
import com.dastanapps.poweroff.wifi.contracts.IDpadListener
import com.dastanapps.poweroff.wifi.dpad.DPadEvent

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:01 PM
 *
 */

class DPadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), View.OnClickListener {

    private val binding = DpadBinding.inflate(LayoutInflater.from(context), this, true)

    private var dpadListener: IDpadListener? = null

    init {
        binding.btnCenter.setOnClickListener(this)
        binding.btnL.setOnClickListener(this)
        binding.btnR.setOnClickListener(this)
        binding.btnT.setOnClickListener(this)
        binding.btnB.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_l -> dpadListener?.left(DPadEvent.LEFT)
            R.id.btn_r -> dpadListener?.left(DPadEvent.RIGHT)
            R.id.btn_t -> dpadListener?.left(DPadEvent.TOP)
            R.id.btn_b -> dpadListener?.left(DPadEvent.BOTTOM)
            R.id.btn_center -> dpadListener?.left(DPadEvent.CENTER)
        }
    }

    fun setOnDPadListener(listener: IDpadListener) {
        this.dpadListener = listener
    }
}