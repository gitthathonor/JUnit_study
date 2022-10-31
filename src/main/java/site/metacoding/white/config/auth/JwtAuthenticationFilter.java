package site.metacoding.white.config.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.domain.User;
import site.metacoding.white.domain.UserRepository;
import site.metacoding.white.dto.ResponseDto;
import site.metacoding.white.dto.UserReqDto.LoginReqDto;
import site.metacoding.white.util.SHA256;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements Filter {

    private final UserRepository userRepository; // DI (FilterConfig에게서 주입받음)

    // "/login" 요청시
    // post 요청시
    // username, password(json)
    // db확인
    // 토큰 생성

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // POST요청이 아닌 것을 거부
        denyNotPostRequest(req, resp);

        // Body 값 받기
        ObjectMapper om = new ObjectMapper();
        LoginReqDto loginReqDto = om.readValue(req.getInputStream(), LoginReqDto.class);
        log.debug("디버그 : " + loginReqDto.getUsername());
        log.debug("디버그 : " + loginReqDto.getPassword());

        User userPS = userRepository.findByUsername(loginReqDto.getUsername());
        if (userPS == null) {
            // findByUsername 나중에 처리하기
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter out = resp.getWriter();
            resp.setStatus(400);
            ResponseDto<?> responseDto = new ResponseDto<>(-1, "유저네임이 없습니다", null);
            ObjectMapper om2 = new ObjectMapper();
            String body = om2.writeValueAsString(responseDto);
            out.println(body);
            out.flush();
            return;
        } else {
            SHA256 sh = new SHA256();
            String encPassword = sh.encrypt(loginReqDto.getPassword());
            if (userPS.getPassword().equals(encPassword)) {
                // JWT 토큰 만들고 응답하기
            } else {
                // findByUsername 나중에 처리하기
                resp.setContentType("application/json; charset=utf-8");
                PrintWriter out = resp.getWriter();
                resp.setStatus(400);
                ResponseDto<?> responseDto = new ResponseDto<>(-1, "패스워드가 틀렸습니다.", null);
                ObjectMapper om2 = new ObjectMapper();
                String body = om2.writeValueAsString(responseDto);
                out.println(body);
                out.flush();
                return;
            }
        }

        // chain.doFilter(req, resp);
    }

    private void denyNotPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, JsonProcessingException {
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();
        if (!req.getMethod().equals("POST")) {
            resp.setStatus(400);
            ResponseDto<?> responseDto = new ResponseDto<>(-1, "로그인 시에는 post요청을 해야 합니다.", null);
            ObjectMapper om = new ObjectMapper();
            String body = om.writeValueAsString(responseDto);
            out.println(body);
            out.flush();
            return;
        }
    }

}