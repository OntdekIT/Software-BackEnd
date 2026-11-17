package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The committed Postman collection must cover every operation in the committed OpenAPI spec. */
class PostmanCollectionTest {

    private static final Path COLLECTION = Path.of("..", "postman", "ClimateChecker.postman_collection.json");
    private final ObjectMapper mapper = new ObjectMapper();

    private static void collectRequests(JsonNode items, Set<String> found) {
        for (JsonNode item : items) {
            if (item.has("item")) {
                collectRequests(item.get("item"), found);
            } else if (item.has("request")) {
                StringBuilder path = new StringBuilder();
                item.get("request").get("url").get("path").forEach(segment -> {
                    String value = segment.asText();
                    path.append('/').append(value.startsWith(":") ? "{" + value.substring(1) + "}" : value);
                });
                found.add(item.get("request").get("method").asText().toLowerCase() + " " + path);
            }
        }
    }

    @Test
    void collectionContainsEveryOperationOfTheSpec() throws Exception {
        assertThat(COLLECTION).exists();
        JsonNode collection = mapper.readTree(Files.readString(COLLECTION));
        JsonNode spec = mapper.readTree(Files.readString(OpenApiSpecTest.SPEC_FILE));

        Set<String> inCollection = new HashSet<>();
        collectRequests(collection.get("item"), inCollection);

        Set<String> inSpec = new HashSet<>();
        spec.get("paths").fields().forEachRemaining(path ->
                path.getValue().fieldNames().forEachRemaining(method -> inSpec.add(method + " " + path.getKey())));

        assertThat(inSpec).isNotEmpty();
        assertThat(inCollection)
                .as("regenerate with scripts/generate-postman.sh after changing docs/api/openapi.json")
                .containsExactlyInAnyOrderElementsOf(inSpec);
    }

    @Test
    void collectionIsPostmanV21WithABaseUrlAndTokenVariable() throws Exception {
        JsonNode collection = mapper.readTree(Files.readString(COLLECTION));

        assertThat(collection.get("info").get("schema").asText()).contains("v2.1.0");
        Set<String> variables = new HashSet<>();
        collection.get("variable").forEach(variable -> variables.add(variable.get("key").asText()));
        assertThat(variables).contains("baseUrl", "bearerToken");
    }

    @Test
    void collectionUsesTheCanonicalPathsOnly() throws Exception {
        String content = Files.readString(COLLECTION);

        assertThat(content).doesNotContain("Availibility").doesNotContain("/Meetstation").doesNotContain("\"Meetstation\"");
        assertThat(content).contains("\"meetstation\"");
    }
}
