package com.dastanapps.poweroff.ui.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 11:18 PM
 *
 */

val ipv4Width = 40.dp
val fontSize = 14.sp

data class Ipv4(
    val ip1: MutableState<String>,
    val ip2: MutableState<String>,
    val ip3: MutableState<String>,
    val ip4: MutableState<String>,
) {

    override fun toString(): String {
        return "${ip1.value}.${ip2.value}.${ip3.value}.${ip4.value}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ipv4AddressField(
    ipv4: MutableState<Ipv4>,
) {
    Row(
        modifier = Modifier.wrapContentWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        TextFieldIpv4(ipv4 = ipv4.value.ip1)

        Text(text = ".", fontSize = 25.sp)

        TextFieldIpv4(ipv4 = ipv4.value.ip2)

        Text(text = ".", fontSize = 25.sp)

        TextFieldIpv4(ipv4 = ipv4.value.ip3)

        Text(text = ".", fontSize = 25.sp)

        TextFieldIpv4(ipv4 = ipv4.value.ip4)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldIpv4(
    ipv4: MutableState<String>,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    label: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    contentPadding: PaddingValues = PaddingValues(4.dp)
) {
    BasicTextField(
        value = ipv4.value,
        onValueChange = {
            if (it.isNotEmpty()) {
                ipv4.value = it
            } else {
                ipv4.value = ""
            }
        },
        modifier = if (label != null) {
            modifier
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp)
        } else {
            modifier
        },
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = TextStyle(
            textAlign = textAlign,
            fontSize = fontSize
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
            value = ipv4.value,
            label = label,
            visualTransformation = visualTransformation,
            innerTextField = innerTextField,
            singleLine = true,
            enabled = true,
            interactionSource = interactionSource,
            contentPadding = contentPadding
        )
    }
}

@Preview
@Composable
fun TextFieldPreview() {
    val ipv4State = remember {
        mutableStateOf(
            Ipv4(
                mutableStateOf("123"),
                mutableStateOf("123"),
                mutableStateOf("123"),
                mutableStateOf("123")
            )
        )
    }
    val ipv4e = remember { mutableStateOf("123") }
    Column {
        TextFieldIpv4(ipv4e)
        Ipv4AddressField(ipv4State)
    }
}