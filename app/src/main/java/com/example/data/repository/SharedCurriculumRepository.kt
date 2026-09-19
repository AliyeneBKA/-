package com.example.data.repository

import android.content.Context
import com.example.data.model.CurriculumDoc
import com.example.data.model.Question
import org.json.JSONArray
import org.json.JSONObject

class SharedCurriculumRepository(context: Context) {
    private val prefs = context.getSharedPreferences("qcm_community_prefs", Context.MODE_PRIVATE)

    companion object {
        val DEFAULT_SPECIALIZATIONS = listOf(
            "القانون الخاص",
            "القانون العام",
            "علوم الحاسوب والذكاء الاصطناعي",
            "العلوم الاقتصادية والتسيير",
            "الطب والصيدلة",
            "الآداب واللغات"
        )
    }

    fun getSpecializations(): List<String> {
        val saved = prefs.getStringSet("specializations_set", null)
        if (saved == null || saved.isEmpty()) {
            return DEFAULT_SPECIALIZATIONS
        }
        val combined = (DEFAULT_SPECIALIZATIONS + saved.toList()).distinct()
        return combined
    }

    fun addSpecialization(name: String): List<String> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return getSpecializations()
        val current = getSpecializations().toMutableSet()
        current.add(trimmed)
        prefs.edit().putStringSet("specializations_set", current).apply()
        return current.toList()
    }

    fun getCommunityDocuments(): List<CurriculumDoc> {
        val jsonStr = prefs.getString("custom_community_docs", null)
        val customList = if (!jsonStr.isNullOrBlank()) {
            try {
                deserializeDocs(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Return custom uploaded community documents first, followed by default verified curricula
        return customList + SampleData.documents
    }

    fun saveCommunityDocument(doc: CurriculumDoc): List<CurriculumDoc> {
        val currentCustom = try {
            val jsonStr = prefs.getString("custom_community_docs", null)
            if (!jsonStr.isNullOrBlank()) deserializeDocs(jsonStr).toMutableList() else mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        // Add to top of custom community uploads
        currentCustom.add(0, doc)
        val serialized = serializeDocs(currentCustom)
        prefs.edit().putString("custom_community_docs", serialized).apply()

        return currentCustom + SampleData.documents
    }

    private fun serializeDocs(docs: List<CurriculumDoc>): String {
        val array = JSONArray()
        for (doc in docs) {
            val obj = JSONObject()
            obj.put("id", doc.id)
            obj.put("title", doc.title)
            obj.put("category", doc.category)
            obj.put("articleCountBadge", doc.articleCountBadge)
            obj.put("summary", doc.summary)
            obj.put("fullContent", doc.fullContent)
            obj.put("isCommunityShared", doc.isCommunityShared)
            obj.put("uploaderName", doc.uploaderName)

            val qArr = JSONArray()
            for (q in doc.questions) {
                val qObj = JSONObject()
                qObj.put("id", q.id)
                qObj.put("text", q.text)
                val optArr = JSONArray()
                for (opt in q.options) {
                    optArr.put(opt)
                }
                qObj.put("options", optArr)
                qObj.put("correctOptionIndex", q.correctOptionIndex)
                qObj.put("explanation", q.explanation)
                qObj.put("articleReference", q.articleReference)
                qObj.put("topic", q.topic)
                qArr.put(qObj)
            }
            obj.put("questions", qArr)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeDocs(jsonStr: String): List<CurriculumDoc> {
        val list = mutableListOf<CurriculumDoc>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val qList = mutableListOf<Question>()
            val qArr = obj.optJSONArray("questions")
            if (qArr != null) {
                for (j in 0 until qArr.length()) {
                    val qObj = qArr.getJSONObject(j)
                    val optList = mutableListOf<String>()
                    val optArr = qObj.optJSONArray("options")
                    if (optArr != null) {
                        for (k in 0 until optArr.length()) {
                            optList.add(optArr.getString(k))
                        }
                    }
                    qList.add(
                        Question(
                            id = qObj.optString("id", "q_$j"),
                            text = qObj.optString("text", ""),
                            options = optList,
                            correctOptionIndex = qObj.optInt("correctOptionIndex", 0),
                            explanation = qObj.optString("explanation", ""),
                            articleReference = qObj.optString("articleReference", ""),
                            topic = qObj.optString("topic", "")
                        )
                    )
                }
            }

            list.add(
                CurriculumDoc(
                    id = obj.optString("id", "doc_$i"),
                    title = obj.optString("title", "مستند بدون عنوان"),
                    category = obj.optString("category", "القانون الخاص"),
                    articleCountBadge = obj.optString("articleCountBadge", "${qList.size} أسئلة"),
                    summary = obj.optString("summary", ""),
                    fullContent = obj.optString("fullContent", ""),
                    questions = qList,
                    isCommunityShared = obj.optBoolean("isCommunityShared", true),
                    uploaderName = obj.optString("uploaderName", "طالب مشارك")
                )
            )
        }
        return list
    }
}
