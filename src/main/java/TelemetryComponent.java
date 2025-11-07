import dagger.Component;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import javax.inject.Singleton;

@Singleton
@Component(modules = {TelemetryModule.class})
public interface TelemetryComponent {
    Server getServer();
    Client getClient();

    Tracer getTracer();
    OpenTelemetry getOpenTelemetry();
}
