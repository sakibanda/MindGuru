package app.mindguru.android.ui

import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mindguru.android.components.Logger
import app.mindguru.android.data.model.Mood
import app.mindguru.android.data.model.MoodLog
import app.mindguru.android.data.repository.FirebaseRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MoodTrackerViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _moodLogs = MutableStateFlow<List<MoodLog>>(emptyList())
    val moodLogs: StateFlow<List<MoodLog>> = _moodLogs

    private val _aiAnalysisResult = MutableStateFlow<String>("")
    val aiAnalysisResult: StateFlow<String> = _aiAnalysisResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMoodLogs() {
        viewModelScope.launch {
            //val logs = firebaseRepository.getMoodLogs()
            //dummy data logs

            var logs = emptyList<MoodLog>().toMutableList()
            for (i in 1..100){
                logs += MoodLog(randomDate(), Mood.entries.toTypedArray().random())
            }
            /*var logs = listOf(
                MoodLog(randomDate(), Mood.SAD),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.SAD),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.NEUTRAL),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.SAD),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.NEUTRAL),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.SAD),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.NEUTRAL),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.SAD),
                MoodLog(randomDate(), Mood.HAPPY),
                MoodLog(randomDate(), Mood.NEUTRAL),
            )*/
            logs = logs.sortedBy { it.date }.toMutableList()
            _moodLogs.value = logs
            _loading.value = false
            if(_moodLogs.value.isNotEmpty()) {
                sendDateToAi()
            }else{
                _aiAnalysisResult.value = "No data available"
            }
            Logger.e("MoodChartViewModel", "fetchMoodLogs: $logs")
        }
    }

    private fun randomDate() : Date {
        val random = Random.nextInt(1, 31)
        val randomMonth = 7
        val randomYear = 2024
        val calendar = Calendar.getInstance()
        calendar.set(randomYear, randomMonth, random)
        return calendar.time
    }

    private fun sendDateToAi() = viewModelScope.launch {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-pro-latest",
            apiKey = "AIzaSyDi1up6dCJCiv5Our6MwnmSgaA_57ejOUM"
        )
        var moodData = "You are a data analyser. Analyse and give very brief summary of my mood based on the recorded data using mobile phone. Following are the mood values recorded data wise:"
        moodData += _moodLogs.value.joinToString(separator = "\n") { "${it.date}: ${it.mood.name}" }
        val response = generativeModel.generateContent(moodData)
        _aiAnalysisResult.value = response.text.toString()
    }
}