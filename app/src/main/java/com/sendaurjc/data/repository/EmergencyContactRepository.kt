package com.sendaurjc.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class EmergencyContact(
    val id: String,
    val name: String,
    val email: String,
    val isSelected: Boolean = false
)

class EmergencyContactRepository(private val context: Context) {
    private val gson = Gson()
    private val fileName = "emergency_contacts.json"
    private val file get() = File(context.filesDir, fileName)

    fun getContacts(): List<EmergencyContact> {
        if (!file.exists()) {
            val initialContacts = listOf(
                EmergencyContact("contact1", "María López García", "m.lopez.2023@alumnos.urjc.es"),
                EmergencyContact("contact2", "Carlos Rodríguez Martín", "j.rodriguez.2015@urjc.es"),
                EmergencyContact("contact3", "Ana Fernández Ruiz", "a.fernandez.2021@alumnos.urjc.es")
            )
            saveContacts(initialContacts)
            return initialContacts
        }
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<EmergencyContact>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveContacts(contacts: List<EmergencyContact>) {
        val json = gson.toJson(contacts)
        file.writeText(json)
    }

    fun updateContactSelection(contactId: String, isSelected: Boolean) {
        val contacts = getContacts().map {
            when {
                it.id == contactId -> it.copy(isSelected = isSelected)
                isSelected -> it.copy(isSelected = false) // Si seleccionamos uno nuevo, deseleccionamos el resto
                else -> it
            }
        }
        saveContacts(contacts)
    }
    
    fun addContact(contact: EmergencyContact) {
        val contacts = getContacts().toMutableList()
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            contacts[index] = contact
        } else {
            contacts.add(contact)
        }
        saveContacts(contacts)
    }

    fun deleteContact(contactId: String) {
        val contacts = getContacts().filter { it.id != contactId }
        saveContacts(contacts)
    }
}
