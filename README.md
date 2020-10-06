# Playground
- 회원관리를 위한 스프링부트 백엔드 서버

## Env
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

DbType Annotation에서 'profile' 속성을 설정하여 data-source를 변경함

<b>피드백</b><br>
Annotation이 적용된 Controller/Service/Repository Class나 Method를 실행 시에만
다른 DB를 사용할 수 있도록 해볼 것.<br><br>

<b>TODO</b><br>
- 다수의 data-source를 동시 연동<br>
- DB Transaction 전에 Interceptor나 AOP로 DbType Annotation 체크<br>
- ...?

<b>참고링크</b><br>
- [dirty read1](https://ilhee.tistory.com/32)
- [dirty read2](https://www.cubrid.org/manual/ko/9.3.0/sql/transaction.html)
- [isolaion level](https://programmer.ink/think/jdbc-transaction-isolation-level.html)
