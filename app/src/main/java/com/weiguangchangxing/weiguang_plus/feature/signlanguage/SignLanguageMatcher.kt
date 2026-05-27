package com.weiguangchangxing.weiguang_plus.feature.signlanguage

class SignLanguageMatcher {

    fun matchPhraseByGesture(
        recognizedGesture: String,
        database: SignLanguageDatabase
    ): SignPhrase? {
        val allPhrases = database.getAllPhrases()
        val exactMatch = allPhrases.filter { phrase ->
            phrase.gestureTemplates.any { template ->
                template.name.contains(recognizedGesture, ignoreCase = true) ||
                        recognizedGesture.contains(template.name.replace(Regex("\\(.*\\)"), "").trim())
            }
        }
        if (exactMatch.isNotEmpty()) {
            return exactMatch.first()
        }

        val fallback = allPhrases.filter { phrase ->
            phrase.gestureTemplates.any { template ->
                val templateBaseName = template.name.replace(Regex("\\(.*\\)"), "").trim()
                val gestureBaseName = recognizedGesture.replace(Regex("\\(.*\\)"), "").trim()
                templateBaseName == gestureBaseName
            }
        }
        if (fallback.isNotEmpty()) {
            return fallback.first()
        }

        return null
    }

    fun matchPhraseByClassifierResult(
        classifierResult: HandGestureClassifier.ClassificationResult,
        database: SignLanguageDatabase
    ): SignPhrase? {
        if (classifierResult.confidence < 0.5f) return null

        val result = matchByExtendedFingers(classifierResult, database)
        if (result != null) return result

        return matchByGesture(classifierResult.gestureName, database)
    }

    private fun matchByExtendedFingers(
        classifierResult: HandGestureClassifier.ClassificationResult,
        database: SignLanguageDatabase
    ): SignPhrase? {
        val template = classifierResult.matchedTemplate ?: return null
        val targetFingers = template.extendedFingers

        val scored = database.getAllPhrases().map { phrase ->
            var score = 0f
            for (gestureTemplate in phrase.gestureTemplates) {
                var matchCount = 0
                for (i in 0 until 5) {
                    if (i < targetFingers.size && i < gestureTemplate.extendedFingers.size) {
                        if (targetFingers[i] == gestureTemplate.extendedFingers[i]) {
                            matchCount++
                        }
                    }
                }
                val matchScore = matchCount / 5f
                if (matchScore > score) {
                    score = matchScore
                }
            }
            Pair(phrase, score)
        }

        val best = scored.maxByOrNull { it.second } ?: return null
        return if (best.second >= 0.6f) best.first else null
    }

    private fun matchByGesture(
        recognizedGesture: String,
        database: SignLanguageDatabase
    ): SignPhrase? {
        val allPhrases = database.getAllPhrases()
        val gestureBaseName = recognizedGesture.replace(Regex("\\(.*\\)"), "").trim()

        return allPhrases.firstOrNull { phrase ->
            phrase.gestureTemplates.any { template ->
                val templateBase = template.name.replace(Regex("\\(.*\\)"), "").trim()
                templateBase == gestureBaseName
            }
        }
    }

    fun searchText(query: String, database: SignLanguageDatabase): List<SignPhrase> {
        return database.searchPhrases(query)
    }

    fun getMatchedPhraseContext(
        phrase: SignPhrase,
        database: SignLanguageDatabase
    ): String {
        val related = database.getRelatedPhrases(phrase.id)
        if (related.isEmpty()) return ""

        return "相关短语: ${related.joinToString("、") { it.text }}"
    }
}