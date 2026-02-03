package com.isaakhanimann.journal.data.substances.classes.harm_reduction

import com.isaakhanimann.journal.data.substances.classes.Substance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarmReductionRepository @Inject constructor() {

    fun getCommonSideEffects(substance: Substance): List<SideEffect> {
        val effects = mutableListOf<SideEffect>()
        
        when {
            substance.isStimulant -> {
                effects.addAll(stimulantSideEffects)
            }
            substance.isHallucinogen -> {
                effects.addAll(psychedelicSideEffects)
            }
        }
        
        // Add category-specific effects
        if (substance.categories.any { it.equals("dissociative", ignoreCase = true) }) {
            effects.addAll(dissociativeSideEffects)
        }
        if (substance.categories.any { it.equals("depressant", ignoreCase = true) }) {
            effects.addAll(depressantSideEffects)
        }
        
        return effects.distinctBy { it.name }
    }

    fun getMitigationInfo(type: MitigationType): MitigationInfo {
        return mitigationInfoMap[type] ?: throw IllegalArgumentException("Unknown mitigation type: $type")
    }

    private val stimulantSideEffects = listOf(
        SideEffect(
            name = "Jaw Clenching / Bruxism",
            description = "Involuntary teeth grinding or jaw tension",
            mitigations = listOf(
                MitigationTopic(MitigationType.JAW_CLENCHING_MAGNESIUM, "Magnesium Supplementation"),
                MitigationTopic(MitigationType.JAW_CLENCHING_GUM, "Chewing Gum")
            )
        ),
        SideEffect(
            name = "Dehydration",
            description = "Reduced awareness of thirst and increased fluid loss",
            mitigations = listOf(
                MitigationTopic(MitigationType.DEHYDRATION_WATER, "Water Pacing"),
                MitigationTopic(MitigationType.DEHYDRATION_ELECTROLYTES, "Electrolytes")
            )
        ),
        SideEffect(
            name = "Insomnia",
            description = "Difficulty falling asleep after effects wear off",
            mitigations = listOf(
                MitigationTopic(MitigationType.INSOMNIA_ENVIRONMENT, "Sleep Environment"),
                MitigationTopic(MitigationType.INSOMNIA_MELATONIN, "Melatonin")
            )
        ),
        SideEffect(
            name = "Anxiety / Overstimulation",
            description = "Racing thoughts, increased heart rate, restlessness",
            mitigations = listOf(
                MitigationTopic(MitigationType.ANXIETY_BREATHING, "Breathing Techniques"),
                MitigationTopic(MitigationType.ANXIETY_ENVIRONMENT, "Environment Control"),
                MitigationTopic(MitigationType.OVERSTIMULATION_BREAKS, "Taking Breaks")
            )
        )
    )

    private val psychedelicSideEffects = listOf(
        SideEffect(
            name = "Anxiety / Difficult Thoughts",
            description = "Challenging emotional states or thought patterns",
            mitigations = listOf(
                MitigationTopic(MitigationType.ANXIETY_BREATHING, "Breathing Techniques"),
                MitigationTopic(MitigationType.ANXIETY_ENVIRONMENT, "Safe Environment")
            )
        ),
        SideEffect(
            name = "Nausea",
            description = "Stomach discomfort, especially during onset",
            mitigations = listOf(
                MitigationTopic(MitigationType.NAUSEA_GINGER, "Ginger"),
                MitigationTopic(MitigationType.NAUSEA_POSITION, "Body Position")
            )
        )
    )

    private val dissociativeSideEffects = listOf(
        SideEffect(
            name = "Nausea",
            description = "Stomach discomfort, especially at higher doses",
            mitigations = listOf(
                MitigationTopic(MitigationType.NAUSEA_GINGER, "Ginger"),
                MitigationTopic(MitigationType.NAUSEA_POSITION, "Body Position")
            )
        )
    )

    private val depressantSideEffects = listOf(
        SideEffect(
            name = "Nausea",
            description = "Stomach discomfort from substance effects",
            mitigations = listOf(
                MitigationTopic(MitigationType.NAUSEA_GINGER, "Ginger"),
                MitigationTopic(MitigationType.NAUSEA_POSITION, "Body Position")
            )
        )
    )

    private val mitigationInfoMap = mapOf(
        MitigationType.JAW_CLENCHING_MAGNESIUM to MitigationInfo(
            type = MitigationType.JAW_CLENCHING_MAGNESIUM,
            displayName = "Magnesium Supplementation",
            whatItIs = "Magnesium is a mineral supplement commonly discussed for muscle relaxation.",
            whyDiscussed = "Some users report that magnesium supplements taken before or during stimulant use may help reduce jaw tension and teeth grinding. The proposed mechanism is muscle relaxation.",
            whatItDoesNot = "Magnesium does NOT eliminate jaw clenching entirely, does NOT reduce the primary psychoactive effects of stimulants, and does NOT make higher doses safer.",
            risksAndCautions = "Excessive magnesium can cause digestive upset or diarrhea. Consult a healthcare provider if you have kidney issues or take other medications. Do not use this as a reason to increase substance doses.",
            evidenceLevel = EvidenceLevel.ANECDOTAL
        ),
        MitigationType.JAW_CLENCHING_GUM to MitigationInfo(
            type = MitigationType.JAW_CLENCHING_GUM,
            displayName = "Chewing Gum",
            whatItIs = "Sugar-free gum used as a physical outlet for jaw tension.",
            whyDiscussed = "Chewing gum provides a controlled way to move the jaw, which some users find more comfortable than involuntary clenching.",
            whatItDoesNot = "Gum does NOT stop jaw clenching, does NOT reduce substance effects, and does NOT prevent all jaw soreness. Excessive chewing can cause jaw fatigue.",
            risksAndCautions = "Prolonged chewing can lead to jaw soreness the next day. Some users find it makes clenching worse. Use sugar-free gum to avoid dental issues.",
            evidenceLevel = EvidenceLevel.ANECDOTAL
        ),
        MitigationType.DEHYDRATION_WATER to MitigationInfo(
            type = MitigationType.DEHYDRATION_WATER,
            displayName = "Water Pacing",
            whatItIs = "Regular, measured water intake throughout an experience.",
            whyDiscussed = "Stimulants and dancing can increase fluid loss while reducing thirst awareness. Moderate water intake helps maintain hydration.",
            whatItDoesNot = "Water does NOT eliminate substance effects, does NOT prevent overheating on its own, and drinking too much can be dangerous (hyponatremia).",
            risksAndCautions = "DO NOT drink excessive amounts of water. A guideline often discussed is approximately 250-500ml per hour if dancing, less if stationary. Too much water can dilute blood sodium to dangerous levels.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.DEHYDRATION_ELECTROLYTES to MitigationInfo(
            type = MitigationType.DEHYDRATION_ELECTROLYTES,
            displayName = "Electrolyte Drinks",
            whatItIs = "Beverages containing sodium, potassium, and other minerals.",
            whyDiscussed = "Electrolytes help maintain fluid balance and may be preferable to plain water during extended physical activity.",
            whatItDoesNot = "Electrolytes do NOT eliminate substance risks, do NOT prevent overheating, and do NOT replace the need for moderation.",
            risksAndCautions = "Use commercial electrolyte drinks or sports drinks in moderation. Avoid excessive sugar. Still monitor total fluid intake to avoid over-hydration.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.INSOMNIA_ENVIRONMENT to MitigationInfo(
            type = MitigationType.INSOMNIA_ENVIRONMENT,
            displayName = "Sleep Environment Optimization",
            whatItIs = "Creating conditions conducive to sleep: dark room, cool temperature, minimal noise.",
            whyDiscussed = "After stimulant use, optimizing the sleep environment may help the body relax once effects subside.",
            whatItDoesNot = "Environment changes do NOT eliminate stimulant-induced wakefulness, do NOT shorten substance duration, and may not enable sleep until effects fully wear off.",
            risksAndCautions = "Do not take additional substances to force sleep without medical supervision. Allow the body time to naturally metabolize substances.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.INSOMNIA_MELATONIN to MitigationInfo(
            type = MitigationType.INSOMNIA_MELATONIN,
            displayName = "Melatonin",
            whatItIs = "A hormone supplement that regulates sleep-wake cycles.",
            whyDiscussed = "Some users take melatonin to help signal sleep after stimulant effects wear off.",
            whatItDoesNot = "Melatonin does NOT force sleep while stimulant effects are active, does NOT reduce the stimulant effects, and does NOT work instantly.",
            risksAndCautions = "Only take melatonin once the primary effects have worn off. Start with low doses (0.5-3mg). Can interact with some medications. Consult a healthcare provider.",
            evidenceLevel = EvidenceLevel.LIMITED_STUDIES
        ),
        MitigationType.ANXIETY_BREATHING to MitigationInfo(
            type = MitigationType.ANXIETY_BREATHING,
            displayName = "Breathing Techniques",
            whatItIs = "Controlled breathing patterns such as box breathing or deep belly breathing.",
            whyDiscussed = "Slow, controlled breathing can activate the parasympathetic nervous system and may help reduce feelings of anxiety or panic.",
            whatItDoesNot = "Breathing techniques do NOT eliminate substance effects, do NOT guarantee anxiety relief, and do NOT replace emergency medical care in a crisis.",
            risksAndCautions = "If anxiety is severe or accompanied by chest pain or other concerning symptoms, seek medical help. Breathing techniques are comfort measures, not medical treatments.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.ANXIETY_ENVIRONMENT to MitigationInfo(
            type = MitigationType.ANXIETY_ENVIRONMENT,
            displayName = "Environment Control",
            whatItIs = "Moving to a calm, familiar, safe space with trusted people.",
            whyDiscussed = "A comfortable environment can reduce external stressors that contribute to anxiety during psychoactive experiences.",
            whatItDoesNot = "Changing environments does NOT stop substance effects, does NOT eliminate anxiety entirely, and does NOT substitute for professional mental health support.",
            risksAndCautions = "If moving to a new location, ensure someone sober is present and aware. Do not drive or operate machinery. Severe anxiety or panic may require medical attention.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.OVERSTIMULATION_BREAKS to MitigationInfo(
            type = MitigationType.OVERSTIMULATION_BREAKS,
            displayName = "Taking Breaks",
            whatItIs = "Periodically resting, sitting down, or stepping away from intense stimuli (loud music, crowds, dancing).",
            whyDiscussed = "Regular breaks can help manage overstimulation and give the body a chance to cool down and rehydrate.",
            whatItDoesNot = "Taking breaks does NOT eliminate substance effects, does NOT prevent all negative effects, and does NOT replace proper dosing and harm reduction.",
            risksAndCautions = "Listen to your body. If you feel overheated, dizzy, or unwell, move to a cooler, quieter place and hydrate. Seek medical help if symptoms worsen.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.OVERSTIMULATION_DOSING to MitigationInfo(
            type = MitigationType.OVERSTIMULATION_DOSING,
            displayName = "Conservative Dosing",
            whatItIs = "Using lower doses and avoiding redosing.",
            whyDiscussed = "Overstimulation is often dose-dependent. Lower doses reduce the likelihood and intensity of uncomfortable overstimulation effects.",
            whatItDoesNot = "Lower doses do NOT eliminate all risks, do NOT guarantee a comfortable experience, and individual responses vary widely.",
            risksAndCautions = "Even low doses carry risks. Unknown substance purity increases danger. Always test substances when possible and start with the smallest dose.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.NAUSEA_GINGER to MitigationInfo(
            type = MitigationType.NAUSEA_GINGER,
            displayName = "Ginger",
            whatItIs = "Ginger root (fresh, tea, capsules, or crystallized) used for nausea relief.",
            whyDiscussed = "Ginger has been studied for nausea and is commonly discussed as a natural anti-nausea remedy.",
            whatItDoesNot = "Ginger does NOT eliminate all nausea, does NOT reduce substance effects, and does NOT work for everyone.",
            risksAndCautions = "Generally safe in moderate amounts. May interact with blood thinners. If vomiting is severe or persistent, seek medical attention.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        ),
        MitigationType.NAUSEA_POSITION to MitigationInfo(
            type = MitigationType.NAUSEA_POSITION,
            displayName = "Body Position Adjustment",
            whatItIs = "Sitting up, lying on your side, or finding a comfortable position.",
            whyDiscussed = "Body position can affect nausea. Some people find sitting up or lying on their left side more comfortable.",
            whatItDoesNot = "Position changes do NOT eliminate nausea, do NOT stop substance effects, and do NOT replace medical care if needed.",
            risksAndCautions = "If you feel like you might vomit, lie on your side to prevent choking. If nausea is severe or accompanied by other concerning symptoms, seek medical help.",
            evidenceLevel = EvidenceLevel.WELL_STUDIED
        )
    )
}
