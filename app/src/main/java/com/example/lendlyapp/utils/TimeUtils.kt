enum class RentalTimeStatus {
    PLENTY, WARNING, CRITICAL
}

object TimeUtils {
    fun calculateRentalTimeStatus(startDate: Long, endDate: Long): RentalTimeStatus {
        val now = System.currentTimeMillis()
        val totalDuration = endDate - startDate
        val remainingTime = endDate - now

        // If rental period is 3 days or less
        if (totalDuration <= 3 * 24 * 60 * 60 * 1000) {
            return when {
                remainingTime <= 6 * 60 * 60 * 1000 -> RentalTimeStatus.CRITICAL  // Last 6 hours
                remainingTime <= 24 * 60 * 60 * 1000 -> RentalTimeStatus.WARNING  // Last day
                else -> RentalTimeStatus.PLENTY
            }
        }
        // For longer rental periods
        return when {
            remainingTime <= 24 * 60 * 60 * 1000 -> RentalTimeStatus.CRITICAL    // Last day
            remainingTime <= 2 * 24 * 60 * 60 * 1000 -> RentalTimeStatus.WARNING // Last 2 days
            else -> RentalTimeStatus.PLENTY
        }
    }
} 