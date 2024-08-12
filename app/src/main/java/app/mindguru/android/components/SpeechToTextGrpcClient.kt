package app.mindguru.android.components

import androidx.compose.ui.text.intl.Locale
import com.google.protobuf.ByteString
import google.cloud.speech.v1.SpeechGrpc
import google.cloud.speech.v1.SpeechOuterClass
import google.cloud.speech.v1.SpeechOuterClass.RecognitionConfig
import google.cloud.speech.v1.SpeechOuterClass.StreamingRecognitionConfig
import google.cloud.speech.v1.SpeechOuterClass.StreamingRecognizeRequest
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class SpeechToTextGrpcClient {
    private val channel = ManagedChannelBuilder.forAddress("speech.googleapis.com", 443)
        .useTransportSecurity()
        .intercept(Interceptor("AIzaSyDQaBGZiFUezJCxU0ji_q35ifv9EwV4zsc", null))
        .build()
    private val stub = SpeechGrpc.newStub(channel)

    fun streamAudio(audioData: ByteArray, onResponse: (String) -> Unit) {
        val latch = CountDownLatch(1)

        val responseObserver = object : StreamObserver<SpeechOuterClass.StreamingRecognizeResponse> {
            override fun onNext(response: SpeechOuterClass.StreamingRecognizeResponse) {
                val transcript = response.resultsList
                    .flatMap { it.alternativesList }
                    .joinToString(" ") { it.transcript }
                onResponse(transcript)
            }

            override fun onError(t: Throwable) {
                t.printStackTrace()
                latch.countDown()
            }

            override fun onCompleted() {
                latch.countDown()
            }
        }

        val requestObserver = stub.streamingRecognize(responseObserver)

        val recognitionConfig =
            RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode(Locale.current.language)
                .setSampleRateHertz(16000)
                .build()

        val streamingRecognitionConfig: StreamingRecognitionConfig =
            StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build()

        val request =
            StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build() // The first request in a streaming call has to be a config

        requestObserver.onNext(request)

        val audio = StreamingRecognizeRequest.newBuilder()
            .setAudioContent(ByteString.copyFrom(audioData))
            .build()

        requestObserver.onNext(audio)
        requestObserver.onCompleted()

        latch.await(1, TimeUnit.MINUTES)
    }

}