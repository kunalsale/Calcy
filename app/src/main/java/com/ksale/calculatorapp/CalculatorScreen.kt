package com.ksale.calculatorapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import com.ksale.calculatorapp.ui.theme.*

@Composable
fun Calculator(calculatorImpl: CalculatorImpl = CalculatorImpl()) {

    var inputState by remember { mutableStateOf("") }
    var outputState by remember { mutableStateOf("") }
    var parenthesisCount by remember { mutableStateOf(0) }
    var darkTheme by remember { mutableStateOf(false) }

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
        ) {
            val (clButtons, txtInput, txtOutput, rowModeToggle) = createRefs()

            CalculatorKeyboard(
                darkTheme = darkTheme,
                modifier = Modifier
                    .constrainAs(clButtons) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .wrapContentHeight()
                    .fillMaxWidth(),
                onClick = {
                    when (it) {
                        Constants.CLEAR_ALL -> {
                            inputState = ""
                            outputState = ""
                            parenthesisCount = 0
                        }
                        Constants.BACKSPACE -> {
                            if (inputState.isNotEmpty()) {
                                val lastElement = inputState[inputState.lastIndex]
                                if (lastElement == Constants.PARENTHESIS_OPEN) {
                                    parenthesisCount--
                                }

                                if (lastElement == Constants.PARENTHESIS_CLOSE) {
                                    parenthesisCount++
                                }
                                inputState = inputState.replaceRange(
                                    startIndex = inputState.length - 1,
                                    endIndex = inputState.length,
                                    ""
                                )
                            }
                        }
                        Constants.EQUALS -> {
                            var input = inputState
                            input = correctTheInput(input)
                            parenthesisCount = 0
                            for (element in input) {
                                if (element == Constants.PARENTHESIS_OPEN) {
                                    parenthesisCount++
                                }
                                if (element == Constants.PARENTHESIS_CLOSE) {
                                    parenthesisCount--
                                }
                            }
                            repeat(parenthesisCount) {
                                input = input.plus(Constants.PARENTHESIS_CLOSE)
                            }
                            if (input.isNotEmpty()) {
                                try {
                                    val output = calculatorImpl.calculateExpression(input)
                                    outputState = output.replace(".0", "")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        else -> {
                            // Handling of the edge cases

                            if (it == Constants.DOT) {
                                // 2 dots should not be consecutive if second dot is typed then return
                                if (inputState.isNotEmpty() && inputState[inputState.length - 1] == Constants.DOT) {
                                    return@CalculatorKeyboard
                                }
                            }

                            // If the typed key is for "X", "/", "+", "-" then
                            if (it == Constants.MULTIPLY_SIGN || it == Constants.DIVISION_SIGN || it == Constants.ADDITION_SIGN || it == Constants.SUBTRACTION_SIGN) {
                                if (inputState.isEmpty()) {
                                    if (it == Constants.SUBTRACTION_SIGN) {
                                        inputState += it
                                    }
                                    return@CalculatorKeyboard
                                }

                                // 2 Operators should be consecutive
                                // +- or -+ or X/ etc
                                val previousElement = inputState[inputState.length - 1]
                                if (previousElement == Constants.MULTIPLY_SIGN || previousElement == Constants.DIVISION_SIGN || previousElement == Constants.ADDITION_SIGN || previousElement == Constants.SUBTRACTION_SIGN || previousElement == Constants.PARENTHESIS_OPEN) {
                                    return@CalculatorKeyboard
                                }

                                // if the the operators are +- then replace +- with -
                                if (it == Constants.SUBTRACTION_SIGN) {
                                    if (previousElement == Constants.ADDITION_SIGN) {
                                        inputState = inputState.replaceRange(
                                            startIndex = inputState.length - 1,
                                            endIndex = inputState.length,
                                            "-"
                                        )
                                        return@CalculatorKeyboard
                                    }
                                }
                            }

                            // First character should not be ")"
                            if (it == Constants.PARENTHESIS_CLOSE && inputState.isEmpty()) {
                                return@CalculatorKeyboard
                            }

                            // "0.11 is not allowed it has to be 0.1"
                            if (inputState.length > 1) {
                                if (inputState[inputState.length - 2] == Constants.DOT && it.isDigit()) {
                                    return@CalculatorKeyboard
                                }
                            }

                            // If the character is "(" then increment the count
                            if (it == Constants.PARENTHESIS_OPEN) {
                                parenthesisCount++
                            }

                            // If the character is ")" then decerement the count
                            if (it == Constants.PARENTHESIS_CLOSE) {
                                if (parenthesisCount <= 0) {
                                    return@CalculatorKeyboard
                                }
                                val previousElement = inputState[inputState.lastIndex]
                                if (!(previousElement.isDigit() || previousElement == Constants.PARENTHESIS_CLOSE)) {
                                    return@CalculatorKeyboard
                                }
                                parenthesisCount--
                            }

                            if (outputState.isNotEmpty()) {
                                inputState = outputState
                                outputState = ""
                            }
                            inputState = inputState.plus(it)
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .constrainAs(rowModeToggle) {
                        top.linkTo(parent.top, margin = Dimens.MARGIN_LARGE)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .background(
                        shape = RoundedCornerShape(
                            topStart = CornerSize(Dimens.CORNER_LARGE),
                            topEnd = CornerSize(Dimens.CORNER_LARGE),
                            bottomStart = CornerSize(Dimens.CORNER_LARGE),
                            bottomEnd = CornerSize(Dimens.CORNER_LARGE)
                        ),
                        color = if (darkTheme) MediumGrey else OffWhite,
                    )
                    .wrapContentSize()
            ) {
                IconButton(onClick = { darkTheme = false }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_brightness),
                        contentDescription = "",
                        tint = if (darkTheme) Color.White else LightTextColor,
                        modifier = Modifier.padding(
                            start = Dimens.PADDING_LARGE,
                            end = Dimens.PADDING_MEDIUM,
                            top = Dimens.PADDING_MEDIUM,
                            bottom = Dimens.PADDING_MEDIUM
                        ),
                    )
                }

                IconButton(onClick = { darkTheme = true }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_night_mode),
                        contentDescription = "",
                        tint = if (darkTheme) Color.White else LightTextColor,
                        modifier = Modifier.padding(
                            start = Dimens.PADDING_MEDIUM,
                            end = Dimens.PADDING_LARGE,
                            top = Dimens.PADDING_MEDIUM,
                            bottom = Dimens.PADDING_MEDIUM
                        )
                    )
                }
            }

            BasicTextField(
                value = TextFieldValue(
                    text = outputState.trim(),
                    selection = TextRange(index = outputState.length)
                ),
                onValueChange = {
                },
                modifier = Modifier
                    .constrainAs(txtOutput) {
                        bottom.linkTo(clButtons.top)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(1f)
                    .padding(horizontal = Dimens.PADDING_XLARGE),
                textStyle = TextStyle(
                    color = if (darkTheme) Color.White else LightTextColor,
                    fontFamily = calibri_font_family,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (outputState.isNotEmpty()) Dimens.OUTPUT_TEXT_SIZE else Dimens.ZERO_TEXT_SIZE,
                    textAlign = TextAlign.Right
                ),
                singleLine = true,
                readOnly = true,
            )

            BasicTextField(
                value = TextFieldValue(
                    text = inputState.trim(),
                    selection = TextRange(index = inputState.length)
                ),
                onValueChange = {
                },
                modifier = Modifier
                    .constrainAs(txtInput) {
                        bottom.linkTo(txtOutput.top)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(1f)
                    .padding(horizontal = Dimens.PADDING_XLARGE),
                textStyle = TextStyle(
                    color = if (darkTheme) Color.White else LightTextColor,
                    fontFamily = calibri_font_family,
                    fontWeight = FontWeight.Bold,
                    fontSize = Dimens.INPUT_TEXT_SIZE,
                    textAlign = TextAlign.Right
                ),
                singleLine = true,
                readOnly = true,
            )
        }
    }

}

@Composable
fun CalculatorKeyboard(darkTheme: Boolean, onClick: (Char) -> Unit, modifier: Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .background(
                shape = RoundedCornerShape(
                    topStart = CornerSize(Dimens.CORNER_XLARGE),
                    topEnd = CornerSize(Dimens.CORNER_XLARGE),
                    bottomStart = CornerSize(Dimens.ZERO_DIMEN),
                    bottomEnd = CornerSize(Dimens.ZERO_DIMEN)
                ),
                color = if (darkTheme) LighterGrey else OffWhite
            )
            .wrapContentHeight(align = Alignment.Bottom)
    ) {
        val (row1, row2, row3, row4, row5) = createRefs()

        Row(
            modifier = Modifier
                .constrainAs(row1) {
                    bottom.linkTo(parent.bottom, margin = Dimens.MARGIN_LARGE)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = Dimens.PADDING_XLARGE),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundCornerButton(
                darkTheme = darkTheme,
                modifier = Modifier,
                onClick = {
                    onClick.invoke('B')
                }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_backspace),
                    contentDescription = "",
                    tint = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('0')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.ZERO,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.DOT)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = ".",
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.EQUALS)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "=",
                    color = LightCoral
                )
            }
        }

        Row(
            modifier = Modifier
                .constrainAs(row2) {
                    bottom.linkTo(row1.top, margin = Dimens.MARGIN_LARGE)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = Dimens.PADDING_XLARGE),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('1')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.ONE,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('2')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.TWO,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('3')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.THREE,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.ADDITION_SIGN)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "+",
                    color = LightCoral
                )
            }
        }

        Row(
            modifier = Modifier
                .constrainAs(row3) {
                    bottom.linkTo(row2.top, margin = Dimens.PADDING_LARGE)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = Dimens.PADDING_XLARGE),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('4')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.FOUR,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('5')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.FIVE,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('6')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.SIX,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.SUBTRACTION_SIGN)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "-",
                    color = LightCoral
                )
            }
        }


        Row(
            modifier = Modifier
                .constrainAs(row4) {
                    bottom.linkTo(row3.top, margin = Dimens.PADDING_LARGE)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .padding(horizontal = Dimens.PADDING_XLARGE),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('7')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.SEVEN,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('8')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.EIGHT,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke('9')
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = Constants.NINE,
                    color = if (darkTheme) Color.White else LightTextColor
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.MULTIPLY_SIGN)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "X",
                    color = LightCoral
                )
            }
        }

        Row(
            modifier = Modifier
                .constrainAs(row5) {
                    bottom.linkTo(row4.top, margin = Dimens.PADDING_LARGE)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top, margin = Dimens.MARGIN_XLARGE)
                }
                .fillMaxWidth()
                .padding(horizontal = Dimens.PADDING_XLARGE),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.CLEAR_ALL)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "AC",
                    color = Turqouise
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.PARENTHESIS_OPEN)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "(",
                    color = Turqouise
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.PARENTHESIS_CLOSE)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = ")",
                    color = Turqouise
                )
            }

            RoundCornerButton(
                darkTheme = darkTheme,
                onClick = {
                    onClick.invoke(Constants.DIVISION_SIGN)
                },
                modifier = Modifier
            ) {
                KeyboardText(
                    text = "/",
                    color = LightCoral
                )
            }
        }
    }
}

@Composable
fun RoundCornerButton(
    darkTheme: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(
            topStart = CornerSize(Dimens.CORNER_LARGE),
            topEnd = CornerSize(Dimens.CORNER_LARGE),
            bottomEnd = CornerSize(Dimens.CORNER_LARGE),
            bottomStart = CornerSize(Dimens.CORNER_LARGE)
        ),
        modifier = modifier
            .size(Dimens.KEY_SIZE),
        colors = ButtonDefaults.buttonColors(backgroundColor = if (darkTheme) MediumGrey else KeyLightColor),
        elevation = ButtonDefaults.elevation(defaultElevation = Dimens.ELEVATION_MEDIUM),
        content = content
    )
}

@Composable
fun KeyboardText(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = Dimens.KEY_TEXT_SIZE,
        fontWeight = FontWeight.Bold,
        fontFamily = calibri_font_family
    )
}

fun correctTheInput(expression: String): String {
    var modifiedExpression = expression
    while (modifiedExpression.isNotEmpty()) {
        val lastElement = modifiedExpression[modifiedExpression.lastIndex]
        if (lastElement.isDigit() || lastElement == Constants.PARENTHESIS_CLOSE) {
            return modifiedExpression
        } else {
            modifiedExpression = modifiedExpression.replaceRange(
                startIndex = modifiedExpression.length - 1,
                endIndex = modifiedExpression.length,
                ""
            )
        }
    }
    return modifiedExpression
}

val calibri_font_family = FontFamily(
    Font(R.font.calibri_bold, weight = FontWeight.Bold),
    Font(R.font.calibri_regular, weight = FontWeight.Normal),
    Font(R.font.calibri_italic, style = FontStyle.Italic)
)
