package Ontdekstation013.ClimateChecker.user;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserFilter;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    @Test
    public void toUserResponse_mapsFields_andHasNoStationsWhenNotRequested() {
        User user = new User(3L, "Jan", "Jansen", "jan@example.com", UserRole.ADMIN, "pw");

        UserResponse response = UserMapper.toUserResponse(user, false);

        assertEquals(3L, response.id());
        assertEquals("Jan", response.firstName());
        assertEquals("Jansen", response.lastName());
        assertEquals("jan@example.com", response.email());
        assertEquals(UserRole.ADMIN, response.role());
        // Zonder includeStations blijft de stationsset leeg (geen lazy load).
        assertNotNull(response.stations());
        assertTrue(response.stations().isEmpty());
    }

    @Test
    public void toUserFilter_mapsRequestFields() {
        GetAllUsersRequest request = new GetAllUsersRequest(0, 20, "Jan", "Jansen", "jan@example.com", UserRole.USER);

        UserFilter filter = UserMapper.toUserFilter(request);

        assertEquals("Jan", filter.getFirstName());
        assertEquals("Jansen", filter.getLastName());
        assertEquals("jan@example.com", filter.getEmail());
        assertEquals(UserRole.USER, filter.getRole());
    }
}
