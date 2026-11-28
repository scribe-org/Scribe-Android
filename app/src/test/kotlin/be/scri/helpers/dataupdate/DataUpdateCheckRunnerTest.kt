// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException

class DataUpdateCheckRunnerTest {
    private class Harness(
        var enabled: Boolean = true,
        var batterySaver: Boolean = false,
        var canNotify: Boolean = true,
        var onCheck: suspend () -> DataUpdateCheckResult = { ok(emptyList()) },
        var onNotify: (List<String>) -> Unit = {},
    ) {
        var checks = 0
        val notified = mutableListOf<List<String>>()
        var nextSlotScheduled = 0
        var batterySaverScheduled = 0

        fun runner() =
            DataUpdateCheckRunner(
                isEnabled = { enabled },
                isBatterySaverOn = { batterySaver },
                canPostNotifications = { canNotify },
                check = {
                    checks++
                    onCheck()
                },
                notify = {
                    onNotify(it)
                    notified.add(it)
                },
                scheduleFollowUp = {
                    when (it) {
                        DataUpdateFollowUp.NEXT_SLOT -> nextSlotScheduled++
                        DataUpdateFollowUp.AFTER_BATTERY_SAVER -> batterySaverScheduled++
                    }
                },
            )
    }

    @Test
    fun foundUpdatesAreNotifiedAndTheNextMonthIsQueued() =
        runTest {
            val h = Harness(onCheck = { ok(listOf("fr", "de")) })

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(listOf(listOf("fr", "de")), h.notified)
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun nothingNewQueuesTheNextMonthWithoutANotification() =
        runTest {
            val h = Harness()

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(0, h.notified.size)
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun switchOffDoesNothing() =
        runTest {
            val h = Harness(enabled = false)

            val outcome = h.runner().run(userInitiated = true, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(0, h.checks)
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun allLanguagesFailingRetriesInsteadOfReportingSuccess() =
        runTest {
            val h = Harness(onCheck = { DataUpdateCheckResult(emptyList(), checkedCount = 2, failedCount = 2) })

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.RETRY, outcome)
            assertEquals(0, h.nextSlotScheduled)
            assertEquals(0, h.notified.size)
        }

    @Test
    fun partialFailureStillNotifiesAndQueuesTheNextMonth() =
        runTest {
            val h = Harness(onCheck = { DataUpdateCheckResult(listOf("fr"), checkedCount = 3, failedCount = 1) })

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(listOf(listOf("fr")), h.notified)
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun anUnexpectedExceptionIsHandledLikeAFailedCheck() =
        runTest {
            val h = Harness(onCheck = { throw IOException("boom") })

            assertEquals(DataUpdateRunOutcome.RETRY, h.runner().run(userInitiated = true, runAttemptCount = 0))
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun retriesStopAtTheCapAndFallThroughToTheNextMonth() =
        runTest {
            val h = Harness(onCheck = { DataUpdateCheckResult(emptyList(), checkedCount = 1, failedCount = 1) })
            val last = DataUpdateCheckRunner.MAX_ATTEMPTS - 1

            assertEquals(DataUpdateRunOutcome.RETRY, h.runner().run(userInitiated = true, runAttemptCount = last - 1))
            assertEquals(0, h.nextSlotScheduled)

            assertEquals(DataUpdateRunOutcome.DONE, h.runner().run(userInitiated = true, runAttemptCount = last))
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun theCapAppliesToScheduledRunsToo() =
        runTest {
            val h = Harness(onCheck = { throw IOException("offline") })

            val outcome =
                h.runner().run(userInitiated = false, runAttemptCount = DataUpdateCheckRunner.MAX_ATTEMPTS - 1)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun givingUpDoesNotQueueANextMonthIfTheSwitchWasTurnedOffMeanwhile() =
        runTest {
            val h = Harness()
            h.onCheck = {
                h.enabled = false
                throw IOException("offline")
            }

            val outcome =
                h.runner().run(userInitiated = true, runAttemptCount = DataUpdateCheckRunner.MAX_ATTEMPTS - 1)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun noNotificationIsPostedIfTheSwitchWasTurnedOffMeanwhile() =
        runTest {
            val h = Harness()
            h.onCheck = {
                h.enabled = false
                ok(listOf("fr"))
            }

            h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(0, h.notified.size)
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun scheduledRunWaitsOutBatterySaver() =
        runTest {
            val h = Harness(batterySaver = true)

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(0, h.checks)
            assertEquals(1, h.batterySaverScheduled)
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun runFromTheSwitchIgnoresBatterySaver() =
        runTest {
            val h = Harness(batterySaver = true)

            h.runner().run(userInitiated = true, runAttemptCount = 0)

            assertEquals(1, h.checks)
            assertEquals(0, h.batterySaverScheduled)
        }

    @Test
    fun withoutNotificationsTheNetworkIsNotUsed() =
        runTest {
            val h = Harness(canNotify = false)

            val outcome = h.runner().run(userInitiated = true, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(0, h.checks)
            assertEquals(0, h.nextSlotScheduled)
            assertEquals(0, h.batterySaverScheduled)
        }

    @Test
    fun aFailingNotificationDoesNotRepeatTheCheckOrLoseTheNextMonth() =
        runTest {
            val h =
                Harness(
                    onCheck = { ok(listOf("fr")) },
                    onNotify = { throw IllegalStateException("channel") },
                )

            val outcome = h.runner().run(userInitiated = false, runAttemptCount = 0)

            assertEquals(DataUpdateRunOutcome.DONE, outcome)
            assertEquals(1, h.checks)
            assertEquals(1, h.nextSlotScheduled)
        }

    @Test
    fun cancellationPropagatesAndSchedulesNothing() =
        runTest {
            val h = Harness(onCheck = { throw CancellationException("stopped") })

            assertThrows(CancellationException::class.java) {
                kotlinx.coroutines.runBlocking { h.runner().run(userInitiated = false, runAttemptCount = 0) }
            }
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun cancellationWhilePostingTheNotificationPropagates() =
        runTest {
            val h =
                Harness(
                    onCheck = { ok(listOf("fr")) },
                    onNotify = { throw CancellationException("stopped") },
                )

            assertThrows(CancellationException::class.java) {
                kotlinx.coroutines.runBlocking { h.runner().run(userInitiated = false, runAttemptCount = 0) }
            }
            assertEquals(0, h.nextSlotScheduled)
        }

    @Test
    fun attemptCapIsThree() {
        assertEquals(3, DataUpdateCheckRunner.MAX_ATTEMPTS)
    }

    private companion object {
        fun ok(updates: List<String>) = DataUpdateCheckResult(updates, checkedCount = 2, failedCount = 0)
    }
}
