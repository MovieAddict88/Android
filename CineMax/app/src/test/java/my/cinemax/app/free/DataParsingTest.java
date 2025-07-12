package my.cinemax.app.free;

import com.google.gson.Gson;
import my.cinemax.app.free.model.ApiResponse;
import my.cinemax.app.free.model.Category;
import my.cinemax.app.free.model.Entry;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataParsingTest {
    private final String json = "{\n" +
            "  \"Categories\": [\n" +
            "    {\n" +
            "      \"MainCategory\": \"Movies\",\n" +
            "      \"SubCategories\": [\n" +
            "        \"Action\",\n" +
            "        \"Comedy\"\n" +
            "      ],\n" +
            "      \"Entries\": [\n" +
            "        {\n" +
            "          \"Title\": \"Test Movie\",\n" +
            "          \"SubCategory\": \"Action\",\n" +
            "          \"Country\": \"USA\",\n" +
            "          \"Description\": \"This is a test movie.\",\n" +
            "          \"Poster\": \"https://example.com/poster.jpg\",\n" +
            "          \"Thumbnail\": \"https://example.com/thumbnail.jpg\",\n" +
            "          \"Rating\": 8.5,\n" +
            "          \"Duration\": \"2:00:00\",\n" +
            "          \"Year\": 2023,\n" +
            "          \"Servers\": [\n" +
            "            {\n" +
            "              \"name\": \"Server 1\",\n" +
            "              \"url\": \"https://example.com/video.mp4\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testParseApiResponse() {
        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(json, ApiResponse.class);

        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getCategories());
        assertEquals(1, apiResponse.getCategories().size());

        Category category = apiResponse.getCategories().get(0);
        assertEquals("Movies", category.getMainCategory());
        assertEquals(2, category.getSubCategories().size());
        assertEquals(1, category.getEntries().size());

        Entry entry = category.getEntries().get(0);
        assertEquals("Test Movie", entry.getTitle());
        assertEquals(8.5, entry.getRating(), 0.0);
        assertEquals(2023, entry.getYear());
        assertEquals(1, entry.getServers().size());
        assertEquals("Server 1", entry.getServers().get(0).getName());
    }
}
