package com.ksale.calculatorapp

import android.util.Log
import java.lang.NumberFormatException
import java.util.*
import kotlin.jvm.Throws

/*
* This class contains all the logic of calculator implementation
* It takes expression to be calculated and performs the following:
* 1. Replace the operators with the specific characters
* 2. Calculation of an expression follows PODMAS:
*    a. Parenthesis
*    b. Order
*    c. Multiplication, Division (anyone which is first from left to right)
*    d. Addition, Subtraction (anyone which is first from left to right)
* 3. So first we have to calculate the innermost parenthesis and then return the value of it.
* 4. This step is recursive as we have to bring all the operation to one level
* Example:
*     Expression: 3+(6-(3+1)+4)
*     1. Then first (3+1) will solved as all the operations are at one level and it is innermost
*     2. Expression becomes 3+(6-4+4)
*     3. Solve (6-4+4) which will result in 6 as first operation will be 6-4 = 2 and then 2+4 = 6
*     4. Last step is 3+6 which is 9
* */
class CalculatorImpl {

    @Throws(NumberFormatException::class)
    fun calculateExpression(expression: String): String {
        Log.i("CalculatorImpl", "expression $expression")
        return normalizeTheParenthesis(expression = prepareExpressionForCalculation(expression))
    }

    // It replaces the operations for calculation
    // Suppose if the expression is -3+5-2*(2/1) then it will convert the string into
    // N3A5S2M(2D1) for the calculation
    private fun prepareExpressionForCalculation(expression: String): String {
        val exp = StringBuilder(expression.length)
        for ((index, value) in expression.withIndex()) {
            when (value) {
                Constants.MULTIPLY_SIGN -> exp.append(Constants.MULTIPLY)
                Constants.DIVISION_SIGN -> exp.append(Constants.DIVISION)
                Constants.ADDITION_SIGN -> exp.append(Constants.ADDITION)
                Constants.SUBTRACTION_SIGN -> {
                    exp.append(
                        if (index == 0
                            || exp[index - 1] == Constants.PARENTHESIS_OPEN
                            || exp[index - 1] == Constants.MULTIPLY
                            || exp[index - 1] == Constants.DIVISION
                            || exp[index - 1] == Constants.ADDITION
                            || exp[index - 1] == Constants.PARENTHESIS_CLOSE
                        ) {
                            // It is negative value
                            Constants.NEGATIVE
                        } else {
                            // It is subtraction
                            Constants.SUBTRACTION
                        }
                    )
                }
                Constants.PARENTHESIS_OPEN -> {
                    if (index > 0 && exp[index - 1].isDigit()) {
                        exp.append(Constants.MULTIPLY)
                    }
                    exp.append(value)
                }
                Constants.PARENTHESIS_CLOSE -> {
                    exp.append(value)
                    if (index != expression.length - 1 && expression[index + 1].isDigit()) {
                        exp.append(Constants.MULTIPLY)
                    }
                }
                else -> exp.append(value)
            }
        }
        return exp.toString()
    }

    @Throws(NumberFormatException::class)
    private fun normalizeTheParenthesis(
        prefix: String = "",
        expression: String,
        suffix: String = ""
    ): String {
        val exp = prefix + expression + suffix
        var parenthesisStart = -1
        var parenthesisClose = -1
        for ((index, value) in exp.withIndex()) {
            if (value == Constants.PARENTHESIS_OPEN) {
                parenthesisStart = index
            }
            if (value == Constants.PARENTHESIS_CLOSE) {
                parenthesisClose = index
                break
            }
        }

        return if (parenthesisStart < parenthesisClose) {
            normalizeTheParenthesis(
                exp.subSequence(0, parenthesisStart).toString(),
                normalizeTheParenthesis(
                    expression = exp.subSequence(parenthesisStart + 1, parenthesisClose).toString()
                ),
                exp.subSequence(parenthesisClose + 1, exp.length).toString()
            )
        } else if (parenthesisStart == -1 && parenthesisClose == -1) {
            calculate(exp)
        } else {
            ""
        }.replace(Constants.NEGATIVE, Constants.SUBTRACTION_SIGN)
    }

    @Throws(NumberFormatException::class)
    private fun calculate(expression: String): String {
        var exp = expression
        while (true) {
            Log.i("CalculatorImpl", "exp $exp")
            // Get the index of all the operators in the expression
            val indexOfX = exp.indexOf(Constants.MULTIPLY)
            val indexOfDiv = exp.indexOf(Constants.DIVISION)
            val indexOfPlus = exp.indexOf(Constants.ADDITION)
            val indexOfMinus = exp.indexOf(Constants.SUBTRACTION)

            // And one by one check the following minimum index

            val operatorIndex: Int =
                if (((indexOfX < indexOfDiv) || indexOfDiv == -1) && indexOfX != -1) {
                    indexOfX
                } else if (((indexOfX > indexOfDiv) || indexOfX == -1) && indexOfDiv != -1) {
                    indexOfDiv
                } else if (((indexOfPlus < indexOfMinus) || indexOfMinus == -1) && indexOfPlus != -1) {
                    indexOfPlus
                } else if (((indexOfPlus > indexOfMinus) || indexOfPlus == -1) && indexOfMinus != -1) {
                    indexOfMinus
                } else {
                    break
                }

            val firstIndexOfExp = getFirstIndex(operatorIndex, exp)
            val endIndexOfExp = getEndIndex(operatorIndex, exp)
            val firstDigit =
                exp.subSequence(firstIndexOfExp, operatorIndex).toString()
                    .replace(Constants.NEGATIVE, Constants.SUBTRACTION_SIGN)
                    .format("%1f")
                    .toDouble()
                    .roundTo(1)
            val secondDigit =
                exp.subSequence(operatorIndex + 1, endIndexOfExp + 1).toString()
                    .replace(Constants.NEGATIVE, Constants.SUBTRACTION_SIGN)
                    .format("%1f")
                    .toDouble()
                    .roundTo(1)

            val operation: String = when (exp[operatorIndex]) {
                Constants.ADDITION -> firstDigit.plus(secondDigit)
                Constants.MULTIPLY -> firstDigit.times(secondDigit)
                Constants.DIVISION -> firstDigit.div(secondDigit)
                Constants.SUBTRACTION -> firstDigit.minus(secondDigit)
                else -> 0.0
            }.roundTo(1).toString()

            exp = exp.replaceRange(
                firstIndexOfExp, endIndexOfExp + 1,
                operation.replace(Constants.SUBTRACTION_SIGN, Constants.NEGATIVE)
            )
        }
        return exp.replace(Constants.SUBTRACTION_SIGN, Constants.NEGATIVE)
    }

    // This method is to find the start index of a number
    private fun getFirstIndex(indexOfOperator: Int, expression: String): Int {
        var start = indexOfOperator - 1
        while (start >= 0) {
            if (expression[start].isDigit()
                || expression[start] == Constants.NEGATIVE
                || expression[start] == Constants.DOT
            ) {
                start--
            } else {
                break
            }
        }
        return start + 1
    }

    // This method is to find the end index of a number
    private fun getEndIndex(indexOfOperator: Int, expression: String): Int {
        var end = indexOfOperator + 1
        while (end < expression.length) {
            if (expression[end].isDigit()
                || expression[end] == Constants.NEGATIVE
                || expression[end] == Constants.DOT
            ) {
                end++
            } else {
                break
            }
        }
        return end - 1
    }

    // Method to round off the digits of double to 1 precision
    private fun Double.roundTo(
        numFractionDigits: Int
    ) = "%.${numFractionDigits}f".format(this, Locale.ENGLISH).toDouble()
}