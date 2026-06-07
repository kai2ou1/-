package com.parking.billing

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.ceil

/**
 * 停车费用计算结果明细
 */
data class FeeBreakdown(
    val totalHours: Double,
    val displayHours: Int,       // 向上取整后的收费小时数
    val isFree: Boolean,
    val baseFee: Double,         // 基础费用（4小时内）
    val extraHours: Int,         // 超时小时数
    val extraFee: Double,        // 超时费用
    val isCapped: Boolean,
    val totalFee: Double,        // 最终应收
    val cappedAt: Double = 20.0  // 封顶价
)

/**
 * 停车计费计算器
 *
 * 收费规则:
 * - 1小时内免费
 * - 4小时内收费5元
 * - 超过4小时后，每增加1小时增加2元（不足1小时按1小时算）
 * - 24小时内封顶价20元
 */
object ParkingCalculator {

    private const val FREE_HOURS = 1.0        // 免费时长（小时）
    private const val BASE_HOURS = 4.0         // 基础计费时长（小时）
    private const val BASE_FEE = 5.0           // 基础费用（元）
    private const val EXTRA_FEE_PER_HOUR = 2.0 // 超时每小时加收（元）
    private const val MAX_FEE = 20.0           // 封顶价（元）

    /**
     * 根据入场时间和出场时间计算费用
     *
     * @param entryTime 入场时间
     * @param exitTime 出场时间
     * @return FeeBreakdown 费用明细
     * @throws IllegalArgumentException 如果出场时间早于入场时间
     */
    fun calculate(entryTime: LocalDateTime, exitTime: LocalDateTime): FeeBreakdown {
        require(exitTime.isAfter(entryTime) || exitTime.isEqual(entryTime)) {
            "出场时间必须晚于或等于入场时间"
        }

        // 计算总时长（分钟），然后向上取整到小时
        val durationMinutes = Duration.between(entryTime, exitTime).toMinutes()
        val totalHours = durationMinutes / 60.0

        // 向上取整收费小时数（不足1小时按1小时算）
        val chargeableHours = if (durationMinutes == 0L) {
            0
        } else {
            ceil(totalHours).toInt()
        }

        // 有空闲时间分钟数
        val minutesRemainder = (durationMinutes % 60).toInt()

        return calculateFee(chargeableHours, totalHours, minutesRemainder)
    }

    /**
     * 根据收费小时数计算费用
     */
    private fun calculateFee(
        chargeableHours: Int,
        actualHours: Double,
        minutesRemainder: Int
    ): FeeBreakdown {
        // 1小时以内免费
        if (chargeableHours <= FREE_HOURS.toInt()) {
            return FeeBreakdown(
                totalHours = actualHours,
                displayHours = if (chargeableHours == 0 && minutesRemainder > 0) 1 else chargeableHours,
                isFree = true,
                baseFee = 0.0,
                extraHours = 0,
                extraFee = 0.0,
                isCapped = false,
                totalFee = 0.0
            )
        }

        // 基础费用（4小时内）
        val baseFee = BASE_FEE

        // 计算超时部分
        if (chargeableHours <= BASE_HOURS.toInt()) {
            return FeeBreakdown(
                totalHours = actualHours,
                displayHours = chargeableHours,
                isFree = false,
                baseFee = baseFee,
                extraHours = 0,
                extraFee = 0.0,
                isCapped = false,
                totalFee = baseFee
            )
        }

        // 超过4小时
        val extraHours = chargeableHours - BASE_HOURS.toInt()
        val extraFee = extraHours * EXTRA_FEE_PER_HOUR
        var totalFee = baseFee + extraFee

        // 封顶
        val isCapped = totalFee > MAX_FEE
        if (isCapped) {
            totalFee = MAX_FEE
        }

        return FeeBreakdown(
            totalHours = actualHours,
            displayHours = chargeableHours,
            isFree = false,
            baseFee = baseFee,
            extraHours = extraHours,
            extraFee = extraFee,
            isCapped = isCapped,
            totalFee = totalFee
        )
    }

    /**
     * 获取费用描述的格式化文本
     */
    fun formatFeeDescription(breakdown: FeeBreakdown): String {
        return when {
            breakdown.isFree -> "1小时内免费停车"
            breakdown.isCapped -> "24小时内封顶价${breakdown.cappedAt.toInt()}元"
            breakdown.extraHours == 0 -> "4小时内收费${breakdown.baseFee.toInt()}元"
            else -> buildString {
                append("基础${breakdown.baseFee.toInt()}元")
                append(" + ")
                append("超时${breakdown.extraHours}小时×${EXTRA_FEE_PER_HOUR.toInt()}元")
            }
        }
    }
}
