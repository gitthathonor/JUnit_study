package site.metacoding.white.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.config.auth.JwtAuthenticationFilter;
import site.metacoding.white.config.auth.JwtAuthorizationFilter;
import site.metacoding.white.domain.UserRepository;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class FilterConfig {

    private final UserRepository userRepository; // DI(스프링 IoC 컨테이너에서 옴)

    // IoC 등록(서버 실행시)
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegister() {
        log.debug("디버그 : 인증 필터 등록");
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthenticationFilter(userRepository));
        bean.addUrlPatterns("/login");
        bean.setOrder(1); // 낮은 순서대로 필터가 실행
        return bean;
    }

    @Profile("dev")
    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> JwtAuthorizationFilterRegister() {
        log.debug("디버그 : 인가 필터 등록");
        FilterRegistrationBean<JwtAuthorizationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthorizationFilter());
        // url 패턴을 적을 때는 걸러낼 수 있도록 주소를 짜는 것이 좋다.
        bean.addUrlPatterns("/s/*");
        bean.setOrder(2); // 이거 다음에는 DS로 직행
        return bean;
    }
}

@Slf4j
class HelloFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getMethod().equals("POST")) {
            log.debug("디버그 : HelloFilter 실행됨");
        } else {
            log.debug("디버그 : POST요청이 아니어서 실행할 수 없습니다.");
        }

        // chain.doFilter(req, resp);
    }

}