package Ontdekstation013.ClimateChecker.user;

import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetAllUsersRequestTest {

    @Test
    void appliesDefaultPagingWhenNotProvided() {
        GetAllUsersRequest request = new GetAllUsersRequest(null, null, null, null, null, null);

        assertEquals(0, request.page());
        assertEquals(20, request.pageSize());
    }

    @Test
    void keepsExplicitPagingValues() {
        GetAllUsersRequest request = new GetAllUsersRequest(3, 50, null, null, null, UserRole.USER);

        assertEquals(3, request.page());
        assertEquals(50, request.pageSize());
        assertEquals(UserRole.USER, request.role());
    }
}
