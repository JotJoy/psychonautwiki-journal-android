/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isaakhanimann.journal.data.room.experiences.ExperienceDao
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.InstantConverter
import com.isaakhanimann.journal.data.room.experiences.entities.ShulginRating
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote

import com.isaakhanimann.journal.data.room.experiences.entities.HarmReductionTip
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage
import com.isaakhanimann.journal.data.room.experiences.SubstanceMetadataDao
import com.isaakhanimann.journal.data.room.experiences.entities.TaperPlan
import com.isaakhanimann.journal.data.room.experiences.entities.TaperStepEntity
import com.isaakhanimann.journal.data.room.experiences.TaperDao
import com.isaakhanimann.journal.data.room.articles.entities.CachedArticle
import com.isaakhanimann.journal.data.room.articles.ArticleDao

@TypeConverters(InstantConverter::class)
@Database(
    version = 11,
    entities = [
        Experience::class, 
        Ingestion::class, 
        SubstanceCompanion::class, 
        CustomSubstance::class, 
        ShulginRating::class, 
        TimedNote::class, 
        CustomUnit::class,

        HarmReductionTip::class,
        SubstanceDosage::class,
        TaperPlan::class,
        TaperStepEntity::class,
        CachedArticle::class
    ],
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (from = 2, to = 3),
        AutoMigration (from = 3, to = 4),
        AutoMigration (from = 4, to = 5),
        AutoMigration (from = 5, to = 6),
        AutoMigration (from = 6, to = 7),
        AutoMigration (from = 7, to = 8),
        AutoMigration (from = 8, to = 9),
        AutoMigration (from = 9, to = 10),
        AutoMigration (from = 10, to = 11),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun experienceDao(): ExperienceDao
    abstract fun substanceMetadataDao(): SubstanceMetadataDao
    abstract fun taperDao(): TaperDao
    abstract fun articleDao(): ArticleDao
}