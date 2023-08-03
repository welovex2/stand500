package egovframework.cmm.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
//      http
//              .authorizeRequests()
//              .anyRequest().authenticated()
//      .and()
//              .formLogin()//Form 로그인 인증 기능이 작동함
//              .loginPage("/login.html")//사용자 정의 로그인 페이지
//              .defaultSuccessUrl("/home")//로그인 성공 후 이동 페이지
//              .failureUrl("/login.html?error=true")// 로그인 실패 후 이동 페이지
//              .usernameParameter("username")//아이디 파라미터명 설정
//              .passwordParameter("password")//패스워드 파라미터명 설정
//              .loginProcessingUrl("/login")//로그인 Form Action Url
//              .successHandler(loginSuccessHandler())//로그인 성공 후 핸들러 (해당 핸들러를 생성하여 핸들링 해준다.)
//              .failureHandler(loginFailureHandler());//로그인 실패 후 핸들러 (해당 핸들러를 생성하여 핸들링 해준다.)
//							.permitAll(); //사용자 정의 로그인 페이지 접근 권한 승인
  }
}
