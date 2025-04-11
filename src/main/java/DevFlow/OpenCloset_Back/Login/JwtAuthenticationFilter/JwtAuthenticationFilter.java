package DevFlow.OpenCloset_Back.Login.JwtAuthenticationFilter;

import DevFlow.OpenCloset_Back.Login.Jwt_Util.JwtUtil;
import DevFlow.OpenCloset_Back.Security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    // JwtUtil 메서드들은 static으로 만들었기 때문에 바로 사용 가능
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 다음 필터로
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String token = authHeader.substring(7); // "Bearer " 이후 토큰만 추출
        String username = JwtUtil.extractUsername(token); // 토큰에서 username 추출

        // 인증이 안 되어 있으면 인증 객체 생성
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (JwtUtil.validateToken(token)) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }

        filterChain.doFilter(request, response); // 다음 필터로 이동
    }
}
