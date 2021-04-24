# Kafka_Simple
1. **Kafka**-related **Basic Knowledge** (Summary of contents of **Kafka The Definitive Guide**)
2. **Simple Projects** (Source code based on [AndersonChoi' lecture](https://github.com/AndersonChoi/tacademy-kafka))

## Contents
1. [Using](#using)
2. [A quick look at Kafka](#a-quick-look-at-kafka)
3. [Kafka installation and configuration](#kafka-installation-and-configuration)
4. [Kafka Producer](#kafka-producer)
5. [Kafka Consumer](#kafka-consumer)
6. [Kafka internal mechanism](#kafka-internal-mechanism)
7. [Reliable data delivery](#reliable-data-delivery)
8. [Build the data pipeline](#build-the-data-pipeline)
9. [Cross-cluster data mirroring](#cross-cluster-data-mirroring)
10. [Kafka management](#kafka-management)
11. [Kafka monitoring](#kafka-monitoring)
12. [Stream processing](#stream-processing)
13. [Simple Projects](#simple-projects)

---------------------------------------------------

## Using
1. BackEnd - Java(JDK 1.8), **Kafka(v2.5.0)**, **Zookeeper**
2. OS - Ubuntu 20.04.1 LTS (VMware), Amazon Linux 2(AWS EC2)
3. IDE - IntelliJ(Community / 11.0.9.1 / 2020.3.2)
4. Server - **AWS(EC2, Route 53)**
5. etc - Homebrew, **telegraf(v1.17.2)**

----------------------------------------------------

## A quick look at Kafka
1. **카프카 (Kafka)**
    - **메시지 발행/구독 시스템**
    - **분산 커밋 로그(distributed commit log)** 또는 분산 스트리밍 플랫폼(distributed streaming platform)
    - 카프카의 데이터도 지속해서 저장하고 읽을 수 있다.
    - 시스템 장애에 대비하고 확장에 따른 성능 저하를 방지하기 위해 데이터가 분산 처리될 수 있다.
2. **메시지 (message)**
    - **데이터의 기본 단위** (이것은 데이터베이스의 행(row)이나 레코드(record)에 비유될 수 있다.)
    - 카프카는 **메시지를 바이트 배열의 데이터로 간주**하므로 특정 형식이나 의미를 갖지 않는다.
    - 메시지에는 **키**(key)라는 메타데이터가 포함될 수 있으며, 이것 역시 바이트 배열이고 특별한 의미를 갖지 않는다.
    - 카프카의 메시지 데이터는 **토픽**(topic)으로 분류된 **파티션(partition)에 수록**되는데, **이때 데이터를 수록할 파티션을 결정하기 위해 일관된 해시 값으로 키를 생성**한다.
    - **따라서 같은 키 값을 갖는 메시지는 항상 같은 파티션에 수록된다.**
3. **배치 (batch)**
    - 카프카는 효율성을 위해서 여러 개의 메시지를 모아 배치 형태로 파티션에 수록하므로 네티워크로부터 매번 각 메시지를 받아서 처리하는 데 따른 부담을 줄일 수 있다.
    - 물론 이 경우 대기 시간(latency)과 처리량(throughput) 간의 트레이트오프가 생길 수 있다.(배치의 크기가 클수록 단위 시간당 처리될 수 있는 메시지는 많아지지만, 각 메시지의 전송 시간은 더 길어진다.)
    - **배치에는 데이터 압축이 적용되므로 더 효율적인 데이터 전송과 저장 능력을 제공한다.**
4. **스키마 (schema)**
    - 카프카는 메시지를 단순히 바이트 배열로 처리하지만, **내용을 이해하기 쉽도록 메시지의 구조를 나타내는 스키마를 사용할 수 있다.**
    - **각 애플리케이션의 필요에 따라 메시지 스키마로 여러 가지 표준 형식을 사용할 수 있다.**
    - 가장 간단한 방법으로는 **JSON**(Javascript Object Notation)이나 **XML**(Extensible Markup Language)이 있으며, 알기 쉽고 사용하기 쉽다. 그러나 **강력한 데이터 타입에 대한 지원이 부족하고 스키마 버전 간의 호환성이 떨어진다.**
    - 따라서 많은 카프카 개발자들이 **아파치 Avro를 선호**한다. 이것은 원래 **하둡**(Hadoop)을 위해 개발된 **직렬화(serializaion) 프레임워크**이다.
    - **Avro는 데이터를 직렬화하는 형식을 제공하며, 메시지와는 별도로 스키마를 유지 관리하므로 스키마가 변경되더라도 애플리케이션의 코드를 추가하거나 변경할 필요가 없다.** 또한, 강력한 데이터 타입을 지원하며 **스키마 신구버전 간의 호환성도 제공**한다.
    - **카프카에서는 일관된 데이터 형식이 중요하다.** 메시지 쓰기와 읽기 작업을 분리해서 할 수 있기 때문이다.
    - 만일 두 작업이 하나로 합쳐져 있다면 구버전과 신버전의 데이터 형식을 병행 처리하기 위해 메시지 구독 애플리케이션이 먼저 업데이트되어야 하며, 그 다음에 메시지 발행 애플리케이션도 업데이트되어야 한다.
    - 그러나 **카프카에서는 잘 정의된 스키마를 공유 리포지토리(repository)에 저장하여 사용할 수 있으므로 애플리케이션 변경 없이 메시지를 처리할 수 있다.**
5. **토픽 (topic)과 파티션 (partition)**
    - 카프카의 **메시지는 토픽으로 분류된다.** (데이터베이스 테이블이나 파일 시스템의 폴더와 유사하다.)
    - **하나의 토픽은 여러 개의 파티션으로 구성**될 수 있다.
    - 커밋 로그 데이터의 관점으로 보면 파티션은 하나의 로그에 해당한다.
    - 메시지는 파티션에 추가되는 형태로만 수록되며, 맨 앞부터 제일 끝까지의 순서로 읽힌다.
    - **대개 하나의 토픽은 여러 개의 파티션을 갖지만, 메시지 처리 순서는 토픽이 아닌 파티션별로 유지 관리된다.**
    - 추가되는 메시지는 각 파티션의 끝에 수록된다.
    - 각 파티션은 서로 다른 서버에 분산될 수 있다. **하나의 토픽이 여러 서버에 걸쳐 수평적으로 확장될 수 있음을 의미하므로 단일 서버로 처리할 때보다 훨씬 성능이 우수하다.**
    - 카프카와 같은 시스템의 데이터를 얘기할 때 **스트림**(stream)이라는 용어가 자주 사용된다.
    - **대부분 스트림은 파티션의 개수와 상관없이 하나의 토픽 데이터로 간주되며, 데이터를 쓰는 프로듀서로부터 데이터를 읽는 컨슈머로 이동되는 연속적인 데이터**를 나타낸다.
    - **카프카 스트림즈(Kafka Streams)**, **아파치 Samza**, **Storm**과 같은 프레임워크에서 실시간으로 메시지를 처리할 때 주로 사용되는 방법이 스트림이다.
    - 이것은 실시간이 아닌 오프라인으로 대량의 데이터를 처리하도록 설계된 프레임워크인 하둡의 방법과 대비된다.
6. **프로듀서 (Producer)와 컨슈머 (Consumer)**
    - **카프카 클라이언트는 시스템의 사용자이며 기본적으로 프로듀서와 컨슈머라는 두 가지 형태가 있다.**
    - 또한, **데이터 통합을 위한 카프카 커넥트(Kafka Connect) API**와 **스트림 처리를 위한 카프카 스트림즈의 클라이언트 API**도 있으며, **이 클라이언트들은 나름의 프로듀서와 컨슈머를 가지고 사용한다.**
    - **프로듀서는 새로운 메시지를 생성한다(쓰기).**
    - **메시지는 특정 토픽으로 생성되며, 기본적으로 프로듀서는 메시지가 어떤 파티션에 수록되는지 관여하지 않는다.**
    - 그러나 **때로는 프로듀서가 특정 파티션에 메시지를 직접 쓰는 경우가 있다.** 이때는 **메시지 키와 파티셔너** (partitioner)를 사용한다.
    - **파티셔너는 키의 해시 값을 생성하고 그것을 특정 파티션에 대응시킴으로써 지정된 키를 갖는 메시지가 항상 같은 파티션에 수록되게 해준다.**
    - **프로듀서는 또한 나름의 커스텀 파티셔너를 갖고 다른 규칙에 따라 메시지가 파티션에 대응되게 할 수도 있다.**
    - **컨슈머는 메시지를 읽는다.**
    - **컨슈머는 하나 이상의 토픽을 구독하여 메시지가 생성된 순서로 읽으며, 메시지의 오프셋(offset)을 유지하여 읽는 메시지의 위치를 알 수 있다.**
    - 또 다른 종류의 메타데이터인 **오프셋**은 지속적으로 증가하는 정숫값이며, 메시지가 생성될 때 **카프카가 추가**해준다.
    - 파티션에 수록된 각 메시지는 고유한 오프셋을 갖는다.
    - 그리고 **주키퍼(Zookeeper)나 카프카에서는 각 파티션에서 마지막에 읽은 메시지의 오프셋을 저장하고 있으므로 컨슈머가 메시지 읽기를 중단했다가 다시 시작하더라도 언제든 그 다음 메시지부터 읽을 수 있다.**
    - **컨슈머는 컨슈머 그룹의 멤버로 동작**한다.
    - **컨슈머 그룹은 하나 이상의 컨슈머로 구성**되며, **한 토픽을 소비하기 위해 같은 그룹의 여러 컨슈머가 함께 동작**한다.
    - 그리고 **한 토픽의 각 파티션은 하나의 컨슈머만 소비할 수 있다.**
    - 각 컨슈머가 특정 파티션에 대응되는 것을 **파티션 소유권**(ownership)이라고 한다.
    - **다량의 메시지를 갖는 토픽을 소비하기 위해 컨슈머를 수평적으로 확장할 수 있다.**
    - **한 컨슈머가 자신의 파티션 메시지를 읽는 데 실패하더라도 같은 그룹의 다른 컨슈머가 파티션 소유권을 재조정받은 후 실패한 컨슈머의 파티션 메시지를 대신 읽을 수 있다.**
7. **브로커(broker)와 클러스터(cluster)**
    - **하나의 카프카 서버를 브로커**라고 한다.
    - **브로커는 프로듀서로부터 메시지를 수신하고 오프셋을 지정한 후 해당 메시지를 디스크에 저장**한다.
    - 또한 컨슈머의 파티션 읽기 요청에 응답하고 디스크에 수록된 메시지를 전송한다.
    - 하나의 브로커는 초당 수천 개의 토픽과 수백만 개의 메시지를 처리할 수 있다.
    - **카프카 브로커는 클러스터의 일부로 동작하도록 설계**되었다.
    - 즉 **여러 개의 브로커가 하나의 클러스터에 포함될 수 있으며, 그 중 하나는 자동으로 선정되는 클러스터 컨트롤러(controller)의 기능을 수행**한다.
    - **컨트롤러는 같은 클러스터의 각 브로커에게 담당 파티션을 할당하고 브로커들이 정상적으로 동작하는지 모니터링하는 관리 기능을 맡는다.**
    - 각 파티션은 클러스터의 한 브로커가 소유하며, 그 브로커를 **파티션 리더**(leader)라고 한다.
    - **같은 파티션이 여러 브로커에 지정될 수도 있는데, 이때는 해당 파티션이 복제(replication)된다.** 이 경우 해당 파티션의 메시지는 중복으로 저장되지만, **관련 브로커에 장애가 생기면 다른 브로커가 소유권을 인계받아 그 파티션을 처리**할 수 있다.
    - **각 파티션을 사용하는 모든 컨슈머와 프로듀서는 파티션 리더에 연결해야 한다.**
8. **보존 (retention)**
    - 아파치 **카프카의 핵심 기능으로 보존**이 있다. 이것은 **일정 기간 메시지를 보존하는 것**이다.
    - 카프카 브로커는 기본적으로 **토픽의 보존 설정을 하도록 구성**되는데 예를 들어, 7일과 같이 **일정 기간** 메시지를 보존하거나 1GB와 같이 **지정된 토픽 크기**가 될 때까지 메시지를 보존할 수 있다.
    - 그리고 이런 **한도 값에 도달하면 최소한의 데이터가 유지되도록 만료 메시지들이 삭제**된다.
    - **메시지가 유용할 때만 보존되도록 토픽마다 보존 설정을 다르게 지정할 수도 있다.**
    - 예를 들어, 지속적인 메시지 유지가 필요한 토픽은 7일 동안 보존하고, 애플리케이션의 성능 관련 메트릭을 저장한 토픽은 수시간 동안만 보존되게 할 수 있다.
    - 또한, **토픽은 압축 로그(log compacted) 설정으로 구성될 수도 있다.**
    - 이 경우 **같은 키를 갖는 메시지들은 가장 최신 것만 보존**된다.
    - 따라서 마지막으로 변경된 것이 중요한 로그 데이터에 유용하다.
9. **다중 클러스터**
    - 카프카가 많이 설치되어 사용될 때 **다중 클러스터(multiple cluster)를 고려하면 다음과 같은 장점**이 있다.
        - **데이터 타입에 따라 구분해서 처리할 수 있음**
        - **보안 요구사항을 분리해서 처리할 수 있음**
        - **재해 복구를 대비한 다중 데이터센터를 유지할 수 있음**
    - 특히 카프카가 다중 데이터센터로 설치되어 동작할 때는 메시지가 데이터센터 간에 복제되어야 하며, 그렇게 하면 온라인 애플리케이션에서는 사용자의 처리 결과를 양쪽 사이트 모두에서 사용할 수 있다.
    - 예를 들어, 한 데이터센터에 접속된 사용자가 자신의 프로파일에 있는 공개 정보를 변경한다면 다른 데이터센터에서도 변경 내용을 볼 수 있게 될 것이다.
    - 또는 여러 사이트에서 수집된 모니터링 데이터를 분석/보안 시스템이 운영되는 센터로 집중시켜 처리할 수 있다.
    - **카프카 클러스터의 복제 메커니즘은 다중 클러스터가 아닌 단일 클러스터에서만 동작하도록 설계되었다.**
    - 따라서 **다중 클러스터를 지원하기 위해** 아파치 카프카 프로젝트에는 **미러메이커**(MirrorMaker)라는 도구가 포함되어 있다.
    - 근본적으로 미러메이커도 카프카의 컨슈머와 프로듀서이며, 각 미러메이커는 큐(queue)로 상호 연결된다.
    - 그리고 하나의 카프카 클러스터에서 소비된 메시지를 다른 클러스터에서도 사용할 수 있도록 생성해준다.
10. **카프카를 사용하는 이유**
    1. **다중 프로듀서**
        - 여러 클라이언트가 많은 토픽을 사용하거나 같은 토픽을 같이 사용해도 카프카는 무리 없이 많은 프로듀서의 메시지를 처리할 수 있다.
        - 따라서 여러 프런트엔드 시스템으로부터 데이터를 수집하고 일관성을 유지하는 데 이상적이다.
        - 예를 들어, 여러 가지 마이크로서비스를 통해 사용자에게 콘텐츠를 서비스하는 사이트에서 모든 마이크로서비스가 같은 형식으로 작성한 페이지 뷰(page view)를 하나의 토픽으로 가질 수 있다.
        - 그 다음에 컨슈머 애플리케이션에서 그 사이트의 모든 애플리케이션 페이지 뷰를 하나의 스트림으로 받을 수 있다.
    2. **다중 컨슈머**
        - 다중 프로듀서와 더불어 카프카는 많은 컨슈머가 상호 간섭 없이 어떤 메시지 스트림도 읽을 수 있게 지원한다.
        - **한 클라이언트가 특정 메시지를 소비하면 다른 클라이언트에서 그 메시지를 사용할 수 없는 큐 시스템과는 다르다.**
        - 카프카 컨슈머는 컨슈머 그룹의 멤버가 되어 메시지 스트림을 공유할 수 있다.
    3. **디스크 기반의 보존**
        - 카프카는 다중 컨슈머를 처리할 수 있을 뿐만 아니라 **지속해서 메시지를 보존**할 수도 있다.
        - **따라서 컨슈머 애플리케이션이 항상 실시간으로 실행되지 않아도 된다.**
        - 메시지는 카프카 구성에 설정된 보존 옵션에 따라 디스크에 저장되어 보존된다. 그리고 보존 옵션을 토픽별로 선택할 수 있으므로, 컨슈머의 요구에 맞게 메시지 스트림마다 서로 다른 보존 옵션을 가질 수 있다.
        - **따라서 처리가 느리거나 접속 폭주로 인해 컨슈머가 메시지를 읽는 데 실패하더라도 데이터 유실될 위험이 없다.**
        - 또한 **보존 기간 동안 컨슈머가 동작하지 않더라도 메시지는 카프카에 보존되므로 프로듀서의 메시지 백업이 필요 없고, 컨슈머의 유지보수도 자유롭게 수행할 수 있다.**
        - 그리고 **컨슈머가 다시 실행되면 중단 시점의 메시지부터 처리할 수 있다.**
    4. **확장성**
        - 카프카는 확장성이 좋아서 어떤 크기의 데이터도 쉽게 처리할 수 있다.
        - 따라서 처음에는 실제로 잘 되는지 검증하는 의미에서 하나의 브로커로 시작한 후 세 개의 브로커로 구성된 소규모의 개발용 클러스터로 확장하면 좋다.
        - 그 다음에 데이터 증가에 따라 10개에서 심지어는 수백 개의 브로커를 갖는 대규모 클러스터로 업무용 환경을 구축하면 된다.
        - **확장 작업은 시스템 전체의 사용에 영향을 주지 않고 클러스터가 온라인 상태일 때도 수행될 수 있다.**
        - 여러 개의 브로커로 구성된 클러스터는 개별적인 브로커의 장애를 처리하면서 클라이언트에게 지속적인 서비스를 할 수 있기 때문이다.
        - 또한, 동시에 여러 브로커에 장애가 생겨도 정상적으로 처리할 수 있는 **복제 팩터**(replication factor)를 더 큰 값으로 구성할 수 있다.
    5. **고성능**
        - 프로듀서, 컨슈머, 브로커 모두 대용량의 메시지 스트림을 쉽게 처리할 수 있도록 확장될 수 있으며, 확장 작업도 1초 미만의 짧은 시간 내에 가능하다.
11. **데이터 생태계**
    - 데이터를 처리하기 위해 구축한 환경에는 많은 애플리케이션이 있다. 그리고 데이터를 생성하는 애플리케이션에 맞춰 입력 형식이 정의되며, 메프틱, 리포트 등으로 출력 형태가 정의된다. 또한, 특정 컴포넌트를 사용해서 시스템의 데이터를 읽은 후 다른 소스에서 받은 데이터를 사용해서 변환시킨다. 그 다음에 어디서든 사용될 수 있도록 최종 데이터를 데이터 기반 구조에 전달한다. 이런 작업은 고유한 콘텐츠와 크기, 용도를 갖는 다양한 유형의 데이터로 처리된다. 이것이 메시지 데이터의 처리 흐름이다.
    - 아파치 카프카는 **데이터 생태계의 순환 시스템을 제공**한다. 즉, **모든 클라이언트에 대해 일관된 인터페이스를 제공하면서 데이터 기반 구조의 다양한 멤버 간에 메시지를 전달**한다. 이때 메시지 구조를 정의한 스키마를 시스템에서 제공하면 프로듀서와 컨슈머가 어떤 형태로든 밀접하게 결합하거나 연결되지 않아도 되므로, 비즈니스 용도가 생기거나 없어질 때 관련 컴포넌트만 추가하거나 제거하면 된다. 또한, 프로듀서는 누가 자신의 데이터를 사용하는지, 그리고 컨슈머 애플리케이션이 몇 개 인지 신경쓰지 않아도 된다.
12. **이용 사례**
    1. **활동 추적**
        - 처음에 카프카는 사용자 활동 추적에 사용하기 위해 링크드인(LinkedIn)에서 설계되었다.
        - 즉, 사용자가 웹 사이트에 접속하여 액션(페이지 이동, 특정 콘텐츠 조회 등)을 수행하면 이것에 관한 메시지 데이터를 생성하는 프런트엔드 애플리케이션이 동작한다. 이런 추적 데이터는 해당 사이트의 페이지 뷰와 클릭 기록이 될 수 있으며, 그 사이트의 사용자가 자신의 프로파일에 추가하는 정보와 같이 더 복잡한 것이 될 수도 있다. 그리고 이와 같은 메시지는 하나 이상의 토픽으로 생성되어 카프카에 저장된 후 백엔드 애플리케이션에서 소비된다. 예를 들어, 리포트를 생성하거나, 머신 러닝 시스템에 제공되거나, 검색 결과를 변경하거나, 풍부한 사용자 경험을 제공하는 데 필요한 다른 작업을 수행하는 데 사용될 수 있다.
    2. **메시지 전송**
        - 카프카 또한 메시지를 전송하는데도 사용될 수 있으므로 사용자에게 알림 메시지를 전송해야 하는 애플리케이션에 유용하다.
        - 이때 각 애플리케이션에서 메시지 형식이나 전송 방법을 신경 쓰지 않게 메시지를 생성할 수 있다.
        - 전송될 모든 메시지를 하나의 애플리케이션이 읽어서 다음 작업을 일관성 있게 처리할 수 있기 때문이다.
            - 같은 룩앤필(look and feel)을 사용해서 메시지의 형식을 만든다.
            - 여러 개의 메시지를 하나의 알림 메시지로 전송하기 위해 합친다.
            - 사용자가 원하는 메시지 수신 방법을 적용한다.
        - 그리고 하나의 애플리케이션에서 처리하면 여러 애플리케이션에 중복된 기능을 가질 필요가 없을 뿐만 아니라 메시지 집중 처리와 같은 기능도 함께 수행할 수 있다.
    3. **메트릭과 로깅**
        - 카프카는 또한 애플리케이션과 시스템의 **메트릭과 로그 데이터**를 모으는 데 이상적이다.
        - 이것은 여러 애플리케이션에서 같은 타입의 메시지를 생성하는 대표적인 이용 사례다.
        - 이 경우 각 애플리케이션에서 장기적으로 메트릭을 생성하여 카프카 토픽으로 저장한 후 모니터링과 보안 시스템에서 그 데이터를 사용할 수 있다. 또한 하둡과 같은 오프라인 시스템에서 데이터 증가 예측과 같은 장기적인 분석을 위해 사용될 수도 있다. 로그 메시지는 모두 같은 방법으로 생성될 수 있으며, 엘라스틱서치(Elasticsearch)나 보안 분석 애플리케이션과 같은 로그 검색 전용 시스템으로 전달될 수 있다.
        - 이외에도 카프카를 사용하면 또 다른 장점이 있다. 즉, 특정 시스템을 변경해야 할 때(로그 저장 시스템을 변경할 시기가 되었을 때와 같이)도 프런트엔드 애플리케이션이나 메시지 수집 방법을 변경할 필요가 없다.
    4. **커밋 로그**
        - 카프카는 커밋 로그 개념을 기반으로 하므로 데이터베이스의 변경 사항이 카프카 메시지 스트림으로 생성될 수 있다.(커밋 로그는 데이터가 변경된 내역을 고르 메시지로 모아둔 것을 말한다).
        - 그리고 애플리케이션에서는 해당 스트림을 모니터링하여 발생 시점의 최신 변경 데이터를 받을 수 있다.
        - 이런 스트림을 변경 로그 스트림(changelog stream)이라고 하며, 이 스트림은 데이터베이스의 변경 데이터를 원격 시스템에 복제하는 데 사용되거나 여러 애플리케이션의 변경 데이터를 하나의 데이터베이스 뷰로 통합하는데 사용될 수 있다.
        - 이때 변경 로그 데이터를 모아두는 버퍼를 제공하기 위해 카프카의 메시지 보존 기능이 유용하게 사용된다.
        - 컨슈머 애플리케이션에 장애가 생긴 이후에도 해당 데이터를 다시 사용할 수 있기 때문이다.
        - 또한 더 오랫동안 데이터를 보존하고자 할 때는 키 하나당 하나의 변경 데이터만 보존하는 압축로그(log-compacted) 토픽을 사용할 수도 있다.
    5. **스트림 프로세싱**
        - 여러 종류의 애플리케이션에서 스트림 프로세싱(stream processing)을 지원한다. 카프카를 사용할 때도 대부분 스트림 프로세싱을 한다.
        - **일반적으로 이 용어는 하둡의 맵/리듀스(map/reduce)처리와 유사한 기능을 제공하는 애플리케이션에 사용된다.**
        - **단, 하둡은 긴 시간(어러 시간이나 여러 날)에 걸쳐 집중된 데이터를 사용하지만, 스트림 프로세싱에서는 메시지가 생성되자마자 실시간으로 데이터를 처리한다는 차이가 있다.**
        - 그리고 스트림 프로세싱을 하는 프레임워크에서는 작은 크기의 애플리케이션을 기능별로 여러 개 작성하여 카프카 메시지를 처리할 수 있다.
        - 예를 들어, 메트릭 데이터의 각종 수치를 계산하는 작업을 수행하는 애플리케이션, 다른 애플리케이션에서 효율적으로 메시지를 처리할 수 있게 파티션에 저장하는 애플리케이션, 여러 소스에서 받은 데이터를 사용해서 메시지를 변환하는 애플리케이션 등이다.

----------------------------------------------------

## Kafka installation and configuration
1. **AWS EC2에 카프카 설치**
    1. **AWS EC2 구축** (EC2 + Route 53 + Elastic beanstalk + RDS), pem파일 저장 [영상](https://www.youtube.com/playlist?list=PLSU3uVI3_Xyvpw2fIiUrKs8skZMwyr3LI) [![Youtube Badge](https://img.shields.io/badge/Youtube-ff0000?style=flat-square&logo=youtube&link=https://www.youtube.com/playlist?list=PLSU3uVI3_Xyvpw2fIiUrKs8skZMwyr3LI)](https://www.youtube.com/playlist?list=PLSU3uVI3_Xyvpw2fIiUrKs8skZMwyr3LI)
    2. pem 파일 위치에서 **AWS Server** 접속
        ```{.bash}
        chmod 400 mykafka.pem
        
        ssh -i mykafka.pem ec20user@{aws ec2 public ip}
        ```
    3. Java, Kafka 설치 및 압축 풀기 / **Kafka 실행 최소 Heap Size 설정 제거**
        ```{.bash}
        sudo yum install -y java-1.8.0-openjdk-devel.x86_64
        wget http://mirror.navercorp.com/apache/kafka/2.5.0/kafka_2.12-2.5.0.tgz
        tar -xvf kafka_2.12-2.5.0.tgz
        
        export KAFKA_HEAP_OPTS="-Xmx400m -Xms400m"
        ```
    4. **카프카 설정 파일 수정**
        ```{.bash}
        vi config/server.properties
        
        listeners=PLAINTEXT://:9092
        advertised.listeners=PLAINTEXT://{aws ec2 public ip}:9092
        ```
    5. **주키퍼** 실행
        ```{.bash}
        bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
        ```
    6. **카프카** 실행
        ```{.bash}
        bin/kafka-server-start.sh -daemon config/server.properties
        ```
    7. 로그 확인
        ```{.bash}
        tail -f logs/*
        ```
2. **Local**에 카프카 설치 및 테스트
    1. 카프카 설치 및 압축 풀기
        ```{.bash}
        curl http://mirror.navercorp.com/apache/kafka/2.5.0/kafka_2.13-2.5.0.tgz
        tar -xvf kafka_2.13-2.5.0.tgz
        ```
    2. 테스트
        ```{.bash}
        cd kafka_2.13-2.5.0/bin
        
        - 토픽 생성
        ./kafka-topics.sh --create --bootstrap-server {aws ec2 public ip}:9092 --replication-factor 1 --partitions 3 --topic test
        
        - Producer 실행
        ./kafka-console-producer.sh --bootstrap-server {aws ec2 public ip}:9092 --topic test
        
        - Consumer 실행
        ./kafka-console-consumer.sh --bootstrap-server {aws ec2 public ip}:9092 --topic test --from-beginning
        ./kafka-console-consumer.sh --bootstrap-server {aws ec2 public ip}:9092 --topic test -group testgroup --from-beginning
        
        - Consumer Group List 확인
        ./kafka-consumer-groups.sh --bootstrap-server {aws ec2 public ip}:9092 --list
        
        - Consumer Group 상태 확인
        ./kafka-consumer-groups.sh --bootstrap-server {aws ec2 public ip}:9092 --group testgroup --describe
        
        - 가장 낮은 Offset으로 Reset
        ./kafka-consumer-groups.sh --bootstrap-server {aws ec2 public ip}:9092 --group testgroup --topic test --reset-offsets --to-earliest --execute
        
        - 특정 파티션을 특정 Offset으로 Reset
        ./kafka-consumer-groups.sh --bootstrap-server {aws ec2 public ip}:9092 --group testgroup --topic test:1 --reset-offsets --to-offset 10 --execute
        ```
        
3. **구성 옵션 설정 관련**
    1. **주키퍼** 구성 옵션 ((KAFKA_HOME)/config/zookeeper.properties)
        - **tickTime** : initLimit 설정 관여
        - **dataDir** : 각 서버는 dataDir에 지정된 디텍터리에 myid라는 이름의 파일을 갖고 있어야 한다. 그리고 이 파일은 구성 파일에 지정된 것과 일치되는 각 서버의 ID 번호를 포함해야 한다.
        - **clientPort** : 주키퍼에 접속하는 클라이언트는 clientPort에 지정된 포트 번호로 앙상블과 연결 (앙상블의 서버들은 세 가지 포트 모두를 사용해서 상호 통신)
        - **initLimit** : 팔로어(대기 서버)가 리더(데이터 쓰기를 하는 서버)에 접속할 수 있는 시간 (initLimit이 20, tickTime이 2000 일 경우 20*2000 ms = 40초)
        - **syncLimit** : 리더가 될 수 있는 팔로어의 최대 개수
        - **앙상블의 서버** 내역 설정
            - **server.X=hostname:peerPort:leaderPort**
            - X : 각 서버의 ID 번호이며 정수
            - hostname : 각 서버의 호스트 이름이나 IP 주소
            - peerPort : 앙상블의 서버들이 상호 통신하는 데 사용하는 TCP 포트 번호
            - leaderPort : 리더를 선출하는 데 사용하는 TCP 포트 번호
    2. **브로커** 구성 옵션 ((KAFKA_HOME)/config/server.properties)
        - **broker.id** : 모든 카프카 브로커는 broker.id에 설정하는 정수로 된 번호를 가져야 한다. 이것의 기본 값은 0 이지만 어떤 값도 가능하다. **단 하나의 카프카 클러스터 내에서 고유한 값이어야 한다.**
        - **port** : 카프카의 실행에 사용되는 TCP 포트, **기본 구성값은 9092**, 이 값은 어떤 포트 번호로도 설정할 수 있지만 1024보다 작은 값으로 설정한다면 카프카가 root 권한으로 시작되어야 한다.
        - **zookeeper.connect** : **브로커의 메타데이터를 저장하기 위해 사용되는 주키퍼의 위치**, 기본 구성 값은 localhost:2181 (로컬 호스트의 2181 포트에서 실행되는 주키퍼를 사용), "호스트이름:포트/경로" 형식으로 지정
        - **log.dirs** : **카프카는 모든 메시지를 로그 세그먼트 파일에 모아서 디스크에 저장**한다. 그리고 이 파일은 **log.dirs에 지정된 디렉터리에 저장**된다. 여러 경로를 쉼표로 구분하여 지정할 수 있고, 두 개 이상의 경로가 지정되면 해당 브로커가 모든 경로에 파티션을 저장한다. (**주의, 브로커가 새로운 파티션을 저장할 때는 가장 적은 디스크 용량을 사용하는 경로가 아닌 가장 적은 수의 파티션을 저장한 경로를 사용한다.**)
        - **num.recovery.threads.per.data.dir** : 카프카는 구성 가능한 스레드 풀을 사용해서 로그 세그먼트를 처리한다. 기본적으로는 로그 디렉터리당 하나의 스레드만 사용한다. 이 스레드들은 브로커의 시작과 종료 시에만 사용되므로 병행 처리를 하도록 많은 수의 스레드를 설정하는 것이 좋다. 그리고 **이 매개변수에 설정하는 스레드의 수는 log.dirs에 지정된 로그 디렉터리마다 적용된다는 것에 유의**해야 한다. (log.dirs에 지정된 경로가 3개, num.recovery.threads.per.data.dir을 8로 설정하면 전체 스레드의 개수는 24개)
        - **auto.create.topics.enable** : 기본 설정에는 true로 지정되어 있다. 직접 토픽 생성을 관리 할 때에는 auto.create.topics.enable 값을 false로 지정하면 된다.
        - **num.partitions** : 새로운 토픽이 몇 개의 파티션으로 생성되는지를 나타내며, 주로 자동 토픽 생성이 활성화될 때 사용된다. (**주의, 토픽의 파티션 개수는 증가만 하고 감소될 수 없다.**)
        - **log.retention.ms** : **얼마 동안 메시지를 보존할지** 설정. 시간 단위인 **log.retention.hours** 를 주로 사용 (기본 값은 1주일인 168시간), **log.retention.minutes**와 **log.retention.ms** 를 사용할 수 있다. 만약 두 개 이상이 같이 지정되면 더 작은 시간 단위의 매개변수 값이 사용되므로 항상 log.retention.ms의 값이 사용된다.
        - **log.retention.bytes** : **저장된 메시지들의 전체 크기를 기준으로 만기를 처리하는 방법**, 이 값은 모든 파티션에 적용된다. (하나의 토픽이 8개의 파티션으로 되어 있고 log.retention.bytes의 값이 1GB로 설정되면 해당 토픽에 저장되는 메시지들의 전체 크기는 최대 8GB가 된다.) 이러한 **메시지 보존은 토픽이 아닌 각 파티션별로 처리된다.**
        - **log.segment.bytes** : 위의 **메시지 보존 설정은 개별적인 메시지가 아닌 로그 세그먼트 파일을 대상으로 처리**된다. 메시지가 카프카 브로커에 생성될 때는 해당 파티션의 로그 세그먼트 파일 끝에 추가된다. 이때 log.segment.bytes 매개변수에 지정된 크기가 되면 해당 로그 세그먼트 파일이 닫히고 새로운 것이 생성되어 열린다. 만약 토픽의 데이터 저장률이 낮다면 로그 세그먼트의 크기를 조정하는 것이 중요하다.(**하루에 100MB의 메시지만 토픽에 수록되고 log.segment.bytes의 값이 1GB로 설정되어 있다면, 하나의 로그 세그먼트를 채우는 데 10일이 소요될 것이다. 그러나 로그 세그먼트 파일은 닫혀야만 만기가 될 수 있으므로 만일 log.retention.ms가 1주일로 설정되어 있다면 로그 세그먼트에 저장된 메시지들은 17일 동안 보존될 것이다. 왜냐하면 마지막 메시지를 쓰면서 로그 세그먼트 파일이 닫히는 데 10일이 소요되고, 다시 보존 시간이 7일이 지나야 만기가 되기 때문**)
        - **log.segment.ms** : **시간을 지정해서 로그 세그먼트 파일이 닫히는 것을 제어**할 수 있다. (log.retention.bytes와 log.retention.ms 매개변수처럼 log.segment.bytes와 log.segment.ms는 상호 연관된다.)
        - **message.max.bytes** : 카프카 브로커는 **쓰려는 메시지의 최대 크기를 제한**할 수 있다. 이때는 기본값이 1000000 (1MB)인 message.max.bytes 매개변수를 지정하면 된다. 그러면 이 값보다 큰 메시지를 전송하려는 프로듀서에게는 브로커가 에러를 보내고 메시지를 받지 않는다. 브로커에 지정되는 다른 모든 바이트 크기와 마찬가지로 이 값도 압축된 메시지의 크기를 나타낸다.
        
----------------------------------------------------

## Kafka Producer
1. **필수 구성 옵션**
    1. **boostrap.servers**
        - **카프카 클러스터에 연결하기 위한 브로커 목록**
        - 프로듀서가 사용하는 브로커들의 host:port 목록을 설정
        - 연결되면 프로듀서가 더 많은 정보를 얻을 수 있기 때문에 **모든 브로커를 포함할 필요는 없다. 그러나 최소한 두 개 이상을 포함하는 것이 좋다. 한 브로커가 중단되는 경우에도 프로듀서는 여전히 클러스터에 연결될 수 있기 때문이다.**
    2. **key.serializer**
        - **메시지 키 직렬화에 사용되는 클래스**
        - 프로듀서가 생성하는 레코드(ProducerRecord 객체)의 메시지 키를 직렬화하기 위해 사용되는 클래스 이름을 이 속성에 설정
        - 카프카 브로커는 바이트 배열로 메시지를 받는다. 그러나 카프카의 프로듀서 인터페이스에서는 매개변수화 타입을 사용해서 키와 값의 쌍으로 된 어떤 자바 객체도 전송할 수 있으므로 알기 쉬운 코드를 작성할 수 있다. 단, 이때는 그런 객체를 바이트 배열로 변환하는 방법을 프로듀서가 알고 있어야 한다.
        - key.serializer에 설정하는 클래스는 org.apache.kafka.common.serialization.Serializer 인터페이스를 구현해야 한다. 프로듀서는 이 클래스를 사용해서 키 객체를 바이트 배열로 직렬화한다.
        - 카프카 클라이언트 패키지에는 세 가지 타입의 직렬화를 지원하는 ByteArraySerializer, StringSerializer, IntegerSerializer가 포함되어 있다. 이런 타입들을 직렬화할 때는 직렬처리기를 따로 구현하지 않아도 된다.
        - **레코드의 키는 생략하고 값만 전송하고자 할 때도 설정해야 한다.**
    3. **value.serializer**
        - **메시지 값을 직렬화하는데 사용되는 클래스**
        - 레코드의 메시지 값을 직렬화하는 데 사용하는 클래스 이름을 설정
        - 직렬화하는 방법은 key.serializer와 동일하다.
    4. **기본 설정**
        ```{.java}
        // Properties 객체 생성
        private Properties kafkaProps = new Properties();
        
        // 속성과 값 설정
        kafkaProps.put("bootstrap.servers", "broker1:9092, broker2:9092");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        
        // 새로운 프로듀서 객체 생성, Properties 객체를 생성자 인자로 전달한다.
        Producer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
        ```
2. **선택 구성 옵션** (Default 값 존재)
    1. **acks**
        - 레코드 전송 **신뢰도 조절** (Replica)
        - 전송된 레코드를 수신하는 파티션 리클리카의 수를 제어, 메시지가 유실될 가능성에 영향을 준다.
        - **acks=0** : **프로듀서는 브로커의 응답을 기다리지 않는다.** 메시지 유실 가능성이 큰데 반해, 매우 높은 처리량이 필요할 때 사용
        - **acks=1** : **리더 리플리카가 메시지를 받는 순간 프로듀서는 브로커로부터 성공적으로 수신했다는 응답을 받는다.** 그러나 만일 리더에서 메시지를 쓸 수 없다면 프로듀서는 에러 응답을 받을 것이고 데이터 유실을 막기 위해 메시지를 다시 전송할 수 있다. 또한 리더가 중단되고 해당 메시지를 아직 복제하지 않은 리플리카가 새로운 리더로 선출될 경우에도 여전히 메시지가 유실될 수 있다. 이때는 동기식이나 비동기식 중 어떤 방법으로 메시지를 전송했는가에 따라 처리량이 달라질 수 있다.
        - **acks=all** : **동기화된 모든 리플리카가 메시지를 받으면 프로듀서가 브로커의 성공 응답을 받는다.** 하나 이상의 브로커가 해당 메시지를 갖고 있으므로, 파티션이나 리더 리플리카에 문제가 생기더라도 메시지가 유실되지 않기 때문에 가장 안전한 형태이다. 그러나 대기 시간이 길어진다.
    2. **compression.type**
        - **snappy, gzip, lz4 중 하나로 압축**하여 전송
        - 기본적으로 메시지는 압축되지 않은 상태로 전송되지만, 이 매개변수를 설정하면 압축되어 전송된다. 따라서 **네트워크 처리량이 제한적일 때** 사용하면 좋다.
        - 메시지 압축을 사용함으로써 카프카로 메시지를 전송할 때 병목 현상이 생길 수 있는 네트워크와 스토리지 사용을 줄일 수 있다.
    3. **retries**
        - 클러스터 장애에 대응하여 **메시지 전송을 재시도하는 횟수**
        - 프로듀서가 서버로부터 받는 에러가 일시적일 수 있기 때문에 메시지 전송을 포기하고 에러로 처리하기 전에 프로듀서가 메시지를 재전송하는 횟수를 제어할 수 있다.
        - 각 재전송 간에 **retry.backoff.ms**에 설정한 시간동안 대기하고 전송한다.
    4. **buffer.memory**
        - **브로커에 전송될 메시지의 버퍼로 사용될 메모리 양**
        - 브로커들에게 전송될 메시지의 버퍼로 사용할 메모리의 양을 설정
        - 메시지들이 서버에 전달될 수 있는 것보다 더 빠른 속도로 애플리케이션에서 전송된다면 추가로 호출되는 send() 메서드는 block.on.buffer.full(max.block.ms) 매개변수의 설정에 따라 예외를 일시 중단 시키거나 바로 발생시킨다.
    5. **batch.size**
        - **여러 데이터를 함께 보내기 위한 레코드 크기**
        - 같은 파티션에 쓰는 다수의 레코드가 전송될 때는 프로듀서가 그것들을 배치로 모은다. 이 매개변수는 각 배치에 사용될 메모리양(byte)을 제어한다. 그리고 해당 배치가 가득 차면 그것의 모든 메시지가 전송된다.
        - **하지만 배치가 가득 찰 때까지 프로듀서가 기다리는 것은 아니다.** 프로듀서는 절반만 채워진 배치와 심지어는 하나의 메시지만 있는 배치도 전송한다.
        - 크게 설정할 경우 메시지 전송은 지연되지 않지만 메모리를 더 많이 사용하게 된다. 반면에 작게 설정하면 프로듀서가 너무 자주 메시지를 전소앻야 하므로 부담을 초래한다.
    6. **linger.ms**
        - **현재의 배치를 전송하기 전까지 기다리는 시간**
        - **현재의 배치가 가득 찼거나, linger.ms에 설정된 제한 시간이 되면 프로듀서가 메시지 배치를 전송**한다.
        - 대기 시간이 증가하는 단점은 있지만, 동시에 처리량도 증가하는 장점이 있다.
    7. **client.id**
        - **어떤 클라이언트인지 구분하는 식별자**
        - 어떤 문자열도 가능하며, 어떤 클라이언트에서 전송된 메시지인지 식별하기 위해 브로커가 사용한다.
        - **주로 로그 메시지와 메트릭 데이터의 전송에 사용**
    8. **max.in.flight.requests.per.connection**
        - **서버의 응답을 받지 않고 프로듀서가 전송하는 메시지의 개수를 제어**한다.
    9. **timeout.ms, request.timeout.ms, metadata.fetch.timeout.ms**
        - timeout.ms : 동기화된 리플리카들이 메시지를 인지하는 동안 브로커가 대기하는 시간 제어
        - request.timeout.ms : 데이터를 전송할 때 프로듀서가 서버 응답을 기다리는 제한 시간
        - metadata.fetch.timeout.ms : 메타데이터를 요청할 때 프로듀서가 서버 응답을 기다리는 제한 시간
    10. **max.block.ms**
        - send() 메서드를 호출할 때 프로듀서의 전송 버퍼가 가득 차거나 partitionsFor() 메소드로 메타데이터를 요청했지만 사용할 수 없을 때 프로듀서가 max.block.ms의 시간동안 일시 중단된다 (그 사이에 에러가 해결될 수 있도록). 그 다음에 max.block.ms의 시간이 되면 시간 경과 예외가 발생한다.
    11. **max.request.size**
        - **프로듀서가 전송하는 쓰기 요청의 크기를 제어**한다.
        - 전송될 수 있는 가장 큰 메시지의 크기와 프로듀서가 하나의 요청으로 전송할 수 있는 메시지의 최대 개수 모두를 이 매개변수로 제한한다.
        - **브로커 설정의 message.max.bytes와 이 값을 일치되도록 설정하는 것이 좋다. 그래야만 브로커가 거부하는 크기의 메시지를 프로듀서가 전송하지 않을 것이기 때문이다.**
    12. **receive.buffer.bytes, send.buffer.bytes**
        - 데이터를 읽고 쓸 때 소켓이 사용하는 TCP 송수신 버퍼의 크기를 나타낸다. 만일 이 매개변수들이 -1로 설정되면 운영체제의 기본값이 사용된다.
3. **메시지 전송 방법**
    1. **Fire-and-forget (전송 후 망각)**
        - 가장 간단한 방법, send() 메서드로 메시지를 **전송만 하고 성공 또는 실패 여부에 따른 후속 조치를 취하지 않는 방법**, 카프카는 가용성이 높고 전송에 실패할 경우에 프로듀서가 자동으로 재전송을 시도하므로 대부분의 경우에 성공적으로 메시지가 전송된다. 그러나 일부 메시지가 유실될 수도 있다.
        ```{.java}
        // ("topic", "key", "value")
        ProducerRecord<String, String> record = new ProducerRecord<>("CustomerCountry", "Precision Products", "France");
        
        try {
            producer.send(record);
        } catch(Exception e){
            e.printStackTrace();
        }
        ```
    2. **Synchronous send (동기식 전송)**
        - send() 메서드로 메시지를 전송하면 자바의 Future 객체가 반환된다. 그 다음에 **Future 객체의 get() 메서드를 곧바로 호출하면 작업이 완료될 때까지 기다렸다가 브로커로부터 처리 결과가 반환**되므로 send()가 성공적으로 수행되었는지 알 수 있다.
        ```{.java}
        ProducerRecord<String, String> record = new ProducerRecord<>("CustomerCountry", "Precision Products", "France");
        
        try {
            producer.send(record).get();
        } catch(Exception e){
            e.printStackTrace();
        }
        ```
    3. **Asynchronous send (비동기식 전송)**
        - send() 메서드를 호출할 때 **콜백 메서드**를 구현한 객체를 매개변수로 전달한다. 이 객체에 구현된 콜백 메서드는 카프카 브로커로부터 응답을 받을 때 자동으로 호출되므로 send()가 성공적으로 수행되었는지 알 수 있다.
        ```{.java}
        private class DemoProducerCallback implements Callback{
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e){
                if(e != null){
                    e.printStackTrace();
                }
            }
        }
        
        ProducerRecord<String, String> record = new ProducerRecord<>("CustomerCountry", "Precision Products", "France");
        
        producer.send(record, new DemoProducerCallback());
        ```
4. **직렬처리기** (**Detailed** : can be found in each directory.)
    1. **커스텀 직렬 처리기** (Customer.java, CustomerSerializer.java)
        - 예를 들어, 고객을 나타내는 간단한 클래스는 customerID와 customerName 속성을 갖는다.
        - 기존의 int형의 ID를 Long 타입으로 변경해야 하거나 새로운 필드를 추가한다면, **기존 메시지와 새로운 메시지 간의 호환성을 유지하는 데 심각한 문제**가 생길 것이다.
        - 이런 이유로 JSON, 아파치 Avro, Thrift, Protobuf 같은 **범용 직렬처리기와 역직렬처기리의 사용을 권장**한다.
    2. **아파치 Avro를 사용해서 직렬화하기**
        - Avro는 언어 중립적인 데이터 직렬화 시스템이다.
        - Avro는 주로 JSON(JavaScript Object Notation) 형식으로 기술하며, 직렬화 역시 JSON을 지원하지만 주로 이진 파일을 사용한다.
        - **Avro가 파일을 읽고 쓸 때는 스키마가 있다고 간주한다** (Avro 파일에는 스키마가 포함된다).
        - 메시지를 쓰는 애플리케이션이 새로운 스키마(데이터 구조)로 전환하더라도 **해당 메시지를 읽는 애플리케이션은 일체의 변형 없이 계속해서 메시지를 처리할 수 있다는 것이 가장 큰 장점**이다.
        - **조건**
            1. **데이터를 쓰는 데 사용되는 스키마와 읽는 애플리케이션에서 기대하는 스키마가 호환**될 수 있어야 한다.
            2. 직렬화된 데이터를 원래의 형식으로 변환하는 **역직렬처리기는 데이터를 쓸 때 사용되었던 스키마를 사용해야 한다**. 그 스키마가 해당 데이터를 읽는 애플리케이션이 기대하는 것과 다른 경우에도 마찬가지다. 따라서 Avro 파일에는 데이터를 쓸 때 사용되었던 스키마가 포함된다. 그러나 카프카 메시지를 Avro로 직렬화할 때는 Avro 파일을 사용하는 것보다 **스키마 레지스트리**를 사용하는 더 좋은 방법이 있다.
    3. **Avro 레코드 사용하기** (AvroSerializer.java, GenericAvroSerializer.java)
        1. 데이터 파일에 스키마 전체를 저장하는 Avro 파일과는 다르게, 데이터를 갖는 각 레코드에 스키마 전체를 저장한다면 레코드 크기가 2배 이상이 되므로 큰 부담이 될 것이다. 그러나 레코드를 **읽을 때 Avro는 스키마 전체를 필요로 하므로 레코드 크기의 부담이 없게 하려면 스키마를 레코드가 아닌 다른 곳에 두어야 한다.** 이렇게 하기 위해 아파치 카프카에서는 아키텍처 패턴에 나온대로 **스키마 레지스트리를 사용**한다. 스키마 레지스트리는 아파치 카프카에 포함되지 않았지만 **오픈 소스에서 선택**할 수 있는 것들이 있다. 여기서는 [Confluent](https://github.com/confluentinc/schema-registry) 스키마 레지스트리를 사용했다.
        2. **카프카에 데이터를 쓰는 데 사용되는 모든 스키마를 레지스트리에 저장하는 것이 스키마 레지스트리의 개념**이다. 그 다음에 **카프카에 쓰는 레코드에는 사용된 스키마의 식별자만 저장하면 된다.** 그러면 컨슈머는 해당 식별자를 사용해서 스키마 레지스트리의 스키마를 가져온 후 이 스키마에 맞춰 데이터를 역직렬화할 수 있다. 이때 모든 작업이 직렬처리기와 역직렬처리기에서 수행된다는 것이 중요하다. 즉, 카프카에 데이터를 쓰는 코드에서는 스키마 레지스트리 관련 코드를 추가하지 않고 종전 직렬처리기를 사용하듯이 Avro를 사용하면 된다.
        3. **필요 jar 및 Gradle Dependency 추가** (Producer_Serializer/lib Directory)
            1. [Apache Avro](https://mvnrepository.com/artifact/org.apache.avro/avro/1.8.2)
            2. [Confluent Avro Serializer](http://packages.confluent.io/maven/io/confluent/kafka-avro-serializer/)
                - Influent는 Gradle Dependency Error가 잘 발생하여 직접 jar 파일을 다운로드해서 사용
                
----------------------------------------------------


## Kafka Consumer


----------------------------------------------------


## Kafka internal mechanism


----------------------------------------------------


## Reliable data delivery


----------------------------------------------------


## Build the data pipeline


----------------------------------------------------


## Cross-cluster data mirroring


----------------------------------------------------


## Kafka management


----------------------------------------------------


## Kafka monitoring


----------------------------------------------------


## Stream processing


----------------------------------------------------

## Simple Projects
- **Detailed Source Code** : can be found in each directory.  

1. **simple-kafka-producer** : key = null, topic만 사용하여 value 전송
2. **Kafka-producer-key-value** : topic, key, value를 사용하여 전송
3. **Kafka-producer-exact-partition** : topic, partition, key, value를 사용하여 전송
4. **simple-kafka-consumer** : topic과 consumer group을 사용하여 데이터 읽기
5. **Kafka-consumer-auto-commit** : 일정 시간, polling 할 때 자동으로 commit
6. **Kafka-consumer-sync-commit** : commitSync(), commitAsync()를 사용하여 commit
7. **Kafka-consumer-multi-thread** : multi thread를 이용하여 파티션이 끊어지거나 새로 할당되는 등의 상황에 안전하게 종료하기
8. **Kafka-consumer-save-metric** : telegraf를 통해 데이터를 수집하고 카프카 적재 후 읽어와서 .csv 형식으로 저장
    1. **Homebrew** [설치](https://www.whatwant.com/entry/LinuxBrew-install-Ubuntu-1804)
    2. **Telegraf** 설치
        ```{.bash}
        brew install telegraf
        ```
    3. **Telegraf 설정 파일 생성 및 설정**
        1. telegraf 설치 경로 확인
        ```{.bash}
        brew info telegraf
        ```
        2. **telegraf.conf 파일 생성**
        ```{.bash}
        cd {telegraf 설치경로}/bin
        vi telegraf.conf
        ```
        3. **telegraf.conf 파일 설정**
        ```{.html}
        [agent]
          interval = "10s"
        [[outputs.kafka]]
          brokers = ["{AWS EC2 Public IP}:9092"]
          ## Kafka topic for producer messages
          topic = "my-computer-metric"
        [[inputs.cpu]]
          percpu = true
          totalcpu = true
          fielddrop = ["time_*"]
        [[inputs.mem]]
        ```
    4. **카프카 토픽 생성**
        ```{.bash}
        (KAFKA_HOME)/bin/kafka-topics.sh --create --bootstrap-server {AWS EC2 Public IP}:9092 --replication-factor 1 --partitions 5 --topic my-computer-metric
        ```
    5. **Telegraf 실행** (telegraf 설치경로/bin)
        ```{.bash}
        ./telegraf --config telegraf.conf
        ```
    5. 데이터 확인
        ```{.bash}
        (KAFKA_HOME)/bin/kafka-console-consumer.sh --bootstrap-server {AWS EC2 Public IP}:9092 --topic my-computer-metric --from-beginning
        ```
    6. **자바 프로젝트 실행(Kafka-consumer-save-metric)** 후 파일 확인
        ```{.bash}
        (자바 프로젝트 위치) tail -f *.csv
        ```
    7. 화면 캡쳐
        <p align="center">
            <img src="Images/brew1.png",width="100%">
        </p>
        <p align="center">
            <img src="Images/brew2.png",width="100%">
        </p>