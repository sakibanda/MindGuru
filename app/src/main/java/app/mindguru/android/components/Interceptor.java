package app.mindguru.android.components;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

final class Interceptor implements ClientInterceptor {
    private final String apiKey;
    private final String authToken;

    private static Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> API_KEY_HEADER1 =
            Metadata.Key.of("X-Goog-Api-Key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> API_KEY_HEADER2 =
            Metadata.Key.of("key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> AUTHORIZATION_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    public Interceptor(String apiKey, String authToken) {
        this.apiKey = apiKey;
        this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT,RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

        call = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (apiKey != null && !apiKey.isEmpty()) {
                    headers.put(API_KEY_HEADER, apiKey);
                    headers.put(API_KEY_HEADER1, apiKey);
                    headers.put(API_KEY_HEADER2, apiKey);
                }
                if (authToken != null && !authToken.isEmpty()) {
                    System.out.println("Attaching auth token");
                    headers.put(AUTHORIZATION_HEADER, "Bearer " + authToken);
                }
                Logger.Companion.e("STT", "Interceptor headers: " + headers);
                super.start(responseListener, headers);
            }
        };
        return call;
    }
}