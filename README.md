# 1.서비스 소개 
<img width="1278" alt="스크린샷 2025-06-30 오후 2 19 01" src="https://github.com/user-attachments/assets/7e898790-4d03-40f5-9601-8899ab514033" />


OpenCloset 어플리케이션은 P2P 사용자 주소 기반 서비스를 통해,이웃 간 의류 대여를 가능하게 합니다.
사용자는 직접 입어보지 못했던 옷이나 입어보고 싶은 옷을이웃에게 빌려 입어보는 경험을 통해 새로운 스타일을 시도하고
자신의 욕구를 충족할 수 있습니다.


## 제작 기간 
프로젝트 기간:2025.03.20 - 2025.06.24

## 기획배경
<img width="997" alt="image" src="https://github.com/user-attachments/assets/a614be6d-25e8-4a83-ac5c-0109abdf3064" />

<img width="377" alt="image" src="https://github.com/user-attachments/assets/d6007709-2125-4d3c-a326-676e56c0747d" /><img width="480" alt="image" src="https://github.com/user-attachments/assets/16174cca-8816-49cc-92e6-f82f48b89f8d" />


버려지는 의류로 인한 환경 문제 및 중고거래 사기로 인한 사례와 20대 대학생들에게 중고의류 키워드만 질문 했을때 떠오르는 
키워드를 통한 조사를 통해 직거래,위치 기반,대여 기능 목표 어플리케이션 설계하였다.

## 🛠️ 기술 스택
- Java 17, Spring Boot 3.x, Spring Security, JWT
- JPA(Hibernate), MariaDB
- Lombok, Gradle

## 🚩 주요 기능 (백엔드)
- **회원가입/로그인**: 비밀번호 암호화, JWT 토큰 발급 및 검증
- **위치 기반 게시글 검색**: 사용자 주소 기반 자동 필터링
- **보안**: CORS, 인증/인가, 예외 처리
