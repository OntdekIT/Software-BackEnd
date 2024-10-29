package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("my-account")
public class MyAccountController {

    @GetMapping("/name")
    public ResponseEntity<String> getName(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getUser(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/id")
    public ResponseEntity<String> getId(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            return ResponseEntity.status(HttpStatus.OK).body(userID.toString());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/admin")
    public ResponseEntity<Boolean> checkAdmin(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
