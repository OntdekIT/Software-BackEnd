package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * The OpenAPI spec is committed (docs/api/openapi.json) and the Postman collection is generated
 * from it (scripts/generate-postman.sh). This test fails when the code and the committed spec
 * drift apart. Refresh the file with: ./mvnw test -Dtest=OpenApiSpecTest -Dopenapi.update=true
 */
class OpenApiSpecTest extends AbstractApiTest {

    static final Path SPEC_FILE = Path.of("..", "docs", "api", "openapi.json");

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private JsonNode generatedSpec() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void committedSpecMatchesTheCode() throws Exception {
        JsonNode generated = generatedSpec();

        if (Boolean.getBoolean("openapi.update")) {
            Files.createDirectories(SPEC_FILE.getParent());
            // Sorted keys keep the diff of the committed file small.
            Object plain = mapper.treeToValue(generated, Object.class);
            ObjectMapper writer = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
            Files.writeString(SPEC_FILE, writer.writeValueAsString(plain) + "\n");
        }

        assertThat(SPEC_FILE).as("run with -Dopenapi.update=true to create it").exists();
        assertThat(mapper.readTree(Files.readString(SPEC_FILE)))
                .as("docs/api/openapi.json is out of date, regenerate it with -Dopenapi.update=true and rerun scripts/generate-postman.sh")
                .isEqualTo(generated);
    }

    @Test
    void specOnlyListsTheCanonicalLowercasePaths() throws Exception {
        List<String> paths = new ArrayList<>();
        generatedSpec().get("paths").fieldNames().forEachRemaining(paths::add);

        assertThat(paths).contains(
                "/api/meetstation/stations",
                "/api/meetstation/claim",
                "/api/meetstation/availability/{stationId}/{workshopCode}",
                "/api/meetstation/stations-met-measurements");
        assertThat(paths).noneMatch(path -> path.replaceAll("\\{[^}]*}", "").chars().anyMatch(Character::isUpperCase));
        assertThat(paths).noneMatch(path -> path.toLowerCase().contains("availibility"));
    }

    @Test
    void successResponsesDescribeTheirBodyInTheSpec() throws Exception {
        JsonNode paths = generatedSpec().get("paths");

        assertThat(schemaRef(paths, "/api/users/{id}", "get", "200")).endsWith("/UserResponse");
        assertThat(schemaRef(paths, "/api/my-account", "get", "200")).endsWith("/UserResponse");
        assertThat(schemaRef(paths, "/api/authentication/login", "post", "200")).endsWith("/LoginResponse");
        assertThat(schemaRef(paths, "/api/authentication/verify", "post", "200")).endsWith("/AuthenticationResponse");
        assertThat(schemaRef(paths, "/api/authentication/register", "post", "201")).endsWith("/UserResponse");
        assertThat(schemaRef(paths, "/api/meetstation/add", "post", "201")).endsWith("/Station");
        assertThat(paths.at("/~1api~1meetstation~1stations/get/responses/200/content/*~1*/schema/type").asText())
                .isEqualTo("array");
    }

    private static String schemaRef(JsonNode paths, String path, String method, String status) {
        JsonNode content = paths.get(path).get(method).get("responses").get(status).get("content");
        assertThat(content).as("%s %s %s has a response body in the spec", method, path, status).isNotNull();
        return content.elements().next().get("schema").path("$ref").asText("");
    }

    @Test
    void noControllerMethodReturnsAWildcardResponseEntity() {
        List<String> offenders = new ArrayList<>();
        for (HandlerMethod handler : handlerMapping.getHandlerMethods().values()) {
            if (!handler.getBeanType().getPackageName().startsWith("Ontdekstation013")) {
                continue;
            }
            Type returnType = handler.getMethod().getGenericReturnType();
            if (isUntypedResponseEntity(returnType)) {
                offenders.add(handler.getBeanType().getSimpleName() + "." + handler.getMethod().getName());
            }
        }
        assertThat(offenders).as("handlers returning ResponseEntity<?> or ResponseEntity<Object>").isEmpty();
    }

    private static boolean isUntypedResponseEntity(Type type) {
        if (!(type instanceof ParameterizedType parameterized) || parameterized.getRawType() != ResponseEntity.class) {
            return false;
        }
        Type argument = parameterized.getActualTypeArguments()[0];
        return argument instanceof WildcardType || argument == Object.class;
    }
}
