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

### 진행현황
DbType Annotation에서 'profile' 속성을 설정하여 data-source를 변경함

<b>피드백</b><br>
Annotation이 적용된 Controller/Service/Repository Class나 Method를 실행 시에만
다른 DB를 사용할 수 있도록 해볼 것.<br><br>

<b>TODO</b><br>
- 다수의 data-source를 동시 연동<br>
- DB Transaction 전에 Interceptor나 AOP로 DbType Annotation 체크<br>
- ...?

### 참고자료
#### 1. 격리 수준(Isolation Level)에 대해
![Isolation-Level](https://postfiles.pstatic.net/MjAyMDEwMDhfMjYw/MDAxNjAyMTI5MDAxMDU1._cOt7NOJdUvyVOmdEBx88BJzXIhaoOU6LPeERwZ4lCog.YVKi__5ZGfpB5Fg1GSRghPUjRmMfEQUpxsq39G5JRlcg.PNG.dragon20002/dirty-read1.PNG?type=w773)
![Isolation-Level desc1](https://postfiles.pstatic.net/MjAyMDEwMDhfMTA1/MDAxNjAyMTI5MDAxMDQw.wT4p_AajvtpqykpNlmCHwTSzqXxZPAE1_m5Q8Pp5gWog.649KPQbv37KS_IMYHVY9rvK8nX-I3C8ZEq33ZrMFh6cg.PNG.dragon20002/dirty-read2.PNG?type=w773)
![Isolation-Level desc2](https://postfiles.pstatic.net/MjAyMDEwMDhfMjIg/MDAxNjAyMTI5MDAxMDU2.0zqKMAFgJwaPk-wNu19tGW4LEOB2LqevLaBkAZrwgNkg.pX7ujD2bdK9J7tr_f5xrZfbX69KgLQmlIluVOZmYQhkg.PNG.dragon20002/dirty-read3.PNG?type=w773)

<b>참고링크</b><br>
- [dirty read1](https://ilhee.tistory.com/32)
- [dirty read2](https://www.cubrid.org/manual/ko/9.3.0/sql/transaction.html)
- [isolaion level](https://programmer.ink/think/jdbc-transaction-isolation-level.html)
