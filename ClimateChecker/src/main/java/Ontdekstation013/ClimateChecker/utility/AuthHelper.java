package Ontdekstation013.ClimateChecker.utility;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

//TODO: Handle authorization more elegantly (e.g. by properly implementing Spring Security)

/**
 * Utility class with authentication related helper functions
 */
public class AuthHelper {

    /**
     * Fetches the id of the logged-in user from the request
     * @param request The request object that the cookie is located in
     * @return the user id as a Long. Is allowed to be null.
     */
    public static Long getNullableUserIdFromRequestCookie(HttpServletRequest request) {
        Long userId = null;
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            userId = Long.parseLong(cookies[0].getValue());
        }

        return userId;
    }

    /**
     * Fetches the id of the logged-in user from the request
     * @throws InvalidArgumentException When the userId could not be fetched.
     * @param request The request object that the cookie is located in
     * @return the user id as a long.
     */
    public static long getUserIdFromRequestCookie(HttpServletRequest request) {
        long userId = -1;
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            userId = Long.parseLong(cookies[0].getValue());
        }

        if (userId == -1) {
            throw new InvalidArgumentException("Invalid user id");
        }

        return userId;
    }
}
