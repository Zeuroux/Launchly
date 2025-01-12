package com.zeuroux.launchly.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import java.io.Serializable

class VersionDatabaseViewModel: ViewModel() {
    private val _downloadedVersions = mutableStateListOf<SavedVersionData>()
    val downloadedVersions: SnapshotStateList<SavedVersionData> get() = _downloadedVersions
    fun loadSavedVersions(context: Context) {
        val versionDbHelper = VersionDatabaseHelper(context)
        _downloadedVersions.clear()
        _downloadedVersions.addAll(versionDbHelper.getSavedVersions())
    }

    fun getIndexById(id: String): Int {
        return downloadedVersions.indexOfFirst { it.installationId == id }
    }
}

data class SavedVersionData(
    val installationId: String,
    val name: String,
    val versionCode: String,
    val versionName: String,
    val versionType: VersionType,
    val architecture: Architecture,
    val status: Status,
    val patches: List<Patch>,
    val customIcon: Boolean,
    val dateModified: Long
) : Serializable

enum class Architecture(val arch: String) {
    ARM64("arm64-v8a"),
    ARM("armeabi-v7a"),
    X86("x86"),
    X86_64("x86_64"),
    UNKNOWN("unknown");
    companion object {
        fun fromString(arch: String): Architecture {
            return entries.firstOrNull { it.arch == arch } ?: UNKNOWN
        }
    }
}


enum class VersionType {
    RELEASE,
    BETA,
    PREMIUM,
    UNKNOWN
}

enum class Status {
    INSTALLED,
    DOWNLOADING,
    NOT_INSTALLED,
    NOT_DOWNLOADED
}

enum class Patch(val patch: String) {
    EXTERNAL_STORAGE("ExternalStorage"),
    MATERIALBINLOADER2("MaterialBinLoader2"),
    MODLOADER("ModLoader")
}

class VersionDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "version.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE versions (
                id TEXT PRIMARY KEY,
                name TEXT,
                versionCode TEXT,
                versionName TEXT,
                versionType TEXT,
                architecture TEXT,
                status TEXT,
                patches TEXT,
                customIcon INTEGER,
                dateModified INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS versions")
        onCreate(db)
    }

    fun addVersion(savedVersionData: SavedVersionData) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", savedVersionData.installationId)
                put("name", savedVersionData.name)
                put("versionCode", savedVersionData.versionCode)
                put("versionName", savedVersionData.versionName)
                put("versionType", savedVersionData.versionType.name)
                put("architecture", savedVersionData.architecture.name)
                put("status", savedVersionData.status.name)
                put("patches", savedVersionData.patches.joinToString(",") { it.name })
                put("customIcon", savedVersionData.customIcon)
                put("dateModified", savedVersionData.dateModified)
            }
            db.insertWithOnConflict("versions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun removeVersion(id: String) {
        writableDatabase.use { db ->
            db.delete("versions", "id = ?", arrayOf(id))
        }
    }

    fun getSavedVersions(): SnapshotStateList<SavedVersionData> {
        val savedVersions = mutableStateListOf<SavedVersionData>()
        readableDatabase.use { db ->
            db.query("versions", null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val patches = cursor.getString(cursor.getColumnIndexOrThrow("patches"))
                        .split(",")
                        .mapNotNull { name -> Patch.entries.find { it.name == name } }

                    savedVersions.add(
                        SavedVersionData(
                            installationId = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            versionCode = cursor.getString(cursor.getColumnIndexOrThrow("versionCode")),
                            versionName = cursor.getString(cursor.getColumnIndexOrThrow("versionName")),
                            versionType = VersionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("versionType"))),
                            architecture = Architecture.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("architecture"))),
                            status = Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                            patches = patches,
                            customIcon = cursor.getInt(cursor.getColumnIndexOrThrow("customIcon")) == 1,
                            dateModified = cursor.getLong(cursor.getColumnIndexOrThrow("dateModified"))
                        )
                    )
                }
            }
        }
        return savedVersions
    }

    fun editVersion(savedVersionData: SavedVersionData) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("name", savedVersionData.name)
                put("versionCode", savedVersionData.versionCode)
                put("versionName", savedVersionData.versionName)
                put("versionType", savedVersionData.versionType.name)
                put("architecture", savedVersionData.architecture.name)
                put("status", savedVersionData.status.name)
                put("patches", savedVersionData.patches.joinToString(",") { it.name })
                put("customIcon", savedVersionData.customIcon)
                put("dateModified", savedVersionData.dateModified)
            }
            db.update("versions", values, "id = ?", arrayOf(savedVersionData.installationId))
        }
    }
}
