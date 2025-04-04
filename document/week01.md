## 정리

### **1. JavaScript와 Event Loop**

- JavaScript는 **싱글 스레드 기반**이지만, **비동기 처리**를 위해 **이벤트 루프(Event Loop)** 를 활용한다.
- `setTimeout`, `Promise`, `async/await` 같은 비동기 함수는 이벤트 루프를 통해 **논블로킹 방식**으로 실행됨.
- JavaScript의 **콜 스택(Call Stack)**, **태스크 큐(Task Queue)**, **마이크로태스크 큐(Microtask Queue)** 를 활용한 이벤트 처리 방식.

### **2. Redis가 멀티스레드를 적극 활용하지 않은 이유**

Redis는 전통적으로 **싱글 스레드** 기반으로 동작해 왔는데, 그 이유는 다음과 같다.

**📌 1) 락(lock) 경합이 없기 때문**

- 멀티스레드 환경에서는 **데이터 동기화 문제**가 발생하여 **락(lock) 관리**가 필요하지만, Redis는 싱글 스레드 구조로 **락 경합 없이 빠른 처리**가 가능함.

**📌 2) 메모리 기반 연산이기 때문**

- Redis는 **디스크 기반 DB(RDB, Elasticsearch)와 달리 메모리에서 모든 연산이 수행**되므로, CPU보다는 **메모리 접근 속도**가 성능을 결정하는 핵심 요소.
- 즉, 데이터 접근 속도가 워낙 빠르기 때문에 **멀티스레드로 쪼개는 것이 큰 이점이 없음**.

**📌 3) 적정 스펙을 유지하면서 성능을 최적화**

- Redis는 **스펙을 최대한 올리지 않고 주어진 서버 자원(CPU, 메모리)에 맞춰 효율적으로 동작**하도록 설계됨.
- 멀티스레드를 적극 활용하는 구조로 가면 **복잡성이 증가**하고, **운영 및 관리 부담**이 커짐.

### **3. Redis 활용 사례**

**📌 Consumer / 중간 필터링 용도**

- Redis는 **데이터 중간 캐싱** 및 **필터링 용도로 활용** 가능
- **시계열 데이터**, **TTL(만료 시간) 관리**에 유용 → 데이터를 짧은 시간 캐싱하고 빠르게 만료시키는 방식
- **메모리 기반 캐시**로 동작하여 DB 접근을 줄이고 성능을 극대화할 수 있음.
- RDB 대신 Redis를 선택하는 이유는 **데이터 처리 속도가 빠르고, 구조가 단순하기 때문**.

**📌 Elasticsearch vs Redis**

- **Elasticsearch**: 대량의 텍스트 검색 및 분석 용도로 최적화 → **비용이 높음**.
- **Redis**: 빠른 데이터 접근이 필요하지만, **복잡한 검색이나 대용량 저장이 필요 없는 경우** 유리.

**📌 Redis 운영 이슈**

- **Redis 장애 시 복구 속도 문제** → `POD`에 배포된 Redis가 장애 발생 시 복구 시간이 오래 걸릴 수 있음.
- **쿠팡 사례**: `int, key` 개수가 **21억 개 이상** 넘어가면서 Redis 운영에 어려움 발생.

### **4. Redis의 인프라 운영 및 관리**

**📌 Redis 인프라팀 운영**

- Redis는 단순한 캐시 서버가 아니라, **운영 환경에 따라 튜닝이 필요한 설정이 많음**.
- 운영팀에서 **딥하게 최적화할 수 있는 요소들**이 많음 (AOF, RDB 백업, 만료 정책 등).

**📌 Redis 자료구조 활용 (Sorted Set)**

- *Sorted Set(ZSET)** 을 활용한 정렬된 데이터 저장 및 검색.
- 점수(score)를 기반으로 정렬하여 **리더보드, 랭킹 시스템, TTL 기반 데이터 정리** 등에 유용.

**📌 Redis의 오버 엔지니어링 문제**

- Redis를 무조건 도입하는 것이 정답이 아님.
- **굳이 필요 없는 경우** 오히려 **기술 부채**가 될 수 있음.
- RDB도 속도가 충분히 빠르기 때문에 **RDB로 해결할 수 있는 것은 Redis 도입을 재검토해야 함**.

### **5. Redis vs Elasticsearch vs Valkey**

- **Redis**: 빠른 키-값 저장, 캐싱 용도, 이벤트 처리에 적합.
- **Elasticsearch**: 대량 데이터 검색, 로그 분석에 최적화 (비용이 높음).
- **Valkey**: Redis의 오픈소스 포크로, **멀티스레드 아키텍처를 실험적으로 도입**.

### **6. Local Cache vs Distributed Cache**

**📌 Spring Caffeine Cache vs Spring Ehcache**

- **Caffeine Cache**: LRU 기반 캐시, Spring Boot 내장 지원, 성능 우수.
- **Ehcache**: 전통적인 JVM 내장 캐시, 디스크 저장 지원 가능.

### **7. Redis의 이벤트 루프**

Redis는 **이벤트 루프 기반**으로 동작하며, **epoll을 활용한 I/O 멀티플렉싱**을 수행한다.

1. **I/O 멀티플렉서 등록**
    - 여러 클라이언트 소켓을 `epoll` 같은 멀티플렉싱 API에 등록.
2. **이벤트 루프(Event Loop) 동작**
    - `epoll_wait()`을 호출하여 이벤트를 감지.
3. **비즈니스 로직 처리**
    - 이벤트가 발생한 소켓에서 **데이터 읽기/쓰기, 명령어 파싱, 응답 생성**을 수행.

> 싱글 스레드이지만 매우 빠른 처리를 위해 I/O 멀티플렉싱을 적극 활용하는 구조
>

### **8. Redis 모니터링 및 운영**

- **메트릭 기반 모니터링**: `INFO`, `MONITOR` 명령어를 활용한 실시간 데이터 체크.
- **Redis 터질 경우 문제**: 장애 발생 시 복구 시간이 오래 걸리므로, **클러스터링, 복제(replication), Sentinel 등의 관리 전략 필요**.