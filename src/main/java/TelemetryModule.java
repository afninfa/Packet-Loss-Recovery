import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Module
public class TelemetryModule {

    private final String serviceName;

    public TelemetryModule(String serviceName) {
        this.serviceName = serviceName;
    }

    @Provides
    @Singleton
    public OpenTelemetry provideOpenTelemetry() {
        Resource resource = Resource.getDefault()
            .toBuilder()
            .put("service.name", serviceName)
            .build();

        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(Registry.JAEGER_ENDPOINT)
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
    }

    @Provides
    @Singleton
    public Tracer provideTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName);
    }
}
