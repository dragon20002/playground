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

# 스터디
## 목차
1. [DB 다중화](#1-db-다중화)<br>
1.1. [Write 작업 시 라우팅 대상 구분](#11-write-작업-시-라우팅-대상-구분)<br>
1.1.1. [Annotation을 활용하여 DB 라우팅하기](#111-annotation을-활용하여-db-라우팅하기)<br>
1.2. [Write 작업 완료 후 동기화 방식](#12-write-작업-완료-후-동기화-방식)<br>
1.2.1. [격리 수준(Isolation Level)에 대해](#121-격리-수준isolation-level에-대해)<br>
1.2.2. [DB 간 동기화를 지원하는 DBMS](#122-db-간-동기화를-지원하는-dbms)<br>
2. [JPA 동작방식](#2-jpa-동작방식)<br>
2.1. [JPA Bean 초기화 과정에 대해](#21-jpa-bean-초기화-과정에-대해)<br>
2.2. [JPA Repository interface 동작방식](#22-jpa-repository-interface-동작방식)<br>
3. [Spring Security 인증](#3-spring-security-인증)<br>
3.1. [JWT (JSON Web Token)](#31-jwt-json-web-token)<br>
3.1.1. [로그인 과정](#311-로그인-과정)<br>
3.1.2. [인증 과정](#312-인증-과정)<br>

## 1. DB 다중화

웹 서버나 WAS는 많은 이용자의 요청을 원활하게 처리하기 위해 여러 대를 배치하여 L4스위치 또는 *HCI\** 로 요청을 적절히 분배할 수 있다. 반면 DB를 다중화하려면 2가지 고려할 점이 있는데 이에 대해 조사하고 해결방안을 고민해보았다.

- 1.1. [Write 작업 시 라우팅 대상 구분](#11-write-작업-시-라우팅-대상-구분)
- 1.2. [Write 작업 완료 후 동기화 방식](#12-write-작업-완료-후-동기화-방식)

> **\* HCI (Hyper Converged Infrastructure)** : 수평 스케일링 장비

### 1.1. Write 작업 시 라우팅 대상 구분

Read/Write 가능 DB와 *Read-only DB\** 로 나눈 환경의 경우, Read 작업 수행 시에는 L4 스위치를 거쳐 적절히 나눌 수 있지만 Write 작업 시에는 Read/Write DB로만 라우팅해야 한다.

![DB-Multiplex1](https://postfiles.pstatic.net/MjAyMDEwMjZfMTkg/MDAxNjAzNzAwMjg3OTE5.kbI59CkxMpdHAsSYw9kcZjPt8E4I8sWcwsCRMYRZpy0g.uQhypC_SwMWwN08eLHn4OEvvvhSO1rD556oOElIbdKog.PNG.dragon20002/write_%EC%9E%91%EC%97%85_%EB%9D%BC%EC%9A%B0%ED%8C%85.PNG?type=w773)

> **\* Read-only Database** : DB가 Read 작업만 처리하도록 설정한다. 주로 Read 요청이 Write 요청보다 많은 환경에서 Write 요청을 수행하는 DB의 부담을 덜어주기 위해 Read 요청만 수행하는 DB를 다중화하여 사용한다.

### 1.1.1. Annotation을 활용하여 DB 라우팅하기
[Write 작업 시 라우팅 대상 구분](#11-write-작업-시-라우팅-대상-구분)에 대한 결정권을 개발자에게 부여하여 Write 작업이 필요한 메소드에 Read/Write DB에 접근하도록 하는 Annotation을 붙이고 Read 작업만 수행하는 나머지 메소드는 L4 스위치가 라우팅하도록 한다.

#### 요구사항
여러 개의 Data source를 만들어두고, Controller/Service의 클래스나 메소드에 <code>DbType</code> Annotation을 설정해두면 <code>DbType.profile</code> 값에 따라 DAO 메소드를 호출 시 다른 Data source에 접근하도록 한다.

#### 구현과정

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
          // property 값을 가져옴
          @ConfigurationProperties(prefix = "spring.datasource.hikari.primary")
          public DataSource primaryDataSource() {
              return DataSourceBuilder.create()
                      .type(HikariDataSource.class)
                      .build();
          }

          @Bean(name = "secondaryDataSource")
          @Qualifier("secondaryDataSource")
          @ConfigurationProperties(prefix = "spring.datasource.hikari.secondary")
          public DataSource secondaryDataSource() {
              return DataSourceBuilder.create()
                      .type(HikariDataSource.class)
                      .build();
          }
      }
      ```

    - Data Source 접근 시
      - Autowired 방식
        ```java
        @Autowired
        @Qualifier("secondaryDataSource")
        private DataSource dataSource;
        ```

      - 생성자 주입 방식
        ```java
        private final DataSource dataSource;

        public MyService(@Qualifier("secondaryDataSource") DataSource dataSource) {
            this.dataSource = dataSource;
        }
        ```

    - 참고링크
      - [Multiple DataSource](https://gigas-blog.tistory.com/122)

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
                  case PRIMARY -> (DataSource) context.getBean("primaryDataSource");
                  case SECONDARY -> (DataSource) context.getBean("secondaryDataSource");
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
      6. *(선택사항) 이전에 연동되어 있던 Data source로 다시 전환한다.*
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
                  dao.setDataSource(profile); // switch data source
                  result = pjp.proceed(); // proceed dao method
                  dao.setDataSource(prevProfile); // restore data source

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
    - 그리고 또...?

7. TODO
    - 2-Phase Commit 스터디
    - JDBC 로드밸런싱 기능에 대해
      - Web서버나 WAS가 아닌, DB를 대상으로 라우팅할 수 있도록 지원됨
    - Read/Write용 DB, Read용 DB에 적용할 수 있을지 고민해볼 것
      - MySQL, PostgreSQL 등은 Read/Write용 DB, Read용 DB로 나눠 동기화하는 기능이 있음
      - DB 다중화 및 동기화 설정 해보기

### 1.2. Write 작업 완료 후 동기화 방식

Read/Write DB에 Write 작업 후 동기화가 제 때 이루어지지 않으면, 다른 DB로 라우팅된 두 요청의 응답이 서로 다른 경우가 발생할 수 있다. 

![DB-Multiplex2](https://postfiles.pstatic.net/MjAyMDEwMjZfMjM5/MDAxNjAzNjk5OTEzNjc2.EszxwVBcr4TXgyAqppgjox0m_5kXCR8uTmQpAidqa68g.PWD8--2VoyLWljWwCKNNa_l8--SnhDb2pF3FgE6aUSkg.PNG.dragon20002/write_%EC%9E%91%EC%97%85_%EB%8F%99%EA%B8%B0%ED%99%94.PNG?type=w773)

- 1.2.1. [격리 수준(Isolation Level)에 대해](#121-격리-수준isolation-level에-대해)

- 1.2.2. [DB 간 동기화를 지원하는 DBMS](#122-db-간-동기화를-지원하는-dbms)

### 1.2.1. 격리 수준(Isolation Level)에 대해
- 격리 수준

  다수의 트랜잭션을 동시 처리 시 발생하는 문제들은 트랜잭션의 격리성을 조절하여 해결할 수 있다. 격리 수준이 높아질 수록 동시성이 낮아지므로 성능을 고려하여 조절해야 한다.

  격리성 비교 : (낮음) <code>Read Uncommitted</code> < <code>Read Committed</code> < <code>Non-Repeatable Read</code> < <code>Serializable</code> (높음)
  > \* 아래 항목 중 <code>RCSI</code>는 MS SQL Server의 <code>Read Committed</code> 격리 수준에서 사용 가능한 옵션으로 다른 격리 수준과 비교하기 애매하여 비교 대상에서 제외함.

  | 격리 수준 | 동작 방식 | <font color="red">Issues</font> |
  | --- | --- | --- |
  | Read Uncommitted | 한 트랜잭션에서 아직 커밋하지 않은 데이터에 다른 트랜잭션이 접근할 수 있다. | <font color="red">Dirty Read,<br>Non-Repeatable Read,<br>Phantom Read</font> |
  | Read Committed (Default) | 커밋이 완료된 데이터만 읽을 수 있다.| <font color="red">Non-Repeatable Read,<br>Phantom Read</font> |
  | Repeatable Read | 트랜잭션 내에서 한번 조회한 데이터는 다른 트랜잭션에서 값이 변경되어도 반복 조회 시 이전과 같은 데이터로 조회한다.| <font color="red">Phantom Read</font> |
  | Serializable | SELECT 시 *공유 잠금\*\** <br>INSERT/UPDATE/DELETE 시 *배타적 잠금\*\*\** | <font color="red">잠금으로 인한 동시성 감소</font> |
  | Snapshot | Serializable과 동일한 격리 수준이지만, 잠금된 테이블에 대해 INSERT/DELETE 작업을 임시테이블(snapshot)에서 진행한 후, 잠금해제되면 임시테이블 변경내용을 적용한다. | <font color="red">잠금으로 인한 동시성 감소</font> |
  | Read Committed Snapshot (RCSI) | 잠금을 사용하지 않고, 트랜잭션 시작 전에 가장 최근에 커밋된 스냅샷을 불러와 작업을 수행한다. | <font color="red">서로 다른 트랜잭션 사이에 Commit 내용의 충돌 위험</font><br><font color="sky-blue">→ 별도의 충돌감지 및 처리 필요</font> |

  > <b>\*\* 공유 잠금 : </b>자원을 공유하기 위한 잠금으로, 다른 트랜잭션에서 공유 잠금(읽기)는 가능하지만 배타적 잠금(쓰기)은 걸 수 없다.<br>
  > <b>\*\*\* 배타적 잠금 : </b>자원을 수정하기 위한 잠금으로, 다른 트랜잭션에서 공유 잠금(읽기), 배타적 잠금(수정)을 걸 수 없다.

- 격리 수준 이슈
1. Dirty Read

    ![Dirty Read](https://postfiles.pstatic.net/MjAyMDEwMjZfMjgy/MDAxNjAzNjk5OTEzNjc5.5Ei5g72MAij-UXMHY1ouabAgGa9DypRj2V2znJDRSO0g.JbY7OQ9CjxSD7o41waME1AdPgOvym8cbjn99rMGpRngg.PNG.dragon20002/dirty_read.png?type=w773)

    - **격리 수준** <br>
      Read Uncommitted 격리 수준에서 발생
    
    - **이슈** <br>
      TRAN2 트랜잭션이 아직 커밋되지 않은 변경된 데이터를 읽었는데, TRAN1 트랜잭션이 롤백하여 ***의미없는 데이터를 가지게 되는 경우*** 를 'Dirty Read'라고 한다.
    
    - **해결 방법** <br>
      커밋된 데이터만 읽을 수 있는 Read Committed 이상의 격리 수준으로 해결할 수 있다.

2. Non-Repeatable Read

    ![Non-Repeatable Read](https://postfiles.pstatic.net/MjAyMDEwMjZfMTM4/MDAxNjAzNjk5OTEzNjc2.PZKKaQ7v2em4qBWnWwao3c7QAXHEfcWPd26kNdC9134g.fT5m5izH4DmfsL3LM9pDmLhQjrbq6SlwITXlapTusYcg.PNG.dragon20002/non-repeatable_read.png?type=w773)

    - **격리 수준** <br>
      Read Uncommitted, Read Committed 격리 수준에서 발생
    
    - **이슈** <br>
      TRAN1이 여러 번 같은 데이터를 읽는 도중에 TRAN2가 데이터 변경 후 커밋하여 TRAN1 ***트랜잭션이 Read할 때마다 같은 데이터에서 다른 값을 읽게 되는 경우*** 를 'Non-Repeatable Read'라고 한다.
    
    - **해결 방법** <br>
      한 트랜잭션에서 같은 데이터를 여러 번 읽을 때 처음 Read한 값을 사용하도록 하는 Repeatable Read 이상의 격리 수준으로 해결할 수 있다.

3. Phantom Read

    ![Phantom Read](https://postfiles.pstatic.net/MjAyMDEwMjdfMTY3/MDAxNjAzNzYzODA4NjU2.q6OnzNudOmosBbwVo6dcM-gndcTYnLB1E_LpuYhsMGkg.-aMY1vTB8FIs77ejOeXNKkYKkzBiwQXxUbb7bhSO7XMg.PNG.dragon20002/phantom_read.png?type=w773)

    - **격리 수준** <br>
      Read Uncommitted, Read Committed, Repeatable Read 격리 수준에서 발생
    
    - **이슈** <br>
      TRAN1 트랜잭션이 같은 데이터를 여러 번 읽는 도중에 TRAN2가 데이터를 추가/삭제하여 TRAN1 ***트랜잭션 도중에 읽을 데이터가 추가*** 되거나 ***존재하지 않는 데이터를 가지게 된 경우*** 를 'Phantom Read(가상 읽기)'라고 한다.
    
    - **해결 방법** <br>
      공유 잠금, 배타적 잠금을 수행하는 Serializable 이상의 격리 수준으로 해결한다.

- 참고링크
  - [트랜잭션, 트랜잭션 격리수준](https://feco.tistory.com/45)
  - [SQL Server RCSRI](https://www.brentozar.com/archive/2013/01/implementing-snapshot-or-read-committed-snapshot-isolation-in-sql-server-a-guide/)

### 1.2.2. DB 간 동기화를 지원하는 DBMS
- MySQL Replication
- PostgresQL Sync Replication
- 참고링크
  - [MySQL database sync between two databases](https://stackoverflow.com/questions/7707859/mysql-database-sync-between-two-databases)
  - [PostgreSQL Sync Replication Guide](https://hevodata.com/learn/postgresql-sync-replication/)

## 2. JPA 동작방식

### 2.1. JPA Bean 초기화 과정에 대해
```java
// TODO
```

### 2.2. JPA Repository interface 동작방식

Repository interface에 대한 코드 생성은 하지 않는다. Spring의 <code>ProxyFacory</code> API가 JDK 프록시 인스턴스를 생성한 다음, Repository의 메소드가 호출될 때마다 <code>MethodInterceptor</code>를 호출하여 아래와 같이 DB 작업을 수행한다.

1. Repository interface의 메소드를 호출하면 <code>MethodInterceptor</code>가 작동한다.
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

## 3. Spring Security 인증
### 3.1. JWT (JSON Web Token)
### 3.1.1. 로그인 과정
<code>UsernamePasswordAuthenticationFilter</code> → <code>AuthenticationProvider</code> → <code>SavedRequestAwareAuthenticationSuccessHandler</code>

1. <code>UsernamePasswordAuthenticationFilter</code>

    request로부터 username/password 입력값을 가져온다.

    ```java
    public class AuthFilter extends UsernamePasswordAuthenticationFilter {

        public AuthFilter(AuthenticationManager authenticationManager) {
            super.setAuthenticationManager(authenticationManager);
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request,
                HttpServletResponse response) throws AuthenticationException {
            UsernamePasswordAuthenticationToken authRequest;
            try {
                // Request Body로부터 username/password를 가져와
                // UserDetails를 상속받은 클래스에 매핑
                Member member = new ObjectMapper().readValue(request.getInputStream(), Member.class);
                authRequest = new UsernamePasswordAuthenticationToken(member.getUsername(), member.getPassword());
            } catch (IOException e) {
                throw new InputMismatchException();
            }

            setDetails(request, authRequest);

            // 인증 시도 (AuthenticationProvider 호출)
            return getAuthenticationManager().authenticate(authRequest);
        }
    }
    ```

2. <code>AuthenticationProvider</code>

    인증 시도하여 성공한 경우 <code>UsernamePasswordAuthenticationToken</code>을 생성/반환하고 실패한 경우 Exception을 발생시킨다.

    ```java
    public class BaseAuthProvider implements AuthenticationProvider {
        private final Logger logger = LoggerFactory.getLogger(BaseAuthProvider.class);

        private final MemberRepository memberRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;

        public BaseAuthProvider(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
            this.memberRepository = memberRepository;
            this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = (String) authentication.getPrincipal();
            String password = (String) authentication.getCredentials();

            List<Member> memberList = memberRepository.findAllByUsername(username);
            Member member = memberList.stream()
                    .filter(Member::isAccountNonExpired)
                    .findFirst()
                    .filter(m -> bCryptPasswordEncoder.matches(password, m.getPassword()))
                    .orElseThrow(() -> new BadCredentialsException("Invalid Password for " + username));

            return new UsernamePasswordAuthenticationToken(member, password, member.getAuthorities());
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }

    }
    ```

3. <code>SavedRequestAwareAuthenticationSuccessHandler</code>

    인증 성공 시 Handler를 통해 JWT 토큰을 생성하여 Header에 넣는다.

    ```java
    public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
        private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

        private final JwtTokenProvider jwtTokenProvider;

        public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
            Member member = ((Member) authentication.getPrincipal());
            String token = jwtTokenProvider.createToken(member.getUsername()); //JWT 토큰 생성
            logger.debug("token={}", token);

            response.addHeader("jws", token);
            response.setStatus(HttpServletResponse.SC_OK);
        }

    }
    ```

### 3.1.2. 인증 과정
<code>HandlerInterceptorAdapter</code> > 인증 필수 요청의 Controller

1. <code>HandlerInterceptorAdapter</code>

    인증을 위해 Header의 JWT 토큰을 체크하여 누락되거나 만료된 경우 401 Unauthorized 에러로 리다이렉션한다.

    ```java
    public class AuthInterceptor extends HandlerInterceptorAdapter {
        private final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

        private final JwtTokenProvider jwtTokenProvider;

        public AuthInterceptor(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
        }

        @Override
        public boolean preHandle(HttpServletRequest request,
                HttpServletResponse response, Object handler) throws Exception {
            String jws = request.getHeader("jws");
            String sub = jwtTokenProvider.getSubject(jws); // JWT 토큰 내용

            if (sub == null) {
                response.sendRedirect("/error/unauthorized");
                return false;
            }

            return true;
        }

    }
    ```

2. 인증 필수 요청의 Controller

    인증이 필요한 요청을 처리하는 작업 수행
