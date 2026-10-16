package com.example.biztrackci.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_BUSINESS_NAME = stringPreferencesKey("business_name")
        val KEY_SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")

        const val ROLE_OWNER = "OWNER"
        const val ROLE_STAFF = "STAFF"

        const val SUBSCRIPTION_ACTIVE = "ACTIVE"
        const val SUBSCRIPTION_TRIAL = "TRIAL"
        const val SUBSCRIPTION_EXPIRED = "EXPIRED"

        const val DEFAULT_BUSINESS_NAME = "Boutique Abidjan Express"
    }

    open val userRole: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ROLE] ?: ROLE_OWNER
    }

    open val businessName: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BUSINESS_NAME] ?: DEFAULT_BUSINESS_NAME
    }

    open val subscriptionStatus: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_SUBSCRIPTION_STATUS] ?: SUBSCRIPTION_ACTIVE
    }

    open suspend fun updateRole(role: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ROLE] = role
        }
    }

    open suspend fun updateBusinessName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BUSINESS_NAME] = name
        }
    }

    open suspend fun updateSubscriptionStatus(status: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SUBSCRIPTION_STATUS] = status
        }
    }
}
