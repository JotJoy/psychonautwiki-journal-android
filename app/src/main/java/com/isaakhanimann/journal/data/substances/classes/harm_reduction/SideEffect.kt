package com.isaakhanimann.journal.data.substances.classes.harm_reduction

data class SideEffect(
    val name: String,
    val description: String,
    val mitigations: List<MitigationTopic>
)

data class MitigationTopic(
    val type: MitigationType,
    val displayName: String
)

enum class MitigationType {
    JAW_CLENCHING_MAGNESIUM,
    JAW_CLENCHING_GUM,
    DEHYDRATION_WATER,
    DEHYDRATION_ELECTROLYTES,
    INSOMNIA_ENVIRONMENT,
    INSOMNIA_MELATONIN,
    ANXIETY_BREATHING,
    ANXIETY_ENVIRONMENT,
    OVERSTIMULATION_BREAKS,
    OVERSTIMULATION_DOSING,
    NAUSEA_GINGER,
    NAUSEA_POSITION
}

data class MitigationInfo(
    val type: MitigationType,
    val displayName: String,
    val whatItIs: String,
    val whyDiscussed: String,
    val whatItDoesNot: String,
    val risksAndCautions: String,
    val evidenceLevel: EvidenceLevel
)

enum class EvidenceLevel {
    ANECDOTAL,
    LIMITED_STUDIES,
    WELL_STUDIED
}
