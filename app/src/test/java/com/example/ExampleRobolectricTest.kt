package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.model.Appointment
import com.example.data.repository.OrderLookupResult
import com.example.data.repository.ReceptionistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ReceptionistRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ReceptionistRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bangla AI", appName)
    }

    @Test
    fun `test initial database seeding and tenant isolation`() = runBlocking {
        DatabaseInitializer.seedInitialData(db)

        val orgs = repository.allOrganizations.first()
        assertTrue(orgs.isNotEmpty())
        val hospital = repository.getOrganization("org_carepoint")
        assertNotNull(hospital)
        assertEquals("CarePoint Hospital & Diagnostic", hospital?.name)

        // Verify Knowledge Base is scoped to org
        val carepointKnowledge = repository.getActiveKnowledgeDirect("org_carepoint")
        assertTrue(carepointKnowledge.isNotEmpty())
        assertTrue(carepointKnowledge.all { it.orgId == "org_carepoint" })
    }

    @Test
    fun `test order security verification requires correct phone digits`() = runBlocking {
        DatabaseInitializer.seedInitialData(db)

        // BD-8842 belongs to 01712343456
        val successResult = repository.verifyAndGetOrder("org_deshiwear", "BD-8842", "3456")
        assertTrue(successResult is OrderLookupResult.Success)

        // Unauthorized phone digits should be rejected
        val failedResult = repository.verifyAndGetOrder("org_deshiwear", "BD-8842", "9999")
        assertTrue(failedResult is OrderLookupResult.PhoneMismatch)
    }

    @Test
    fun `test appointment booking inserts confirmed record`() = runBlocking {
        DatabaseInitializer.seedInitialData(db)

        val newAppt = Appointment(
            orgId = "org_carepoint",
            customerName = "হাসান মাহমুদ",
            customerPhone = "01719988776",
            department = "কার্ডিওলজি",
            doctorOrStaff = "ডা. ফারহানা আহমেদ",
            appointmentDate = "২০২৬-১০-০৫",
            timeSlot = "সন্ধ্যা ৭:০০ PM"
        )

        val id = repository.bookAppointment(newAppt)
        assertTrue(id > 0)

        val appts = repository.getAppointmentsForOrg("org_carepoint").first()
        val created = appts.find { it.customerName == "হাসান মাহমুদ" }
        assertNotNull(created)
        assertEquals("CONFIRMED", created?.status)
    }
}
