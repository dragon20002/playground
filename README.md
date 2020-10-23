# Playground
- 회원관리를 위한 스프링부트 백엔드 서버

## 개발환경
- Spring Boot 2.x
- Gradle
- OpenJDK 14

## REST API 테이블
### 회원관리
| Method | Path | Description |
| --- | --- | --- |
| GET    | /api/members/{id} | 회원상세조회 |
| GET    | /api/members      | 회원목록조회 |
| POST   | /api/members      | 회원등록 |
| PUT    | /api/members/{id} | 회원수정(미구현) |
| DELETE | /api/members/{id} | 회원삭제 |

## [스터디] Annotation을 활용하여 DB 변경하기
### 1. 격리 수준(Isolation Level)에 대해
![Isolation-Level](https://postfiles.pstatic.net/MjAyMDEwMDhfMjYw/MDAxNjAyMTI5MDAxMDU1._cOt7NOJdUvyVOmdEBx88BJzXIhaoOU6LPeERwZ4lCog.YVKi__5ZGfpB5Fg1GSRghPUjRmMfEQUpxsq39G5JRlcg.PNG.dragon20002/dirty-read1.PNG?type=w773)
![Isolation-Level desc1](https://postfiles.pstatic.net/MjAyMDEwMDhfMTA1/MDAxNjAyMTI5MDAxMDQw.wT4p_AajvtpqykpNlmCHwTSzqXxZPAE1_m5Q8Pp5gWog.649KPQbv37KS_IMYHVY9rvK8nX-I3C8ZEq33ZrMFh6cg.PNG.dragon20002/dirty-read2.PNG?type=w773)
![Isolation-Level desc2](https://postfiles.pstatic.net/MjAyMDEwMDhfMjIg/MDAxNjAyMTI5MDAxMDU2.0zqKMAFgJwaPk-wNu19tGW4LEOB2LqevLaBkAZrwgNkg.pX7ujD2bdK9J7tr_f5xrZfbX69KgLQmlIluVOZmYQhkg.PNG.dragon20002/dirty-read3.PNG?type=w773)

- 참고링크
  - [트랜잭션 격리수준](https://ilhee.tistory.com/32)
  - [CUBRID SQL 설명서 » 트랜잭션과 잠금](https://www.cubrid.org/manual/ko/9.3.0/sql/transaction.html)
  - [Isolation Level에 대해](https://programmer.ink/think/jdbc-transaction-isolation-level.html)

### 2. Multiple DataSource 설정하기
- application.yaml

  <code>primary</code>와 <code>secondary</code>라는 이름을 가진 Data Source property를 정의한다.

  ```yaml
  spring:
  datasource:
      hikari:
      primary:
          jdbc-url: jdbc:h2:~/test
          driver-class-name: org.h2.Driver
          username: sa
          password:
      secondary:
          jdbc-url: jdbc:h2:~/playground
          driver-class-name: org.h2.Driver
          username: sa
          password:
  ```

- DataSourceProperties.java

  <code>primary</code>와 <code>secondary</code> property을 적용한 Data Source <code>Bean</code>을 생성한다.

  ```java
  @Configuration
  @EnableConfigurationProperties
  public class DataSourceProperties {

      @Bean(name = "primaryDataSource")
      @Qualifier("primaryDataSource")
      @Primary // 주 DataSource
      @ConfigurationProperties(prefix = "spring.datasource.hikari.primary") // property 값을 가져옴
      public DataSource primaryDataSource() {
          return DataSourceBuilder.create().type(HikariDataSource.class).build();
      }

      @Bean(name = "secondaryDataSource")
      @Qualifier("secondaryDataSource")
      @ConfigurationProperties(prefix = "spring.datasource.hikari.secondary")
      public DataSource secondaryDataSource() {
          return DataSourceBuilder.create().type(HikariDataSource.class).build();
      }
  }
  ```

- Data Source 접근 시

  ```java
  @Autowired
  @Qualifier("secondaryDataSource")
  private DataSource dataSource;
  ```

  ```java
  private final DataSource dataSource;

  public MyService(@Qualifier("secondaryDataSource") DataSource dataSource) {
      this.dataSource = dataSource;
  }
  ```

- 참고링크
  - [Multiple DataSource](https://gigas-blog.tistory.com/122)

#### 3. JPA Repository interface 동작방식

Repository interface에 대한 코드 생성은 하지 않는다. Spring의 <code>ProxyFacory</code> API가 JDK 프록시 인스턴스를 생성한 다음, Repository의 메소드가 호출될 때마다 <code>MethodInterceptor</code>를 호출하여 아래와 같이 DB 작업을 수행한다.

1. 다음과 같이 Repository interface의 메소드를 호출하면 <code>MethodInterceptor</code>가 작동한다.
    ```java
    List<Member> memberList = memberRepository.findAllByUserId(userId);
    ```

2. <code>SimpleJpaRepository</code> 등에 구현된 메소드를 호출한다.
    ```java
    List<Member> memberList = memberRepository.findAll();
    ```

3. 직접 생성한 <code>@Query</code> 메소드인 경우, <code>JpaQueryLookupStrategy</code>가 메소드 이름을 구문분석하고 <code>JpaQueryCreator</code>로 쿼리를 생성한다.
    ```java
    @Query
    public List<Member> findAllByUserId(String userId);
    ```

- 참고링크
  - [how-are-spring-data-repositories-actually-implemented](https://stackoverflow.com/questions/38509882/how-are-spring-data-repositories-actually-implemented)

#### 4. JPA Bean 초기화 과정에 대해
```java
// TODO
```

#### 5. 기능 구현 (JdbcTemplate)

여러 개의 Data source를 만들어두고, Controller/Service의 클래스나 메소드에 <code>DbType</code> Annotation을 설정해두면 <code>DbType.profile</code> 값에 따라 DAO 메소드를 호출 시 다른 Data source에 접근하도록 한다.

1. Annotation 추가

    ```java
    @Target({ElementType.TYPE, ElementType.METHOD}) // Class나 Method 앞에 사용가능
    @Retention(RetentionPolicy.RUNTIME) // Annotation 수명 설정
    public @interface DbType {
        enum Profile { PRIMARY, SECONDARY }

        Profile profile() default Profile.PRIMARY; // Annotation 속성 및 기본값 설정

    }
    ```

2. Multiple Data Source 설정

    <a href="#2-multiple-datasource-설정하기"># 2. Multiple DataSource 설정하기</a> 참고

3. DAO 추가

    - <code>JdbcTemplate</code>를 가진 추상클래스 <code>BaseDao</code>

      ```java
      public abstract class BaseDao {
          private final ApplicationContext context;
          protected final JdbcTemplate jdbcTemplate;
          private DbType.Profile profile = DbType.Profile.PRIMARY;

          public BaseDao(ApplicationContext context, JdbcTemplate jdbcTemplate) {
              this.context = context;
              this.jdbcTemplate = jdbcTemplate;
          }

          /*** Data source를 전환하기 위한 메소드 ***/

          /**
           * @return 현재 DbType을 반환한다.
           */
          public DbType.Profile getProfile() {
              return profile;
          }

          /**
           * @param profile 전환할 Data source의 profile
           */
          public void setDataSource(DbType.Profile profile) {
              if (profile == null)
                  return;

              this.profile = profile;

              DataSource dataSource = switch (profile) {
                  case PRIMARY -> (DataSource) context.getBean(DataSourceProperties.PRIMARY);
                  case SECONDARY -> (DataSource) context.getBean(DataSourceProperties.SECONDARY);
              };

              jdbcTemplate.setDataSource(dataSource);
          }
      }
      ```

    - CRUD를 구현한 <code>MemberDao</code> 생성. <code>BaseDao</code> 클래스를 상속해야 한다.

      ```java
      @Component
      public class MemberDao extends BaseDao {

          public MemberDao(ApplicationContext context, JdbcTemplate jdbcTemplate) {
              super(context, jdbcTemplate);
          }

          public Member findById(Long id) {
              String sql = "SELECT * FROM MEMBER WHERE ID = ?";
              return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MemberRowMapper());
          }

          public MemberSec findByIdSec(Long id) {
              String sql = "SELECT ID, USER_ID, NAME, TEL_NO, EMAIL, ADDRESS, EXPR_DATE FROM MEMBER WHERE ID = ?";
              return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MemberSecRowMapper());
          }
          ...
      }
      ```

4. (AOP) Aspect 추가

    - Point Cut : DAO 패키지의 메소드 호출 시
    - 기능 요약
      1. <code>StackTrace</code>로 DAO 메소드를 호출한 Controller/Service의 클래스/메소드를 찾는다.
      2. 클래스/메소드에 <code>DbType</code> Annotation이 있는지 확인하고 <code>DbType.profile</code> 값을 가져온다.
      3. <code>profile</code> 값이 DAO에 설정된 <code>JdbcTemplate</code>의 Data source와 다른지 확인한다.
      4. 다르면 DAO에 Lock을 걸고 <code>profile</code>에 맞는 Data source로 전환한다.
      5. DAO 메소드 실행
      6. <i>(선택사항) 이전에 연동되어 있던 Data source로 다시 전환한다.</i>
      7. DAO에 대한 Lock 해제

      ```java
      @Around("execution(* net.ldcc.playground.dao..*.*(..))") // [PointCut] DAO 패키지의 메소드 실행
      public Object switchDataSource(ProceedingJoinPoint pjp) throws Throwable {
          @SuppressWarnings("rawtypes")
          Class cls = pjp.getSignature().getDeclaringType();
          String clsName = pjp.getSignature().getDeclaringTypeName();
          String mtdName = pjp.getSignature().getName();

          // StackTrace to find the controller/service method has called current method
          StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

          // > [DEBUG] Print StackTrace
          Arrays.stream(stackTrace).forEach(t -> logger.debug("StackTrace : {}.{}", t.getClassName(), t.getMethodName()));

          // > Find the current method's position
          Iterator<StackTraceElement> iter = Arrays.stream(stackTrace).iterator();
          while (iter.hasNext()) {
              StackTraceElement element = iter.next();
              if (element.getClassName().contains(clsName) && element.getMethodName().equals(mtdName)) {
                  break;
              }
          }

          // > Check 'DbType' Annotation
          DbType.Profile profile = null;
          if (iter.hasNext()) {
              StackTraceElement stSvc = iter.next(); // (DAO 메소드를 호출한 Service)

              // >> Service
              @SuppressWarnings("rawtypes")
              Class clsSvc = Class.forName(stSvc.getClassName());
              Method mtdSvc = Arrays.stream(clsSvc.getDeclaredMethods())
                      .filter(m -> m.getName().equals(stSvc.getMethodName()))
                      .findFirst()
                      .orElse(null);

              if (clsSvc.isAnnotationPresent(DbType.class)) {
                  DbType dbType = (DbType) clsSvc.getAnnotation(DbType.class);
                  profile = dbType.profile();
              } else if (mtdSvc != null && mtdSvc.isAnnotationPresent(DbType.class)) {
                  DbType dbType = mtdSvc.getAnnotation(DbType.class);
                  profile = dbType.profile();
              }

              // >> Controller
              if (iter.hasNext() && profile == null) {
                  StackTraceElement stCtrl = iter.next(); // (Service 메소드를 호출한 Controller)

                  @SuppressWarnings("rawtypes")
                  Class clsCtrl = Class.forName(stCtrl.getClassName());
                  Method mtdCtrl = Arrays.stream(clsCtrl.getDeclaredMethods())
                          .filter(m -> m.getName().equals(stCtrl.getMethodName()))
                          .findFirst()
                          .orElse(null);

                  if (clsCtrl.isAnnotationPresent(DbType.class)) {
                      DbType dbType = (DbType) clsCtrl.getAnnotation(DbType.class);
                      profile = dbType.profile();
                  } else if (mtdCtrl != null && mtdCtrl.isAnnotationPresent(DbType.class)) {
                      DbType dbType = mtdCtrl.getAnnotation(DbType.class);
                      profile = dbType.profile();
                  }
              }
          }

          // [DEBUG] Selected DbType Profile
          logger.debug("DbType.Profile={}", profile);

          // Switch data source
          @SuppressWarnings("rawtypes")
          BaseDao dao = (BaseDao) context.getBean(cls);
          Object result;

          // > Lock/UnLock data source
          synchronized (dao) {
              if (profile != null) {
                  DbType.Profile prevProfile = dao.getProfile();
                  dao.setDataSource(profile);
                  result = pjp.proceed();
                  dao.setDataSource(prevProfile);

              } else {
                  result = pjp.proceed();
              }
          }

          return result;
      }
      ```

5. 사용 예
  - Controller/Service 클래스에 사용 시, 해당 클래스의 모든 메소드를 대상으로 적용된다.
    ```java
    @DbType(profile = DbType.Profile.SECONDARY)
    @RestController
    public class MemberRestController {
        private final MemberService memberService;
        ...
    }
    ```

  - Controller/Service의 메소드에 사용 시, 해당 메소드에만 적용된다.
    ```java
    @DbType(profile = DbType.Profile.SECONDARY)
    @GetMapping("/api/members")
    public ResponseEntity<List<MemberSec>> getMembers() {
        List<MemberSec> memberList = memberService.getMembersSec();

        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }
    ```

6. 제약사항
    - JdbcTemplate 활용 (JPA 활용불가)
    - 같은 DAO에 동시 접근하는 경우 상호배제로 인한 성능 저하
    - ...?

## [스터디] Spring Security 인증
### JSON Web Token
- 로그인 과정

  <code>request</code> > <code>UsernamePasswordAuthenticationFilter</code> > <code>AuthenticationProvider</code> > <code>SavedRequestAwareAuthenticationSuccessHandler</code> > <code>response</code>

- 인증 과정

  <code>request</code> > <code>HandlerInterceptorAdapter</code> > <code>Controller ...</code>
